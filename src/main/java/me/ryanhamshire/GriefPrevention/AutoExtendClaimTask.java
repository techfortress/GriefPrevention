package me.ryanhamshire.GriefPrevention;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.block.Biome;

//automatically extends a claim downward based on block types detected
class AutoExtendClaimTask implements Runnable
{
    private Claim claim;
    private ArrayList<ChunkSnapshot> chunks;
    private Environment worldType;

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
        if(newY < this.claim.getLesserBoundaryCorner().getBlockY())
        {
            Bukkit.getScheduler().runTask(GriefPrevention.instance, new ExecuteExtendClaimTask(claim, newY));
        }
    }

    private int getLowestBuiltY()
    {
        int y = this.claim.getLesserBoundaryCorner().getBlockY();
        
        if(this.yTooSmall(y)) return y;

        try
        {
            for(ChunkSnapshot chunk : this.chunks)
            {
                Biome biome = chunk.getBiome(0,  0);
                ArrayList<Material> playerBlockIDs = RestoreNatureProcessingTask.getPlayerBlocks(this.worldType, biome);

                boolean ychanged = true;
                while(!this.yTooSmall(y) && ychanged)
                {
                    ychanged = false;
                    for(int x = 0; x < 16; x++)
                    {
                        for(int z = 0; z < 16; z++)
                        {
                            Material blockType = chunk.getBlockType(x, y, z);
                            while(!this.yTooSmall(y) && playerBlockIDs.contains(blockType))
                            {
                                ychanged = true;
                                blockType = chunk.getBlockType(x, --y, z);
                            }

                            if(this.yTooSmall(y)) return y;
                        }
                    }
                }

                if(this.yTooSmall(y)) return y;
            }
        }
        catch (NoSuchMethodError e)
        {
            GriefPrevention.instance.getLogger().severe("You are running an outdated build of Craftbukkit/Spigot/Paper. Please update.");
            for(ChunkSnapshot chunk : this.chunks)
            {
                Biome biome = chunk.getBiome(0,  0);
                ArrayList<Material> playerBlockIDs = RestoreNatureProcessingTask.getPlayerBlocks(this.worldType, biome);

                boolean ychanged = true;
                while(!this.yTooSmall(y) && ychanged)
                {
                    ychanged = false;
                    for(int x = 0; x < 16; x++)
                    {
                        for(int z = 0; z < 16; z++)
                        {
                            	Material blockType = chunk.getBlockType(x, y, z);
				while(!this.yTooSmall(y) && playerBlockIDs.contains(blockType))
				{
				    ychanged = true;
				    blockType = chunk.getBlockType(x, --y, z);
				}

				if(this.yTooSmall(y)) return y;
                        }
                    }
                }

                if(this.yTooSmall(y)) return y;
            }

        }

        
        return y;
    }

    private boolean yTooSmall(int y)
    {
        return y == 0 || y <= GriefPrevention.instance.config_claims_maxDepth;
    }
    
    //runs in the main execution thread, where it can safely change claims and save those changes
    private class ExecuteExtendClaimTask implements Runnable
    {
        private Claim claim;
        private int newY;

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
