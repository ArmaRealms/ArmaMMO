package com.gmail.nossr50.database;

import com.gmail.nossr50.datatypes.database.PlayerNameAndUUID;
import com.gmail.nossr50.mcMMO;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.bukkit.Server;
import org.jetbrains.annotations.NotNull;

final class InactiveUserPurge {
    private static final String PLAYER_TOKEN = "{@player}";
    private static final String UUID_TOKEN = "{@uuid}";
    private static final Pattern SAFE_USERNAME = Pattern.compile("[A-Za-z0-9_.+][A-Za-z0-9_.+-]*");

    private InactiveUserPurge() {
    }

    /** Reports committed removals and schedules their console commands on the server thread. */
    static void report(@NotNull Logger logger, @NotNull List<PlayerNameAndUUID> removedPlayers) {
        for (final PlayerNameAndUUID player : removedPlayers) {
            final String name = player.playerName().replaceAll("\\p{Cntrl}", "?");
            final String uuid = player.uuid() == null ? "unknown" : player.uuid().toString();
            logger.info("Purged inactive player " + name + " (UUID: " + uuid + ").");
        }
        logger.info("Purged " + removedPlayers.size() + " users from the database.");
        if (removedPlayers.isEmpty()) {
            return;
        }

        final var plugin = mcMMO.p;
        final List<String> commands = plugin.getGeneralConfig().getInactiveUserPurgeCommands();
        if (!commands.isEmpty()) {
            plugin.getFoliaLib().getScheduler().runNextTick(task -> {
                final Server server = plugin.getServer();
                for (final PlayerNameAndUUID player : removedPlayers) {
                    dispatchCommands(logger, server, player, commands);
                }
            });
        }
    }

    private static void dispatchCommands(@NotNull Logger logger, @NotNull Server server,
                                         @NotNull PlayerNameAndUUID player, @NotNull List<String> commands) {
        final String username = player.playerName();
        final boolean hasUsableName = !UsernamePlaceholder.isInvalidOldUsername(username)
                && SAFE_USERNAME.matcher(username).matches();
        final String uuid = player.uuid() == null ? "" : player.uuid().toString();
        for (final String configured : commands) {
            final String command = configured.strip();
            if (command.isEmpty()) {
                continue;
            }
            if ((!hasUsableName && command.contains(PLAYER_TOKEN)) || (uuid.isEmpty() && command.contains(UUID_TOKEN))) {
                logger.warning("Skipped inactive user purge command due to an unavailable username or UUID: "
                        + command);
                continue;
            }

            final String expanded = command.replace(PLAYER_TOKEN, username).replace(UUID_TOKEN, uuid);
            try {
                if (!server.dispatchCommand(server.getConsoleSender(), expanded)) {
                    logger.warning("Unknown inactive user purge command: " + expanded);
                }
            } catch (RuntimeException ex) {
                logger.log(Level.WARNING, "Failed to execute inactive user purge command: " + expanded, ex);
            }
        }
    }
}
