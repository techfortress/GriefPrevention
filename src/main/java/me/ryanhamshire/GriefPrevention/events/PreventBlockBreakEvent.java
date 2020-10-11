package me.ryanhamshire.GriefPrevention.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockBreakEvent;

//if cancelled, GriefPrevention will allow a block to be broken which it would not have otherwise
public class PreventBlockBreakEvent extends Event implements Cancellable
{
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;
    private final BlockBreakEvent innerEvent;

    public static HandlerList getHandlerList()
    {
        return handlers;
    }

    public PreventBlockBreakEvent(BlockBreakEvent innerEvent)
    {
        this.innerEvent = innerEvent;
    }

    public BlockBreakEvent getInnerEvent()
    {
        return this.innerEvent;
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