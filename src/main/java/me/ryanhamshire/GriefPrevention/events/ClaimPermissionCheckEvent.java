package me.ryanhamshire.GriefPrevention.events;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

/**
 * This event is called when a Player attempts anything inside of a {@link Claim} that would require a
 * specific level of trust.
 */
public class ClaimPermissionCheckEvent extends PlayerEvent
{

    private static final HandlerList handlers = new HandlerList();

    private final Claim claim;
    private final ClaimPermission requiredPermission;
    private final Event triggeringEvent;
    private String denial;

    public ClaimPermissionCheckEvent(Player who, Claim claim, ClaimPermission required, Event triggeringEvent)
    {
        super(who);
        this.claim = claim;
        this.requiredPermission = required;
        this.triggeringEvent = triggeringEvent;
    }

    public Claim getClaim()
    {
        return claim;
    }

    public ClaimPermission getRequiredPermission()
    {
        return requiredPermission;
    }

    public Event getTriggeringEvent()
    {
        return triggeringEvent;
    }

    public String getDenialMessage()
    {
        return denial;
    }

    public void setDenialMessage(String denial)
    {
        this.denial = denial; // TODO should this be included in debug logging to track down problematic addons?
    }

    @Override
    public HandlerList getHandlers()
    {
        return handlers;
    }

    public static HandlerList getHandlerList()
    {
        return handlers;
    }

}
