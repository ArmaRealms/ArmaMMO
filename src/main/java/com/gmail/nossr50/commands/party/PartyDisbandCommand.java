package com.gmail.nossr50.commands.party;

import com.gmail.nossr50.datatypes.party.Party;
import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.events.party.McMMOPartyChangeEvent.EventReason;
import com.gmail.nossr50.locale.LocaleLoader;
import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.util.player.UserManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PartyDisbandCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 1) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(LocaleLoader.getString("Commands.NoConsole"));
                return true;
            }

            final McMMOPlayer mcMMOPlayer = UserManager.getPlayer(player);
            if (mcMMOPlayer == null) {
                sender.sendMessage(LocaleLoader.getString("Profile.PendingLoad"));
                return true;
            }

            final Party playerParty = mcMMOPlayer.getParty();
            if (playerParty == null) {
                return true;
            }

            final String partyName = playerParty.getName();

            for (Player member : playerParty.getOnlineMembers()) {
                if (!mcMMO.p.getPartyManager().handlePartyChangeEvent(member, partyName, null, EventReason.KICKED_FROM_PARTY)) {
                    return true;
                }

                member.sendMessage(LocaleLoader.getString("Party.Disband"));
            }

            mcMMO.p.getPartyManager().disbandParty(mcMMOPlayer, playerParty);
            return true;
        }
        sender.sendMessage(LocaleLoader.getString("Commands.Usage.1", "party", "debandar"));
        return true;
    }
}
