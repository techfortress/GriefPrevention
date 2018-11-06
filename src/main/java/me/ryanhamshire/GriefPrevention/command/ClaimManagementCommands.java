package me.ryanhamshire.GriefPrevention.command;

import me.ryanhamshire.GriefPrevention.claim.Claim;
import me.ryanhamshire.GriefPrevention.claim.ClaimClerk;
import me.ryanhamshire.GriefPrevention.message.Message;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created on 6/16/2018.
 *
 * @author RoboMWM
 */
public abstract class ClaimManagementCommands implements CommandExecutor
{
    protected ClaimClerk claimClerk;

    public ClaimManagementCommands(ClaimClerk claimClerk)
    {
        this.claimClerk = claimClerk;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (!(sender instanceof Player))
            return false;

        Player player = (Player)sender;

        Claim claim = claimClerk.getClaim(player, player.getLocation(), true);

        if (claim == null)
        {
            Message.NoClaimHere.send(player);
            return true;
        }

        if (claim.getOwnerUUID() != player.getUniqueId()) //TODO: include /permissiontrust
        {
            Message.NotYourClaim.send(player);
            return true;
        }

        if (execute(player, cmd, args, claim))
        {
            //print remaining claim blocks, etc.
            return true;
        } else
        {
            //print help
            return false;
        }
    }

    public abstract boolean execute(Player player, Command cmd, String[] args, Claim claim);
}