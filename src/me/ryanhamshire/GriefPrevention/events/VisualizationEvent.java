package me.ryanhamshire.GriefPrevention.events;

import me.ryanhamshire.GriefPrevention.Claim;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import java.util.Collection;
import java.util.Collections;

public class VisualizationEvent extends PlayerEvent {
    private static final HandlerList handlers = new HandlerList();
    private final Collection<Claim> claims;
    private final boolean showSubdivides;

    public VisualizationEvent(Player player, Claim claim) {
        super(player);
        this.claims = Collections.singleton(claim);
        this.showSubdivides = true;
    }

    public VisualizationEvent(Player player, Collection<Claim> claims) {
        super(player);
        this.claims = claims;
        this.showSubdivides = false;
    }

    public Collection<Claim> getClaims() {
        return claims;
    }

    public boolean showSubdivides() {
        return showSubdivides;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
