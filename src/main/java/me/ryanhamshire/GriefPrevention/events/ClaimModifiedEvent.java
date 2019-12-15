package me.ryanhamshire.GriefPrevention.events;

import me.ryanhamshire.GriefPrevention.Claim;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This Event is thrown when a claim is changed....it is not modifiable or
 * cancellable and only serves as a notification a claim has changed. The
 * CommandSender can be null in the event that the modification is called by the
 * plugin itself. Created by Narimm on 5/08/2018.
 */
public class ClaimModifiedEvent extends Event {

	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}

	private final Claim claim;
	private CommandSender modifier;

	public ClaimModifiedEvent(Claim claim, CommandSender modifier) {
		this.claim = claim;
		this.modifier = modifier;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	/**
	 * The claim
	 *
	 * @return the claim
	 */
	public Claim getClaim() {
		return claim;
	}

	/**
	 * The actor making the change...can be null
	 *
	 * @return the CommandSender or null
	 */
	public CommandSender getModifier() {
		return modifier;
	}
}
