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

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

//basically, just a few data points from a block conveniently encapsulated in a class
//this is used only by the RestoreNature code
public class BlockSnapshot
{
    public Location location;
    public Material typeId;
    public BlockData data;

    public BlockSnapshot(Location location, Material typeId, BlockData data)
    {
        this.location = location;
        this.typeId = typeId;
        this.data = data;
    }
}
