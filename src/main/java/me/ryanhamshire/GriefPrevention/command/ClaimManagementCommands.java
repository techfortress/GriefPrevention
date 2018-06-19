package me.ryanhamshire.GriefPrevention.command;

import me.ryanhamshire.GriefPrevention.claim.Claim;
import me.ryanhamshire.GriefPrevention.claim.ClaimClerk;
import me.ryanhamshire.GriefPrevention.claim.ClaimRegistrar;
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
            player.sendMessage("no claim here");
            return true;
        }

        if (claim.getOwnerUUID() != player.getUniqueId()) //TODO: include /permissiontrust
        {
            player.sendMessage("not your claim");
            return true;
        }

        if (execute(player, args, claim))
        {
            //print remaining claim blocks, etc.
            return true;
        }
        else
        {
            //print help
            return false;
        }

    }

    public abstract boolean execute(Player player, String[] args, Claim claim);
}
