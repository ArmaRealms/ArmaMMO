package com.gmail.nossr50.commands;

import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.locale.LocaleLoader;
import com.gmail.nossr50.util.commands.CommandUtils;
import com.gmail.nossr50.util.player.UserManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class ToggleCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        switch (args.length) {
            case 0:
                if (!(sender instanceof Player)) {
                    sender.sendMessage(LocaleLoader.getString("Commands.NoConsole"));
                    return true;
                }

                if (!hasSelfPermission(sender)) {
                    String commandPermissionMessage = command.getPermissionMessage();
                    if (commandPermissionMessage != null) {
                        sender.sendMessage(commandPermissionMessage);
                    }
                    return true;
                }

                if (!CommandUtils.hasPlayerDataKey(sender)) {
                    return true;
                }

                applyCommandAction(UserManager.getPlayer(sender.getName()));
                return true;

            case 1:
                if (!hasOtherPermission(sender)) {
                    String commandPermissionMessage = command.getPermissionMessage();
                    if (commandPermissionMessage != null) {
                        sender.sendMessage(commandPermissionMessage);
                    }
                    return true;
                }

                String playerName = CommandUtils.getMatchedPlayerName(args[0]);
                McMMOPlayer mmoPlayer = UserManager.getPlayer(playerName);

                if (!CommandUtils.checkPlayerExistence(sender, playerName, mmoPlayer)) {
                    return true;
                }

                if (CommandUtils.isOffline(sender, mmoPlayer.getPlayer())) {
                    return true;
                }

                applyCommandAction(mmoPlayer);
                sendSuccessMessage(sender, playerName);
                return true;

            default:
                return false;
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (args.length == 1) {
            return CommandUtils.getOnlinePlayerNames(sender).stream().filter(s -> s.startsWith(args[0])).toList();
        }
        return List.of();
    }

    protected abstract boolean hasOtherPermission(CommandSender sender);
    protected abstract boolean hasSelfPermission(CommandSender sender);
    protected abstract void applyCommandAction(McMMOPlayer mmoPlayer);
    protected abstract void sendSuccessMessage(CommandSender sender, String playerName);
}
