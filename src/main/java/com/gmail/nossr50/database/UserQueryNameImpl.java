package com.gmail.nossr50.database;

import org.jetbrains.annotations.NotNull;

public record UserQueryNameImpl(@NotNull String name) implements UserQueryName {

    @Override
    public @NotNull UserQueryType getType() {
        return UserQueryType.NAME;
    }
}
