package me.ryanhamshire.GriefPrevention.events.triage;

/**
 * Created on 3/11/2017.
 * Called when a block's state is modified.
 *
 * Exception here is crop farming (primarily to maintain existing functionality of /containertrust)
 *
 * Includes:
 * Chest access
 * Item frame - rotation
 * Redstone settings
 * Uprooting crops
 * Planting crops
 *
 *
 * The following are considered "access" alterations:
 * Opening doors
 * Opening trap doors
 * Toggling levers, pressing buttons
 *
 * @author RoboMWM
 */
public class GPBlockMutateStateEvent
{
}
