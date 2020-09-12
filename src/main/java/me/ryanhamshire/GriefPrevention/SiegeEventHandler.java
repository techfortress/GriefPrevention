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
        Player player = event.getPlayer();

        if (event.getRequiredPermission() == ClaimPermission.Edit)
        {
            // Admin claims are always editable.
            if (!claim.isAdminClaim())
                event.setDenialMessage(GriefPrevention.instance.dataStore.getMessage(Messages.NoModifyDuringSiege));
            return;
        }

        if (event.getRequiredPermission() == ClaimPermission.Access)
        {
            // Following a siege where the defender lost, the claim will allow everyone access for a time.
            if (claim.doorsOpen)
                event.setDenialMessage(null);
            return;
        }

        if (event.getRequiredPermission() == ClaimPermission.Inventory)
        {
            // Trying to access inventory in a claim may extend an existing siege to include this claim.
            GriefPrevention.instance.dataStore.tryExtendSiege(player, claim);

            // If under siege, nobody accesses containers.
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
