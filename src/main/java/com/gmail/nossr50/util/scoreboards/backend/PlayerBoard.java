package com.gmail.nossr50.util.scoreboards.backend;

import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface PlayerBoard {
    @Nullable Scoreboard show();

    void hide(@NotNull Player targetPlayer, @Nullable Scoreboard targetBoard);

    boolean isShown();

    void setTitle(@NotNull String displayName);

    void draw(@NotNull List<SidebarLine> lines);

    void close();
}
