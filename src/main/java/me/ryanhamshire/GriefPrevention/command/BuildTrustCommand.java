package me.ryanhamshire.GriefPrevention.command;

import me.ryanhamshire.GriefPrevention.claim.Claim;
import me.ryanhamshire.GriefPrevention.claim.ClaimClerk;
import org.bukkit.entity.Player;

/**
 * Created on 6/16/2018.
 *
 * @author RoboMWM
 */
public class BuildTrustCommand extends ClaimManagementCommands
{
    public BuildTrustCommand(ClaimClerk claimClerk)
    {
        super(claimClerk);
    }

    @Override
    public boolean execute(Player player, String[] args, Claim claim)
    {
        return false;
    }
}
