package me.ryanhamshire.GriefPrevention;

import me.ryanhamshire.GriefPrevention.events.ClaimPermissionCheckEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class SiegeEventHandler implements Listener
{

    @EventHandler(priority = EventPriority.LOWEST)
    public void onClaimPermissionCheck(ClaimPermissionCheckEvent event)
    {
        if (event.getRequiredPermission() == ClaimPermission.Manage) return;

        Claim claim = event.getClaim();

        // Admin claims cannot be sieged.
        if (claim.isAdminClaim()) return;

        // Claim modification during siege is not allowed.
        if (event.getRequiredPermission() == ClaimPermission.Edit)
        {
            if (claim.siegeData != null)
                event.setDenialMessage(GriefPrevention.instance.dataStore.getMessage(Messages.NoModifyDuringSiege));
            return;
        }

        // Following a siege where the defender lost, the claim will allow everyone access for a time.
        if (event.getRequiredPermission() == ClaimPermission.Access)
        {
            if (claim.doorsOpen)
                event.setDenialMessage(null);
            return;
        }

        Player player = event.getPlayer();

        // If under siege, nobody accesses containers.
        if (event.getRequiredPermission() == ClaimPermission.Inventory)
        {
            // Trying to access inventory in a claim may extend an existing siege to include this claim.
            GriefPrevention.instance.dataStore.tryExtendSiege(player, claim);

            if (claim.siegeData != null)
                event.setDenialMessage(GriefPrevention.instance.dataStore.getMessage(Messages.NoContainersSiege, claim.siegeData.attacker.getName()));

            return;
        }

        // When a player tries to build in a claim, if he's under siege, the siege may extend to include the new claim.
        GriefPrevention.instance.dataStore.tryExtendSiege(player, claim);

        // If claim is not under siege and doors are not open, use default behavior.
        if (claim.siegeData == null && !claim.doorsOpen)
            return;

        // If under siege, some blocks will be breakable.
        if (event.getTriggeringEvent() instanceof BlockBreakEvent
                || event.getTriggeringEvent() instanceof Claim.CompatBuildBreakEvent && ((Claim.CompatBuildBreakEvent) event.getTriggeringEvent()).isBreak())
        {
            boolean breakable = GriefPrevention.instance.config_siege_blocks.contains(((BlockBreakEvent) event.getTriggeringEvent()).getBlock().getType());

            // Error messages for siege mode.
            if (!breakable)
                event.setDenialMessage(GriefPrevention.instance.dataStore.getMessage(Messages.NonSiegeMaterial));
            else if (player.getUniqueId().equals(claim.ownerID))
                event.setDenialMessage(GriefPrevention.instance.dataStore.getMessage(Messages.NoOwnerBuildUnderSiege));
            return;
        }

        // No building while under siege.
        if (claim.siegeData != null)
            event.setDenialMessage(GriefPrevention.instance.dataStore.getMessage(Messages.NoBuildUnderSiege, claim.siegeData.attacker.getName()));

    }

}
