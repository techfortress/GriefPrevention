package me.ryanhamshire.GriefPrevention;

import com.google.common.collect.ImmutableList;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TabCompleterTest
{

    @Mock
    private CommandTabCompleter.Delegate delegate;

    @Mock
    private Player sender;

    @Mock
    private Claim claim;

    @Mock
    private Command command;

    @BeforeEach
    void beforeEach()
    {
        CommandTabCompleter.setDelegate(delegate);
        when(delegate.isPlayer(sender)).thenReturn(true);
    }

    @AfterEach
    void afterEach()
    {
        CommandTabCompleter.setDelegate(null);
    }

    @Test
    void doNotTabCompleteForConsole()
    {
        when(delegate.isPlayer(sender)).thenReturn(false);

        List<String> options = CommandTabCompleter.onTabComplete(sender, command, "anything", noArguments());

        assertThat(options).isNull();
    }

    @Test
    void noResultsForContainertrustInWilderness()
    {
        List<String> options = onTabComplete("containertrust", noArguments());
        assertThat(options).isEmpty();
    }

    @Test
    void noResultsForContainertrustInClaimWithoutManagePermission()
    {
        when(delegate.getCurrentClaim(sender)).thenReturn(claim);
        when(claim.hasPermission(sender, ClaimPermission.Manage)).thenReturn(false);

        List<String> options = onTabComplete("containertrust", noArguments());

        assertThat(options).isEmpty();
        verify(delegate, never()).listOnlineNames();
    }

    @Test
    void verifyResultsForContainertrustInClaimWithEmptyTrustList()
    {
        when(delegate.getCurrentClaim(sender)).thenReturn(claim);
        when(delegate.listOnlineNames()).thenReturn(listOf("Nouish", "RoboMWM"));
        when(claim.getOwnerName()).thenReturn("Nouish");
        when(claim.getContainerTrustList()).thenReturn(emptyList());
        when(claim.hasPermission(sender, ClaimPermission.Manage)).thenReturn(true);

        List<String> options = onTabComplete("containertrust", noArguments());

        assertThat(options).containsExactlyInAnyOrder("public", "RoboMWM");
    }

    @Test
    void verifyResultsForContainertrustInClaimWithTrustList()
    {
        when(delegate.trustListToNameList(anyCollection())).thenAnswer(invocation -> invocation.getArgument(0));
        when(delegate.getCurrentClaim(sender)).thenReturn(claim);
        when(delegate.listOnlineNames()).thenReturn(listOf("Nouish", "RoboMWM", "Jikoo"));
        when(claim.getOwnerName()).thenReturn("RoboMWM");
        when(claim.getContainerTrustList()).thenReturn(listOf("Nouish"));
        when(claim.hasPermission(sender, ClaimPermission.Manage)).thenReturn(true);

        List<String> options = onTabComplete("containertrust", noArguments());

        assertThat(options).containsExactlyInAnyOrder("public", "Jikoo");
    }

    @Test
    void noResultsForUntrustInWilderness()
    {
        List<String> options = onTabComplete("untrust", noArguments());
        assertThat(options).isEmpty();
    }

    @Test
    void noResultsForUntrustInClaimWithoutManagePermission()
    {
        when(delegate.getCurrentClaim(sender)).thenReturn(claim);
        when(claim.hasPermission(sender, ClaimPermission.Manage)).thenReturn(false);

        List<String> options = onTabComplete("untrust", noArguments());

        assertThat(options).isEmpty();
        verify(claim, never()).getAccessTrustList();
        verify(claim, never()).getBuildTrustList();
        verify(claim, never()).getContainerTrustList();
        verify(claim, never()).getManagerTrustList();
    }

    @Test
    void verifyResultsForUntrustInClaimWithEmptyTrustList()
    {
        when(delegate.getCurrentClaim(sender)).thenReturn(claim);
        when(claim.getAccessTrustList()).thenReturn(emptyList());
        when(claim.getBuildTrustList()).thenReturn(emptyList());
        when(claim.getContainerTrustList()).thenReturn(emptyList());
        when(claim.getManagerTrustList()).thenReturn(emptyList());
        when(claim.hasPermission(sender, ClaimPermission.Manage)).thenReturn(true);

        List<String> options = onTabComplete("untrust", noArguments());

        assertThat(options).isEmpty();
    }

    @Test
    void verifyResultsForUntrustInClaimWithTrustList()
    {
        when(delegate.trustListToNameList(anyCollection())).thenAnswer(invocation -> invocation.getArgument(0));
        when(delegate.getCurrentClaim(sender)).thenReturn(claim);
        when(claim.getAccessTrustList()).thenReturn(listOf("Nouish"));
        when(claim.getBuildTrustList()).thenReturn(listOf("Jikoo"));
        when(claim.getContainerTrustList()).thenReturn(listOf("public"));
        when(claim.getManagerTrustList()).thenReturn(listOf("RoboMWM"));
        when(claim.hasPermission(sender, ClaimPermission.Manage)).thenReturn(true);

        List<String> options = onTabComplete("untrust", noArguments());

        assertThat(options).containsExactlyInAnyOrder("public", "Nouish", "Jikoo", "RoboMWM");
    }

    @Test
    void verifyNoDuplicateResultsForUntrustInClaimWithTrustListWhereUserHasDifferentPermissions()
    {
        when(delegate.trustListToNameList(anyCollection())).thenAnswer(invocation -> invocation.getArgument(0));
        when(delegate.getCurrentClaim(sender)).thenReturn(claim);
        when(claim.getAccessTrustList()).thenReturn(listOf("Nouish"));
        when(claim.getBuildTrustList()).thenReturn(listOf("Jikoo"));
        when(claim.getContainerTrustList()).thenReturn(listOf("Nouish"));
        when(claim.getManagerTrustList()).thenReturn(listOf("RoboMWM"));
        when(claim.hasPermission(sender, ClaimPermission.Manage)).thenReturn(true);

        List<String> options = onTabComplete("untrust", noArguments());

        assertThat(options).containsExactlyInAnyOrder("Nouish", "Jikoo", "RoboMWM");
    }

    @Test
    void verifyResultsForAdjustBonusClaimBlocksAll()
    {
        when(sender.hasPermission("griefprevention.adjustclaimblocks")).thenReturn(true);

        List<String> options = onTabComplete("adjustbonusclaimblocksall", noArguments());

        assertThat(options).containsExactlyInAnyOrder("10", "100", "1000", "10000");
    }

    @Test
    void verifyResultsForAdjustBonusClaimBlocksWithNoArgs()
    {
        when(sender.hasPermission("griefprevention.adjustclaimblocks")).thenReturn(true);
        when(delegate.listOnlineNames()).thenReturn(listOf("Jikoo", "BigScary"));

        List<String> options = onTabComplete("adjustbonusclaimblocks", noArguments());

        assertThat(options).containsExactlyInAnyOrder("Jikoo", "BigScary");
    }

    @Test
    void verifyResultsForAdjustBonusClaimBlocksWithTarget()
    {
        when(sender.hasPermission("griefprevention.adjustclaimblocks")).thenReturn(true);

        List<String> options = onTabComplete("adjustbonusclaimblocks", withArguments("Notch"));

        assertThat(options).containsExactlyInAnyOrder("10", "100", "1000", "10000");
    }


    // Verify that commands that don't take arguments don't suggest any options
    @ParameterizedTest(name = "no options: {0}")
    @ValueSource(strings = {
        "abandonclaim", "abandontoplevelclaim", "abandonallclaims", "subdivideclaims", "restrictsubclaim",
        "deleteclaim", "adminclaims", "restorenature", "restorenatureaggressive", "basicclaims", "trapped",
        "trustlist", "ignoreclaims", "deletealladminclaims", "adminclaimslist", "unlockdrops", "claimexplosions",
        "gpreload", "gpblockinfo", "ignoredplayerlist"
    })
    void verifyNoResultsForAbandonClaim(String name)
    {
        List<String> options = onTabComplete(name, noArguments());
        assertThat(options).isEmpty();
    }

    // Following are helper functions for the tests


    private List<String> onTabComplete(String name, String[] args)
    {
        // TabCompleter depends on command.getName() to handle aliases.
        when(command.getName()).thenReturn(name);
        return CommandTabCompleter.onTabComplete(sender, command, name, args);
    }

    // Bukkit never returns a completely empty args-array.
    private static String[] noArguments()
    {
        return new String[] { "" };
    }

    private static String[] withArguments(String... args)
    {
        String[] arguments = Arrays.copyOf(args, args.length + 1);
        arguments[args.length] = "";
        return arguments;
    }

    private static <E> ImmutableList<E> emptyList()
    {
        return ImmutableList.of();
    }

    private static <E> ImmutableList<E> listOf(E e)
    {
        return ImmutableList.of(e);
    }
    
    private static <E> ImmutableList<E> listOf(E e1, E e2)
    {
        return ImmutableList.of(e1, e2);
    }

    private static <E> ImmutableList<E> listOf(E e1, E e2, E e3)
    {
        return ImmutableList.of(e1, e2, e3);
    }
}
