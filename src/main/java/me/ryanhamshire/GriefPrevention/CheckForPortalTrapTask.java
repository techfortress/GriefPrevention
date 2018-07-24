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
 
package me.ryanhamshire.GriefPrevention;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

//players can be "trapped" in a portal frame if they don't have permission to break
//solid blocks blocking them from exiting the frame
//if that happens, we detect the problem and send them back through the portal.
class CheckForPortalTrapTask extends BukkitRunnable
{
	GriefPrevention instance;
	//player who recently teleported via nether portal 
	private Player player;
	
	//where to send the player back to if he hasn't left the portal frame
	private Location returnLocation;
	
	public CheckForPortalTrapTask(Player player, GriefPrevention plugin, Location locationToReturn)
	{
		this.player = player;
		this.instance = plugin;
		this.returnLocation = locationToReturn;
		player.setMetadata("GP_PORTALRESCUE", new FixedMetadataValue(instance, locationToReturn));
	}
	
	@Override
	public void run()
	{
	    if(player.isOnline() && player.getPortalCooldown() >= 10)
		{
			instance.AddLogEntry("Rescued " + player.getName() + " from a nether portal.\nTeleported from " + player.getLocation().toString() + " to " + ((Location)player.getMetadata("GP_PORTALRESCUE").get(0).value()).toString(), CustomLogEntryTypes.Debug);
			player.teleport(returnLocation);
			player.removeMetadata("GP_PORTALRESCUE", instance);
		}
        instance.portalReturnTaskMap.remove(player.getUniqueId());
	}
}
