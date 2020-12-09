package me.ryanhamshire.GriefPrevention;

import org.bukkit.Bukkit;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.block.Biome;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

//automatically extends a claim downward based on block types detected
class AutoExtendClaimTask implements Runnable
{
    private final Claim claim;
    private final ArrayList<ChunkSnapshot> chunks;
    private final Environment worldType;
    private final Map<Biome, Set<Material>> biomeMaterials = new HashMap<>();

    public AutoExtendClaimTask(Claim claim, ArrayList<ChunkSnapshot> chunks, Environment worldType)
    {
        this.claim = claim;
        this.chunks = chunks;
        this.worldType = worldType;
    }

    @Override
    public void run()
    {
        int newY = this.getLowestBuiltY();
        if (newY < this.claim.getLesserBoundaryCorner().getBlockY())
        {
            Bukkit.getScheduler().runTask(GriefPrevention.instance, new ExecuteExtendClaimTask(claim, newY));
        }
    }

    private int getLowestBuiltY()
    {
        int y = this.claim.getLesserBoundaryCorner().getBlockY();

        if (this.yTooSmall(y)) return y;

        for (ChunkSnapshot chunk : this.chunks)
        {
            boolean ychanged = true;
            while (!this.yTooSmall(y) && ychanged)
            {
                ychanged = false;
                for (int x = 0; x < 16; x++)
                {
                    for (int z = 0; z < 16; z++)
                    {
                        Material blockType = chunk.getBlockType(x, y, z);
                        Biome biome = chunk.getBiome(x, y, z);
                        while (!this.yTooSmall(y) && this.getBiomeBlocks(biome).contains(blockType))
                        {
                            ychanged = true;
                            blockType = chunk.getBlockType(x, --y, z);
                            biome = chunk.getBiome(x, y, z);
                        }

                        if (this.yTooSmall(y)) return y;
                    }
                }
            }

            if (this.yTooSmall(y)) return y;
        }


        return y;
    }

    private Set<Material> getBiomeBlocks(Biome biome)
    {
        return biomeMaterials.computeIfAbsent(biome, newBiome -> RestoreNatureProcessingTask.getPlayerBlocks(this.worldType, newBiome));
    }

    private boolean yTooSmall(int y)
    {
        return y == 0 || y <= GriefPrevention.instance.config_claims_maxDepth;
    }

    //runs in the main execution thread, where it can safely change claims and save those changes
    private class ExecuteExtendClaimTask implements Runnable
    {
        private final Claim claim;
        private final int newY;

        public ExecuteExtendClaimTask(Claim claim, int newY)
        {
            this.claim = claim;
            this.newY = newY;
        }

        @Override
        public void run()
        {
            GriefPrevention.instance.dataStore.extendClaim(claim, newY);
        }
    }

}
