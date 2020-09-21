package me.ryanhamshire.GriefPrevention.events;

import me.ryanhamshire.GriefPrevention.Claim;
import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//if destination field is set, then GriefPrevention will send the player to that location instead of searching for one
public class SaveTrappedPlayerEvent extends Event implements Cancellable
{
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;
    private Location destination = null;

    public static @NotNull HandlerList getHandlerList()
    {
        return handlers;
    }

    Claim claim;

    public SaveTrappedPlayerEvent(@NotNull Claim claim)
    {
        this.claim = claim;
    }

    public @Nullable Location getDestination()
    {
        return destination;
    }

    public void setDestination(@NotNull Location destination)
    {
        this.destination = destination;
    }

    public Claim getClaim()
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