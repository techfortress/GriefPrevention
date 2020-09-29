package me.ryanhamshire.GriefPrevention.events;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

/**
 * This event is called when a {@link Claim} requires a specific level of trust.
 * If the denial reason is null, the trust requirements are met and the action is allowed.
 */
public class ClaimPermissionCheckEvent extends Event
{

    private static final HandlerList handlers = new HandlerList();

    private final Player checkedPlayer;
    private final UUID checkedUUID;
    private final Claim claim;
    private final ClaimPermission requiredPermission;
    private final Event triggeringEvent;
    private String denial;

    /**
     * Constructor for a ClaimPermissionCheckEvent.
     *
     * @param checked the Player being checked for permissions
     * @param claim the Claim in which permissions are being checked
     * @param required the ClaimPermission level required
     * @param triggeringEvent the Event triggering the permission check
     */
    public ClaimPermissionCheckEvent(Player checked, Claim claim, ClaimPermission required, Event triggeringEvent)
    {
        this(checked, checked.getUniqueId(), claim, required, triggeringEvent);
    }

    /**
     * Constructor for a ClaimPermissionCheckEvent.
     *
     * @param checked the UUID being checked for permissions
     * @param claim the Claim in which permissions are being checked
     * @param required the ClaimPermission level required
     * @param triggeringEvent the Event triggering the permission check
     */
    public ClaimPermissionCheckEvent(UUID checked, Claim claim, ClaimPermission required, Event triggeringEvent)
    {
        this(Bukkit.getPlayer(checked), checked, claim, required, triggeringEvent);
    }

    private ClaimPermissionCheckEvent(Player checkedPlayer, UUID checkedUUID, Claim claim, ClaimPermission required, Event triggeringEvent)
    {
        this.checkedPlayer = checkedPlayer;
        this.checkedUUID = checkedUUID;
        this.claim = claim;
        this.requiredPermission = required;
        this.triggeringEvent = triggeringEvent;
    }

    /**
     * Returns the Player being checked for permission if online.
     *
     * @return the Player being checked or null if offline
     */
    public Player getCheckedPlayer()
    {
        return checkedPlayer;
    }

    /**
     * Returns the UUID being checked for permission.
     *
     * @return the UUID being checked for permission
     */
    public UUID getCheckedUUID()
    {
        return checkedUUID;
    }

    /**
     * Returns the Claim in which permission is being checked.
     *
     * @return the Claim in which permission is being checked
     */
    public Claim getClaim()
    {
        return claim;
    }

    /**
     * Returns the ClaimPermission being checked for.
     *
     * @return the ClaimPermission being checked for
     */
    public ClaimPermission getRequiredPermission()
    {
        return requiredPermission;
    }

    /**
     * Returns the Event causing this event to fire.
     *
     * @return the Event triggering this event or null if none was provided
     */
    public Event getTriggeringEvent()
    {
        return triggeringEvent;
    }

    /**
     * Returns the reason the ClaimPermission check failed.
     * If the check did not fail, the message will be null.
     *
     * @return the denial reason or null if permission is granted
     */
    public String getDenialReason()
    {
        return denial;
    }

    /**
     * Sets the reason for denial.
     * @param denial the denial reason
     */
    public void setDenialReason(String denial)
    {
        this.denial = denial;
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
