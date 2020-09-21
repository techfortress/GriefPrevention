package me.ryanhamshire.GriefPrevention.events;

import me.ryanhamshire.GriefPrevention.Claim;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//if cancelled, GriefPrevention will not protect items dropped by a player on death
public class ProtectDeathDropsEvent extends Event implements Cancellable
{
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;

    public static @NotNull HandlerList getHandlerList()
    {
        return handlers;
    }

    private final Claim claim;

    public ProtectDeathDropsEvent(@Nullable Claim claim)
    {
        this.claim = claim;
    }

    public @Nullable Claim getClaim()
    {
        return this.claim;
    }

    @Override
    public @NotNull HandlerList getHandlers()
    {
        return handlers;
    }

    @Override
    public boolean isCancelled()
    {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled)
    {
        this.cancelled = cancelled;
    }
}