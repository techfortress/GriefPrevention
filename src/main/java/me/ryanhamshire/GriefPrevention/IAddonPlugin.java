package me.ryanhamshire.GriefPrevention;

import org.bukkit.Material;
import org.bukkit.entity.Player;

public interface IAddonPlugin
{
	/**
	 * Is the player allowed to edit this claim
	 * @param claim the claim the player wants to edit
	 * @param player the player
	 * @return an error message or null if the player may edit the claim
	 */
	public String allowEdit(Claim claim, Player player);
	
	/**
	 * Is the player allowed to build on this claim
	 * @param claim The claim
	 * @param player The player
	 * @param material The material the player wants to build with
	 * @return an error message or null if the player is allowed to build on the claim
	 */
	public String allowBuild(Claim claim, Player player, Material material);

	/**
	 * Is the player allowed to access the claim
	 * @param claim The claim
	 * @param player The player
	 * @return an error message or null if the player is allowed to access the claim
	 */
	public String allowAccess(Claim claim, Player player);

	/**
	 * Is the player allowed to use the containers of the claim
	 * @param claim The claim
	 * @param player The player
	 * @return An error message or null if the player is allowed to access the containers
	 */
	public String allowContainers(Claim claim, Player player);

	/**
	 * Is the player allowed to grant permissions for this claim
	 * @param claim The claim
	 * @param player The player
	 * @return an error message or null if the player is allowed to grant permissions
	 */
	public String allowGrantPermission(Claim claim, Player player);

	/**
	 * Is the player allowed to resize this claim
	 * @param claim The claim
	 * @param player The player
	 * @param newx1 new X1
	 * @param newx2 new X2
	 * @param newy1 new Y1
	 * @param newy2 new Y2
	 * @param newz1 new Z1
	 * @param newz2 new Z2
	 * @return an error message or null if the player is allowed to resize the claim
	 */
	public String mayResizeClaim(Claim claim, Player player, int newx1, int newx2, int newy1, int newy2, int newz1, int newz2);

	/**
	 * Is the player allowed to abandon this claim
	 * @param claim The claim
	 * @param player The player
	 * @return an error message or null if the player is allowed to abandon the claim
	 */
	public String mayAbandonClaim(Claim claim, Player player);
}
