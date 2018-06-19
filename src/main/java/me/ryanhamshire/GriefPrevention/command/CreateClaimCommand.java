package me.ryanhamshire.GriefPrevention.command;

import me.ryanhamshire.GriefPrevention.claim.Claim;
import me.ryanhamshire.GriefPrevention.claim.ClaimClerk;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created on 6/6/2018.
 *
 * @author RoboMWM
 */
public class CreateClaimCommand extends ClaimManagementCommands
{
    public CreateClaimCommand(ClaimClerk claimClerk)
    {
        super(claimClerk);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (!(sender instanceof Player))
            return false;

        Player player = (Player)sender;

        execute(player, args, null);

        return true;
    }

    @Override
    public boolean execute(Player player, String[] args, Claim claim)
    {
        //TODO: permission checks

        int radius = 10; //TODO: replace with config

        try
        {
            if (args.length > 0)
                radius = Integer.parseInt(args[0]);
        }
        catch (NumberFormatException ignored){}

        Location firstCorner = player.getLocation().add(radius, 0, radius);
        Location secondCorner = player.getLocation().subtract(radius, 0, radius);

        claimClerk.registerNewClaim(player, firstCorner, secondCorner);

        return true;
    }
}
