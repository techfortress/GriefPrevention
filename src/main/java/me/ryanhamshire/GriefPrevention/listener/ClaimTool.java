package me.ryanhamshire.GriefPrevention.listener;

import me.ryanhamshire.GriefPrevention.claim.Claim;
import me.ryanhamshire.GriefPrevention.claim.ClaimClerk;
import me.ryanhamshire.GriefPrevention.claim.ClaimUtils;
import me.ryanhamshire.GriefPrevention.visualization.VisualizationManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockIterator;

import java.util.HashMap;
import java.util.Map;

/**
 * Created on 7/1/2018.
 * <p>
 *
 * @author RoboMWM
 */
public class ClaimTool implements Listener
{
    private Plugin plugin;
    private ClaimClerk claimClerk;
    private VisualizationManager visualizationManager;
    private Material toolType;

    private Map<Player, FirstCorner> firstCornerMap = new HashMap<>();

    public ClaimTool(Plugin plugin, ClaimClerk claimClerk, VisualizationManager visualizationManager, Material toolType)
    {
        this.plugin = plugin;
        this.claimClerk = claimClerk;
        this.visualizationManager = visualizationManager;
        this.toolType = toolType;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private void onHotbarChange(PlayerItemHeldEvent event)
    {
        //TODO: permission check
        Player player = event.getPlayer();
        firstCornerMap.remove(event.getPlayer());
        ItemStack item = player.getInventory().getItem(event.getNewSlot());
        if (item == null || item.getType() != toolType)
            return;

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                ItemStack stack = player.getInventory().getItemInMainHand();
                if (stack == null || stack.getType() != toolType)
                    return;
                player.sendMessage("You have x number of claimblocks yada yada."); //TODO: message
            }
        }.runTaskLater(plugin, 15L);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private void onPlayerRightClickShovel(PlayerInteractEvent event)
    {
        //TODO: prevent accidental doubleclicks (add small cooldown)

        Player player = event.getPlayer();

        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        //Ignore left clicks
        if (event.getHand() != EquipmentSlot.HAND)
            return;

        //TODO: configurable.
        if (event.getItem() == null || event.getItem().getType() != Material.GOLDEN_SHOVEL)
            return;

        //TODO: ClaimToolEvent

        event.setCancelled(true);

        Block block = event.getClickedBlock();

        if (block == null)
            block = getTargetBlock(player, 100); //TODO: use view distance(?)

        if (block == null)
        {
            player.sendMessage("No block found! (Or too far away.)"); //TODO: message
            return;
        }


        if (!firstCornerMap.containsKey(player))
        {
            setFirstCorner(player, block.getLocation());

            //TODO: determine claim mode

            return;
        }

        FirstCorner firstCorner = firstCornerMap.remove(player);
        Location secondCorner = block.getLocation();


        switch (firstCorner.getToolMode())
        {
            case CREATE:
                claimClerk.registerNewClaim(player, firstCorner.getLocation(), secondCorner);
                break;
        }
    }

    private void setFirstCorner(Player player, Location location)
    {
        //If clicking within claim, see if we can validly enter resizing mode.
        Claim claim = claimClerk.getClaim(player, location, false);
        if (claim != null)
        {
            if (claim.hasPermission(player, null) && ClaimUtils.isCorner(claim, location))
            {
                firstCornerMap.put(player, new FirstCorner(location, ToolMode.EXTEND, claim));
                player.sendMessage("Resizing claim. Click where you want to move this corner."); //TODO: message
                player.sendBlockChange(location, location.getBlock().getBlockData());
                return;
            }

            player.sendMessage("There is a claim here already! Click a corner to resize a claim, or click elsewhere to create a new claim."); //TODO: message
            visualizationManager.apply(player, claim);
            return;
        }

        firstCornerMap.put(player, new FirstCorner(location, ToolMode.CREATE, null));

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                player.sendBlockChange(location, Material.DIAMOND_BLOCK.createBlockData());
            }
        }.runTask(plugin);

        player.sendMessage("Entered claim creation mode, now select opposite corner."); //TODO: message
    }

    private Block getTargetBlock(Player player, int maxDistance) throws IllegalStateException
    {
        Location eye = player.getEyeLocation();
        Material eyeMaterial = eye.getBlock().getType();
        boolean passThroughWater = (eyeMaterial == Material.WATER);
        BlockIterator iterator = new BlockIterator(player.getLocation(), player.getEyeHeight(), maxDistance);
        Block result = player.getLocation().getBlock().getRelative(BlockFace.UP);
        while (iterator.hasNext())
        {
            result = iterator.next();
            Material type = result.getType();
            if (type != Material.AIR &&
                    (!passThroughWater || type != Material.WATER) &&
                    type != Material.TALL_GRASS &&
                    type != Material.SNOW) return result;
        }

        return result;
    }
}

class FirstCorner
{
    private ToolMode toolMode;
    private Location location;
    private Claim claim;

    FirstCorner(Location location, ToolMode toolMode, Claim claim)
    {
        this.location = location;
        this.toolMode = toolMode;
        this.claim = claim;
    }

    public Location getLocation()
    {
        return location;
    }

    public ToolMode getToolMode()
    {
        return toolMode;
    }

    public Claim getClaim()
    {
        return claim;
    }
}

enum ToolMode
{
    ADMIN_CREATE,
    CREATE,
    EXTEND
}