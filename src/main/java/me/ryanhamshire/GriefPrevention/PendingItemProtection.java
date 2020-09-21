package me.ryanhamshire.GriefPrevention;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

class PendingItemProtection
{
    @NotNull public Location location;
    @NotNull public UUID owner;
    long expirationTimestamp;
    @NotNull ItemStack itemStack;

    public PendingItemProtection(@NotNull Location location, @NotNull UUID owner, long expirationTimestamp, @NotNull ItemStack itemStack)
    {
        this.location = location;
        this.owner = owner;
        this.expirationTimestamp = expirationTimestamp;
        this.itemStack = itemStack;
    }
}
