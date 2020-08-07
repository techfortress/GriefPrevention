package me.ryanhamshire.GriefPrevention.events;

import me.ryanhamshire.GriefPrevention.Claim;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import java.util.Collection;
import java.util.Collections;

/**
 * Event is called whenever a user inspects a block using the inspection tool.
 * @author FrankHeijden
 */
public class ClaimInspectionEvent extends PlayerEvent implements Cancellable
{
    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList()
    {
        return handlers;
    }

    private final Collection<Claim> claims;
    private final Block inspectedBlock;
    private final boolean inspectingNearbyClaims;
    private boolean cancelled;

    /**
     * Constructs a new ClaimInspectionEvent with a player and claim instance.
     * @param player The player actor
     * @param inspectedBlock The inspected block
     * @param claim The claim involved
     */
    public ClaimInspectionEvent(Player player, Block inspectedBlock, Claim claim) {
        super(player);

        this.inspectedBlock = inspectedBlock;
        if (claim != null) {
            this.claims = Collections.singleton(claim);
        } else {
            this.claims = null;
        }
        this.inspectingNearbyClaims = false;
    }

    /**
     * Constructs a new ClaimInspectionEvent with a player, list of claims and boolean flag inspectingNearbyClaims.
     * @param player The player actor
     * @param inspectedBlock The inspected block
     * @param claims The list of claims involved
     * @param inspectingNearbyClaims Whether or not the user is inspecting nearby claims ("shift-clicking")
     */
    public ClaimInspectionEvent(Player player, Block inspectedBlock, Collection<Claim> claims, boolean inspectingNearbyClaims) {
        super(player);
        this.inspectedBlock = inspectedBlock;
        this.claims = claims;
        this.inspectingNearbyClaims = inspectingNearbyClaims;
    }

    public Block getInspectedBlock()
    {
        return inspectedBlock;
    }

    public Collection<Claim> getClaims()
    {
        return claims;
    }

    public boolean isInspectingNearbyClaims()
    {
        return inspectingNearbyClaims;
    }

    @Override
    public HandlerList getHandlers()
    {
        return handlers;
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
