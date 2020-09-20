package me.ryanhamshire.GriefPrevention.events;

import me.ryanhamshire.GriefPrevention.Claim;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This event gets called whenever a claim is going to be deleted. This event is
 * not called when a claim is resized.
 *
 * @author Tux2
 */
public class ClaimDeletedEvent extends Event
{

    // Custom Event Requirements
    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList()
    {
        return handlers;
    }

    private final Claim claim;

    public ClaimDeletedEvent(Claim claim)
    {
        this.claim = claim;
    }

    /**
     * Gets the claim to be deleted.
     *
     * @return
     */
    public Claim getClaim()
    {
        return claim;
    }

    @Override
    public HandlerList getHandlers()
    {
        return handlers;
    }
}