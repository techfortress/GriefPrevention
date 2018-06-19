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

package me.ryanhamshire.GriefPrevention.visualization;

import java.util.ArrayList;

import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.claim.Claim;
import me.ryanhamshire.GriefPrevention.player.PlayerData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

//represents a visualization sent to a player
//FEATURE: to show players visually where claim boundaries are, we send them fake block change packets
//the result is that those players see new blocks, but the world hasn't been changed.  other players can't see the new blocks, either.
public class Visualization 
{
	private ArrayList<VisualizationElement> elements = new ArrayList<VisualizationElement>();

    public ArrayList<VisualizationElement> getElements()
    {
        return elements;
    }

    public World getWorld()
    {
        if (elements.isEmpty())
            return null;
        return elements.get(0).location.getWorld();
    }

    //adds a claim's visualization to the current visualization
	//handy for combining several visualizations together, as when visualization a top level claim with several subdivisions inside
	//locality is a performance consideration.  only create visualization blocks for around 100 blocks of the locality
    public void addClaimElements(Claim claim, int height, VisualizationType visualizationType, Location locality)
	{
		Location smallXsmallZ = claim.getLesserBoundaryCorner();
		Location bigXbigZ = claim.getGreaterBoundaryCorner();
		World world = smallXsmallZ.getWorld();
		boolean waterIsTransparent = locality.getBlock().getType() == Material.STATIONARY_WATER;
		
		int smallx = smallXsmallZ.getBlockX();
		int smallz = smallXsmallZ.getBlockZ();
		int bigx = bigXbigZ.getBlockX();
		int bigz = bigXbigZ.getBlockZ();
		
		Material cornerMaterial;
		Material accentMaterial;
		
		ArrayList<VisualizationElement> newElements = new ArrayList<VisualizationElement>();

        switch (visualizationType)
        {
            case Claim:
                cornerMaterial = Material.GLOWSTONE;
                accentMaterial = Material.GOLD_BLOCK;
                break;
            case AdminClaim:
                cornerMaterial = Material.GLOWSTONE;
                accentMaterial = Material.PUMPKIN;
                break;
            case RestoreNature:
                cornerMaterial = Material.DIAMOND_BLOCK;
                accentMaterial = Material.DIAMOND_BLOCK;
                break;
            default:
                cornerMaterial = Material.GLOWING_REDSTONE_ORE;
                accentMaterial = Material.NETHERRACK;
                break;
        }
		
		//initialize visualization elements without Y values and real storage
		//that will be added later for only the visualization elements within visualization range
		
		//locality
		int minx = locality.getBlockX() - 75;
		int minz = locality.getBlockZ() - 75;
		int maxx = locality.getBlockX() + 75;
		int maxz = locality.getBlockZ() + 75;
		
		final int STEP = 10;
		
		//top line		
		newElements.add(new VisualizationElement(new Location(world, smallx, 0, bigz), cornerMaterial, (byte)0, Material.AIR, (byte)0));
		newElements.add(new VisualizationElement(new Location(world, smallx + 1, 0, bigz), accentMaterial, (byte)0, Material.AIR, (byte)0));
		for(int x = smallx + STEP; x < bigx - STEP / 2; x += STEP)
		{
			if(x > minx && x < maxx)
			    newElements.add(new VisualizationElement(new Location(world, x, 0, bigz), accentMaterial, (byte)0, Material.AIR, (byte)0));
		}
		newElements.add(new VisualizationElement(new Location(world, bigx - 1, 0, bigz), accentMaterial, (byte)0, Material.AIR, (byte)0));
		
		//bottom line
		newElements.add(new VisualizationElement(new Location(world, smallx + 1, 0, smallz), accentMaterial, (byte)0, Material.AIR, (byte)0));
        for(int x = smallx + STEP; x < bigx - STEP / 2; x += STEP)
		{
			if(x > minx && x < maxx)
			    newElements.add(new VisualizationElement(new Location(world, x, 0, smallz), accentMaterial, (byte)0, Material.AIR, (byte)0));
		}
        newElements.add(new VisualizationElement(new Location(world, bigx - 1, 0, smallz), accentMaterial, (byte)0, Material.AIR, (byte)0));
		
		//left line
        newElements.add(new VisualizationElement(new Location(world, smallx, 0, smallz), cornerMaterial, (byte)0, Material.AIR, (byte)0));
        newElements.add(new VisualizationElement(new Location(world, smallx, 0, smallz + 1), accentMaterial, (byte)0, Material.AIR, (byte)0));
		for(int z = smallz + STEP; z < bigz - STEP / 2; z += STEP)
		{
			if(z > minz && z < maxz)
			    newElements.add(new VisualizationElement(new Location(world, smallx, 0, z), accentMaterial, (byte)0, Material.AIR, (byte)0));
		}
		newElements.add(new VisualizationElement(new Location(world, smallx, 0, bigz - 1), accentMaterial, (byte)0, Material.AIR, (byte)0));
        
		//right line
		newElements.add(new VisualizationElement(new Location(world, bigx, 0, smallz), cornerMaterial, (byte)0, Material.AIR, (byte)0));
        newElements.add(new VisualizationElement(new Location(world, bigx, 0, smallz + 1), accentMaterial, (byte)0, Material.AIR, (byte)0));
		for(int z = smallz + STEP; z < bigz - STEP / 2; z += STEP)
		{
			if(z > minz && z < maxz)
			    newElements.add(new VisualizationElement(new Location(world, bigx, 0, z), accentMaterial, (byte)0, Material.AIR, (byte)0));
		}
		newElements.add(new VisualizationElement(new Location(world, bigx, 0, bigz - 1), accentMaterial, (byte)0, Material.AIR, (byte)0));
		newElements.add(new VisualizationElement(new Location(world, bigx, 0, bigz), cornerMaterial, (byte)0, Material.AIR, (byte)0));
        
        //remove any out of range elements
		this.removeElementsOutOfRange(newElements, minx, minz, maxx, maxz);
		
		//remove any elements outside the claim
		for(int i = 0; i < newElements.size(); i++)
		{
		    VisualizationElement element = newElements.get(i);
		    if(!claim.contains(element.location, true))
		    {
		        newElements.remove(i--);
		    }
		}
		
		//set Y values and real block information for any remaining visualization blocks
		for(VisualizationElement element : newElements)
		{
		    Location tempLocation = element.location;
		    element.location = getVisibleLocation(tempLocation.getWorld(), tempLocation.getBlockX(), height, tempLocation.getBlockZ(), waterIsTransparent);
		    height = element.location.getBlockY();
		    element.realMaterial = element.location.getBlock().getType();
		    element.realData = element.location.getBlock().getData();
		}
		
		this.elements.addAll(newElements);
	}
	
	//removes any elements which are out of visualization range
	public void removeElementsOutOfRange(ArrayList<VisualizationElement> elements, int minx, int minz, int maxx, int maxz)
	{
	    for(int i = 0; i < elements.size(); i++)
	    {
	        Location location = elements.get(i).location;
	        if(location.getX() < minx || location.getX() > maxx || location.getZ() < minz || location.getZ() > maxz)
	        {
	            elements.remove(i--);
	        }
	    }
    }

	//finds a block the player can probably see.  this is how visualizations "cling" to the ground or ceiling
    private Location getVisibleLocation(World world, int x, int y, int z, boolean waterIsTransparent)
	{
		Block block = world.getBlockAt(x,  y, z);
		BlockFace direction = (isTransparent(block, waterIsTransparent)) ? BlockFace.DOWN : BlockFace.UP;

		while(	block.getY() >= 1 && 
				block.getY() < world.getMaxHeight() - 1 &&
				(!isTransparent(block.getRelative(BlockFace.UP), waterIsTransparent) || isTransparent(block, waterIsTransparent)))
		{
			block = block.getRelative(direction);
		}
		
		return block.getLocation();
	}
	
	//helper method for above.  allows visualization blocks to sit underneath partly transparent blocks like grass and fence
	private boolean isTransparent(Block block, boolean waterIsTransparent)
	{
		switch (block.getType())
		{
		    //Blacklist
			case SNOW:
            case CARPET:
				return false;
            case STATIONARY_WATER:
                return waterIsTransparent;
		}
        return block.getType().isTransparent();
    }
}
