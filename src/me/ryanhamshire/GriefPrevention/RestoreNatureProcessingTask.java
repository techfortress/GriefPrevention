/*
    GriefPrevention Server Plugin for Minecraft
    Copyright (C) 2012 Ryan Hamshire

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 
package me.ryanhamshire.GriefPrevention;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

//non-main-thread task which processes world data to repair the unnatural
//after processing is complete, creates a main thread task to make the necessary changes to the world
class RestoreNatureProcessingTask implements Runnable 
{
	//world information captured from the main thread
	//will be updated and sent back to main thread to be applied to the world
	private BlockSnapshot[][][] snapshots;
	
	//other information collected from the main thread.
	//not to be updated, only to be passed back to main thread to provide some context about the operation
	private int miny;
	private Environment environment;
	private Location lesserBoundaryCorner;
	private Location greaterBoundaryCorner;
	private Player player;			//absolutely must not be accessed.  not thread safe.
	private Biome biome;
	private boolean creativeMode;
	private int seaLevel;
	private boolean aggressiveMode;
	
	//two lists of materials
	private ArrayList<Material> notAllowedToHang;    //natural blocks which don't naturally hang in their air
	private ArrayList<Material> playerBlocks;		//a "complete" list of player-placed blocks.  MUST BE MAINTAINED as patches introduce more
	
	@SuppressWarnings("deprecation")
    public RestoreNatureProcessingTask(BlockSnapshot[][][] snapshots, int miny, Environment environment, Biome biome, Location lesserBoundaryCorner, Location greaterBoundaryCorner, int seaLevel, boolean aggressiveMode, boolean creativeMode, Player player)
	{
		this.snapshots = snapshots;
		this.miny = miny;
		if(this.miny < 0) this.miny = 0;
		this.environment = environment;
		this.lesserBoundaryCorner = lesserBoundaryCorner;
		this.greaterBoundaryCorner = greaterBoundaryCorner;
		this.biome = biome;
		this.seaLevel = seaLevel;
		this.aggressiveMode = aggressiveMode;
		this.player = player;
		this.creativeMode = creativeMode;
		
		this.notAllowedToHang = new ArrayList<Material>();
		this.notAllowedToHang.add(Material.DIRT);
		this.notAllowedToHang.add(Material.LONG_GRASS);
		this.notAllowedToHang.add(Material.SNOW);
		this.notAllowedToHang.add(Material.LOG);
		
		if(this.aggressiveMode)
		{
			this.notAllowedToHang.add(Material.GRASS);			
			this.notAllowedToHang.add(Material.STONE);
		}
		
		this.playerBlocks = new ArrayList<Material>();
		this.playerBlocks.addAll(RestoreNatureProcessingTask.getPlayerBlocks(this.environment, this.biome));
		
		//in aggressive or creative world mode, also treat these blocks as user placed, to be removed
		//this is helpful in the few cases where griefers intentionally use natural blocks to grief,
		//like a single-block tower of iron ore or a giant penis constructed with melons
		if(this.aggressiveMode || this.creativeMode)
		{
			this.playerBlocks.add(Material.IRON_ORE);
			this.playerBlocks.add(Material.GOLD_ORE);
			this.playerBlocks.add(Material.DIAMOND_ORE);
			this.playerBlocks.add(Material.MELON_BLOCK);
			this.playerBlocks.add(Material.MELON_STEM);
			this.playerBlocks.add(Material.BEDROCK);
			this.playerBlocks.add(Material.COAL_ORE);
			this.playerBlocks.add(Material.PUMPKIN);
			this.playerBlocks.add(Material.PUMPKIN_STEM);
			this.playerBlocks.add(Material.MELON);
		}
		
		if(this.aggressiveMode)
		{
			this.playerBlocks.add(Material.LEAVES);
			this.playerBlocks.add(Material.LEAVES_2);
			this.playerBlocks.add(Material.LOG);
			this.playerBlocks.add(Material.LOG_2);
			this.playerBlocks.add(Material.VINE);
		}
	}
	
	@Override
	public void run()
	{
		//order is important!
		
		//remove sandstone which appears to be unnatural
		this.removeSandstone();
		
		//remove any blocks which are definitely player placed
		this.removePlayerBlocks();
		
		//reduce large outcroppings of stone, sandstone
		this.reduceStone();
		
		//reduce logs, except in jungle biomes
		this.reduceLogs();
		
		//remove natural blocks which are unnaturally hanging in the air
		this.removeHanging();
		
		//remove natural blocks which are unnaturally stacked high
		this.removeWallsAndTowers();
		
		//fill unnatural thin trenches and single-block potholes
		this.fillHolesAndTrenches();
		
		//fill water depressions and fix unnatural surface ripples
		this.fixWater();
		
		//remove water/lava above sea level
		this.removeDumpedFluids();
		
		//cover surface stone and gravel with sand or grass, as the biome requires
		this.coverSurfaceStone();
		
		//remove any player-placed leaves
		this.removePlayerLeaves();
		
		//schedule main thread task to apply the result to the world
		RestoreNatureExecutionTask task = new RestoreNatureExecutionTask(this.snapshots, this.miny, this.lesserBoundaryCorner, this.greaterBoundaryCorner, this.player);
		GriefPrevention.instance.getServer().getScheduler().scheduleSyncDelayedTask(GriefPrevention.instance, task);
	}
	
	@SuppressWarnings("deprecation")
    private void removePlayerLeaves()
	{
		if(this.seaLevel < 1) return;
	    
	    for(int x = 1; x < snapshots.length - 1; x++)
		{
			for(int z = 1; z < snapshots[0][0].length - 1; z++)
			{
				for(int y = this.seaLevel - 1; y < snapshots[0].length; y++)
				{
					//note: see minecraft wiki data values for leaves
					BlockSnapshot block = snapshots[x][y][z];
					if(block.typeId == Material.LEAVES && (block.data & 0x4) != 0)
					{
						block.typeId = Material.AIR;
					}
				}
			}
		}		
	}

	//converts sandstone adjacent to sand to sand, and any other sandstone to air
	@SuppressWarnings("deprecation")
    private void removeSandstone()
	{
		for(int x = 1; x < snapshots.length - 1; x++)
		{
			for(int z = 1; z < snapshots[0][0].length - 1; z++)
			{
				for(int y = snapshots[0].length - 2; y > miny; y--)
				{
					if(snapshots[x][y][z].typeId != Material.SANDSTONE) continue;
					
					BlockSnapshot leftBlock = this.snapshots[x + 1][y][z];
					BlockSnapshot rightBlock = this.snapshots[x - 1][y][z];
					BlockSnapshot upBlock = this.snapshots[x][y][z + 1];
					BlockSnapshot downBlock = this.snapshots[x][y][z - 1];
					BlockSnapshot underBlock = this.snapshots[x][y - 1][z];
					BlockSnapshot aboveBlock = this.snapshots[x][y + 1][z];
					
					//skip blocks which may cause a cave-in
					if(aboveBlock.typeId == Material.SAND && underBlock.typeId == Material.AIR) continue;
					
					//count adjacent non-air/non-leaf blocks
					if(	leftBlock.typeId == Material.SAND || 
						rightBlock.typeId == Material.SAND ||
						upBlock.typeId == Material.SAND ||
						downBlock.typeId == Material.SAND ||
						aboveBlock.typeId == Material.SAND ||
						underBlock.typeId == Material.SAND)
					{
						snapshots[x][y][z].typeId = Material.SAND;
					}
					else
					{
						snapshots[x][y][z].typeId = Material.AIR;
					}					
				}				
			}
		}
	}
	
	@SuppressWarnings("deprecation")
    private void reduceStone()
	{
		if(this.seaLevel < 1) return;
	    
	    for(int x = 1; x < snapshots.length - 1; x++)
		{
			for(int z = 1; z < snapshots[0][0].length - 1; z++)
			{
				int thisy = this.highestY(x, z, true);
				
				while(thisy > this.seaLevel - 1 && (this.snapshots[x][thisy][z].typeId == Material.STONE || this.snapshots[x][thisy][z].typeId == Material.SANDSTONE))
				{
					BlockSnapshot leftBlock = this.snapshots[x + 1][thisy][z];
					BlockSnapshot rightBlock = this.snapshots[x - 1][thisy][z];
					BlockSnapshot upBlock = this.snapshots[x][thisy][z + 1];
					BlockSnapshot downBlock = this.snapshots[x][thisy][z - 1];
					
					//count adjacent non-air/non-leaf blocks
					byte adjacentBlockCount = 0;
					if(leftBlock.typeId != Material.AIR && leftBlock.typeId != Material.LEAVES && leftBlock.typeId != Material.VINE)
					{
						adjacentBlockCount++;
					}
					if(rightBlock.typeId != Material.AIR && rightBlock.typeId != Material.LEAVES && rightBlock.typeId != Material.VINE)
					{
						adjacentBlockCount++;
					}
					if(downBlock.typeId != Material.AIR && downBlock.typeId != Material.LEAVES && downBlock.typeId != Material.VINE)
					{
						adjacentBlockCount++;
					}
					if(upBlock.typeId != Material.AIR && upBlock.typeId != Material.LEAVES && upBlock.typeId != Material.VINE)
					{
						adjacentBlockCount++;
					}
					
					if(adjacentBlockCount < 3)
					{
						this.snapshots[x][thisy][z].typeId = Material.AIR;
					}

					thisy--;
				}				
			}
		}
	}
	
	@SuppressWarnings("deprecation")
    private void reduceLogs()
	{
		if(this.seaLevel < 1) return;
	    
	    boolean jungleBiome = this.biome == Biome.JUNGLE || this.biome == Biome.JUNGLE_HILLS;
		
		//scan all blocks above sea level
		for(int x = 1; x < snapshots.length - 1; x++)
		{
			for(int z = 1; z < snapshots[0][0].length - 1; z++)
			{
				for(int y = this.seaLevel - 1; y < snapshots[0].length; y++)
				{
					BlockSnapshot block = snapshots[x][y][z];
					
					//skip non-logs
					if(block.typeId != Material.LOG) continue;
					if(block.typeId != Material.LOG_2) continue;
					
					//if in jungle biome, skip jungle logs
					if(jungleBiome && block.data == 3) continue;
				
					//examine adjacent blocks for logs
					BlockSnapshot leftBlock = this.snapshots[x + 1][y][z];
					BlockSnapshot rightBlock = this.snapshots[x - 1][y][z];
					BlockSnapshot upBlock = this.snapshots[x][y][z + 1];
					BlockSnapshot downBlock = this.snapshots[x][y][z - 1];
					
					//if any, remove the log
					if(leftBlock.typeId == Material.LOG || rightBlock.typeId == Material.LOG || upBlock.typeId == Material.LOG || downBlock.typeId == Material.LOG)
					{
						this.snapshots[x][y][z].typeId = Material.AIR;
					}
				}				
			}
		}
	}
	
	@SuppressWarnings("deprecation")
    private void removePlayerBlocks()
	{
		int miny = this.miny;
		if(miny < 1) miny = 1;
		
		//remove all player blocks
		for(int x = 1; x < snapshots.length - 1; x++)
		{
			for(int z = 1; z < snapshots[0][0].length - 1; z++)
			{
				for(int y = miny; y < snapshots[0].length - 1; y++)
				{
					BlockSnapshot block = snapshots[x][y][z];
					if(this.playerBlocks.contains(block.typeId))
					{
						block.typeId = Material.AIR;
					}
				}
			}
		}				
	}
	
	@SuppressWarnings("deprecation")
    private void removeHanging()
	{
		int miny = this.miny;
		if(miny < 1) miny = 1;
		
		for(int x = 1; x < snapshots.length - 1; x++)
		{
			for(int z = 1; z < snapshots[0][0].length - 1; z++)
			{
				for(int y = miny; y < snapshots[0].length - 1; y++)
				{
					BlockSnapshot block = snapshots[x][y][z];
					BlockSnapshot underBlock = snapshots[x][y - 1][z];
					
					if(underBlock.typeId == Material.AIR || underBlock.typeId == Material.STATIONARY_WATER || underBlock.typeId == Material.STATIONARY_LAVA || underBlock.typeId == Material.LEAVES)
					{
						if(this.notAllowedToHang.contains(block.typeId))
						{
							block.typeId = Material.AIR;
						}
					}
				}
			}
		}
	}
	
	@SuppressWarnings("deprecation")
    private void removeWallsAndTowers()
	{
		Material [] excludedBlocksArray = new Material []
		{
			Material.CACTUS,
			Material.LONG_GRASS,
			Material.RED_MUSHROOM,
			Material.BROWN_MUSHROOM,
			Material.DEAD_BUSH,
			Material.SAPLING,
			Material.YELLOW_FLOWER,
			Material.RED_ROSE,
			Material.SUGAR_CANE_BLOCK,
			Material.VINE,
			Material.PUMPKIN,
			Material.WATER_LILY,
			Material.LEAVES
		};
		
		ArrayList<Material> excludedBlocks = new ArrayList<Material>();
		for(int i = 0; i < excludedBlocksArray.length; i++) excludedBlocks.add(excludedBlocksArray[i]);
		
		boolean changed;
		do
		{
			changed = false;
			for(int x = 1; x < snapshots.length - 1; x++)
			{
				for(int z = 1; z < snapshots[0][0].length - 1; z++)
				{
					int thisy = this.highestY(x, z, false);
					if(excludedBlocks.contains(this.snapshots[x][thisy][z].typeId)) continue;
						
					int righty = this.highestY(x + 1, z, false);
					int lefty = this.highestY(x - 1, z, false);
					while(lefty < thisy && righty < thisy)
					{
						this.snapshots[x][thisy--][z].typeId = Material.AIR;
						changed = true;
					}
					
					int upy = this.highestY(x, z + 1, false);
					int downy = this.highestY(x, z - 1, false);
					while(upy < thisy && downy < thisy)
					{
						this.snapshots[x][thisy--][z].typeId = Material.AIR;
						changed = true;
					}
				}
			}
		}while(changed);
	}
	
	@SuppressWarnings("deprecation")
    private void coverSurfaceStone()
	{
		for(int x = 1; x < snapshots.length - 1; x++)
		{
			for(int z = 1; z < snapshots[0][0].length - 1; z++)
			{
				int y = this.highestY(x, z, true);
				BlockSnapshot block = snapshots[x][y][z];
				
				if(block.typeId == Material.STONE || block.typeId == Material.GRAVEL || block.typeId == Material.SOIL || block.typeId == Material.DIRT || block.typeId == Material.SANDSTONE)
				{
					if(this.biome == Biome.DESERT || this.biome == Biome.DESERT_HILLS || this.biome == Biome.BEACHES)
					{
						this.snapshots[x][y][z].typeId = Material.SAND;
					}
					else
					{
						this.snapshots[x][y][z].typeId = Material.GRASS;
					}
				}
			}
		}
	}
	
	@SuppressWarnings("deprecation")
    private void fillHolesAndTrenches()
	{
		ArrayList<Material> fillableBlocks = new ArrayList<Material>();
		fillableBlocks.add(Material.AIR);
		fillableBlocks.add(Material.STATIONARY_WATER);
		fillableBlocks.add(Material.STATIONARY_LAVA);
		fillableBlocks.add(Material.LONG_GRASS);
		
		ArrayList<Material> notSuitableForFillBlocks = new ArrayList<Material>();
		notSuitableForFillBlocks.add(Material.LONG_GRASS);
		notSuitableForFillBlocks.add(Material.CACTUS);
		notSuitableForFillBlocks.add(Material.STATIONARY_WATER);
		notSuitableForFillBlocks.add(Material.STATIONARY_LAVA);
		notSuitableForFillBlocks.add(Material.LOG);
		notSuitableForFillBlocks.add(Material.LOG_2);
		
		boolean changed;
		do
		{
			changed = false;
			for(int x = 1; x < snapshots.length - 1; x++)
			{
				for(int z = 1; z < snapshots[0][0].length - 1; z++)
				{
					for(int y = 0; y < snapshots[0].length - 1; y++)
					{
						BlockSnapshot block = this.snapshots[x][y][z];
						if(!fillableBlocks.contains(block.typeId)) continue;
							
						BlockSnapshot leftBlock = this.snapshots[x + 1][y][z];
						BlockSnapshot rightBlock = this.snapshots[x - 1][y][z];
						
						if(!fillableBlocks.contains(leftBlock.typeId) && !fillableBlocks.contains(rightBlock.typeId))
						{
							if(!notSuitableForFillBlocks.contains(rightBlock.typeId))
							{
								block.typeId = rightBlock.typeId;
								changed = true;
							}
						}
					
						BlockSnapshot upBlock = this.snapshots[x][y][z + 1];
						BlockSnapshot downBlock = this.snapshots[x][y][z - 1];
						
						if(!fillableBlocks.contains(upBlock.typeId) && !fillableBlocks.contains(downBlock.typeId))
						{	
							if(!notSuitableForFillBlocks.contains(downBlock.typeId))
							{
								block.typeId = downBlock.typeId;
								changed = true;
							}
						}
					}
				}
			}
		}while(changed);
	}	
	
	@SuppressWarnings("deprecation")
    private void fixWater()
	{
		int miny = this.miny;
		if(miny < 1) miny = 1;
		
		boolean changed;
		
		//remove hanging water or lava
		for(int x = 1; x < snapshots.length - 1; x++)
		{
			for(int z = 1; z < snapshots[0][0].length - 1; z++)
			{
				for(int y = miny; y < snapshots[0].length - 1; y++)
				{
					BlockSnapshot block = this.snapshots[x][y][z];
					BlockSnapshot underBlock = this.snapshots[x][y][z];
					if(block.typeId == Material.STATIONARY_WATER || block.typeId == Material.STATIONARY_LAVA)
					{
						if(underBlock.typeId == Material.AIR || (underBlock.data != 0))
						{
							block.typeId = Material.AIR;
						}
					}
				}
			}
		}
		
		//fill water depressions
		do
		{
			changed = false;		
			for(int y = Math.max(this.seaLevel - 10, 0); y <= this.seaLevel; y++)			
			{
				for(int x = 1; x < snapshots.length - 1; x++)				
				{
					for(int z = 1; z < snapshots[0][0].length - 1; z++)
					{
						BlockSnapshot block = snapshots[x][y][z];
						
						//only consider air blocks and flowing water blocks for upgrade to water source blocks
						if(block.typeId == Material.AIR || (block.typeId == Material.STATIONARY_WATER && block.data != 0))
						{
							BlockSnapshot leftBlock = this.snapshots[x + 1][y][z];
							BlockSnapshot rightBlock = this.snapshots[x - 1][y][z];
							BlockSnapshot upBlock = this.snapshots[x][y][z + 1];
							BlockSnapshot downBlock = this.snapshots[x][y][z - 1];
							BlockSnapshot underBlock = this.snapshots[x][y - 1][z];
							
							//block underneath MUST be source water
							if(underBlock.typeId != Material.STATIONARY_WATER || underBlock.data != 0) continue;
							
							//count adjacent source water blocks
							byte adjacentSourceWaterCount = 0;
							if(leftBlock.typeId == Material.STATIONARY_WATER && leftBlock.data == 0)
							{
								adjacentSourceWaterCount++;
							}
							if(rightBlock.typeId == Material.STATIONARY_WATER && rightBlock.data == 0)
							{
								adjacentSourceWaterCount++;
							}
							if(upBlock.typeId == Material.STATIONARY_WATER && upBlock.data == 0)
							{
								adjacentSourceWaterCount++;
							}
							if(downBlock.typeId == Material.STATIONARY_WATER && downBlock.data == 0)
							{
								adjacentSourceWaterCount++;
							}
							
							//at least two adjacent blocks must be source water
							if(adjacentSourceWaterCount >= 2)
							{
								block.typeId = Material.STATIONARY_WATER;
								block.data = 0;
								changed = true;
							}
						}
					}
				}
			}
		}while(changed);
	}
	
	@SuppressWarnings("deprecation")
    private void removeDumpedFluids()
	{
		if(this.seaLevel < 1) return;
	    
	    //remove any surface water or lava above sea level, presumed to be placed by players
		//sometimes, this is naturally generated.  but replacing it is very easy with a bucket, so overall this is a good plan
		if(this.environment == Environment.NETHER) return;
		for(int x = 1; x < snapshots.length - 1; x++)
		{
			for(int z = 1; z < snapshots[0][0].length - 1; z++)
			{
				for(int y = this.seaLevel - 1; y < snapshots[0].length - 1; y++)
				{
					BlockSnapshot block = snapshots[x][y][z];
					if(block.typeId == Material.STATIONARY_WATER || block.typeId == Material.STATIONARY_LAVA ||
					   block.typeId == Material.WATER || block.typeId == Material.LAVA)
					{
						block.typeId = Material.AIR;
					}
				}
			}
		}
	}
	
	@SuppressWarnings("deprecation")
    private int highestY(int x, int z, boolean ignoreLeaves)
	{
		int y;
		for(y = snapshots[0].length - 1; y > 0; y--)
		{
			BlockSnapshot block = this.snapshots[x][y][z];
			if(block.typeId != Material.AIR &&
			!(ignoreLeaves && block.typeId == Material.SNOW) &&
			!(ignoreLeaves && block.typeId == Material.LEAVES) &&
			!(ignoreLeaves && block.typeId == Material.LEAVES_2) &&
			!(block.typeId == Material.STATIONARY_WATER) &&
			!(block.typeId == Material.WATER) &&
			!(block.typeId == Material.LAVA) &&
			!(block.typeId == Material.STATIONARY_LAVA))
			{
				return y;
			}
		}
		
		return y;
	}
	
	@SuppressWarnings("deprecation")
    static ArrayList<Material> getPlayerBlocks(Environment environment, Biome biome) 
	{
		//NOTE on this list.  why not make a list of natural blocks?
		//answer: better to leave a few player blocks than to remove too many natural blocks.  remember we're "restoring nature"
		//a few extra player blocks can be manually removed, but it will be impossible to guess exactly which natural materials to use in manual repair of an overzealous block removal
		ArrayList<Material> playerBlocks = new ArrayList<Material>();
		playerBlocks.add(Material.FIRE);
		playerBlocks.add(Material.BED_BLOCK);
		playerBlocks.add(Material.WOOD);
		playerBlocks.add(Material.BOOKSHELF);
		playerBlocks.add(Material.BREWING_STAND);
		playerBlocks.add(Material.BRICK);
		playerBlocks.add(Material.COBBLESTONE);
		playerBlocks.add(Material.GLASS);
		playerBlocks.add(Material.LAPIS_BLOCK);
		playerBlocks.add(Material.DISPENSER);
		playerBlocks.add(Material.NOTE_BLOCK);
		playerBlocks.add(Material.POWERED_RAIL);
		playerBlocks.add(Material.DETECTOR_RAIL);
		playerBlocks.add(Material.PISTON_STICKY_BASE);
		playerBlocks.add(Material.PISTON_BASE);
		playerBlocks.add(Material.PISTON_EXTENSION);
		playerBlocks.add(Material.WOOL);
		playerBlocks.add(Material.PISTON_MOVING_PIECE);
		playerBlocks.add(Material.GOLD_BLOCK);
		playerBlocks.add(Material.IRON_BLOCK);
		playerBlocks.add(Material.DOUBLE_STEP);
		playerBlocks.add(Material.STEP);
		playerBlocks.add(Material.CROPS);
		playerBlocks.add(Material.TNT);
		playerBlocks.add(Material.MOSSY_COBBLESTONE);
		playerBlocks.add(Material.TORCH);
		playerBlocks.add(Material.FIRE);
		playerBlocks.add(Material.WOOD_STAIRS);
		playerBlocks.add(Material.CHEST);
		playerBlocks.add(Material.REDSTONE_WIRE);
		playerBlocks.add(Material.DIAMOND_BLOCK);
		playerBlocks.add(Material.WORKBENCH);
		playerBlocks.add(Material.FURNACE);
		playerBlocks.add(Material.BURNING_FURNACE);
		playerBlocks.add(Material.WOODEN_DOOR);
		playerBlocks.add(Material.SIGN_POST);
		playerBlocks.add(Material.LADDER);
		playerBlocks.add(Material.RAILS);
		playerBlocks.add(Material.COBBLESTONE_STAIRS);
		playerBlocks.add(Material.WALL_SIGN);
		playerBlocks.add(Material.STONE_PLATE);
		playerBlocks.add(Material.LEVER);
		playerBlocks.add(Material.IRON_DOOR_BLOCK);
		playerBlocks.add(Material.WOOD_PLATE);
		playerBlocks.add(Material.REDSTONE_TORCH_ON);
		playerBlocks.add(Material.REDSTONE_TORCH_OFF);
		playerBlocks.add(Material.STONE_BUTTON);
		playerBlocks.add(Material.SNOW_BLOCK);
		playerBlocks.add(Material.JUKEBOX);
		playerBlocks.add(Material.FENCE);
		playerBlocks.add(Material.PORTAL);
		playerBlocks.add(Material.JACK_O_LANTERN);
		playerBlocks.add(Material.CAKE_BLOCK);
		playerBlocks.add(Material.DIODE_BLOCK_ON);
		playerBlocks.add(Material.DIODE_BLOCK_OFF);
		playerBlocks.add(Material.TRAP_DOOR);
		playerBlocks.add(Material.SMOOTH_BRICK);
		playerBlocks.add(Material.HUGE_MUSHROOM_1);
		playerBlocks.add(Material.HUGE_MUSHROOM_2);
		playerBlocks.add(Material.IRON_FENCE);
		playerBlocks.add(Material.THIN_GLASS);
		playerBlocks.add(Material.MELON_STEM);
		playerBlocks.add(Material.FENCE_GATE);
		playerBlocks.add(Material.BRICK_STAIRS);
		playerBlocks.add(Material.SMOOTH_STAIRS);
		playerBlocks.add(Material.ENCHANTMENT_TABLE);
		playerBlocks.add(Material.BREWING_STAND);
		playerBlocks.add(Material.CAULDRON);
		playerBlocks.add(Material.DIODE_BLOCK_ON);
		playerBlocks.add(Material.DIODE_BLOCK_ON);		
		playerBlocks.add(Material.WEB);
		playerBlocks.add(Material.SPONGE);
		playerBlocks.add(Material.GRAVEL);
		playerBlocks.add(Material.EMERALD_BLOCK);
		playerBlocks.add(Material.SANDSTONE);
		playerBlocks.add(Material.WOOD_STEP);
		playerBlocks.add(Material.WOOD_DOUBLE_STEP);
		playerBlocks.add(Material.ENDER_CHEST);
		playerBlocks.add(Material.SANDSTONE_STAIRS);
		playerBlocks.add(Material.SPRUCE_WOOD_STAIRS);
		playerBlocks.add(Material.JUNGLE_WOOD_STAIRS);
		playerBlocks.add(Material.COMMAND);
		playerBlocks.add(Material.BEACON);
		playerBlocks.add(Material.COBBLE_WALL);
		playerBlocks.add(Material.FLOWER_POT);
		playerBlocks.add(Material.CARROT);
		playerBlocks.add(Material.POTATO);
		playerBlocks.add(Material.WOOD_BUTTON);
		playerBlocks.add(Material.SKULL);
		playerBlocks.add(Material.ANVIL);
		playerBlocks.add(Material.SPONGE);
		playerBlocks.add(Material.DOUBLE_STONE_SLAB2);
		playerBlocks.add(Material.STAINED_GLASS);
		playerBlocks.add(Material.STAINED_GLASS_PANE);
		playerBlocks.add(Material.BANNER);
		playerBlocks.add(Material.STANDING_BANNER);
		playerBlocks.add(Material.ACACIA_STAIRS);
		playerBlocks.add(Material.BIRCH_WOOD_STAIRS);
		playerBlocks.add(Material.DARK_OAK_STAIRS);
		playerBlocks.add(Material.TRAPPED_CHEST);
		playerBlocks.add(Material.GOLD_PLATE);
		playerBlocks.add(Material.IRON_PLATE);
		playerBlocks.add(Material.REDSTONE_COMPARATOR_OFF);
		playerBlocks.add(Material.REDSTONE_COMPARATOR_ON);
		playerBlocks.add(Material.DAYLIGHT_DETECTOR);
		playerBlocks.add(Material.DAYLIGHT_DETECTOR_INVERTED);
		playerBlocks.add(Material.REDSTONE_BLOCK);
		playerBlocks.add(Material.HOPPER);
		playerBlocks.add(Material.QUARTZ_BLOCK);
		playerBlocks.add(Material.QUARTZ_STAIRS);
		playerBlocks.add(Material.DROPPER);
		playerBlocks.add(Material.SLIME_BLOCK);
		playerBlocks.add(Material.IRON_TRAPDOOR);
		playerBlocks.add(Material.PRISMARINE);
		playerBlocks.add(Material.HAY_BLOCK);
		playerBlocks.add(Material.CARPET);
		playerBlocks.add(Material.SEA_LANTERN);
		playerBlocks.add(Material.RED_SANDSTONE_STAIRS);
		playerBlocks.add(Material.STONE_SLAB2);
		playerBlocks.add(Material.ACACIA_FENCE);
		playerBlocks.add(Material.ACACIA_FENCE_GATE);
		playerBlocks.add(Material.BIRCH_FENCE);
		playerBlocks.add(Material.BIRCH_FENCE_GATE);
		playerBlocks.add(Material.DARK_OAK_FENCE);
		playerBlocks.add(Material.DARK_OAK_FENCE_GATE);
		playerBlocks.add(Material.JUNGLE_FENCE);
        playerBlocks.add(Material.JUNGLE_FENCE_GATE);
        playerBlocks.add(Material.SPRUCE_FENCE);
        playerBlocks.add(Material.SPRUCE_FENCE_GATE);
        playerBlocks.add(Material.ACACIA_DOOR);
        playerBlocks.add(Material.SPRUCE_DOOR);
        playerBlocks.add(Material.DARK_OAK_DOOR);
        playerBlocks.add(Material.JUNGLE_DOOR);
        playerBlocks.add(Material.BIRCH_DOOR);
        playerBlocks.add(Material.COAL_BLOCK);
        playerBlocks.add(Material.REDSTONE_LAMP_OFF);
        playerBlocks.add(Material.REDSTONE_LAMP_ON);
        playerBlocks.add(Material.PURPUR_BLOCK);
        playerBlocks.add(Material.PURPUR_SLAB);
        playerBlocks.add(Material.PURPUR_DOUBLE_SLAB);
        playerBlocks.add(Material.PURPUR_PILLAR);
        playerBlocks.add(Material.PURPUR_STAIRS);
        playerBlocks.add(Material.NETHER_WART_BLOCK);
        playerBlocks.add(Material.RED_NETHER_BRICK);
        playerBlocks.add(Material.BONE_BLOCK);
        
		//these are unnatural in the standard world, but not in the nether
		if(environment != Environment.NETHER)
		{
			playerBlocks.add(Material.NETHERRACK);
			playerBlocks.add(Material.SOUL_SAND);
			playerBlocks.add(Material.GLOWSTONE);
			playerBlocks.add(Material.NETHER_BRICK);
			playerBlocks.add(Material.NETHER_FENCE);
			playerBlocks.add(Material.NETHER_BRICK_STAIRS);
			playerBlocks.add(Material.MAGMA);
		}
		
		//these are unnatural in the standard and nether worlds, but not in the end
		if(environment != Environment.THE_END)
		{
			playerBlocks.add(Material.OBSIDIAN);
			playerBlocks.add(Material.ENDER_STONE);
			playerBlocks.add(Material.ENDER_PORTAL_FRAME);
			playerBlocks.add(Material.CHORUS_PLANT);
			playerBlocks.add(Material.CHORUS_FLOWER);
		}
		
		//these are unnatural in sandy biomes, but not elsewhere
		if(biome == Biome.DESERT || biome == Biome.DESERT_HILLS || biome == Biome.BEACHES || environment != Environment.NORMAL)
		{
			playerBlocks.add(Material.LEAVES);
			playerBlocks.add(Material.LEAVES_2);
			playerBlocks.add(Material.LOG);
			playerBlocks.add(Material.LOG_2);
		}
		
		return playerBlocks;
	}
}
