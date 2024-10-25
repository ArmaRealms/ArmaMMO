package com.gmail.nossr50.commands.party;

import com.gmail.nossr50.datatypes.party.Party;
import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.locale.LocaleLoader;
import com.gmail.nossr50.util.player.UserManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PartyChangePasswordCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(LocaleLoader.getString("Commands.NoConsole"));
            return true;
        }

        McMMOPlayer mcMMOPlayer = UserManager.getPlayer(player);
        if (mcMMOPlayer == null) {
            sender.sendMessage(LocaleLoader.getString("Profile.PendingLoad"));
            return true;
        }

        Party party = mcMMOPlayer.getParty();
        if (party == null) return true;

        switch (args.length) {
            case 1 -> {
                unprotectParty(party, sender);
                return true;
            }

            case 2 -> {
                if (args[1].equalsIgnoreCase("limpar") || args[1].equalsIgnoreCase("resetar")) {
                    unprotectParty(party, sender);
                    return true;
                }

                protectParty(party, sender, args[1]);
                return true;
            }

            default -> {
                sender.sendMessage(LocaleLoader.getString("Commands.Usage.2", "party", "senha", "[limpar|resetar]"));
                sender.sendMessage(LocaleLoader.getString("Commands.Usage.2", "party", "senha", "<" + LocaleLoader.getString("Commands.Usage.Password") + ">"));
                return true;
            }
        }
    }

    private void unprotectParty(Party party, CommandSender sender) {
        party.setLocked(true);
        party.setPassword(null);
        sender.sendMessage(LocaleLoader.getString("Party.Password.Removed"));
    }

    private void protectParty(Party party, CommandSender sender, String password) {
        party.setLocked(true);
        party.setPassword(password);
        sender.sendMessage(LocaleLoader.getString("Party.Password.Set", password));
    }
}
