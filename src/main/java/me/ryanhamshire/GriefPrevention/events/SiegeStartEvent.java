package me.ryanhamshire.GriefPrevention.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.ryanhamshire.GriefPrevention.Claim;

public class SiegeStartEvent extends Event implements Cancellable {
	
	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	
	private final Player attacker;
	private final Player defender;
	private final Claim claim;
	private boolean cancelled;
	
	public SiegeStartEvent(Player attacker, Player defender, Claim claim) {
		this.attacker = attacker;
		this.defender = defender;
		this.claim = claim;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	/**
	 * @deprecated Use the {@link #getAttacker() getAttacker} method.
	 */
	@Deprecated
	public Player getPlayer() {
		return attacker;
	}
	
	public Player getAttacker() {
		return attacker;
	}
	
	public Player getDefender() {
		return defender;
	}
	
	public Claim getDefenderClaim() {
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
