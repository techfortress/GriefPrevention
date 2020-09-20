package me.ryanhamshire.GriefPrevention.events;


import me.ryanhamshire.GriefPrevention.Claim;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This Event is thrown when a claim is changed....it is not modifiable or cancellable and only serves as a notification
 * a claim has changed. The CommandSender can be null in the event that the modification is called by the plugin itself.
 * Created by Narimm on 5/08/2018.
 */
public class ClaimModifiedEvent extends Event implements Cancellable
{

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList()
    {
        return handlers;
    }

    private final Claim from;
    private final Claim to;
    private final CommandSender modifier;
    private boolean cancelled;

    public ClaimModifiedEvent(Claim from, Claim to, CommandSender modifier)
    {
        this.from = from;
        this.to = to;
        this.modifier = modifier;
    }

    @Override
    public HandlerList getHandlers()
    {
        return handlers;
    }

    /**
     * The claim
     *
     * @return the claim
     * @deprecated Use the {@link #getTo() getTo} method.
     */
    @Deprecated
    public Claim getClaim()
    {
        return to;
    }

    public Claim getFrom()
    {
        return from;
    }

    public Claim getTo()
    {
        return to;
    }

    /**
     * The actor making the change...can be null
     *
     * @return the CommandSender or null
     */
    public CommandSender getModifier()
    {
        return modifier;
    }

    @Override
    public boolean isCancelled()
    {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled)
    {
        this.cancelled = cancelled;
    }
}
