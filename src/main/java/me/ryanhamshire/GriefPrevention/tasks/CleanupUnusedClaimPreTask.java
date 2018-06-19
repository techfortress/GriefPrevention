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

import me.ryanhamshire.GriefPrevention.CustomLogEntryTypes;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.claim.Claim;
import me.ryanhamshire.GriefPrevention.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

//asynchronously loads player storage without caching it in the datastore, then
//passes those storage to a claim cleanup task which might decide to delete a claim for inactivity

class CleanupUnusedClaimPreTask implements Runnable 
{	
	private Claim claim = null;
	
	CleanupUnusedClaimPreTask(Claim claim)
	{
		this.claim = claim;
	}
	
	@Override
	public void run()
	{
		//get the storage
	    PlayerData ownerData = GriefPrevention.instance.storage.getPlayerData(claim.ownerID);
	    OfflinePlayer ownerInfo = Bukkit.getServer().getOfflinePlayer(claim.ownerID);
	    
	    //expiration code uses last logout timestamp to decide whether to expire claims
	    //don't expire claims for online players
	    if(ownerInfo.isOnline()) return;
		if(ownerInfo.getLastPlayed() <= 0) return;
	    
	    GriefPrevention.AddLogEntry("Looking for expired claims.  Checking storage for " + claim.ownerID.toString(), CustomLogEntryTypes.Debug, true);
	    
	    //skip claims belonging to exempted players based on block totals in config
	    int bonusBlocks = ownerData.getBonusClaimBlocks();
	    if(bonusBlocks >= GriefPrevention.instance.config_claims_expirationExemptionBonusBlocks || bonusBlocks + ownerData.getAccruedClaimBlocks() >= GriefPrevention.instance.config_claims_expirationExemptionTotalBlocks)
        {
            GriefPrevention.AddLogEntry("Player exempt from claim expiration based on claim block counts vs. config file settings.", CustomLogEntryTypes.Debug, true);
            return;
        }
		
	    //pass it back to the main server thread, where it's safe to delete a claim if needed
	    Bukkit.getScheduler().scheduleSyncDelayedTask(GriefPrevention.instance, new CleanupUnusedClaimTask(claim, ownerData, ownerInfo), 1L);
	}
}