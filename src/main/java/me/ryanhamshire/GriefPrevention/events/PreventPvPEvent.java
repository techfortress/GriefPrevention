package me.ryanhamshire.GriefPrevention.events;

import me.ryanhamshire.GriefPrevention.Claim;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

//if cancelled, GriefPrevention will not cancel the PvP event it's processing.
public class PreventPvPEvent extends Event implements Cancellable
{
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;

    public static HandlerList getHandlerList()
    {
        return handlers;
    }

    Claim claim;
    Player attacker;
    Entity defender;

    public PreventPvPEvent(Claim claim, Player attacker, Entity defender)
    {
        this.claim = claim;
        this.attacker = attacker;
        this.defender = defender;
    }

    public Claim getClaim()
    {
        return this.claim;
    }

    public Player getAttacker()
    {
        return attacker;
    }

    /**
     * @return The defender -- almost in all cases a player, unless the attacker damages a Tamable (pet),
     *         in which case the pet is returned.
     */
    public Entity getDefender()
    {
        return defender;
    }

    @Override
    public HandlerList getHandlers()
    {
        return handlers;
    }

    @Override
    public boolean isCancelled()
    {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled)
    {
        this.cancelled = cancelled;
    }
}