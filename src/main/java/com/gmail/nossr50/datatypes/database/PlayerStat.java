package com.gmail.nossr50.datatypes.database;

import org.jetbrains.annotations.NotNull;

public record PlayerStat(String name, int statVal) implements Comparable<PlayerStat> {
    @Override
    public int compareTo(@NotNull final PlayerStat o) {
        // Descending order
        final int cmp = Integer.compare(o.statVal, this.statVal);
        if (cmp != 0) return cmp;
        // Tie-breaker
        return this.name.compareTo(o.name);
    }
}
