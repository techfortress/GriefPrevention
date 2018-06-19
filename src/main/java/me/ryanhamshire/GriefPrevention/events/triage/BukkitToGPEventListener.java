package me.ryanhamshire.GriefPrevention.events.triage;

import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;

import java.util.ArrayList;
import java.util.List;

import static org.bukkit.event.EventPriority.LOWEST;

/**
 * Fires the respective triage GP events, and handles cancellation
 * TODO: listen at lowest priority?
 * TODO: call events even if canceled?
 * Created on 2/23/2017.
 *
 * @author RoboMWM
 */
public class BukkitToGPEventListener implements Listener
{
    private GriefPrevention plugin;
    public BukkitToGPEventListener(GriefPrevention griefPrevention)
    {
        this.plugin = griefPrevention;
    }

    private boolean callEvent(GPBaseEvent event)
    {
        Cancellable baseEvent = null;

        //GPevent.isCancelled() = baseevent.isCancelled()
        if (event.getBaseEvent() instanceof Cancellable)
        {
            baseEvent = (Cancellable)event.getBaseEvent();
            event.setCancelled(baseEvent.isCancelled());
        }

        plugin.getServer().getPluginManager().callEvent(event);

        //baseevent.isCancelled() = GPevent.isCancelled()
        if (baseEvent != null)
        {
            baseEvent.setCancelled(event.isCancelled());
        }

        return event.isCancelled();
    }

    private boolean callWithoutCancelingEvent(GPBaseEvent event)
    {
        plugin.getServer().getPluginManager().callEvent(event);
        return event.isCancelled();
    }

    @EventHandler(priority = LOWEST)
    private void onBlockPlace(BlockPlaceEvent event)
    {
        callEvent(new GPBlockMutateTypeEvent(event, event.getPlayer(), event.getBlock().getLocation(), event.getBlock()));
    }
    @EventHandler(priority = LOWEST)
    private void onBlockBreak(BlockBreakEvent event)
    {
        callEvent(new GPBlockMutateTypeEvent(event, event.getPlayer(), event.getBlock().getLocation(), event.getBlock()));
    }

    @EventHandler(priority = LOWEST)
    private void onPaintingPlace(HangingPlaceEvent event)
    {
        //TODO: block location, or entity location? Big_Scary used entity location
        callEvent(new GPBlockMutateTypeEvent(event, event.getPlayer(), event.getEntity().getLocation(), event.getEntity()));
    }
    @EventHandler(priority = LOWEST)
    private void onPaintingBreak(HangingBreakEvent event)
    {
        Entity destroyerEntity = null;
        if (event instanceof HangingBreakByEntityEvent)
        {
            HangingBreakByEntityEvent entityEvent = (HangingBreakByEntityEvent)event;
            destroyerEntity = entityEvent.getRemover();
        }

        callEvent(new GPBlockMutateTypeEvent(event, destroyerEntity, event.getEntity().getLocation(), event.getEntity()));
    }
    @EventHandler(priority = LOWEST)
    private void onBlockLikeEntityDamage(EntityDamageByEntityEvent event)
    {
        switch (event.getEntityType())
        {
            case ITEM_FRAME:
            case ARMOR_STAND:
            case ENDER_CRYSTAL:
                callEvent(new GPBlockMutateTypeEvent(event, event.getDamager(), event.getEntity().getLocation(), event.getEntity()));
        }
    }

    @EventHandler(priority = LOWEST)
    private void onVehicleDamage(VehicleDamageEvent event)
    {
        //TODO: entity damage
        //callEvent(new GPBlockMutateTypeEvent(event, event.getAttacker(), event.getVehicle().getLocation(), event.getAttacker()));
    }

    @EventHandler(priority = LOWEST)
    private void onEntityExplode(EntityExplodeEvent event)
    {
        //Call an event for each block that's to-be-destroyed
        //Thus, the base event won't be canceled unless it's explicitly canceled
        List<Block> blocksToRemove = new ArrayList<>();
        for (Block block : event.blockList())
        {
            if (callWithoutCancelingEvent(new GPBlockMutateTypeEvent(event, event.getEntity(), block.getLocation(), block)))
                blocksToRemove.add(block);
        }
        event.blockList().removeAll(blocksToRemove);
    }
    @EventHandler(priority = LOWEST)
    private void onBlockExplode(BlockExplodeEvent event) //largely same as above, but block as source
    {
        List<Block> blocksToRemove = new ArrayList<>();
        for (Block block : event.blockList())
        {
            if (callWithoutCancelingEvent(new GPBlockMutateTypeEvent(event, event.getBlock(), block.getLocation(), block)))
                blocksToRemove.add(block);
        }
        event.blockList().removeAll(blocksToRemove);
    }

    @EventHandler(priority = LOWEST)
    private void onEntityFormBlock(EntityBlockFormEvent event) //Frost walker
    {
        callEvent(new GPBlockMutateTypeEvent(event, event.getEntity(), event.getBlock().getLocation(), event.getBlock()));
    }
}
