package com.gmail.nossr50.commands.party;

import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.locale.LocaleLoader;
import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.util.player.UserManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PartyAcceptCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, String[] args) {
        if (args.length == 1) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(LocaleLoader.getString("Commands.NoConsole"));
                return true;
            }

            //Check if player profile is loaded
            McMMOPlayer mmoPlayer = UserManager.getPlayer(player);
            if (mmoPlayer == null) {
                sender.sendMessage(LocaleLoader.getString("Profile.PendingLoad"));
                return true;
            }

            if (!mmoPlayer.hasPartyInvite()) {
                sender.sendMessage(LocaleLoader.getString("mcMMO.NoInvites"));
                return true;
            }

            // Changing parties
            if (!mcMMO.p.getPartyManager()
                    .changeOrJoinParty(mmoPlayer, mmoPlayer.getPartyInvite().getName())) {
                return true;
            }

            mcMMO.p.getPartyManager().joinInvitedParty(mmoPlayer);
            return true;
        }
        sender.sendMessage(LocaleLoader.getString("Commands.Usage.1", "party", "aceitar"));
        return true;
    }
}
