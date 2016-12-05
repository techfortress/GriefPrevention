package me.ryanhamshire.GriefPrevention.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when GP is about to deliver claim blocks to a player (~every 10 minutes)
 *
 * @author RoboMWM
 * 11/15/2016.
 */
public class AccrueClaimBlocksEvent extends Event
{
    // Custom Event Requirements
    private static final HandlerList handlers = new HandlerList();
    public static HandlerList getHandlerList() {
        return handlers;
    }
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    private Player player;
    private int blocksToAccrue;
    private boolean cancelled = false;

    public AccrueClaimBlocksEvent(Player player, int blocksToAccrue)
    {
        this.player = player;
        this.blocksToAccrue = blocksToAccrue / 6;
    }

    public Player getPlayer()
    {
        return this.player;
    }

    /**
     * @return amount of claim blocks GP will deliver to the player for this 10 minute interval
     */
    public int getBlocksToAccrue()
    {
        return this.blocksToAccrue;
    }

    public boolean isCancelled()
    {
        return this.cancelled;
    }

    /**
     * Modify the amount of claim blocks to deliver to the player for this 10 minute interval
     * @param blocksToAccrue blocks to deliver
     */
    public void setBlocksToAccrue(int blocksToAccrue)
    {
        this.blocksToAccrue = blocksToAccrue;
    }

    /**
     * Similar to setBlocksToAccrue(int), but automatically converting from a per-hour rate value to a 10-minute rate value
     * @param blocksToAccruePerHour the per-hour rate of blocks to deliver
     */

    public void setBlocksToAccruePerHour(int blocksToAccruePerHour)
    {
        this.blocksToAccrue = blocksToAccruePerHour / 6;
    }

    public void setCancelled(boolean cancel)
    {
        this.cancelled = cancel;
    }
}
