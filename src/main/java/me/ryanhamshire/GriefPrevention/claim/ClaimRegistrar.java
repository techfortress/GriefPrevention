package me.ryanhamshire.GriefPrevention.claim;

import me.ryanhamshire.GriefPrevention.storage.Storage;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.message.Messages;
import me.ryanhamshire.GriefPrevention.player.PlayerData;
import me.ryanhamshire.GriefPrevention.TextMode;
import me.ryanhamshire.GriefPrevention.visualization.Visualization;
import me.ryanhamshire.GriefPrevention.visualization.VisualizationType;
import me.ryanhamshire.GriefPrevention.events.ClaimDeletedEvent;
import org.apache.commons.lang.Validate;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created on 5/19/2017.
 *
 * @author RoboMWM
 */
public class ClaimRegistrar implements Listener
{
    private JavaPlugin plugin;
    private Set<Claim> claims;
    private Map<Long, Set<Claim>> chunksToClaimsMap = new ConcurrentHashMap<>();
    private Storage storage;
    private long lastUsedClaimId;

    public ClaimRegistrar(JavaPlugin plugin, Storage storage)
    {
        this.plugin = plugin;
        this.storage = storage;
        claims.addAll(storage.getClaims());
        lastUsedClaimId = System.currentTimeMillis();
    }

    /**
     * @return the "next" available claim ID.
     */
    public long nextClaimId()
    {
        long currentTime = System.currentTimeMillis();
        if (currentTime == lastUsedClaimId)
            ++currentTime;
        lastUsedClaimId = currentTime;
        return currentTime;
    }

    public void changeOwner(Claim claim, UUID newOwnerID) throws Exception
    {
        claim.setOwnerUUID(newOwnerID);
        storage.saveClaim(claim);
    }

    /**
     * Deletes a claim entirely, from storage and in internal register
     * @param claim
     * @return if the deletion succeeded
     */
    public boolean deleteClaim(Claim claim)
    {
        if (!storage.deleteClaim(claim))
            return false;
        claims.remove(claim);

        Set<Long> chunkHashes = ClaimUtils.getChunkHashes(claim);
        for(Long chunkHash : chunkHashes)
            this.chunksToClaimsMap.get(chunkHash).remove(claim);

        plugin.getServer().getPluginManager().callEvent(new ClaimDeletedEvent(claim));
        return true;
    }



    //Utilities useful for claims

    /**
     * Gets the claim at a specific location
     *
     * @param ignoreDepth Whether a location underneath the claim should return the claim.
     * @param cachedClaim can be NULL, but will help performance if you have a reasonable guess about which claim the location is in
     */
    public Claim getClaim(Location location, boolean ignoreDepth, Claim cachedClaim)
    {
        //check cachedClaim guess first.  if it's in the datastore and the location is inside it, we're done
        if(cachedClaim != null && claims.contains(cachedClaim) && cachedClaim.contains(location, ignoreDepth))
            return cachedClaim;

        Set<Claim> claimsInChunk = this.chunksToClaimsMap.get(ClaimUtils.getChunkHash(location));
        if(claimsInChunk == null)
            return null;
        for(Claim claim : claimsInChunk)
        {
            if(claim.contains(location, ignoreDepth))
                return claim;
        }

        return null;
    }

    /**
     * Get a collection of all land claims.
     *
     * If you need to make changes, use provided methods like .deleteClaim() and .createClaim().
     * This will ensure primary memory (RAM) and secondary memory (disk, database) stay in sync
     *
     * @return a read-only access point for the list of all land claims
     */
    public Collection<Claim> getClaims()
    {
        return Collections.unmodifiableCollection(this.claims);
    }

    /**
     * Get a collection of all land claims within the specified chunk coordinates.
     *
     * Note that there is no world parameter, so this will include all claims from all worlds in this chunk coordinate.
     *
     * @return a read-only access point for the list of all land claims
     */
    public Collection<Claim> getClaims(int chunkx, int chunkz)
    {
        Set<Claim> chunkClaims = this.chunksToClaimsMap.get(ClaimUtils.getChunkHash(chunkx, chunkz));
        if(chunkClaims != null)
        {
            return Collections.unmodifiableCollection(chunkClaims);
        }
        else
        {
            return Collections.unmodifiableCollection(new HashSet<>());
        }
    }
    
    /**
     * Creates and registers a new claim
     * @param firstCorner
     * @param secondCorner
     * @param ownerID the owner of this new claim. Null designates administrative claim.
     * @return a ClaimClaimResult
     * @throws IllegalArgumentException if corners' worlds don't match
     * @throws Exception if the newly-created claim was not able to be saved.
     * @see CreateClaimResult
     */
    public CreateClaimResult createClaim(Location firstCorner, Location secondCorner, UUID ownerID) throws Exception
    {

        Location[] corners = ClaimUtils.retrieveSortedCorners(firstCorner, secondCorner);

        //create a new claim instance (but don't save it, yet)
        Claim claimCandidate = new Claim(corners[0], corners[1], ownerID,null, nextClaimId());

        //ensure this new claim won't overlap any existing claims
        for(Claim claim : this.claims)
        {
            //if we find an existing claim which will be overlapped
            if(ClaimUtils.overlaps(claimCandidate, claim))
            {
                //Failed, return conflicting claim
                return new CreateClaimResult(false, claim);
            }
        }

        this.registerClaim(claimCandidate);

        //then return success along with reference to new claim
        return new CreateClaimResult(true, claimCandidate);
    }

    /**
     * @param claim
     * @param firstCorner
     * @param secondCorner
     * @return a CreateClaimResult
     * @throws IllegalArgumentException if corners' worlds don't match
     * @throws Exception if the newly-created claim was not able to be saved.
     * @see CreateClaimResult
     */
    synchronized public CreateClaimResult resizeClaim(Claim claim, Location firstCorner, Location secondCorner) throws Exception
    {
        Location[] corners = ClaimUtils.retrieveSortedCorners(firstCorner, secondCorner);

        //retain original depth
        corners[0].setY(claim.getLesserBoundaryCorner().getBlockY());

        //create a new claim instance (but don't save it, yet)
        Claim claimCandidate = new Claim(corners[0], corners[1], claim.getOwnerUUID(),null, claim.getID());

        //ensure this new claim won't overlap any existing claims
        for(Claim existingClaim : this.claims)
        {
            //skip claim we are resizing
            if (existingClaim == claim)
                continue;

            //if we find an existing claim which will be overlapped
            if(ClaimUtils.overlaps(existingClaim, claimCandidate))
            {
                //Failed, return conflicting claim
                return new CreateClaimResult(false, existingClaim);
            }
        }

        //Extend original claim
        claim.setLesserBoundaryCorner(corners[0]);
        claim.setGreaterBoundaryCorner(corners[1]);

        //Remove the claim from the chunkhash map (may be unnecessary but likely helps avoid checking invalidated claims in a chunk)
        Set<Long> chunkHashes = ClaimUtils.getChunkHashes(claim);
        for(Long chunkHash : chunkHashes)
            this.chunksToClaimsMap.get(chunkHash).remove(claim);

        this.registerClaim(claim);

        return new CreateClaimResult(true, claim);
    }

    //TODO: move this out
    void resizeClaimWithChecks(Player player, PlayerData playerData, int newx1, int newx2, int newy1, int newy2, int newz1, int newz2)
    {
        //for top level claims, apply size rules and claim blocks requirement
        if(playerData.claimResizing.parent == null)
        {
            //measure new claim, apply size rules
            int newWidth = (Math.abs(newx1 - newx2) + 1);
            int newHeight = (Math.abs(newz1 - newz2) + 1);
            boolean smaller = newWidth < playerData.claimResizing.getWidth() || newHeight < playerData.claimResizing.getHeight();

            if(!player.hasPermission("griefprevention.adminclaims") && !playerData.claimResizing.isAdminClaim() && smaller)
            {
                if(newWidth < GriefPrevention.instance.config_claims_minWidth || newHeight < GriefPrevention.instance.config_claims_minWidth)
                {
                    GriefPrevention.sendMessage(player, TextMode.Err, Messages.ResizeClaimTooNarrow, String.valueOf(GriefPrevention.instance.config_claims_minWidth));
                    return;
                }

                int newArea = newWidth * newHeight;
                if(newArea < GriefPrevention.instance.config_claims_minArea)
                {
                    GriefPrevention.sendMessage(player, TextMode.Err, Messages.ResizeClaimInsufficientArea, String.valueOf(GriefPrevention.instance.config_claims_minArea));
                    return;
                }
            }

            //make sure player has enough blocks to make up the difference
            if(!playerData.claimResizing.isAdminClaim() && player.getName().equals(playerData.claimResizing.getOwnerName()))
            {
                int newArea =  newWidth * newHeight;
                int blocksRemainingAfter = playerData.getRemainingClaimBlocks() + playerData.claimResizing.getArea() - newArea;

                if(blocksRemainingAfter < 0)
                {
                    GriefPrevention.sendMessage(player, TextMode.Err, Messages.ResizeNeedMoreBlocks, String.valueOf(Math.abs(blocksRemainingAfter)));
                    this.tryAdvertiseAdminAlternatives(player);
                    return;
                }
            }
        }

        //special rule for making a top-level claim smaller.  to check this, verifying the old claim's corners are inside the new claim's boundaries.
        //rule: in any mode, shrinking a claim removes any surface fluids
        Claim oldClaim = playerData.claimResizing;
        boolean smaller = false;
        if(oldClaim.parent == null)
        {
            //temporary claim instance, just for checking contains()
            Claim newClaim = new Claim(
                    new Location(oldClaim.getLesserBoundaryCorner().getWorld(), newx1, newy1, newz1),
                    new Location(oldClaim.getLesserBoundaryCorner().getWorld(), newx2, newy2, newz2),
                    null, new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>(), null);

            //if the new claim is smaller
            if(!newClaim.contains(oldClaim.getLesserBoundaryCorner(), true, false) || !newClaim.contains(oldClaim.getGreaterBoundaryCorner(), true, false))
            {
                smaller = true;

                //remove surface fluids about to be unclaimed
                oldClaim.removeSurfaceFluids(newClaim);
            }
        }

        //ask the datastore to try and resize the claim, this checks for conflicts with other claims
        CreateClaimResult result = GriefPrevention.instance.storage.resizeClaim(playerData.claimResizing, newx1, newx2, newy1, newy2, newz1, newz2, player);

        if(result.succeeded)
        {
            //decide how many claim blocks are available for more resizing
            int claimBlocksRemaining = 0;
            if(!playerData.claimResizing.isAdminClaim())
            {
                UUID ownerID = playerData.claimResizing.ownerID;
                if(playerData.claimResizing.parent != null)
                {
                    ownerID = playerData.claimResizing.parent.ownerID;
                }
                if(ownerID == player.getUniqueId())
                {
                    claimBlocksRemaining = playerData.getRemainingClaimBlocks();
                }
                else
                {
                    PlayerData ownerData = this.getPlayerData(ownerID);
                    claimBlocksRemaining = ownerData.getRemainingClaimBlocks();
                    OfflinePlayer owner = GriefPrevention.instance.getServer().getOfflinePlayer(ownerID);
                    if(!owner.isOnline())
                    {
                        this.clearCachedPlayerData(ownerID);
                    }
                }
            }

            //inform about success, visualize, communicate remaining blocks available
            GriefPrevention.sendMessage(player, TextMode.Success, Messages.ClaimResizeSuccess, String.valueOf(claimBlocksRemaining));
            Visualization visualization = Visualization.FromClaim(result.claim, player.getEyeLocation().getBlockY(), VisualizationType.Claim, player.getLocation());
            Visualization.apply(player, visualization);

            //if resizing someone else's claim, make a log entry
            if(!player.getUniqueId().equals(playerData.claimResizing.ownerID) && playerData.claimResizing.parent == null)
            {
                GriefPrevention.AddLogEntry(player.getName() + " resized " + playerData.claimResizing.getOwnerName() + "'s claim at " + GriefPrevention.getfriendlyLocationString(playerData.claimResizing.lesserBoundaryCorner) + ".");
            }

            //if in a creative mode world and shrinking an existing claim, restore any unclaimed area
            if(smaller && GriefPrevention.instance.creativeRulesApply(oldClaim.getLesserBoundaryCorner()))
            {
                GriefPrevention.sendMessage(player, TextMode.Warn, Messages.UnclaimCleanupWarning);
                GriefPrevention.instance.restoreClaim(oldClaim, 20L * 60 * 2);  //2 minutes
                GriefPrevention.AddLogEntry(player.getName() + " shrank a claim @ " + GriefPrevention.getfriendlyLocationString(playerData.claimResizing.getLesserBoundaryCorner()));
            }

            //clean up
            playerData.claimResizing = null;
            playerData.lastShovelLocation = null;
        }
        else
        {
            if(result.claim != null)
            {
                //inform player
                GriefPrevention.sendMessage(player, TextMode.Err, Messages.ResizeFailOverlap);

                //show the player the conflicting claim
                Visualization visualization = Visualization.FromClaim(result.claim, player.getEyeLocation().getBlockY(), VisualizationType.ErrorClaim, player.getLocation());
                Visualization.apply(player, visualization);
            }
            else
            {
                GriefPrevention.sendMessage(player, TextMode.Err, Messages.ResizeFailOverlapRegion);
            }
        }
    }

    /**
     * Returns a set of claims near a given location
     * @param location
     * @param radius specified in blocks
     * @return a set of claims
     */
    Set<Claim> getNearbyClaims(Location location, int radius)
    {
        Set<Claim> claims = new HashSet<Claim>();

        Chunk lesserChunk = location.getWorld().getChunkAt(location.subtract(radius, 0, radius));
        Chunk greaterChunk = location.getWorld().getChunkAt(location.add(radius, 0, radius));

        for(int chunk_x = lesserChunk.getX(); chunk_x <= greaterChunk.getX(); chunk_x++)
        {
            for(int chunk_z = lesserChunk.getZ(); chunk_z <= greaterChunk.getZ(); chunk_z++)
            {
                Chunk chunk = location.getWorld().getChunkAt(chunk_x, chunk_z);
                Long chunkID = ClaimUtils.getChunkHash(chunk.getBlock(0,  0,  0).getLocation());
                Set<Claim> claimsInChunk = this.chunksToClaimsMap.get(chunkID);
                if(claimsInChunk != null)
                {
                    for(Claim claim : claimsInChunk)
                    {
                        if(claim.getLesserBoundaryCorner().getWorld().equals(location.getWorld()))
                        {
                            claims.add(claim);
                        }
                    }
                }
            }
        }

        return claims;
    }

    //Used internally to register a claim
    //Registers both the claim and its chunk hash to the respective maps, as well as saving to storage
    private void registerClaim(Claim newClaim) throws Exception
    {
        storage.saveClaim(newClaim);
        this.claims.add(newClaim);
        Set<Long> chunkHashes = ClaimUtils.getChunkHashes(newClaim);
        for(Long chunkHash : chunkHashes)
        {
            Set<Claim> claimsInChunk = this.chunksToClaimsMap.get(chunkHash);
            if(claimsInChunk == null)
            {
                claimsInChunk = new HashSet<>();
                this.chunksToClaimsMap.put(chunkHash, claimsInChunk);
            }
            claimsInChunk.add(newClaim);
        }
    }
}
