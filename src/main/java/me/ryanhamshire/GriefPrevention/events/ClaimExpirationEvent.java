package me.ryanhamshire.GriefPrevention.events;

import me.ryanhamshire.GriefPrevention.Claim;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

//if cancelled, the claim will not be deleted
public class ClaimExpirationEvent extends Event implements Cancellable
{
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;
    
    public static HandlerList getHandlerList()
    {
        return handlers;
    }

    Claim claim;

    public ClaimExpirationEvent(Claim claim)
    {
        this.claim = claim;
    }

    public Claim getClaim()
    {
        return this.claim;
    }

    @Override
    public HandlerList getHandlers()
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