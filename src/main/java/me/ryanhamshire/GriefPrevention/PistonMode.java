package me.ryanhamshire.GriefPrevention;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum PistonMode {

    EVERYWHERE,
    EVERYWHERE_SIMPLE,
    CLAIMS_ONLY,
    IGNORED;

    public static @NotNull PistonMode of(@Nullable String value)
    {
        if (value == null) {
            return CLAIMS_ONLY;
        }
        try
        {
            return valueOf(value.toUpperCase());
        }
        catch (IllegalArgumentException e)
        {
            return CLAIMS_ONLY;
        }
    }

}
