/*
    GriefPrevention Server Plugin for Minecraft
    Copyright (C) 2012 Ryan Hamshire

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package me.ryanhamshire.GriefPrevention.tasks;

import java.util.Collection;

import me.ryanhamshire.GriefPrevention.CustomLogEntryTypes;
import me.ryanhamshire.GriefPrevention.storage.Storage;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.events.AccrueClaimBlocksEvent;
import me.ryanhamshire.GriefPrevention.player.PlayerData;
import org.bukkit.Location;
import org.bukkit.entity.Player;

//FEATURE: give players claim blocks for playing, as long as they're not away from their computer

//runs every 5 minutes in the main thread, grants blocks per hour / 12 to each online player who appears to be actively playing
class DeliverClaimBlocksTask implements Runnable 
{	
    private Player player;
    private GriefPrevention instance;
    private int idleThresholdSquared;
    
    public DeliverClaimBlocksTask(Player player, GriefPrevention instance)
    {
        this.player = player;
        this.instance = instance;
        this.idleThresholdSquared = instance.config_claims_accruedIdleThreshold * instance.config_claims_accruedIdleThreshold;
    }
    
	@Override
	public void run()
	{
	    //if no player specified, this task will create a player-specific task for each online player, scheduled one tick apart
	    if(this.player == null)
		{
	        @SuppressWarnings("unchecked")
            Collection<Player> players = (Collection<Player>)GriefPrevention.instance.getServer().getOnlinePlayers();
	        
	        long i = 0;
	        for(Player onlinePlayer : players)
	        {
	            DeliverClaimBlocksTask newTask = new DeliverClaimBlocksTask(onlinePlayer, instance);
	            instance.getServer().getScheduler().scheduleSyncDelayedTask(instance, newTask, i++);
	        }
		}
	    
	    //otherwise, deliver claim blocks to the specified player
	    else
	    {
	        if(!this.player.isOnline())
            {
                return;
            }
	        
	        Storage storage = instance.storage;
            PlayerData playerData = storage.getPlayerData(player.getUniqueId());
            
            Location lastLocation = playerData.lastAfkCheckLocation;
            try
            {
                //if he's not in a vehicle and has moved at least three blocks since the last check
                //and he's not being pushed around by fluids
                if(!player.isInsideVehicle() && 
                   (lastLocation == null || lastLocation.distanceSquared(player.getLocation()) > idleThresholdSquared) &&
                   !player.getLocation().getBlock().isLiquid())
                {                   
                    //determine how fast blocks accrue for this player //RoboMWM: addons determine this instead
                    int accrualRate = instance.config_claims_blocksAccruedPerHour_default;

                    AccrueClaimBlocksEvent event = new AccrueClaimBlocksEvent(player, accrualRate);
                    instance.getServer().getPluginManager().callEvent(event);
                    if (event.isCancelled())
                    {
                        GriefPrevention.AddLogEntry(player.getName() + " claim block delivery was canceled by another plugin.", CustomLogEntryTypes.Debug, true);
                    }
                    else
                    {
                        accrualRate = event.getBlocksToAccrue();
                        if (accrualRate < 0) accrualRate = 0;
                        playerData.accrueBlocks(accrualRate);
                        GriefPrevention.AddLogEntry("Delivering " + event.getBlocksToAccrue() + " blocks to " + player.getName(), CustomLogEntryTypes.Debug, true);

                        //intentionally NOT saving storage here to reduce overall secondary storage access frequency
                        //many other operations will cause this player's storage to save, including his eventual logout
                        //storage.savePlayerData(player.getUniqueIdentifier(), playerData);
                    }
                }
                else
                {
                    GriefPrevention.AddLogEntry(player.getName() + " wasn't active enough to accrue claim blocks this round.", CustomLogEntryTypes.Debug, true);
                }
            }
            catch(IllegalArgumentException e)  //can't measure distance when to/from are different worlds
            {
                
            }
            catch(Exception e)
            {
                GriefPrevention.AddLogEntry("Problem delivering claim blocks to player " + player.getName() + ":");
                e.printStackTrace();
            }
            
            //remember current location for next time
            playerData.lastAfkCheckLocation = player.getLocation();
	    }
	}
}
