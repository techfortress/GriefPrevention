package me.ryanhamshire.GriefPrevention.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when GP is about to kick or ban a player
 *
 * @author BillyGalbreath
 * 03/10/2017.
 */
public class PlayerKickBanEvent extends Event
{
    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList()
    {
        return handlers;
    }

    @Override
    public HandlerList getHandlers()
    {
        return handlers;
    }

    private final Player player;
    private final String reason;
    private final String source;
    private final boolean ban;
    private boolean cancelled = false;

    /**
     * @param player Player getting kicked and/or banned
     * @param reason Reason message for kick/ban
     * @param source What caused the kick/ban
     * @param ban True if player is getting banned
     */
    public PlayerKickBanEvent(Player player, String reason, String source, boolean ban)
    {
        this.player = player;
        this.reason = reason;
        this.source = source;
        this.ban = ban;
    }

    /**
     * @return player getting kicked/banned
     */
    public Player getPlayer()
    {
        return this.player;
    }

    /**
     * @return reason player is getting kicked/banned
     */
    public String getReason()
    {
        return this.reason;
    }

    /**
     * @return source that is kicking/banning the player
     */
    public String getSource()
    {
        return this.source;
    }

    /**
     * @return is player getting banned
     */
    public boolean getBan()
    {
        return this.ban;
    }

    public boolean isCancelled()
    {
        return this.cancelled;
    }

    public void setCancelled(boolean cancel)
    {
        this.cancelled = cancel;
    }
}
