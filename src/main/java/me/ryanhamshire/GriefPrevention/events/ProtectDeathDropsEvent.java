package me.ryanhamshire.GriefPrevention.events;

import me.ryanhamshire.GriefPrevention.Claim;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

//if cancelled, GriefPrevention will not protect items dropped by a player on death
public class ProtectDeathDropsEvent extends Event implements Cancellable
{
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;

    public static HandlerList getHandlerList()
    {
        return handlers;
    }

    Claim claim;

    public ProtectDeathDropsEvent(Claim claim)
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