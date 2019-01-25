package me.ryanhamshire.GriefPrevention;

import org.bukkit.block.Block;

public final class LazyColumn {

	private final String world;
	private final int x, z;
	private final LazyChunk chunk;

	public LazyColumn(String world, int x, int z) {
		if (world == null) {
			throw new NullPointerException("world cannot be null");
		}
		this.world = world;
		this.x = x;
		this.z = z;
		this.chunk = new LazyChunk(world, x >> 4, z >> 4);
	}

	public LazyColumn(LazyBlock block) {
		this(block.getWorld(), block.getX(), block.getZ());
	}

	public LazyColumn(Block block) {
		this(block.getWorld().getName(), block.getX(), block.getZ());
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

	public LazyChunk getChunk() {
		return chunk;
	}

	public boolean contains(Block block) {
		return block.getWorld().getName().equals(world) && block.getX() == x && block.getZ() == z;
	}

	public boolean contains(LazyBlock block) {
		return block.getWorld().equals(world) && block.getX() == x && block.getZ() == z;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof LazyColumn) {
			LazyColumn o = (LazyColumn) other;
			return this.world.equals(o.world)
					&& this.x == o.x
					&& this.z == o.z;
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 59 * hash + this.world.hashCode();
		hash = 59 * hash + this.x;
		hash = 59 * hash + this.z;
		return hash;
	}
}
