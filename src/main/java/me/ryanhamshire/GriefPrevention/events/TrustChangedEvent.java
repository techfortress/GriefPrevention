package me.ryanhamshire.GriefPrevention.events;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimPermission;

/**
 * This event is thrown when the trust is changed in one or more claims
 * 
 * @author roinujnosde
 *
 */
public class TrustChangedEvent extends Event implements Cancellable {
	
    private static final HandlerList handlers = new HandlerList();
    
    private final Player changer;
    private final List<Claim> claims;
    private final ClaimPermission claimPermission;
    private final boolean given;
    private final String identifier;
    private boolean cancelled;
    
	public TrustChangedEvent(Player changer, List<Claim> claims, ClaimPermission claimPermission, boolean given,
			String identifier) {
		super();
		this.changer = changer;
		this.claims = claims;
		this.claimPermission = claimPermission;
		this.given = given;
		this.identifier = identifier;
	}

	public TrustChangedEvent(Player changer, Claim claim, ClaimPermission claimPermission, boolean given, String identifier) {
		this.changer = changer;
		claims = new ArrayList<>();
		claims.add(claim);
		this.claimPermission = claimPermission;
		this.given = given;
		this.identifier = identifier;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	/**
	 * Gets who made the change
	 * 
	 * @return the changer
	 */
	public Player getChanger() {
		return changer;
	}

	/**
	 * Gets the changed claims
	 * 
	 * @return the changed claims
	 */
	public List<Claim> getClaims() {
		return claims;
	}

	/**
	 * Gets the claim permission (null if the permission is being taken)
	 * 
	 * @return the claim permission
	 */
	public ClaimPermission getClaimPermission() {
		return claimPermission;
	}

	/**
	 * Checks if the trust is being given
	 * 
	 * @return true if given, false if taken
	 */
	public boolean isGiven() {
		return given;
	}

	/**
	 * Gets the identifier of the receiver of this action
	 * Can be: "public", "all", a UUID, a permission
	 * 
	 * @return the identifier
	 */
	public String getIdentifier() {
		return identifier;
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
