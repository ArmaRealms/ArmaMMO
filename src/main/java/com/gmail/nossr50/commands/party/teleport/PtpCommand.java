package com.gmail.nossr50.commands.party.teleport;

import com.gmail.nossr50.config.WorldBlacklist;
import com.gmail.nossr50.datatypes.party.Party;
import com.gmail.nossr50.datatypes.party.PartyFeature;
import com.gmail.nossr50.datatypes.party.PartyTeleportRecord;
import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.locale.LocaleLoader;
import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.runnables.items.TeleportationWarmup;
import com.gmail.nossr50.util.EventUtils;
import com.gmail.nossr50.util.Misc;
import com.gmail.nossr50.util.Permissions;
import com.gmail.nossr50.util.commands.CommandUtils;
import com.gmail.nossr50.util.player.UserManager;
import com.gmail.nossr50.util.skills.SkillUtils;
import com.gmail.nossr50.worldguard.WorldGuardManager;
import com.gmail.nossr50.worldguard.WorldGuardUtils;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PtpCommand implements TabExecutor {
    public static final List<String> TELEPORT_SUBCOMMANDS = List.of("alternar", "aceitar",
            "aceitar-qualquer",
            "aceitar-tudo");
    private final CommandExecutor ptpToggleCommand = new PtpToggleCommand();
    private final CommandExecutor ptpAcceptAnyCommand = new PtpAcceptAnyCommand();
    private final CommandExecutor ptpAcceptCommand = new PtpAcceptCommand();

    protected static boolean canTeleport(CommandSender sender, Player player, String targetName) {
        McMMOPlayer mcMMOTarget = UserManager.getPlayer(targetName);

        if (!CommandUtils.checkPlayerExistence(sender, targetName, mcMMOTarget)) return false;

        if (mcMMOTarget == null) return false;

        Player target = mcMMOTarget.getPlayer();
        if (player.equals(target)) {
            player.sendMessage(LocaleLoader.getString("Party.Teleport.Self"));
            return false;
        }

        if (!mcMMO.p.getPartyManager().inSameParty(player, target)) {
            player.sendMessage(LocaleLoader.getString("Party.NotInYourParty", targetName));
            return false;
        }

        PartyTeleportRecord ptpRecord = mcMMOTarget.getPartyTeleportRecord();
        if (ptpRecord == null) return false;

        if (!ptpRecord.isEnabled()) {
            player.sendMessage(LocaleLoader.getString("Party.Teleport.Disabled", targetName));
            return false;
        }

        if (!target.isValid()) {
            player.sendMessage(LocaleLoader.getString("Party.Teleport.Dead"));
            return false;
        }

        return true;
    }

    protected static void handleTeleportWarmup(Player teleportingPlayer, Player targetPlayer) {
        if (UserManager.getPlayer(targetPlayer) == null) {
            targetPlayer.sendMessage(LocaleLoader.getString("Profile.PendingLoad"));
            return;
        }

        if (UserManager.getPlayer(teleportingPlayer) == null) {
            teleportingPlayer.sendMessage(LocaleLoader.getString("Profile.PendingLoad"));
            return;
        }

        McMMOPlayer mcMMOPlayer = UserManager.getPlayer(teleportingPlayer);
        if (mcMMOPlayer == null) return;

        McMMOPlayer mcMMOTarget = UserManager.getPlayer(targetPlayer);

        long warmup = mcMMO.p.getGeneralConfig().getPTPCommandWarmup();

        mcMMOPlayer.actualizeTeleportCommenceLocation(teleportingPlayer);

        if (warmup > 0) {
            teleportingPlayer.sendMessage(LocaleLoader.getString("Teleport.Commencing", warmup));
            mcMMO.p.getFoliaLib().getScheduler().runAtEntityLater(teleportingPlayer, new TeleportationWarmup(mcMMOPlayer, mcMMOTarget), 20 * warmup);
        } else {
            EventUtils.handlePartyTeleportEvent(teleportingPlayer, targetPlayer);
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(LocaleLoader.getString("Commands.NoConsole"));
            return true;
        }

        /* WORLD GUARD MAIN FLAG CHECK */
        if (WorldGuardUtils.isWorldGuardLoaded()) {
            if (!WorldGuardManager.getInstance().hasMainFlag(player)) {
                return true;
            }
        }

        /* WORLD BLACKLIST CHECK */
        if (WorldBlacklist.isWorldBlacklisted(player.getWorld())) {
            return true;
        }

        if (!UserManager.hasPlayerDataKey(player)) return true;

        McMMOPlayer mcMMOPlayer = UserManager.getPlayer(player);
        if (mcMMOPlayer == null) {
            sender.sendMessage(LocaleLoader.getString("Profile.PendingLoad"));
            return true;
        }

        if (!mcMMOPlayer.inParty()) {
            sender.sendMessage(LocaleLoader.getString("Commands.Party.None"));
            return true;
        }

        Party party = mcMMOPlayer.getParty();
        if (party == null) return true;

        if (party.getLevel() < mcMMO.p.getGeneralConfig()
                .getPartyFeatureUnlockLevel(PartyFeature.TELEPORT)) {
            sender.sendMessage(LocaleLoader.getString("Party.Feature.Disabled.2"));
            return true;
        }

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("alternar")) {
                return ptpToggleCommand.onCommand(sender, command, label, args);
            }

            if (args[0].equalsIgnoreCase("aceitar-qualquer") || args[0].equalsIgnoreCase("aceitar-tudo")) {
                return ptpAcceptAnyCommand.onCommand(sender, command, label, args);
            }

            long recentlyHurt = mcMMOPlayer.getRecentlyHurt();
            int hurtCooldown = mcMMO.p.getGeneralConfig().getPTPCommandRecentlyHurtCooldown();

            if (hurtCooldown > 0) {
                int timeRemaining = SkillUtils.calculateTimeLeft(
                        recentlyHurt * Misc.TIME_CONVERSION_FACTOR,
                        hurtCooldown, player);

                if (timeRemaining > 0) {
                    player.sendMessage(LocaleLoader.getString("Item.Injured.Wait", timeRemaining));
                    return true;
                }
            }

            if (args[0].equalsIgnoreCase("aceitar")) {
                return ptpAcceptCommand.onCommand(sender, command, label, args);
            }

            if (!Permissions.partyTeleportSend(sender)) {
                String commandPermissionMessage = command.getPermissionMessage();
                if (commandPermissionMessage != null) {
                    sender.sendMessage(commandPermissionMessage);
                }
                return true;
            }

            int ptpCooldown = mcMMO.p.getGeneralConfig().getPTPCommandCooldown();
            PartyTeleportRecord ptpRecord = mcMMOPlayer.getPartyTeleportRecord();
            if (ptpRecord == null) return true;

            long ptpLastUse = ptpRecord.getLastUse();

            if (ptpCooldown > 0) {
                int timeRemaining = SkillUtils.calculateTimeLeft(
                        ptpLastUse * Misc.TIME_CONVERSION_FACTOR, ptpCooldown,
                        player);

                if (timeRemaining > 0) {
                    player.sendMessage(LocaleLoader.getString("Item.Generic.Wait", timeRemaining));
                    return true;
                }
            }

            sendTeleportRequest(sender, player, CommandUtils.getMatchedPlayerName(args[0]));
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias,
                                      String[] args) {
        if (!(sender instanceof Player player)) return List.of();

        if (args.length == 1) {
            List<String> matches = TELEPORT_SUBCOMMANDS.stream()
                    .filter(s -> s.startsWith(args[0])).toList();

            if (matches.isEmpty()) {
                McMMOPlayer mcMMOPlayer = UserManager.getPlayer(player);
                if (mcMMOPlayer == null) return List.of();
                if (!mcMMOPlayer.inParty()) return List.of();
                Party party = mcMMOPlayer.getParty();
                if (party == null) return List.of();
                return mcMMOPlayer.getParty().getOnlinePlayerNames(player).stream().filter(s -> s.startsWith(args[0])).toList();
            }

            return matches;
        }
        return List.of();
    }

    private void sendTeleportRequest(CommandSender sender, Player player, String targetName) {
        if (!canTeleport(sender, player, targetName)) {
            return;
        }

        McMMOPlayer mcMMOTarget = UserManager.getPlayer(targetName);
        if (mcMMOTarget == null) return;
        Player target = mcMMOTarget.getPlayer();

        if (mcMMO.p.getGeneralConfig().getPTPCommandWorldPermissions()) {
            World targetWorld = target.getWorld();
            World playerWorld = player.getWorld();

            if (!Permissions.partyTeleportAllWorlds(player)) {
                if (!Permissions.partyTeleportWorld(target, targetWorld)) {
                    player.sendMessage(
                            LocaleLoader.getString("Commands.ptp.NoWorldPermissions",
                                    targetWorld.getName()));
                    return;
                } else if (targetWorld != playerWorld && !Permissions.partyTeleportWorld(player,
                        targetWorld)) {
                    player.sendMessage(
                            LocaleLoader.getString("Commands.ptp.NoWorldPermissions",
                                    targetWorld.getName()));
                    return;
                }
            }
        }

        PartyTeleportRecord ptpRecord = mcMMOTarget.getPartyTeleportRecord();
        if (ptpRecord == null) return;

        if (!ptpRecord.isConfirmRequired()) {
            handleTeleportWarmup(player, target);
            return;
        }

        ptpRecord.setRequestor(player);
        ptpRecord.actualizeTimeout();

        player.sendMessage(LocaleLoader.getString("Commands.Invite.Success"));

        target.sendMessage(LocaleLoader.getString("Commands.ptp.Request1", player.getName()));
        target.sendMessage(LocaleLoader.getString("Commands.ptp.Request2", mcMMO.p.getGeneralConfig().getPTPCommandTimeout()));
    }
}
