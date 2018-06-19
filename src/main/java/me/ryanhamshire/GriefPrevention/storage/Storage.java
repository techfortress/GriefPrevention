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

package me.ryanhamshire.GriefPrevention.storage;

import me.ryanhamshire.GriefPrevention.claim.Claim;
import me.ryanhamshire.GriefPrevention.player.PlayerData;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Stores and retrieves storage from storage.
 */
public interface Storage
{
    //pattern for unique user identifiers (UUIDs)
    //protected final static Pattern uuidpattern = Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");

    /**
     * Retrieves a set of all claims in storage
     * @return a set of all claims
     */
    Set<Claim> getClaims();

    /**
     * Saves claim to storage
     *
     * @param claim
     * @throws Exception if it is unable to save the claim for whatever reason.
     */
    void saveClaim(Claim claim) throws Exception;

    /**
     * Deletes claim from storage
     *
     * @param claim
     * @return Whether the claim exists at the time this is returned. (Successfully deleted or never existed.)
     */
    boolean deleteClaim(Claim claim);

    /**
     * Retrieves the playerData for the specified player from storage. Creates a new one if none exists.
     *
     * @param uuid player's uuid
     * @return
     */
    PlayerData getPlayerData(UUID uuid);

    /**
     * Saves the playerData to storage
     *
     * @param playerID
     * @param playerData
     */
    void savePlayerData(UUID playerID, PlayerData playerData);
}
