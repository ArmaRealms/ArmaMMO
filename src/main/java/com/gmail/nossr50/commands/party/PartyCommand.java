package com.gmail.nossr50.commands.party;

import com.gmail.nossr50.commands.party.alliance.PartyAllianceCommand;
import com.gmail.nossr50.commands.party.teleport.PtpCommand;
import com.gmail.nossr50.datatypes.party.Party;
import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.locale.LocaleLoader;
import com.gmail.nossr50.util.Permissions;
import com.gmail.nossr50.util.commands.CommandUtils;
import com.gmail.nossr50.util.player.UserManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class PartyCommand implements TabExecutor {
    private static final List<String> PARTY_SUBCOMMANDS = PartySubcommandType.getSubcommands();
    private static final List<String> XPSHARE_COMPLETIONS = List.of("nenhum", "igual");
    private static final List<String> ITEMSHARE_COMPLETIONS = List.of("nenhum", "igual", "aleatorio", "saque", "mineracao", "herbalismo", "lenhador", "outros");
    private static final List<String> ITEMSHARE_CATEGORY = List.of("saque", "mineracao", "herbalismo", "lenhador", "outros");
    private final CommandExecutor partyJoinCommand;
    private final CommandExecutor partyAcceptCommand;
    private final CommandExecutor partyCreateCommand;
    private final CommandExecutor partyQuitCommand;
    private final CommandExecutor partyXpShareCommand;
    private final CommandExecutor partyItemShareCommand;
    private final CommandExecutor partyInviteCommand;
    private final CommandExecutor partyKickCommand;
    private final CommandExecutor partyDisbandCommand;
    private final CommandExecutor partyChangeOwnerCommand;
    private final CommandExecutor partyLockCommand;
    private final CommandExecutor partyChangePasswordCommand;
    private final CommandExecutor partyRenameCommand;
    private final CommandExecutor partyInfoCommand;
    private final CommandExecutor partyHelpCommand;
    private final CommandExecutor partyTeleportCommand;
    private final CommandExecutor partyAllianceCommand;

    public PartyCommand() {
        partyJoinCommand = new PartyJoinCommand();
        partyAcceptCommand = new PartyAcceptCommand();
        partyCreateCommand = new PartyCreateCommand();
        partyQuitCommand = new PartyQuitCommand();
        partyXpShareCommand = new PartyXpShareCommand();
        partyItemShareCommand = new PartyItemShareCommand();
        partyInviteCommand = new PartyInviteCommand();
        partyKickCommand = new PartyKickCommand();
        partyDisbandCommand = new PartyDisbandCommand();
        partyChangeOwnerCommand = new PartyChangeOwnerCommand();
        partyLockCommand = new PartyLockCommand();
        partyChangePasswordCommand = new PartyChangePasswordCommand();
        partyRenameCommand = new PartyRenameCommand();
        partyInfoCommand = new PartyInfoCommand();
        partyHelpCommand = new PartyHelpCommand();
        partyTeleportCommand = new PtpCommand();
        partyAllianceCommand = new PartyAllianceCommand();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(LocaleLoader.getString("Commands.NoConsole"));
            return true;
        }

        String commandPermissionMessage = command.getPermissionMessage();
        if (!Permissions.party(sender)) {
            if (commandPermissionMessage != null) {
                sender.sendMessage(commandPermissionMessage);
            }
            return true;
        }

        if (!UserManager.hasPlayerDataKey(player)) return true;

        McMMOPlayer mcMMOPlayer = UserManager.getPlayer(player);
        if (mcMMOPlayer == null) {
            player.sendMessage(LocaleLoader.getString("Profile.PendingLoad"));
            return true;
        }

        if (args.length < 1) {
            if (!mcMMOPlayer.inParty()) {
                sender.sendMessage(LocaleLoader.getString("Commands.Party.None"));
                return printUsage(player);
            }

            return partyInfoCommand.onCommand(sender, command, label, args);
        }

        PartySubcommandType subcommand = PartySubcommandType.getSubcommand(args[0]);

        if (subcommand == null) return printUsage(player);

        // Can't use this for lock/unlock since they're handled by the same command
        if (subcommand != PartySubcommandType.LOCK && subcommand != PartySubcommandType.UNLOCK && !Permissions.partySubcommand(sender, subcommand)) {
            if (commandPermissionMessage != null) {
                sender.sendMessage(commandPermissionMessage);
            }
            return true;
        }

        switch (subcommand) {
            case JOIN -> {
                return partyJoinCommand.onCommand(sender, command, label, args);
            }
            case ACCEPT -> {
                return partyAcceptCommand.onCommand(sender, command, label, args);
            }
            case CREATE -> {
                return partyCreateCommand.onCommand(sender, command, label, args);
            }
            case HELP -> {
                return partyHelpCommand.onCommand(sender, command, label, args);
            }
        }

        // Party member commands
        if (!mcMMOPlayer.inParty()) {
            sender.sendMessage(LocaleLoader.getString("Commands.Party.None"));
            return printUsage(player);
        }

        switch (subcommand) {
            case INFO -> {
                return partyInfoCommand.onCommand(sender, command, label, args);
            }
            case QUIT -> {
                return partyQuitCommand.onCommand(sender, command, label, args);
            }
            case INVITE -> {
                return partyInviteCommand.onCommand(sender, command, label, args);
            }
            case TELEPORT -> {
                return partyTeleportCommand.onCommand(sender, command, label, extractArgs(args));
            }
        }

        // Party leader commands
        Party party = mcMMOPlayer.getParty();
        if (party == null) {
            sender.sendMessage(LocaleLoader.getString("Profile.PendingLoad"));
            return true;
        }

        if (!party.getLeader().getUniqueId().equals(player.getUniqueId())) {
            sender.sendMessage(LocaleLoader.getString("Party.NotOwner"));
            return true;
        }

        return switch (subcommand) {
            case XPSHARE -> partyXpShareCommand.onCommand(sender, command, label, args);
            case ITEMSHARE -> partyItemShareCommand.onCommand(sender, command, label, args);
            case KICK -> partyKickCommand.onCommand(sender, command, label, args);
            case DISBAND -> partyDisbandCommand.onCommand(sender, command, label, args);
            case OWNER -> partyChangeOwnerCommand.onCommand(sender, command, label, args);
            case LOCK, UNLOCK -> partyLockCommand.onCommand(sender, command, label, args);
            case PASSWORD -> partyChangePasswordCommand.onCommand(sender, command, label, args);
            case RENAME -> partyRenameCommand.onCommand(sender, command, label, args);
            case ALLIANCE -> partyAllianceCommand.onCommand(sender, command, label, args);
            default -> true;
        };
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        switch (args.length) {
            case 1 -> {
                return PARTY_SUBCOMMANDS.stream().filter(s -> s.startsWith(args[0])).toList();
            }
            case 2 -> {
                PartySubcommandType subcommand = PartySubcommandType.getSubcommand(args[0]);
                if (subcommand == null) return List.of();

                List<String> playerNames = CommandUtils.getOnlinePlayerNames(sender);

                switch (subcommand) {
                    case JOIN, INVITE, KICK, OWNER -> {
                        return playerNames.stream().filter(s -> s.startsWith(args[1])).toList();
                    }

                    case XPSHARE -> {
                        return XPSHARE_COMPLETIONS.stream().filter(s -> s.startsWith(args[1])).toList();
                    }

                    case ITEMSHARE -> {
                        return ITEMSHARE_COMPLETIONS.stream().filter(s -> s.startsWith(args[1])).toList();

                    }
                    case LOCK, CHAT -> {
                        return CommandUtils.TRUE_FALSE_OPTIONS.stream().filter(s -> s.startsWith(args[1])).toList();

                    }
                    case PASSWORD -> {
                        return CommandUtils.RESET_OPTIONS.stream().filter(s -> s.startsWith(args[1])).toList();

                    }
                    case TELEPORT -> {
                        List<String> matches = PtpCommand.TELEPORT_SUBCOMMANDS.stream().filter(s -> s.startsWith(args[0])).toList();

                        if (matches.isEmpty()) {
                            Player player = (Player) sender;
                            final McMMOPlayer mcMMOPlayer = UserManager.getPlayer(player);

                            //Not Loaded
                            if (mcMMOPlayer == null) {
                                sender.sendMessage(LocaleLoader.getString("Profile.PendingLoad"));
                                return List.of();
                            }

                            if (mcMMOPlayer.getParty() == null) return List.of();

                            final Party party = mcMMOPlayer.getParty();

                            playerNames = party.getOnlinePlayerNames(player);
                            return playerNames.stream().filter(s -> s.startsWith(args[1])).toList();
                        }

                        return matches;
                    }
                    default -> {
                        return List.of();
                    }
                }
            }
            case 3 -> {
                if (PartySubcommandType.getSubcommand(args[0]) == PartySubcommandType.ITEMSHARE && isItemShareCategory(args[1])) {
                    return CommandUtils.TRUE_FALSE_OPTIONS.stream().filter(s -> s.startsWith(args[2])).toList();
                }
                return List.of();
            }
            default -> {
                return List.of();
            }
        }
    }

    private boolean printUsage(Player player) {
        player.sendMessage(LocaleLoader.getString("Party.Help.0", "/party entrar"));
        player.sendMessage(LocaleLoader.getString("Party.Help.1", "/party criar"));
        player.sendMessage(LocaleLoader.getString("Party.Help.2", "/party ajuda"));
        return true;
    }

    private String[] extractArgs(String[] args) {
        return Arrays.copyOfRange(args, 1, args.length);
    }

    private boolean isItemShareCategory(String category) {
        return ITEMSHARE_CATEGORY.contains(category);
    }
}

