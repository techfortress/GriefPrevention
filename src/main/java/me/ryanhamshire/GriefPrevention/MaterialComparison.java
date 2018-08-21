package me.ryanhamshire.GriefPrevention;

import org.bukkit.Material;

public final class MaterialComparison
{
	public static boolean isSpawnEgg(Material material)
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

	public static boolean isInfested(Material material)
	{
		switch(material)
		{
			case INFESTED_STONE:
			case INFESTED_COBBLESTONE:
			case INFESTED_STONE_BRICKS:
			case INFESTED_MOSSY_STONE_BRICKS:
			case INFESTED_CRACKED_STONE_BRICKS:
			case INFESTED_CHISELED_STONE_BRICKS:
				return true;
			default:
				return false;
		}
	}

	public static boolean isStateful(Material material)
	{
		switch(material)
		{
			case NOTE_BLOCK:
			case REPEATER:
			case DRAGON_EGG:
			case DAYLIGHT_DETECTOR:
			case COMPARATOR:
			case FLOWER_POT:
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
