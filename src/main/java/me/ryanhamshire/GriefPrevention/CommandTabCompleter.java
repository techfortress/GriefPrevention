package me.ryanhamshire.GriefPrevention;

import com.google.common.annotations.VisibleForTesting;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

// See notes in GriefPrevention.onTabComplete() regarding why this is static, and not an actual implementation
// of org.bukkit.command.TabCompleter.
class CommandTabCompleter
{
    private static final String THE_PUBLIC = "public";

    private CommandTabCompleter()
    {
        throw new AssertionError("CommandTabCompleter should not be instantiated");
    }

    static List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
    {
        // Assume all completions will be for players, and not console.
        if (!delegate.isPlayer(sender))
        {
            return null;
        }

        final String label = command.getName().toLowerCase(Locale.ROOT);
        final Player player = (Player) sender;
        List<String> options = new ArrayList<>();

        if (args.length == 1 && label.equals("containertrust"))
        {
            Claim claim = delegate.getCurrentClaim(player);
            if (claim != null && claim.hasPermission(player, ClaimPermission.Manage))
            {
                options.add(THE_PUBLIC);
                options.addAll(delegate.listOnlineNames());
                options.removeAll(delegate.trustListToNameList(claim.getContainerTrustList()));
                removeIfNotNull(options, claim.getOwnerName());
            }
        }

        if (args.length == 1 && label.equals("untrust"))
        {
            Claim claim = delegate.getCurrentClaim(player);
            if (claim != null && claim.hasPermission(player, ClaimPermission.Manage))
            {
                // One person may have multiple trust types, so this avoids duplicate options
                Set<String> trustees = new HashSet<>();
                trustees.addAll(claim.getAccessTrustList());
                trustees.addAll(claim.getBuildTrustList());
                trustees.addAll(claim.getContainerTrustList());
                trustees.addAll(claim.getManagerTrustList());
                options.addAll(delegate.trustListToNameList(trustees));
            }
        }

        return options;
    }

    private static void removeIfNotNull(List<String> list, String str)
    {
        if (str != null)
        {
            list.remove(str);
        }
    }

    // This exists purely to ease injecting mocks
    interface Delegate
    {
        boolean isPlayer(CommandSender sender);
        Claim getCurrentClaim(Player player);
        Collection<String> trustListToNameList(Collection<String> trustList);
        Collection<String> listOnlineNames();
    }

    private static final Delegate SYSTEM_DELEGATE =
        new Delegate()
        {
            @Override
            public boolean isPlayer(CommandSender sender)
            {
                return sender instanceof Player;
            }

            @Override
            public Claim getCurrentClaim(Player player)
            {
                PlayerData playerData =  GriefPrevention.instance.dataStore.getPlayerData(player.getUniqueId());
                return GriefPrevention.instance.dataStore.getClaimAt(player.getLocation(), false, playerData.lastClaim);
            }

            @Override
            public Collection<String> trustListToNameList(Collection<String> trustList)
            {
                return trustList.stream()
                        .map(entry -> GriefPrevention.instance.trustEntryToPlayerName(entry))
                        .collect(Collectors.toList());
            }

            @Override
            public Collection<String> listOnlineNames()
            {
                return Bukkit.getOnlinePlayers().stream()
                        .map(HumanEntity::getName)
                        .collect(Collectors.toList());
            }
        };

    private static Delegate delegate = SYSTEM_DELEGATE;

    @VisibleForTesting
    static void setDelegate(Delegate delegate)
    {
        if (delegate != null)
        {
            CommandTabCompleter.delegate = delegate;
        }
        else
        {
            CommandTabCompleter.delegate = SYSTEM_DELEGATE;
        }
    }

}
