package me.ryanhamshire.GriefPrevention;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

public final class LazyChunk {

	private final String world;
	private final int x, z;

	public LazyChunk(String world, int x, int z) {
		if (world == null) {
			throw new NullPointerException("world cannot be null");
		}
		this.world = world;
		this.x = x;
		this.z = z;
	}

	public LazyChunk(Chunk chunk) {
		this(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
	}

	public LazyChunk(Block block) {
		this(block.getWorld().getName(), block.getX() >> 4, block.getZ() >> 4);
	}

	public LazyChunk(LazyBlock block) {
		this(block.getWorld(), block.getX() >> 4, block.getZ() >> 4);
	}

	public LazyChunk(Location location) {
		this(location.getWorld().getName(), location.getBlockX() >> 4, location.getBlockZ() >> 4);
	}

	public String getWorld() {
		return world;
	}

	public int getX() {
		return x;
	}

	public int getZ() {
		return z;
	}

	public boolean isLoaded() {
		World w = Bukkit.getWorld(world);
		if (w == null) {
			return false;
		}
		return w.isChunkLoaded(x, z);
	}

	public Chunk getBukkitChunk() {
		World w = Bukkit.getWorld(world);
		if (w == null) {
			return null;
		}
		return w.getChunkAt(x, z);
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof LazyChunk) {
			LazyChunk o = (LazyChunk) other;
			return this.world.equals(o.world)
					&& this.x == o.x
					&& this.z == o.z;
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 67 * hash + this.world.hashCode();
		hash = 67 * hash + this.x;
		hash = 67 * hash + this.z;
		return hash;
	}
}
