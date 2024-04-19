package com.gmail.nossr50.commands.party;

import com.gmail.nossr50.datatypes.party.Party;
import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.locale.LocaleLoader;
import com.gmail.nossr50.util.Permissions;
import com.gmail.nossr50.util.commands.CommandUtils;
import com.gmail.nossr50.util.player.UserManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PartyLockCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        switch (args.length) {
            case 1 -> {
                if (args[0].equalsIgnoreCase("travar")) {
                    togglePartyLock(sender, true);
                } else if (args[0].equalsIgnoreCase("destravar")) {
                    togglePartyLock(sender, false);
                }

                return true;
            }

            case 2 -> {
                if (!args[0].equalsIgnoreCase("travar")) {
                    sendUsageStrings(sender);
                    return true;
                }

                if (CommandUtils.shouldEnableToggle(args[1])) {
                    togglePartyLock(sender, true);
                } else if (CommandUtils.shouldDisableToggle(args[1])) {
                    togglePartyLock(sender, false);
                } else {
                    sendUsageStrings(sender);
                }

                return true;
            }

            default -> {
                sendUsageStrings(sender);
                return true;
            }
        }
    }

    private void sendUsageStrings(CommandSender sender) {
        sender.sendMessage(LocaleLoader.getString("Commands.Usage.2", "party", "travar", "[sim|nao]"));
        sender.sendMessage(LocaleLoader.getString("Commands.Usage.1", "party", "destravar"));
    }

    private void togglePartyLock(CommandSender sender, boolean lock) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(LocaleLoader.getString("Commands.NoConsole"));
            return;
        }

        McMMOPlayer mcMMOPlayer = UserManager.getPlayer(player);
        if (mcMMOPlayer == null) {
            sender.sendMessage(LocaleLoader.getString("Profile.PendingLoad"));
            return;
        }

        Party party = mcMMOPlayer.getParty();
        if (party == null) {
            return;
        }

        if (!Permissions.partySubcommand(sender, lock ? PartySubcommandType.LOCK : PartySubcommandType.UNLOCK)) {
            sender.sendMessage(LocaleLoader.getString("mcMMO.NoPermission"));
            return;
        }

        if (lock == party.isLocked()) {
            sender.sendMessage(LocaleLoader.getString("Party." + (lock ? "IsLocked" : "IsntLocked")));
            return;
        }

        party.setLocked(lock);
        sender.sendMessage(LocaleLoader.getString("Party." + (lock ? "Locked" : "Unlocked")));
    }
}
