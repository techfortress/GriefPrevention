package me.ryanhamshire.GriefPrevention.metrics;

import me.ryanhamshire.GriefPrevention.ClaimsMode;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.World;

import java.util.concurrent.Callable;

/**
 * Created on 9/22/2018.
 *
 * @author RoboMWM
 */
public class MetricsHandler
{
    private final Metrics metrics;

    public MetricsHandler(GriefPrevention plugin, String dataMode)
    {
        metrics = new Metrics(plugin);

        try
        {
            addSimplePie("custom_build", plugin.getDescription().getVersion().equals("15.2.2"));
            addSimplePie("bukkit_impl", plugin.getServer().getVersion().split("-")[1]);
        }
        catch (Throwable ignored) {}

        //enums and etc. would be amazing.

        addSimplePie("lock_death_drops_pvp", plugin.config_lockDeathDropsInPvpWorlds);
        addSimplePie("lock_death_drops_nonpvp", plugin.config_lockDeathDropsInNonPvpWorlds);

        //PvP - only send PvP configs for those who use them
        boolean pvpApplies = false;
        for (World world : plugin.getServer().getWorlds())
        {
            if (plugin.pvpRulesApply(world))
            {
                addSimplePie("no_pvp_in_player_claims", plugin.config_pvp_noCombatInPlayerLandClaims);
                addSimplePie("protect_pets_pvp", plugin.config_pvp_protectPets);
                addSimplePie("protect_fresh_spawns_pvp", plugin.config_pvp_protectFreshSpawns);
                pvpApplies = true;
                break;
            }
        }

        addSimplePie("uses_pvp", pvpApplies);

        //spam
        addSimplePie("uses_spam", plugin.config_spam_enabled);
        if (plugin.config_spam_enabled)
        {
            addSimplePie("ban_spam_offenders", plugin.config_spam_banOffenders);
            addSimplePie("use_ban_command", plugin.config_ban_useCommand);
        }

        //Used for claims?
        boolean claimsEnabled = false;
        for (ClaimsMode mode : plugin.config_claims_worldModes.values())
        {
            if (mode != ClaimsMode.Disabled)
            {
                claimsEnabled = true;
                break;
            }
        }

        addSimplePie("uses_claims", claimsEnabled);

        //Don't send any claim/nature-related configs if claim protections aren't used at all
        if (!claimsEnabled)
            return;

        //How many people want vanilla fire behavior?
        addSimplePie("fire_spreads", plugin.config_fireSpreads);
        addSimplePie("fire_destroys", plugin.config_fireDestroys);

        //Everything that is wooden should be accessible by default?
        addSimplePie("lock_wooden_doors", plugin.config_claims_lockWoodenDoors);
        addSimplePie("lock_fence_gates", plugin.config_claims_lockFenceGates);
        addSimplePie("lock_trapdoors", plugin.config_claims_lockTrapDoors);

        addSimplePie("protect_horses", plugin.config_claims_protectHorses);
        addSimplePie("protect_donkeys", plugin.config_claims_protectDonkeys);
        addSimplePie("protect_llamas", plugin.config_claims_protectLlamas);

        addSimplePie("prevent_buttons_switches", plugin.config_claims_preventButtonsSwitches);
        addSimplePie("villager_trading_requires_trust", plugin.config_claims_villagerTradingRequiresTrust);

        //CPU-intensive options
        addSimplePie("survival_nature_restoration", plugin.config_claims_survivalAutoNatureRestoration);
        addSimplePie("block_sky_trees", plugin.config_blockSkyTrees);
        addSimplePie("limit_tree_growth", plugin.config_limitTreeGrowth);

        addSimplePie("pistons_only_work_in_claims", plugin.config_pistonMovement.name().toLowerCase().replace('_', ' '));
        addSimplePie("creatures_trample_crops", plugin.config_creaturesTrampleCrops);

        addSimplePie("claim_tool", plugin.config_claims_modificationTool.name());
        addSimplePie("claim_inspect_tool", plugin.config_claims_investigationTool.name());

        addSimplePie("block_surface_creeper_explosions", plugin.config_blockSurfaceCreeperExplosions);
        addSimplePie("block_surface_other_explosions", plugin.config_blockSurfaceOtherExplosions);
        addSimplePie("endermen_move_blocks", plugin.config_endermenMoveBlocks);

        addSimplePie("storage_mode", dataMode);

        //siege
        addSimplePie("uses_siege", !plugin.config_siege_enabledWorlds.isEmpty());
    }

    private void addSimplePie(String id, boolean value)
    {
        addSimplePie(id, Boolean.toString(value));
    }

    private void addSimplePie(String id, String value)
    {
        metrics.addCustomChart(new Metrics.SimplePie(id, new Callable<String>()
        {
            @Override
            public String call() throws Exception
            {
                return value;
            }
        }));
    }
}
