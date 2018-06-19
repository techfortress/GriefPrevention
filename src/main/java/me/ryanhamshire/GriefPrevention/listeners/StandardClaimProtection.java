package me.ryanhamshire.GriefPrevention.listeners;

import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.events.triage.GPBlockMutateTypeEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Created on 2/26/2017.
 *
 * @author RoboMWM
 */
public class StandardClaimProtection implements Listener
{
    GriefPrevention instance;
    public StandardClaimProtection(GriefPrevention griefPrevention)
    {
        instance = griefPrevention;
    }

    @EventHandler(ignoreCancelled = true)
    void onGPPlaceorDestroy(GPBlockMutateTypeEvent event)
    {
        //don't track in worlds where claims are not enabled
        if (!GriefPrevention.instance.claimsEnabledForWorld(event.getLocation().getWorld())) return;

        if (!event.isPlayer()) return;
        Player player = event.getPlayer();


        //if the player doesn't have build permission, don't allow the breakage
        String noBuildReason = GriefPrevention.instance.allowBuild(playerRemover, event.getSourceEntity().getLocation(), Material.AIR);
        if (noBuildReason != null)
        {
            event.setCancelled(true);
            GriefPrevention.sendMessage(playerRemover, TextMode.Err, noBuildReason);
        }
    }
}
