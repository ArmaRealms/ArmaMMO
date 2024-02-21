package com.gmail.nossr50.datatypes.party;

import org.jetbrains.annotations.Nullable;

public enum ShareMode {
    NONE("nenhum"),
    EQUAL("igual"),
    RANDOM("aleatorio");
    private final String name;

    ShareMode(String name) {
        this.name = name;
    }

    public static @Nullable ShareMode getShareMode(String string) {
        for (ShareMode shareMode : ShareMode.values()) {
            if (shareMode.name.equalsIgnoreCase(string)) {
                return shareMode;
            }
        }
        return null;
    }
}
