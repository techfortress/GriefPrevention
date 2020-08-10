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

import me.ryanhamshire.GriefPrevention.events.AllowPlayerClaimPermissionEvent.ClaimPermissionType;

//basic enum stuff
public enum ClaimPermission
{
    Build(true, Messages.NoBuildUnderSiege, Messages.NoBuildPermission, ClaimPermissionType.BUILD),
    Inventory(true, Messages.NoContainersSiege, Messages.NoContainersPermission, ClaimPermissionType.CONTAINERS),
    Access(false, null, Messages.NoAccessPermission, ClaimPermissionType.ACCESS),
    Edit(false, Messages.NoModifyDuringSiege, Messages.OnlyOwnersModifyClaims, ClaimPermissionType.EDIT);

    private boolean canExtendClaimInSiege;
    private Messages siegeMessage;
    private Messages denialMessage;
    private ClaimPermissionType pair;
    ClaimPermission(boolean canExtendClaimInSiege, Messages siegeMessage, Messages denialMessage, ClaimPermissionType pair)
    {
        this.canExtendClaimInSiege = canExtendClaimInSiege;
        this.siegeMessage = siegeMessage;
        this.denialMessage = denialMessage;
        this.pair = pair;
	}

    public boolean canExtendClaimInSiege()
    {
        return canExtendClaimInSiege;
    }

    public Messages getSiegeMessage()
    {
        return siegeMessage;
    }

    public Messages getDenialMessage() 
    {
        return denialMessage;
    }

    public ClaimPermissionType getPair() {
		return pair;
	}

    /**
     * Check if a ClaimPermission is granted by another ClaimPermission.
     *
     * @param other the ClaimPermission to compare against
     * @return true if this ClaimPermission is equal or lesser than the provided ClaimPermission
     */
    public boolean isGrantedBy(ClaimPermission other)
    {
        // As this uses declaration order to compare, if trust levels are reordered this method must be rewritten.
        return other != null && other.ordinal() <= this.ordinal();
    }
}
