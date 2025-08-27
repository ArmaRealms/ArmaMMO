package com.gmail.nossr50.database;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class UserQueryFull implements UserQueryUUID, UserQueryName {

    private final @NotNull String name;
    private final @NotNull UUID uuid;

    public UserQueryFull(@NotNull final String name, @NotNull final UUID uuid) {
        this.name = name;
        this.uuid = uuid;
    }

    @Override
    public @NotNull UserQueryType getType() {
        return UserQueryType.UUID_AND_NAME;
    }

    @Override
    public @NotNull String name() {
        return name;
    }

    @Override
    public @NotNull UUID getUUID() {
        return uuid;
    }
}
