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

import org.bukkit.entity.Player;

//checks to see whether or not a siege should end based on the locations of the players
//for example, defender escaped or attacker gave up and left
class SiegeCheckupTask implements Runnable
{
    private final SiegeData siegeData;

    public SiegeCheckupTask(SiegeData siegeData)
    {
        this.siegeData = siegeData;
    }

    @Override
    public void run()
    {
        DataStore dataStore = GriefPrevention.instance.dataStore;
        Player defender = this.siegeData.defender;
        Player attacker = this.siegeData.attacker;

        //where is the defender?
        Claim defenderClaim = dataStore.getClaimAt(defender.getLocation(), false, null);

        //if this is a new claim and he has some permission there, extend the siege to include it
        if (defenderClaim != null)
        {
            String noAccessReason = defenderClaim.allowAccess(defender);
            if (defenderClaim.canSiege(defender) && noAccessReason == null)
            {
                this.siegeData.claims.add(defenderClaim);
                defenderClaim.siegeData = this.siegeData;
            }
        }

        //determine who's close enough to the siege area to be considered "still here"
        boolean attackerRemains = this.playerRemains(attacker);
        boolean defenderRemains = this.playerRemains(defender);

        //if they're both here, just plan to come check again later
        if (attackerRemains && defenderRemains)
        {
            this.scheduleAnotherCheck();
        }

        //otherwise attacker wins if the defender runs away
        else if (attackerRemains && !defenderRemains)
        {
            dataStore.endSiege(this.siegeData, attacker.getName(), defender.getName(), null);
        }

        //or defender wins if the attacker leaves
        else if (!attackerRemains && defenderRemains)
        {
            dataStore.endSiege(this.siegeData, defender.getName(), attacker.getName(), null);
        }

        //if they both left, but are still close together, the battle continues (check again later)
        else if (attacker.getWorld().equals(defender.getWorld()) && attacker.getLocation().distanceSquared(defender.getLocation()) < 2500) //50-block radius for chasing
        {
            this.scheduleAnotherCheck();
        }

        //otherwise they both left and aren't close to each other, so call the attacker the winner (defender escaped, possibly after a chase)
        else
        {
            dataStore.endSiege(this.siegeData, attacker.getName(), defender.getName(), null);
        }
    }

    //a player has to be within 25 blocks of the edge of a besieged claim to be considered still in the fight
    private boolean playerRemains(Player player)
    {
        for (int i = 0; i < this.siegeData.claims.size(); i++)
        {
            Claim claim = this.siegeData.claims.get(i);
            if (claim.isNear(player.getLocation(), 25))
            {
                return true;
            }
        }

        return false;
    }

    //schedules another checkup later
    private void scheduleAnotherCheck()
    {
        this.siegeData.checkupTaskID = GriefPrevention.instance.getServer().getScheduler().scheduleSyncDelayedTask(GriefPrevention.instance, this, 20L * 30);
    }
}
