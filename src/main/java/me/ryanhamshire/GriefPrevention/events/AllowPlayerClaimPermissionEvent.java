package me.ryanhamshire.GriefPrevention.events;

import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.ryanhamshire.GriefPrevention.Claim;

/**
 * This event is called when we check if a player has some permission allowed in a claim.
 * If the message is null that means that he has the permission otherwise it is the error message.
 *
 */
public class AllowPlayerClaimPermissionEvent extends Event {

	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	
	private final Player player;
	private final Claim claim;
	private final AllowedPermissionType permissionType;
	private String message;
	@Nullable
	private final Material material;
	
	public AllowPlayerClaimPermissionEvent(Player player, Claim claim, AllowedPermissionType permissionType, String message) {
		this(player, claim, permissionType, message, null);
	}
	
	public AllowPlayerClaimPermissionEvent(Player player, Claim claim, AllowedPermissionType permissionType, String message,
			@Nullable Material material) {
		this.player = player;
		this.claim = claim;
		this.permissionType = permissionType;
		this.message = message;
		this.material = material;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	/**
	 * @return the player for whom we want to know if he has the permission
	 */
	public Player getPlayer() {
		return player;
	}
	
	/**
	 * @return the involved claim.
	 */
	public Claim getClaim() {
		return claim;
	}
	
	/**
	 * @return the permission type of the event
	 */
	public AllowedPermissionType getPermissionType() {
		return permissionType;
	}
	
	/**
	 * @return the error message of the permission of this player in this claim for this permission type
	 */
	public String getMessage() {
		return message;
	}
	
	/**
	 * @param error message of the permission of this player in this claim for this permission type
	 */
	public void setMessage(String message) {
		this.message = message;
	}
	
	/**
	 * @return the material for permission type build.
	 */
	@Nullable
	public Material getMaterial() {
		return material;
	}
	
	public static enum AllowedPermissionType {
		EDIT, BUILD, ACCESS, CONTAINERS, GRANT_PERMISSION;
	}
	
}
