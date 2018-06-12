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

import java.util.Random;

import org.bukkit.Bukkit;

//FEATURE: automatically remove claims owned by inactive players which:
//...aren't protecting much OR
//...are a free new player claim (and the player has no other claims) OR
//...because the player has been gone a REALLY long time, and that expiration has been configured in config.yml

//runs every 1 minute in the main thread
class FindUnusedClaimsTask implements Runnable 
{	
	int nextClaimIndex;
	
	FindUnusedClaimsTask()
	{
		//start scanning in a random spot
		if(GriefPrevention.instance.dataStore.claims.size() == 0)
		{
			this.nextClaimIndex = 0;
		}
		else
		{
			Random randomNumberGenerator = new Random();
			this.nextClaimIndex = randomNumberGenerator.nextInt(GriefPrevention.instance.dataStore.claims.size());
		}
	}
	
	@Override
	public void run()
	{
		//don't do anything when there are no claims
		if(GriefPrevention.instance.dataStore.claims.size() == 0) return;

		//wrap search around to beginning
		if(this.nextClaimIndex >= GriefPrevention.instance.dataStore.claims.size()) this.nextClaimIndex = 0;
		
		//decide which claim to check next
		Claim claim = GriefPrevention.instance.dataStore.claims.get(this.nextClaimIndex++);
		
		//skip administrative claims
		if(claim.isAdminClaim()) return;
		
		Bukkit.getScheduler().runTaskAsynchronously(GriefPrevention.instance, new CleanupUnusedClaimPreTask(claim));
	}
}
