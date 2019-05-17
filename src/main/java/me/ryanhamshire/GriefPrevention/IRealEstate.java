package me.ryanhamshire.GriefPrevention;

import org.bukkit.Material;
import org.bukkit.entity.Player;

public interface IRealEstate
{
	public String allowEdit(Claim claim, Player player);
	
	public String allowBuild(Claim claim, Player player, Material material);

	public String allowAccess(Claim claim, Player player);

	public String allowContainers(Claim claim, Player player);

	public String allowGrantPermission(Claim claim, Player player);

	public boolean anyTransaction(Claim claim);
}
