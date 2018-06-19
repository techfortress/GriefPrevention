package me.ryanhamshire.GriefPrevention.events.triage;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.metadata.Metadatable;

/**
 * Fired when a block or block-like sourceEntity is placed or destroyed.
 * (I.e. the type is modified from/to AIR).
 * Block-like entities include item frames, armor stands, paintings, etc.
 *
 * Created on 2/23/2017.
 *
 * @author RoboMWM
 */
public class GPBlockMutateTypeEvent extends GPBaseEvent
{
    public GPBlockMutateTypeEvent(Event baseEvent, Entity sourceEntity, Location location, Metadatable target)
    {
        super(baseEvent, sourceEntity, location, target);
    }
    public GPBlockMutateTypeEvent(Event baseEvent, Metadatable source, Location location, Metadatable target)
    {
        super(baseEvent, source, location, target);
    }
}
