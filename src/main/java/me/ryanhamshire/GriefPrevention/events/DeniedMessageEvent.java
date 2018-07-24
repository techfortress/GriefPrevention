package me.ryanhamshire.GriefPrevention.events;

import me.ryanhamshire.GriefPrevention.Messages;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import javax.annotation.Nonnull;

/**
 * Called when GP is retrieving the denial message to send to the player when canceling an action
 *
 * @author RoboMWM
 * Created 1/4/2017.
 */
public class DeniedMessageEvent extends Event
{
    // Custom Event Requirements
    private static final HandlerList handlers = new HandlerList();
    public static HandlerList getHandlerList() {
        return handlers;
    }
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    private String message;
    private Messages messageID;

    public DeniedMessageEvent(Messages messageID, String message)
    {
        this.message = message;
        this.messageID = messageID;
    }

    public Messages getMessageID()
    {
        return this.messageID;
    }

    public String getMessage()
    {
        return this.message;
    }

    /**
     * Sets the message to print to the player.
     * @param message Cannot be null. Set to an empty string if you wish for no message to be printed.
     */
    public void setMessage(@Nonnull String message)
    {
        this.message = message;
    }

}
