package me.ryanhamshire.GriefPrevention;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

public final class LazyBlock {

	private final LazyChunk chunk;
	private final LazyColumn column;
	private final String world;
	private final int x, y, z;

	public LazyBlock(String world, int x, int y, int z) {
		if (world == null) {
			throw new NullPointerException("world cannot be null");
		}
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
		this.chunk = new LazyChunk(world, x >> 4, z >> 4);
		this.column = new LazyColumn(world, x, z);
	}

	public LazyBlock(Block block) {
		this(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
	}
	
	public LazyBlock(LazyColumn column, int y) {
		this(column.getWorld(), column.getX(), y, column.getZ());
	}

	public LazyBlock(Location location) {
		this(location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
	}

	public String getWorld() {
		return world;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getZ() {
		return z;
	}

	public boolean isInLoadedChunk() {
		return chunk.isLoaded();
	}

	public LazyChunk getChunk() {
		return chunk;
	}

	public LazyColumn getColumn() {
		return column;
	}

	public Block getBukkitBlock() {
		World w = Bukkit.getWorld(world);
		if (w == null) {
			return null;
		}
		return w.getBlockAt(x, y, z);
	}

	public Location getLocation() {
		World w = Bukkit.getWorld(world);
		if (w == null) {
			return null;
		}
		return new Location(w, (double) x, (double) y, (double) z);
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof LazyBlock) {
			LazyBlock o = (LazyBlock) other;
			return this.world.equals(o.world)
					&& this.x == o.x
					&& this.y == o.y
					&& this.z == o.z;
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 67 * hash + this.world.hashCode();
		hash = 67 * hash + this.x;
		hash = 67 * hash + this.y;
		hash = 67 * hash + this.z;
		return hash;
	}
}
