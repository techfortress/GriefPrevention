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

import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

//applies a visualization for a player by sending him block change packets
class VisualizationApplicationTask extends BukkitRunnable
{
	private Visualization visualization;
	private Player player;
	private JavaPlugin instance;

	public VisualizationApplicationTask(Player player, Visualization visualization, JavaPlugin plugin)
	{
		this.visualization = visualization;
		this.player = player;
		this.instance = plugin;
	}

    @Override
	public void run()
	{
		if (!player.isOnline())
			return;
		//for each element (=block) of the visualization
		for(VisualizationElement element : visualization.getElements())
		{
			//send the player a fake block change event
			if(!element.location.getChunk().isLoaded()) continue;  //cheap distance check
			player.sendBlockChange(element.location, element.visualizedMaterial, element.visualizedData);
		}
		
		//remember the visualization applied to this player for later (so it can be inexpensively reverted)
		//TODO: use metadata
        player.setMetadata(VisualizationManager.METADATA_KEY, new FixedMetadataValue(instance, visualization));
		//playerData.currentVisualization = visualization;
	}
}
