package me.ryanhamshire.GriefPrevention;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.BukkitPlayer;
import com.sk89q.worldguard.internal.permission.RegionPermissionModel;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

class WorldGuardWrapper
{
    private WorldGuardPlugin worldGuard = null;
    
    public WorldGuardWrapper() throws ClassNotFoundException
    {
        this.worldGuard = (WorldGuardPlugin)GriefPrevention.instance.getServer().getPluginManager().getPlugin("WorldGuard");
    }
    
    public boolean canBuild(Location lesserCorner, Location greaterCorner, Player creatingPlayer)
    {
        if (worldGuard == null)
        {
            GriefPrevention.AddLogEntry("WorldGuard is out of date and not enabled. Please update or remove WorldGuard.", CustomLogEntryTypes.Debug, false);
            return true;
        }

        BukkitPlayer localPlayer = new BukkitPlayer(this.worldGuard, creatingPlayer);
        World world = WorldGuard.getInstance().getPlatform().getWorldByName(lesserCorner.getWorld().getName());

        RegionManager manager =  WorldGuard.getInstance().getPlatform().getRegionContainer().get(world);

        if(new RegionPermissionModel(localPlayer).mayIgnoreRegionProtection(world)) return true;

        if(manager != null)
        {
            ProtectedCuboidRegion tempRegion = new ProtectedCuboidRegion(
                "GP_TEMP",
                new BlockVector(lesserCorner.getX(), 0, lesserCorner.getZ()),
                new BlockVector(greaterCorner.getX(), world.getMaxY(), greaterCorner.getZ()));

            ApplicableRegionSet overlaps = manager.getApplicableRegions(tempRegion);
            for (ProtectedRegion r : overlaps.getRegions()) {
                if (!manager.getApplicableRegions(r).testState(localPlayer, Flags.BUILD)) {
                    return false;
                }
            }
            return true;
        }
        
        return true;
    }
}
