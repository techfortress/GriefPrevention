package me.ryanhamshire.GriefPrevention.claim;

import me.ryanhamshire.GriefPrevention.message.Message;
import me.ryanhamshire.GriefPrevention.player.PlayerData;
import me.ryanhamshire.GriefPrevention.player.PlayerDataRegistrar;
import me.ryanhamshire.GriefPrevention.storage.Storage;
import me.ryanhamshire.GriefPrevention.visualization.VisualizationManager;
import me.ryanhamshire.GriefPrevention.visualization.VisualizationType;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created on 6/7/2018.
 * <p>
 * Utility class to register and access claims, and handle errors (somewhat) gracefully.
 * Provides a cache for getClaim calls.
 * <p>
 * TODO: replace sendMessage with calls to Message
 *
 * @author RoboMWM
 */
public class ClaimClerk implements Listener
{
    private Storage storage;
    private ClaimRegistrar claimRegistrar;
    private PlayerDataRegistrar playerDataRegistrar;
    private VisualizationManager visualizationManager;

    /**
     * Creates a new ClaimClerk, which helps assist in obtaining and performing actions on the claim and playerdata registrars.
     * <p>
     * You really <i>shouldn't</i> be instantiating this unless you're performing unusual actions and would benefit from a separate getClaim cache.
     *
     * @param plugin              Used to cleanup caches created using getClaim. Can be null <i>only</i> if you never use getClaim or always pass null for the Player param in getClaim.
     * @param claimRegistrar
     * @param playerDataRegistrar
     * @param storage
     */
    public ClaimClerk(JavaPlugin plugin, ClaimRegistrar claimRegistrar, PlayerDataRegistrar playerDataRegistrar, Storage storage, VisualizationManager visualizationManager)
    {
        if (plugin != null)
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.claimRegistrar = claimRegistrar;
        this.playerDataRegistrar = playerDataRegistrar;
        this.storage = storage;
        this.visualizationManager = visualizationManager;
    }

    /**
     * Registers a new claim
     *
     * @param firstCorner
     * @param secondCorner
     * @return true if successful, false otherwise
     */
    public boolean registerNewClaim(Player player, Location firstCorner, Location secondCorner)
    {
        PlayerData playerData = playerDataRegistrar.getOrCreatePlayerData(player.getUniqueId());
        if (playerData.getRemainingClaimBlocks(claimRegistrar) < ClaimUtils.getArea(firstCorner, secondCorner))
        {
            Message.NotEnoughClaimBlocks.send(player);
            return false;
        }

        CreateClaimResult claimResult = claimRegistrar.createClaim(firstCorner, secondCorner, player.getUniqueId());
        if (!claimResult.isSuccess())
        {
            Message.OverlapsAnotherClaim.send(player);
            return false;
        }

        Message.ClaimCreateSuccess.send(player);
        visualizationManager.apply(player, visualizationManager.fromClaim(claimResult.getClaim(), VisualizationType.Claim, player.getLocation()));

        return true;
    }

    /**
     * Resizes a given claim
     *
     * @param claim
     * @param firstCorner
     * @param secondCorner
     * @return
     */
    public boolean resizeClaim(Player player, Claim claim, Location firstCorner, Location secondCorner)
    {
        PlayerData playerData = playerDataRegistrar.getOrCreatePlayerData(claim.getOwnerUUID());

        if (playerData.getRemainingClaimBlocks(claimRegistrar) + claim.getArea() < ClaimUtils.getArea(firstCorner, secondCorner))
        {
            Message.ClaimCreateSuccess.send(player);
            return false;
        }

        try
        {
            CreateClaimResult claimResult = claimRegistrar.resizeClaim(claim, firstCorner, secondCorner);
            if (claimResult.isSuccess())
                return true;

            Message.OverlapsAnotherClaim.send(player);
        }
        catch (Exception e)
        {
            Message.ErrorSavingResizedClaim.send(player);
            e.printStackTrace();
            return false;
        }
        return false;
    }

    //For caching last-known claim
    private Map<Player, Claim> lastAccessedClaim = new HashMap<>();

    @EventHandler
    private void onQuit(PlayerQuitEvent event)
    {
        lastAccessedClaim.remove(event.getPlayer());
    }

    /**
     * Gets the claim at a specific location
     *
     * @param player      The player for whom we're retrieving the claim for. Used for caching.
     * @param ignoreDepth Whether a location underneath the claim should return the claim.
     */
    public Claim getClaim(Player player, Location location, boolean ignoreDepth)
    {
        return claimRegistrar.getClaim(location, ignoreDepth, lastAccessedClaim.get(player));
    }

    /**
     * Replaces the old map of trustees with the provided map, and saves the claim to storage
     *
     * @param claim
     * @param newTrustees
     * @return false if there was an issue saving the claim (may need to undo changes)
     */
    public void changeTrustees(Claim claim, Map<UUID, ClaimPermission> newTrustees)
    {
        claim.getTrusteesMap().clear();
        claim.getTrusteesMap().putAll(newTrustees);
        storage.saveClaim(claim);
    }
}
