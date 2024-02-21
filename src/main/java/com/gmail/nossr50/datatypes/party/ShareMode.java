package com.gmail.nossr50.datatypes.party;

import org.jetbrains.annotations.Nullable;

public enum ShareMode {
    NONE("Nenhum"),
    EQUAL("Igual"),
    RANDOM("Aleatorio");
    private final String customName;

    ShareMode(String customName) {
        this.customName = customName;
    }

    public static @Nullable ShareMode getShareMode(String string) {
        for (ShareMode shareMode : ShareMode.values()) {
            if (shareMode.customName.equalsIgnoreCase(string)) {
                return shareMode;
            }
            if (shareMode.name().equalsIgnoreCase(string)) {
                return shareMode;
            }
        }
        return null;
    }

    public String customName() {
        return customName;
    }
}
