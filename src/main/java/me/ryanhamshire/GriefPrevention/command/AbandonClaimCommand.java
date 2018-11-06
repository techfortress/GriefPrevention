package me.ryanhamshire.GriefPrevention.command;

import me.ryanhamshire.GriefPrevention.claim.Claim;
import me.ryanhamshire.GriefPrevention.claim.ClaimClerk;
import me.ryanhamshire.GriefPrevention.claim.ClaimRegistrar;
import me.ryanhamshire.GriefPrevention.message.Message;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;

/**
 * Created on 6/13/2018.
 *
 * @author RoboMWM
 */
public class AbandonClaimCommand extends ClaimManagementCommands
{
    private ClaimRegistrar claimRegistrar;

    public AbandonClaimCommand(ClaimClerk claimClerk, ClaimRegistrar claimRegistrar)
    {
        super(claimClerk);
        this.claimRegistrar = claimRegistrar;
    }

    @Override
    public boolean execute(Player player, Command command, String[] args, Claim claim)
    {
        //TODO: ask user to confirm

        if (!claimRegistrar.deleteClaim(claim))
            Message.ErrorInDeletingClaim.send(player);
        else
            Message.ClaimAbandoned.send(player);
        return true;
    }
}
