package me.ryanhamshire.GriefPrevention.command;

import me.ryanhamshire.GriefPrevention.claim.Claim;
import me.ryanhamshire.GriefPrevention.claim.ClaimClerk;
import me.ryanhamshire.GriefPrevention.claim.ClaimRegistrar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
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
    public boolean execute(Player player, String[] args, Claim claim)
    {
        //TODO: ask user to confirm

        if (!claimRegistrar.deleteClaim(claim))
            player.sendMessage("error in deleting your claim");
        else
            player.sendMessage("Claim abandoned");

        return true;
    }
}
