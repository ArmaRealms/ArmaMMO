package com.gmail.nossr50.commands.party.teleport;

import com.gmail.nossr50.datatypes.party.PartyTeleportRecord;
import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.locale.LocaleLoader;
import com.gmail.nossr50.util.Permissions;
import com.gmail.nossr50.util.player.UserManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PtpAcceptAnyCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, String[] args) {
        if (!Permissions.partyTeleportAcceptAll(sender)) {
            String commandPermissionMessage = command.getPermissionMessage();
            if (commandPermissionMessage != null) {
                sender.sendMessage(commandPermissionMessage);
            }
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(LocaleLoader.getString("Commands.NoConsole"));
            return true;
        }

        McMMOPlayer mcMMOPlayer = UserManager.getPlayer(player);
        if (mcMMOPlayer == null) {
            sender.sendMessage(LocaleLoader.getString("Profile.PendingLoad"));
            return true;
        }

        PartyTeleportRecord ptpRecord = mcMMOPlayer.getPartyTeleportRecord();
        if (ptpRecord == null) return true;

        if (ptpRecord.isConfirmRequired()) {
            sender.sendMessage(LocaleLoader.getString("Commands.ptp.AcceptAny.Disabled"));
        } else {
            sender.sendMessage(LocaleLoader.getString("Commands.ptp.AcceptAny.Enabled"));
        }

        ptpRecord.toggleConfirmRequired();
        return true;
    }
}
