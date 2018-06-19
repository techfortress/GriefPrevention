package me.ryanhamshire.GriefPrevention.claim;

import me.ryanhamshire.GriefPrevention.player.PlayerData;
import me.ryanhamshire.GriefPrevention.player.PlayerDataRegistrar;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created on 6/7/2018.
 *
 * Utility class to register claims and handle errors.
 *
 * @author RoboMWM
 */
public class ClaimClerk implements Listener
{
    private ClaimRegistrar claimRegistrar;
    private PlayerDataRegistrar playerDataRegistrar;

    public ClaimClerk(JavaPlugin plugin, ClaimRegistrar claimRegistrar, PlayerDataRegistrar playerDataRegistrar)
    {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.claimRegistrar = claimRegistrar;
        this.playerDataRegistrar = playerDataRegistrar;
    }

    /**
     * Registers a new claim
     * @param firstCorner
     * @param secondCorner
     * @return true if successful, false otherwise
     */
    public boolean registerNewClaim(Player player, Location firstCorner, Location secondCorner)
    {
        PlayerData playerData = playerDataRegistrar.getPlayerData(player.getUniqueId());
        if (playerData.getRemainingClaimBlocks() < ClaimUtils.getArea(firstCorner, secondCorner))
        {
            player.sendMessage("Not enough claim blocks");
            return false;
        }

        try
        {
            CreateClaimResult claimResult = claimRegistrar.createClaim(firstCorner, secondCorner, player.getUniqueId());
            if (claimResult.isSuccess())
                return true;

            player.sendMessage("Overlaps another claim");
            //send overlapped claim
        }
        catch (Exception e)
        {
            player.sendMessage("Error occurred while attempting to save your new claim, see console log for details.");
            e.printStackTrace();
            return false;
        }
        return false;
    }

    /**
     * Resizes a given claim
     * @param claim
     * @param firstCorner
     * @param secondCorner
     * @return
     */
    public boolean resizeClaim(Player player, Claim claim, Location firstCorner, Location secondCorner)
    {
        PlayerData playerData = playerDataRegistrar.getPlayerData(claim.getOwnerUUID());

        if (playerData.getRemainingClaimBlocks() + claim.getArea() < ClaimUtils.getArea(firstCorner, secondCorner))
        {
            player.sendMessage("Not enough claim blocks");
            return false;
        }

        try
        {
            CreateClaimResult claimResult = claimRegistrar.resizeClaim(claim, firstCorner, secondCorner);
            if (claimResult.isSuccess())
                return true;

            player.sendMessage("Overlaps another claim");
            //send overlapped claim
        }
        catch (Exception e)
        {
            player.sendMessage("Error occurred while attempting to save your resized claim, see console log for details.");
            e.printStackTrace();
            return false;
        }
        return false;
    }

    //For caching last-known claim
    private Map<Player, Claim> lastAccessedClaim = new HashMap<>();

    @EventHandler
    private void onQuit(Player player)
    {
        lastAccessedClaim.remove(player);
    }

    /**
     * Gets the claim at a specific location
     *
     * @param player The player for whom we're retrieving the claim for. Used for caching.
     * @param ignoreDepth Whether a location underneath the claim should return the claim.
     */
    public Claim getClaim(Player player, Location location, boolean ignoreDepth)
    {
        return claimRegistrar.getClaim(location, ignoreDepth, lastAccessedClaim.get(player));
    }
}
