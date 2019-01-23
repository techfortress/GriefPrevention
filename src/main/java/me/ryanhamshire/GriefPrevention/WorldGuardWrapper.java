package me.ryanhamshire.GriefPrevention;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.math.BlockVector3;
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
        try
        {
            BukkitPlayer localPlayer = new BukkitPlayer(this.worldGuard, creatingPlayer);
            World world = WorldGuard.getInstance().getPlatform().getMatcher().getWorldByName(lesserCorner.getWorld().getName());

            if(new RegionPermissionModel(localPlayer).mayIgnoreRegionProtection(world)) return true;

            RegionManager manager =  WorldGuard.getInstance().getPlatform().getRegionContainer().get(world);

            if(manager != null)
            {
                ProtectedCuboidRegion tempRegion = new ProtectedCuboidRegion(
                        "GP_TEMP",
                        BlockVector3.at(lesserCorner.getX(), 0, lesserCorner.getZ()),
                        BlockVector3.at(greaterCorner.getX(), world.getMaxY(), greaterCorner.getZ()));

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
        catch (Throwable rock)
        {
            GriefPrevention.AddLogEntry("WorldGuard was found but unable to hook into. It could be that you're " +
                    "using an outdated version or WorldEdit broke their API... again." +
                    "Consider updating/downgrading/removing WorldGuard or disable WorldGuard integration in GP's config " +
                    "(CreationRequiresWorldGuardBuildPermission). If you're going to report this please be kind because " +
                    "WorldEdit's API hasn't been :c", CustomLogEntryTypes.Debug, false);
            return true;
        }

    }
}