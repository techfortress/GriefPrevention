package me.ryanhamshire.GriefPrevention.events;

import me.ryanhamshire.GriefPrevention.Claim;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import java.util.Collection;
import java.util.Collections;

/**
 * Called when GriefPrevention is sending claim visuals to a player
 */
public class VisualizationEvent extends PlayerEvent
{
    private static final HandlerList handlers = new HandlerList();
    private final Collection<Claim> claims;
    private final boolean showSubdivides;
    private final boolean nearbyClaims;

    /**
     * New visualization being sent to player
     *
     * @param player Player receiving visuals
     * @param claim The claim being visualized (with subdivides), or null if visuals being removed
     */
    public VisualizationEvent(Player player, Claim claim)
    {
        super(player);
        this.claims = Collections.singleton(claim);
        this.showSubdivides = true;
        this.nearbyClaims = false;
    }

    /**
     * New visualization being sent to player
     *
     * @param player Player receiving visuals
     * @param claims Claims being visualized (without subdivides)
     */
    public VisualizationEvent(Player player, Collection<Claim> claims)
    {
        this(player, claims, false);
    }

    /**
     * New visualization being sent to player
     *
     * @param player Player receiving visuals
     * @param claims Claims being visualized (without subdivides)
     * @param nearbyClaims If the event is called on nearby claims (shift inspecting)
     */
    public VisualizationEvent(Player player, Collection<Claim> claims, boolean nearbyClaims)
    {
        super(player);
        this.claims = claims;
        this.showSubdivides = false;
        this.nearbyClaims = nearbyClaims;
    }

    /**
     * Get the claims being visualized, or null if visualization being removed
     *
     * @return Claims being visualized
     */
    public Collection<Claim> getClaims()
    {
        return claims;
    }

    /**
     * Check if subdivide claims are being shown
     *
     * @return True if subdivide claims are being shown
     */
    public boolean showSubdivides()
    {
        return showSubdivides;
    }

    /**
     * Check if event was called through shift-inspecting with the inspection tool.
     * @return True if shift-inspecting
     */
    public boolean isNearbyClaims()
    {
        return nearbyClaims;
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
