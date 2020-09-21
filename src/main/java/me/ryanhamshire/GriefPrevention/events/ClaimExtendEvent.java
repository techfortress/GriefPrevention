package me.ryanhamshire.GriefPrevention.events;

import me.ryanhamshire.GriefPrevention.Claim;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * A cancellable event which is called when a claim's depth (lower y bound) is about to be extended.
 * @author FrankHeijden
 */
public class ClaimExtendEvent extends Event implements Cancellable
{
    private static final HandlerList handlers = new HandlerList();

    public static @NotNull HandlerList getHandlerList()
    {
        return handlers;
    }

    private final Claim claim;
    private final int newDepth;
    private boolean cancelled;

    public ClaimExtendEvent(@NotNull Claim claim, int newDepth)
    {
        this.claim = claim;
        this.newDepth = newDepth;
    }

    @Override
    public @NotNull HandlerList getHandlers()
    {
        return handlers;
    }

    public @NotNull Claim getClaim()
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
