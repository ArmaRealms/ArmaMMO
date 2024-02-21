package com.gmail.nossr50.commands.party.alliance;

import com.gmail.nossr50.datatypes.party.Party;
import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.locale.LocaleLoader;
import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.util.player.UserManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PartyAllianceAcceptCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 2) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(LocaleLoader.getString("Commands.NoConsole"));
                return true;
            }

            McMMOPlayer mcMMOPlayer = UserManager.getPlayer(player);
            if (mcMMOPlayer == null) {
                sender.sendMessage(LocaleLoader.getString("Profile.PendingLoad"));
                return true;
            }

            if (!mcMMOPlayer.hasPartyAllianceInvite()) {
                sender.sendMessage(LocaleLoader.getString("mcMMO.NoInvites"));
                return true;
            }

            Party party = mcMMOPlayer.getParty();
            if (party == null) return true;

            if (party.getAlly() != null) {
                player.sendMessage(LocaleLoader.getString("Commands.Party.Alliance.AlreadyAllies"));
                return true;
            }

            mcMMO.p.getPartyManager().acceptAllianceInvite(mcMMOPlayer);
            return true;
        }

        sender.sendMessage(LocaleLoader.getString("Commands.Usage.2", "party", "alianca", "aceitar"));
        return true;
    }
}
