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

package me.ryanhamshire.GriefPrevention.claim;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * represents a player claim
 *
 */
public class Claim
{
	//two locations, which together define the boundaries of the claim
	//note that the upper Y value is always ignored, because claims ALWAYS extend up to the sky
	private Location lesserBoundaryCorner;
	private Location greaterBoundaryCorner;
	private Long id;
    private UUID ownerUUID;
    private Map<UUID, ClaimPermission> trustees;
    private boolean naturalGriefAllowed = false;

	public Map<UUID, ClaimPermission> getTrustees()
	{
		return trustees;
	}

	/**
     * Unique ID number of the claim. Should never change.
	 * @return Unique ID of this claim.
	 */
	public Long getID()
	{
		return this.id;
	}

	/**
	 * Returns the UUID of the player that owns this claim. For admin claims, this will return null
	 * @return the owner's UUID
	 */
	public UUID getOwnerUUID()
	{
		return ownerUUID;
	}

    void setOwnerUUID(UUID ownerUUID)
    {
        this.ownerUUID = ownerUUID;
    }

    /**
     * whether or not this is an administrative claim
     * administrative claims are created and maintained by players with the griefprevention.adminclaims permission.
     * @return true if this claim is an admin claim
     */
	public boolean isAdminClaim()
	{
		return (this.ownerUUID == null);
	}

	//copy constructor
	public Claim(Claim claim, UUID ownerUUID)
	{
		this.lesserBoundaryCorner = claim.lesserBoundaryCorner;
		this.greaterBoundaryCorner = claim.greaterBoundaryCorner;
		this.trustees = claim.trustees;
		this.id = claim.id;

		this.ownerUUID = ownerUUID;
	}

	//When loading from storage
	public Claim(Location lesserBoundaryCorner, Location greaterBoundaryCorner, UUID ownerUUID, Map<UUID, ClaimPermission> trustees, Long id)
	{
		//id
		this.id = id;
		
		//store corners
		this.lesserBoundaryCorner = lesserBoundaryCorner;
		this.greaterBoundaryCorner = greaterBoundaryCorner;
		
		//owner
		this.ownerUUID = ownerUUID;
		
		//permissions
        if (trustees == null)
            this.trustees = new HashMap<>();
        else
		    this.trustees = trustees;
	}

    /**
     * Returns the permission map of the claim.
     * A null entry refers to the default permission granted to any player.
     * Any changes to this map will modify the claim's permission map; don't forget to save if you modify it!
     * @return the permission map of the claim.
     */
	public Map<UUID, ClaimPermission> getPermissions()
    {
        return trustees;
    }


    /**
     * Convenience method to get the "general" location of a claim.
     * @see Claim#getLesserBoundaryCorner()
     */
    public Location getLocation()
    {
        return getLesserBoundaryCorner();
    }

    /**
     * @return a copy of the location representing lower x, y, z limits
     */
	public Location getLesserBoundaryCorner()
	{
		return this.lesserBoundaryCorner.clone();
	}

    /**
     * NOTE: remember upper Y will always be ignored, all claims always extend to the sky
     * @return returns a copy of the location representing upper x, y, z limits
     */
    public Location getGreaterBoundaryCorner()
    {
        return this.greaterBoundaryCorner.clone();
    }

	//Used for resizing, obviously
    public void setLesserBoundaryCorner(Location lesserBoundaryCorner)
    {
        this.lesserBoundaryCorner = lesserBoundaryCorner;
    }

    public void setGreaterBoundaryCorner(Location greaterBoundaryCorner)
    {
        this.greaterBoundaryCorner = greaterBoundaryCorner;
    }

    /**
     * Sets the depth of this claim
     * @param y minimum y-coordinate of this claim
     */
    public void setDepth(int y)
    {
        lesserBoundaryCorner.setY(y);
    }

    /*Convenience methods*/

    //measurements.  all measurements are in blocks

    /**
     * @return Area of claim, in blocks
     */
    public int getArea()
    {
        return this.getWidth() * this.getHeight();
    }

    /**
     * @return Width of claim, in blocks
     */
    public int getWidth()
    {
        return this.greaterBoundaryCorner.getBlockX() - this.lesserBoundaryCorner.getBlockX() + 1;
    }

    /**
     * @return Height of claim, in blocks
     */
    public int getHeight()
    {
        return this.greaterBoundaryCorner.getBlockZ() - this.lesserBoundaryCorner.getBlockZ() + 1;
    }


	/**
	 * whether or not a location is in or under a claim. (Ignores height.)
     * @see ClaimUtils#isWithin(Claim, Location)
	 * @param location
	 * @return
	 */
	boolean contains(Location location)
	{
		return contains(location, true);
	}

	/**
	 * Whether or not the given location is in this claim
     * This is currently not implemented in ClaimUtils as this method is called often (to prevent unnecessary Location clones)
     * @see ClaimUtils#isWithin(Claim, Location, boolean)
	 * @param location
	 * @param ignoreDepth Whether a location underneath the claim should be considered within the claim.
     *                    If true, a location underneath the claim will return TRUE
	 * @return
	 */
	boolean contains(Location location, boolean ignoreDepth)
    {
        //not in the same world implies false
        if (!location.getWorld().equals(this.lesserBoundaryCorner.getWorld())) return false;

        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();

        //main check
        return (ignoreDepth || y >= this.lesserBoundaryCorner.getY()) &&
                x >= this.lesserBoundaryCorner.getX() &&
                x < this.greaterBoundaryCorner.getX() + 1 &&
                z >= this.lesserBoundaryCorner.getZ() &&
                z < this.greaterBoundaryCorner.getZ() + 1;
    }
}
