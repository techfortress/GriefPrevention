package me.ryanhamshire.GriefPrevention.events;

import me.ryanhamshire.GriefPrevention.Claim;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * A cancellable event which is called when a claim is extended in the Y direction.
 * @author FrankHeijden
 */
public class ClaimExtendEvent extends Event implements Cancellable
{
    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList()
    {
        return handlers;
    }

    private final Claim claim;
    private final int newDepth;
    private boolean cancelled;

    public ClaimExtendEvent(Claim claim, int newDepth)
    {
        this.claim = claim;
        this.newDepth = newDepth;
    }

    @Override
    public HandlerList getHandlers()
    {
        return handlers;
    }

    public Claim getClaim()
    {
        return claim;
    }

    public int getNewDepth()
    {
        return newDepth;
    }

    @Override
    public boolean isCancelled()
    {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled)
    {
        this.cancelled = cancelled;
    }
}
