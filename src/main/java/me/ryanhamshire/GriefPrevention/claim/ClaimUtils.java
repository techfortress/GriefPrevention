package me.ryanhamshire.GriefPrevention.claim;

import org.apache.commons.lang.Validate;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.HashSet;
import java.util.Set;

/**
 * Created on 4/4/2018.
 *
 * @author RoboMWM
 */
public class ClaimUtils
{
    private ClaimUtils(){} //do not instantiate

    /**
     * Gets an almost-unique, persistent identifier for a chunk
     * @param chunkx Chunk coordinate x value
     * @param chunkz Chunk coordinate y value
     * @return the hash of the chunk
     */
    public static Long getChunkHash(long chunkx, long chunkz)
    {
        return (chunkz ^ (chunkx << 32));
    }

    /**
     * Gets an almost-unique, persistent identifier for the chunk within this location
     * @return the hash of the chunk
     */
    public static Long getChunkHash(Location location)
    {
        return getChunkHash(location.getBlockX() >> 4, location.getBlockZ() >> 4);
    }

    /**
     * @param claim
     * @return a set of chunk hashes of the chunks and partial chunks contained within a given claim
     */
    public static Set<Long> getChunkHashes(Claim claim)
    {
        Location lesserCorner = claim.getLesserBoundaryCorner();
        Location greaterCorner = claim.getGreaterBoundaryCorner();
        Set<Long> hashes = new HashSet<>();
        int smallX = lesserCorner.getBlockX() >> 4;
        int smallZ = lesserCorner.getBlockZ() >> 4;
        int largeX = greaterCorner.getBlockX() >> 4;
        int largeZ = greaterCorner.getBlockZ() >> 4;

        for(int x = smallX; x <= largeX; x++)
        {
            for(int z = smallZ; z <= largeZ; z++)
            {
                hashes.add(getChunkHash(x, z));
            }
        }

        return hashes;
    }


    /**
     * Does claim overlap otherClaim?
     * @param claim The claim we wish to place, generally
     * @param otherClaim The claim that already exists in the world, generally
     * @return whether or not two claims overlap
     * @throws IllegalArgumentException if claim == otherClaim
     */
    public static boolean overlaps(Claim claim, Claim otherClaim)
    {
        Validate.isTrue(claim != otherClaim, "Cannot check if a claim is overlapping itself (Well, you can, but this is very likely a mistake.");

        Location lesserCorner = claim.getLesserBoundaryCorner();
        Location greaterCorner = claim.getGreaterBoundaryCorner();

        Location otherLesserCorner = otherClaim.getLesserBoundaryCorner();
        Location otherGreaterCorner = otherClaim.getGreaterBoundaryCorner();

        if(!lesserCorner.getWorld().equals(otherClaim.getLesserBoundaryCorner().getWorld())) return false;

        //first, check the corners (easy to determine if corners are within a claim).
        if(otherClaim.contains(lesserCorner, false)) return true;
        if(otherClaim.contains(greaterCorner, false)) return true;
        if(otherClaim.contains(new Location(lesserCorner.getWorld(), lesserCorner.getBlockX(), 0, greaterCorner.getBlockZ()), false)) return true;
        if(otherClaim.contains(new Location(lesserCorner.getWorld(), greaterCorner.getBlockX(), 0, lesserCorner.getBlockZ()), false)) return true;

        //verify that no claim's lesser boundary point is inside claim new claim, to cover the "existing claim is entirely inside new claim" case
        if(claim.contains(otherClaim.getLesserBoundaryCorner(), false)) return true;

        //verify claim claim doesn't band across an existing claim, either horizontally or vertically
        if(	lesserCorner.getBlockZ() <= otherClaim.getGreaterBoundaryCorner().getBlockZ() &&
                lesserCorner.getBlockZ() >= otherClaim.getLesserBoundaryCorner().getBlockZ() &&
                lesserCorner.getBlockX() < otherClaim.getLesserBoundaryCorner().getBlockX() &&
                greaterCorner.getBlockX() > otherClaim.getGreaterBoundaryCorner().getBlockX() )
            return true;

        if(	greaterCorner.getBlockZ() <= otherClaim.getGreaterBoundaryCorner().getBlockZ() &&
                greaterCorner.getBlockZ() >= otherClaim.getLesserBoundaryCorner().getBlockZ() &&
                lesserCorner.getBlockX() < otherClaim.getLesserBoundaryCorner().getBlockX() &&
                greaterCorner.getBlockX() > otherClaim.getGreaterBoundaryCorner().getBlockX() )
            return true;

        if(	lesserCorner.getBlockX() <= otherClaim.getGreaterBoundaryCorner().getBlockX() &&
                lesserCorner.getBlockX() >= otherClaim.getLesserBoundaryCorner().getBlockX() &&
                lesserCorner.getBlockZ() < otherClaim.getLesserBoundaryCorner().getBlockZ() &&
                greaterCorner.getBlockZ() > otherClaim.getGreaterBoundaryCorner().getBlockZ() )
            return true;

        if(	greaterCorner.getBlockX() <= otherClaim.getGreaterBoundaryCorner().getBlockX() &&
                greaterCorner.getBlockX() >= otherClaim.getLesserBoundaryCorner().getBlockX() &&
                lesserCorner.getBlockZ() < otherClaim.getLesserBoundaryCorner().getBlockZ() &&
                greaterCorner.getBlockZ() > otherClaim.getGreaterBoundaryCorner().getBlockZ() )
            return true;

        return false;
    }

    /**
     * Returns a set of the chunks fully contained within the claim (no partial chunks)
     * @return the chunks within this claim
     */
    public static Set<Chunk> getChunks(Claim claim)
    {
        Set<Chunk> chunks = new HashSet<>();

        World world = claim.getLesserBoundaryCorner().getWorld();
        Chunk lesserChunk = claim.getLesserBoundaryCorner().getChunk();
        Chunk greaterChunk = claim.getGreaterBoundaryCorner().getChunk();

        for(int x = lesserChunk.getX(); x <= greaterChunk.getX(); x++)
            for(int z = lesserChunk.getZ(); z <= greaterChunk.getZ(); z++)
                chunks.add(world.getChunkAt(x, z));

        return chunks;
    }

    /**
     * Whether or not the given location is in this claim, ignoring y value.
     * @param location
     * @return
     */
    public static boolean isWithin(Claim claim, Location location)
    {
        return isWithin(claim, location, false);
    }

    /**
     * Whether or not the given location is in this claim
     * @param location
     * @param includeHeight Whether a location underneath the claim should be considered within the claim.
     *                      If true, a location underneath the claim will return TRUE
     * @return
     */
    public static boolean isWithin(Claim claim, Location location, boolean includeHeight)
    {
        return claim.contains(location, includeHeight);
    }

    /**
     * Determines if a location is near a given claim
     * Distance in this case is a band around the outside of the claim rather then euclidean distance
     * @param location Location in question. Height (y value) is effectively ignored.
     * @param howNear distance in blocks to check
     * @return
     */
    public static boolean isNear(Claim claim, Location location, int howNear)
    {
        Claim areaToCheck = new Claim
                (new Location(claim.getLesserBoundaryCorner().getWorld(), claim.getLesserBoundaryCorner().getBlockX() - howNear, claim.getLesserBoundaryCorner().getBlockY(), claim.getLesserBoundaryCorner().getBlockZ() - howNear),
                        new Location(claim.getGreaterBoundaryCorner().getWorld(), claim.getGreaterBoundaryCorner().getBlockX() + howNear, claim.getGreaterBoundaryCorner().getBlockY(), claim.getGreaterBoundaryCorner().getBlockZ() + howNear),
                        null, null, null);

        return areaToCheck.contains(location, true);
    }

    /**
     * "Sorts" two given corners into a "lesser" and "greater" variant.
     * Idk how else to explain this other than the lesser's xyz values are lesser than the greater's xyz values.
     * @param firstCorner
     * @param secondCorner
     * @return the sorted corners, with index 0 as the lesser, and index 1 as the greater
     * @throws IllegalArgumentException if corners' worlds don't match
     */
    public static Location[] retrieveSortedCorners(Location firstCorner, Location secondCorner)
    {
        Validate.isTrue(firstCorner.getWorld() == secondCorner.getWorld());

        int smallx, bigx, smally, bigy, smallz, bigz;

        int x1 = firstCorner.getBlockX(); int x2 = secondCorner.getBlockX();
        int y1 = firstCorner.getBlockY(); int y2 = secondCorner.getBlockY();
        int z1 = firstCorner.getBlockZ(); int z2 = secondCorner.getBlockZ();

        //determine small versus big inputs
        if(x1 < x2)
        {
            smallx = x1;
            bigx = x2;
        }
        else
        {
            smallx = x2;
            bigx = x1;
        }

        if(y1 < y2)
        {
            smally = y1;
            bigy = y2;
        }
        else
        {
            smally = y2;
            bigy = y1;
        }

        if(z1 < z2)
        {
            smallz = z1;
            bigz = z2;
        }
        else
        {
            smallz = z2;
            bigz = z1;
        }

        Location[] corners = new Location[2];
        corners[0] = new Location(firstCorner.getWorld(), smallx, smally, smallz);
        corners[1] = new Location(firstCorner.getWorld(), bigx, bigy, bigz);
        return corners;
    }

    /**
     * Returns the area covered by two corner locations in the xz plane
     * @param firstCorner
     * @param secondCorner
     * @return
     */
    public static int getArea(Location firstCorner, Location secondCorner)
    {
        Location[] corners = retrieveSortedCorners(firstCorner, secondCorner);
        Location lesserBoundaryCorner = corners[0];
        Location greaterBoundaryCorner = corners[1];
        int width = greaterBoundaryCorner.getBlockX() - lesserBoundaryCorner.getBlockX() + 1;
        int height = greaterBoundaryCorner.getBlockZ() - lesserBoundaryCorner.getBlockZ() + 1;
        return width * height;
    }
}
