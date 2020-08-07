package me.ryanhamshire.GriefPrevention;

import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ClaimTest {

    @Test
    public void testClaimOverlap()
    {
        World world = Mockito.mock(World.class);

        // One corner inside
        Claim claimA = new Claim(new Location(world, 0, 0, 0), new Location(world, 10, 0, 10), null,
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), false, 0L);
        Claim claimB = new Claim(new Location(world, 5, 0, 5), new Location(world, 15, 0, 15), null,
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), false, 0L);
        Claim claimC = new Claim(new Location(world, -5, 0, -5), new Location(world, 4, 0, 4), null,
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), false, 0L);

        assertTrue(claimA.overlaps(claimB));
        assertTrue(claimB.overlaps(claimA));
        assertTrue(claimA.overlaps(claimC));
        assertTrue(claimC.overlaps(claimA));
        assertFalse(claimB.overlaps(claimC));
        assertFalse(claimC.overlaps(claimB));

        // Complete containment
        claimA = new Claim(new Location(world, 0, 0, 0), new Location(world, 20, 0, 20), null,
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), false, 0L);
        claimB = new Claim(new Location(world, 5, 0, 5), new Location(world, 15, 0, 15), null,
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), false, 0L);

        assertTrue(claimA.overlaps(claimB));
        assertTrue(claimB.overlaps(claimA));

        // Central intersection
        claimA = new Claim(new Location(world, 0, 0, 5), new Location(world, 10, 0, 15), null,
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), false, 0L);
        claimB = new Claim(new Location(world, 5, 0, 0), new Location(world, 15, 0, 10), null,
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), false, 0L);

        assertTrue(claimA.overlaps(claimB));
        assertTrue(claimB.overlaps(claimA));
    }
}
