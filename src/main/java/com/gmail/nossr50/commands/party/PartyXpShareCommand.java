package com.gmail.nossr50.commands.party;

import com.gmail.nossr50.datatypes.party.Party;
import com.gmail.nossr50.datatypes.party.PartyFeature;
import com.gmail.nossr50.datatypes.party.ShareMode;
import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.locale.LocaleLoader;
import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.util.commands.CommandUtils;
import com.gmail.nossr50.util.player.UserManager;
import com.gmail.nossr50.util.text.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PartyXpShareCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(LocaleLoader.getString("Commands.NoConsole"));
            return true;
        }

        McMMOPlayer mmoPlayer = UserManager.getPlayer(player);
        if (mmoPlayer == null) {
            sender.sendMessage(LocaleLoader.getString("Profile.PendingLoad"));
            return true;
        }

        Party party = mmoPlayer.getParty();
        if (party == null) {
            sender.sendMessage(LocaleLoader.getString("Commands.Party.NotInParty"));
            return true;
        }

        if (party.getLevel() < mcMMO.p.getGeneralConfig().getPartyFeatureUnlockLevel(PartyFeature.XP_SHARE)) {
            sender.sendMessage(LocaleLoader.getString("Party.Feature.Disabled.5"));
            return true;
        }

        if (args.length == 2) {
            if (args[1].equalsIgnoreCase("nenhum") || CommandUtils.shouldDisableToggle(args[1])) {
                handleChangingShareMode(party, ShareMode.NONE);
            } else if (args[1].equalsIgnoreCase("igual") || args[1].equalsIgnoreCase("even") || CommandUtils.shouldEnableToggle(args[1])) {
                handleChangingShareMode(party, ShareMode.EQUAL);
            } else {
                sender.sendMessage(LocaleLoader.getString("Commands.Usage.2", "party", "xpshare", "<nenhum | igual>"));
            }

            return true;
        }
        sender.sendMessage(LocaleLoader.getString("Commands.Usage.2", "party", "xpshare", "<nenhum | igual>"));
        return true;
    }

    private void handleChangingShareMode(@NotNull Party party, ShareMode mode) {
        party.setXpShareMode(mode);

        String changeModeMessage = LocaleLoader.getString("Commands.Party.SetSharing", LocaleLoader.getString("Party.ShareType.Xp"), LocaleLoader.getString("Party.ShareMode." + StringUtils.getCapitalized(mode.toString())));

        for (Player member : party.getOnlineMembers()) {
            member.sendMessage(changeModeMessage);
        }
    }
}
