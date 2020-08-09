package me.ryanhamshire.GriefPrevention.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.ryanhamshire.GriefPrevention.Claim;

public class PlayerAbandonClaimEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }


    private final Player player;
    private final Claim claim;
    private boolean cancelled;

    public PlayerAbandonClaimEvent(Player player, Claim claim) {
        this.player = player;
        this.claim = claim;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public Player getPlayer() {
        return player;
    }

    public Claim getClaim() {
        return claim;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}