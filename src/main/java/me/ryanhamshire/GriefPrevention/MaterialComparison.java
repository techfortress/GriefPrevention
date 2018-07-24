package me.ryanhamshire.GriefPrevention;

import org.bukkit.Material;

public final class MaterialComparison
{
	public static boolean isSpawnerEgg(Material material)
	{
		switch(material) {

			case ELDER_GUARDIAN_SPAWN_EGG:
			case BAT_SPAWN_EGG:
			case BLAZE_SPAWN_EGG:
			case CAVE_SPIDER_SPAWN_EGG:
			case CREEPER_SPAWN_EGG:
			case DOLPHIN_SPAWN_EGG:
			case DROWNED_SPAWN_EGG:
			case ENDERMAN_SPAWN_EGG:
			case ENDERMITE_SPAWN_EGG:
			case EVOKER_SPAWN_EGG:
			case GHAST_SPAWN_EGG:
			case GUARDIAN_SPAWN_EGG:
			case HUSK_SPAWN_EGG:
			case LLAMA_SPAWN_EGG:
			case MAGMA_CUBE_SPAWN_EGG:
			case PHANTOM_SPAWN_EGG:
			case POLAR_BEAR_SPAWN_EGG:
			case SHULKER_SPAWN_EGG:
			case SILVERFISH_SPAWN_EGG:
			case SKELETON_SPAWN_EGG:
			case SLIME_SPAWN_EGG:
			case SPIDER_SPAWN_EGG:
			case STRAY_SPAWN_EGG:
			case VEX_SPAWN_EGG:
			case VINDICATOR_SPAWN_EGG:
			case WITCH_SPAWN_EGG:
			case WITHER_SKELETON_SPAWN_EGG:
			case WOLF_SPAWN_EGG:
			case ZOMBIE_SPAWN_EGG:
			case ZOMBIE_PIGMAN_SPAWN_EGG:
			case ZOMBIE_VILLAGER_SPAWN_EGG:
			case CHICKEN_SPAWN_EGG:
			case COD_SPAWN_EGG:
			case COW_SPAWN_EGG:
			case MOOSHROOM_SPAWN_EGG:
			case OCELOT_SPAWN_EGG:
			case PARROT_SPAWN_EGG:
			case PIG_SPAWN_EGG:
			case PUFFERFISH_SPAWN_EGG:
			case RABBIT_SPAWN_EGG:
			case SALMON_SPAWN_EGG:
			case SHEEP_SPAWN_EGG:
			case SKELETON_HORSE_SPAWN_EGG:
			case SQUID_SPAWN_EGG:
			case TROPICAL_FISH_SPAWN_EGG:
			case TURTLE_SPAWN_EGG:
			case VILLAGER_SPAWN_EGG:
			case ZOMBIE_HORSE_SPAWN_EGG:
				return true;
			default:
				return false;
		}
	}
	
	public static boolean isBoat(Material material)
	{
		switch(material)
		{
			case OAK_BOAT:
			case ACACIA_BOAT:
			case BIRCH_BOAT:
			case DARK_OAK_BOAT:
			case JUNGLE_BOAT:
			case SPRUCE_BOAT:
				return true;
			default:
				return false;
		}
	}

	public static boolean isLeaves(Material material)
	{
		switch(material)
		{
			case OAK_LEAVES:
			case ACACIA_LEAVES:
			case BIRCH_LEAVES:
			case DARK_OAK_LEAVES:
			case JUNGLE_LEAVES:
			case SPRUCE_LEAVES:
				return true;
			default:
				return false;
		}
	}

	public static boolean isLog(Material material)
	{
		switch(material)
		{
			case OAK_LOG:
			case ACACIA_LOG:
			case BIRCH_LOG:
			case DARK_OAK_LOG:
			case JUNGLE_LOG:
			case SPRUCE_LOG:
				return true;
			default:
				return false;
		}
	}

	public static boolean isSapling(Material material)
	{
		switch(material)
		{
			case OAK_SAPLING:
			case ACACIA_SAPLING:
			case BIRCH_SAPLING:
			case DARK_OAK_SAPLING:
			case JUNGLE_SAPLING:
			case SPRUCE_SAPLING:
				return true;
			default:
				return false;
		}
	}

	public static boolean isMinecart(Material material)
	{
		switch(material)
		{
			case MINECART:
			case FURNACE_MINECART:
			case CHEST_MINECART:
			case TNT_MINECART:
			case HOPPER_MINECART:
				return true;
			default:
				return false;
		}
	}
	
	public static boolean isDoor(Material material)
	{
		switch(material)
		{
			case OAK_DOOR:
			case ACACIA_DOOR:
			case BIRCH_DOOR:
			case JUNGLE_DOOR:
			case SPRUCE_DOOR:
			case DARK_OAK_DOOR:
				return true;
			default:
				return false;
		}
	}
	
	public static boolean isTrapDoor(Material material)
	{
		switch(material)
		{
			case OAK_TRAPDOOR:
			case ACACIA_TRAPDOOR:
			case BIRCH_TRAPDOOR:
			case JUNGLE_TRAPDOOR:
			case SPRUCE_TRAPDOOR:
			case DARK_OAK_TRAPDOOR:
				return true;
			default:
				return false;
		}
	}

	public static boolean isButton(Material material)
	{
		switch (material)
		{
			case BIRCH_BUTTON:
			case OAK_BUTTON:
			case ACACIA_BUTTON:
			case JUNGLE_BUTTON:
			case SPRUCE_BUTTON:
			case DARK_OAK_BUTTON:
			case STONE_BUTTON:
			case LEVER:
				return true;
			default:
				return false;
		}
	}
	
	public static boolean isFenceGate(Material material)
	{
		switch(material)
		{
			case OAK_FENCE_GATE:
			case ACACIA_FENCE_GATE:
			case BIRCH_FENCE_GATE:
			case JUNGLE_FENCE_GATE:
			case SPRUCE_FENCE_GATE:
			case DARK_OAK_FENCE_GATE:
				return true;
			default:
				return false;
		}
	}
	
	public static boolean isBed(Material material)
	{
		switch(material)
		{
			case BLACK_BED:
			case BLUE_BED:
			case BROWN_BED:
			case CYAN_BED:
			case GRAY_BED:
			case GREEN_BED:
			case LIGHT_BLUE_BED:
			case LIGHT_GRAY_BED:
			case LIME_BED:
			case MAGENTA_BED:
			case ORANGE_BED:
			case PINK_BED:
			case PURPLE_BED:
			case RED_BED:
			case WHITE_BED:
			case YELLOW_BED:
				return true;
			default:
				return false;
		}
	}
}
