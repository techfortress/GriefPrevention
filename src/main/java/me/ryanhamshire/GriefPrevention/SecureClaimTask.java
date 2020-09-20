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

import java.util.Collection;

//secures a claim after a siege looting window has closed
class SecureClaimTask implements Runnable
{
    private final SiegeData siegeData;

    public SecureClaimTask(SiegeData siegeData)
    {
        this.siegeData = siegeData;
    }

    @Override
    public void run()
    {
        //for each claim involved in this siege
        for (int i = 0; i < this.siegeData.claims.size(); i++)
        {
            //lock the doors
            Claim claim = this.siegeData.claims.get(i);
            claim.doorsOpen = false;

            //eject bad guys
            @SuppressWarnings("unchecked")
            Collection<Player> onlinePlayers = (Collection<Player>) GriefPrevention.instance.getServer().getOnlinePlayers();
            for (Player player : onlinePlayers)
            {
                if (claim.contains(player.getLocation(), false, false) && claim.allowAccess(player) != null)
                {
                    GriefPrevention.sendMessage(player, TextMode.Err, Messages.SiegeDoorsLockedEjection);
                    GriefPrevention.instance.ejectPlayer(player);
                }
            }
        }
    }
}
