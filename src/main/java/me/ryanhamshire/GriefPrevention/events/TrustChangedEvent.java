package me.ryanhamshire.GriefPrevention.events;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * This event is thrown when the trust is changed in one or more claims
 *
 * @author roinujnosde
 */
public class TrustChangedEvent extends Event implements Cancellable
{

    private static final HandlerList handlers = new HandlerList();

    private final Player changer;
    private final List<Claim> claims;
    private final ClaimPermission claimPermission;
    private final boolean given;
    private final String identifier;
    private boolean cancelled;

    public TrustChangedEvent(@NotNull Player changer, @NotNull List<Claim> claims,
                             @Nullable ClaimPermission claimPermission, boolean given, @NotNull String identifier)
    {
        super();
        this.changer = changer;
        this.claims = claims;
        this.claimPermission = claimPermission;
        this.given = given;
        this.identifier = identifier;
    }

    public TrustChangedEvent(@NotNull Player changer, @NotNull Claim claim, @Nullable ClaimPermission claimPermission,
                             boolean given,  @NotNull String identifier)
    {
        this.changer = changer;
        claims = new ArrayList<>();
        claims.add(claim);
        this.claimPermission = claimPermission;
        this.given = given;
        this.identifier = identifier;
    }

    @Override
    public @NotNull HandlerList getHandlers()
    {
        return handlers;
    }

    public static @NotNull HandlerList getHandlerList()
    {
        return handlers;
    }

    /**
     * Gets who made the change
     *
     * @return the changer
     */
    public @NotNull Player getChanger()
    {
        return changer;
    }

    /**
     * Gets the changed claims
     *
     * @return the changed claims
     */
    public @NotNull List<Claim> getClaims()
    {
        return claims;
    }

    /**
     * Gets the claim permission (null if the permission is being taken)
     *
     * @return the claim permission
     */
    public @Nullable ClaimPermission getClaimPermission()
    {
        return claimPermission;
    }

    /**
     * Checks if the trust is being given
     *
     * @return true if given, false if taken
     */
    public boolean isGiven()
    {
        return given;
    }

    /**
     * Gets the identifier of the receiver of this action
     * Can be: "public", "all", a UUID, a permission
     *
     * @return the identifier
     */
    public @NotNull String getIdentifier()
    {
        return identifier;
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
