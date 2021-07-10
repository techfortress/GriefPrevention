package me.ryanhamshire.GriefPrevention;

import com.google.common.collect.ImmutableList;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static me.ryanhamshire.GriefPrevention.CommandTabCompleter.onTabComplete;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TabCompleterTest
{

    private static CommandTabCompleter.Delegate delegate;

    @Mock
    private Player sender;

    @Mock
    private Claim claim;

    @Mock
    private Command command;

    @BeforeAll
    static void beforeAll()
    {
        delegate = mock(CommandTabCompleter.Delegate.class);
        CommandTabCompleter.setDelegate(delegate);
    }

    @AfterAll
    static void afterAll()
    {
        delegate = null;
        CommandTabCompleter.setDelegate(null);
    }

    @BeforeEach
    void beforeEach()
    {
        when(delegate.isPlayer(sender)).thenReturn(true);
    }

    @Test
    void doNotTabCompleteForConsole()
    {
        when(delegate.isPlayer(sender)).thenReturn(false);

        List<String> options = onTabComplete(sender, command, "anything", noArguments());

        assertThat(options).isNull();
    }

    @Test
    void noResultsForContainertrustInWilderness()
    {
        List<String> options = onTabComplete(sender, command, "containertrust", noArguments());
        assertThat(options).isEmpty();
    }

    @Test
    void verifyResultsForContainertrustInClaimWithEmptyTrustList()
    {
        when(delegate.getCurrentClaim(sender)).thenReturn(claim);
        when(delegate.listOnlineNames()).thenReturn(ImmutableList.of("Nouish", "RoboMWM"));
        when(claim.getOwnerName()).thenReturn("Nouish");
        when(claim.getContainerTrustList()).thenReturn(ImmutableList.of());

        List<String> options = onTabComplete(sender, command, "containertrust", noArguments());

        assertThat(options).containsExactlyInAnyOrder("public", "RoboMWM");
    }

    @Test
    void verifyResultsForContainertrustInClaimWithTrustList()
    {
        when(delegate.trustListToNameList(anyCollection())).thenAnswer(invocation -> invocation.getArgument(0));
        when(delegate.getCurrentClaim(sender)).thenReturn(claim);
        when(delegate.listOnlineNames()).thenReturn(ImmutableList.of("Nouish", "RoboMWM", "Jikoo"));
        when(claim.getOwnerName()).thenReturn("RoboMWM");
        when(claim.getContainerTrustList()).thenReturn(ImmutableList.of("Nouish"));

        List<String> options = onTabComplete(sender, command, "containertrust", noArguments());

        assertThat(options).containsExactlyInAnyOrder("public", "Jikoo");
    }

    @Test
    void noResultsForUntrustInWilderness()
    {
        List<String> options = onTabComplete(sender, command, "untrust", noArguments());
        assertThat(options).isEmpty();
    }

    @Test
    void verifyResultsForUntrustInClaimWithEmptyTrustList()
    {
        when(delegate.getCurrentClaim(sender)).thenReturn(claim);
        when(claim.getAccessTrustList()).thenReturn(ImmutableList.of());
        when(claim.getBuildTrustList()).thenReturn(ImmutableList.of());
        when(claim.getContainerTrustList()).thenReturn(ImmutableList.of());
        when(claim.getManagerTrustList()).thenReturn(ImmutableList.of());

        List<String> options = onTabComplete(sender, command, "untrust", noArguments());

        assertThat(options).isEmpty();
    }

    @Test
    void verifyResultsForUntrustInClaimWithTrustList()
    {
        when(delegate.getCurrentClaim(sender)).thenReturn(claim);
        when(claim.getAccessTrustList()).thenReturn(ImmutableList.of("Nouish"));
        when(claim.getBuildTrustList()).thenReturn(ImmutableList.of("Jikoo"));
        when(claim.getContainerTrustList()).thenReturn(ImmutableList.of("public"));
        when(claim.getManagerTrustList()).thenReturn(ImmutableList.of("RoboMWM"));

        List<String> options = onTabComplete(sender, command, "untrust", noArguments());

        assertThat(options).containsExactlyInAnyOrder("public", "Nouish", "Jikoo", "RoboMWM");
    }

    @Test
    void verifyNoDuplicateResultsForUntrustInClaimWithTrustListWhereUserHasDifferentPermissions()
    {
        when(delegate.trustListToNameList(anyCollection())).thenAnswer(invocation -> invocation.getArgument(0));
        when(delegate.getCurrentClaim(sender)).thenReturn(claim);
        when(claim.getAccessTrustList()).thenReturn(ImmutableList.of("Nouish"));
        when(claim.getBuildTrustList()).thenReturn(ImmutableList.of("Jikoo"));
        when(claim.getContainerTrustList()).thenReturn(ImmutableList.of("Nouish"));
        when(claim.getManagerTrustList()).thenReturn(ImmutableList.of("RoboMWM"));

        List<String> options = onTabComplete(sender, command, "untrust", noArguments());

        assertThat(options).containsExactlyInAnyOrder("Nouish", "Jikoo", "RoboMWM");
    }

    // Bukkit never returns a completely empty args-array.
    private static String[] noArguments()
    {
        return new String[] { "" };
    }

}
