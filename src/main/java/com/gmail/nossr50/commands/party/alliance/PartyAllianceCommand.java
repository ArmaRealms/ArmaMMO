package com.gmail.nossr50.commands.party.alliance;

import com.gmail.nossr50.datatypes.party.Party;
import com.gmail.nossr50.datatypes.party.PartyFeature;
import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.locale.LocaleLoader;
import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.util.commands.CommandUtils;
import com.gmail.nossr50.util.player.UserManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PartyAllianceCommand implements TabExecutor {
    public static final List<String> ALLIANCE_SUBCOMMANDS = List.of("convidar", "aceitar", "debandar");
    private final CommandExecutor partyAllianceInviteCommand = new PartyAllianceInviteCommand();
    private final CommandExecutor partyAllianceAcceptCommand = new PartyAllianceAcceptCommand();
    private final CommandExecutor partyAllianceDisbandCommand = new PartyAllianceDisbandCommand();
    private Player player;
    private Party playerParty;
    private Party targetParty;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, String[] args) {
        if (CommandUtils.noConsoleUsage(sender)) {
            return true;
        }

        if (UserManager.getPlayer((Player) sender) == null) {
            sender.sendMessage(LocaleLoader.getString("Profile.PendingLoad"));
            return true;
        }

        player = (Player) sender;
        final McMMOPlayer mmoPlayer = UserManager.getPlayer(player);

        playerParty = mmoPlayer.getParty();

        switch (args.length) {
            case 1:
                if (playerParty.getLevel() < mcMMO.p.getGeneralConfig()
                        .getPartyFeatureUnlockLevel(PartyFeature.ALLIANCE)) {
                    sender.sendMessage(LocaleLoader.getString("Party.Feature.Disabled.3"));
                    return true;
                }

                if (playerParty.getAlly() == null) {
                    printUsage();
                    return true;
                }

                targetParty = playerParty.getAlly();

                displayPartyHeader();
                displayMemberInfo(mmoPlayer);
                return true;

            case 2:
            case 3:
                if (playerParty.getLevel() < mcMMO.p.getGeneralConfig()
                        .getPartyFeatureUnlockLevel(PartyFeature.ALLIANCE)) {
                    sender.sendMessage(LocaleLoader.getString("Party.Feature.Disabled.3"));
                    return true;
                }

                if (args[1].equalsIgnoreCase("convidar")) {
                    return partyAllianceInviteCommand.onCommand(sender, command, label, args);
                }

                if (args[1].equalsIgnoreCase("aceitar")) {
                    return partyAllianceAcceptCommand.onCommand(sender, command, label, args);
                }

                if (args[1].equalsIgnoreCase("debandar")) {
                    return partyAllianceDisbandCommand.onCommand(sender, command, label, args);
                }

                if (playerParty.getAlly() == null) {
                    printUsage();
                    return true;
                }

                targetParty = playerParty.getAlly();

                displayPartyHeader();
                displayMemberInfo(mmoPlayer);
                return true;

            default:
                return false;
        }
    }

    private boolean printUsage() {
        player.sendMessage(LocaleLoader.getString("Commands.Party.Alliance.Help.0"));
        player.sendMessage(LocaleLoader.getString("Commands.Party.Alliance.Help.1"));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender,
                                      @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 1) {
            List<String> matches = ALLIANCE_SUBCOMMANDS.stream().filter(s -> s.startsWith(args[0])).toList();

            if (matches.isEmpty()) {
                return CommandUtils.getOnlinePlayerNames(commandSender).stream().filter(s -> s.startsWith(args[0])).toList();
            }

            return matches;
        }
        return List.of();
    }

    private void displayPartyHeader() {
        player.sendMessage(LocaleLoader.getString("Commands.Party.Alliance.Header"));
        player.sendMessage(
                LocaleLoader.getString("Commands.Party.Alliance.Ally", playerParty.getName(),
                        targetParty.getName()));
    }

    private void displayMemberInfo(McMMOPlayer mmoPlayer) {
        List<Player> nearMembers = mcMMO.p.getPartyManager().getNearMembers(mmoPlayer);
        player.sendMessage(LocaleLoader.getString("Commands.Party.Alliance.Members.Header"));
        player.sendMessage(playerParty.createMembersList(player));
        player.sendMessage(ChatColor.DARK_GRAY + "----------------------------");
        player.sendMessage(targetParty.createMembersList(player));
    }
}
