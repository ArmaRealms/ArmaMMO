package com.gmail.nossr50.util.commands;

import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.datatypes.player.PlayerProfile;
import com.gmail.nossr50.datatypes.skills.PrimarySkillType;
import com.gmail.nossr50.locale.LocaleLoader;
import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.util.MetadataConstants;
import com.gmail.nossr50.util.Misc;
import com.gmail.nossr50.util.player.UserManager;
import com.gmail.nossr50.util.skills.SkillTools;
import com.gmail.nossr50.util.skills.SkillUtils;
import com.gmail.nossr50.util.text.StringUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class CommandUtils {
    public static final List<String> TRUE_FALSE_OPTIONS = List.of("on", "off", "sim", "nao");
    public static final List<String> RESET_OPTIONS = List.of("limpar", "resetar");
    public static final List<String> TRUE_OPTIONS = List.of("on", "true", "enabled", "ativar", "sim");
    public static final List<String> FALSE_OPTIONS = List.of("off", "false", "disabled", "desativar", "nao");

    private CommandUtils() {
    }

    public static boolean isChildSkill(CommandSender sender, PrimarySkillType skill) {
        if (skill == null || !SkillTools.isChildSkill(skill)) return false;
        sender.sendMessage("Skill secundária ainda não tem suporte para este comando!"); // TODO: Localize this
        return true;
    }

    public static boolean tooFar(CommandSender sender, Player target, boolean hasPermission) {
        if (!target.isOnline() && !hasPermission) {
            sender.sendMessage(LocaleLoader.getString("Inspect.Offline"));
            return true;
        } else if (sender instanceof Player player && !Misc.isNear(player.getLocation(), target.getLocation(), mcMMO.p.getGeneralConfig().getInspectDistance()) && !hasPermission) {
            sender.sendMessage(LocaleLoader.getString("Inspect.TooFar"));
            return true;
        }

        return false;
    }

    public static boolean hidden(CommandSender sender, Player target, boolean hasPermission) {
        return sender instanceof Player player && !player.canSee(target) && !hasPermission;
    }

    public static boolean isOffline(CommandSender sender, OfflinePlayer player) {
        if (player.isOnline()) return false;

        sender.sendMessage(LocaleLoader.getString("Commands.Offline"));
        return true;
    }

    /**
     * Checks if there is a valid mmoPlayer object.
     *
     * @param sender      CommandSender, who used the command
     * @param playerName  name of the target player
     * @param mmoPlayer mmoPlayer object of the target player
     * @return true if the player is online and a valid mmoPlayer object was found
     */
    public static boolean checkPlayerExistence(CommandSender sender, String playerName, McMMOPlayer mmoPlayer) {
        if (mmoPlayer != null) {
            if (CommandUtils.hidden(sender, mmoPlayer.getPlayer(), false)) {
                sender.sendMessage(LocaleLoader.getString("Commands.Offline"));
                return false;
            }
            return true;
        }

        PlayerProfile profile = new PlayerProfile(playerName, false, 0);

        if (unloadedProfile(sender, profile)) {
            return false;
        }

        sender.sendMessage(LocaleLoader.getString("Commands.DoesNotExist"));
        return false;
    }

    public static boolean unloadedProfile(CommandSender sender, PlayerProfile profile) {
        if (profile.isLoaded()) return false;
        sender.sendMessage(LocaleLoader.getString("Commands.Offline"));
        return true;
    }

    public static boolean hasPlayerDataKey(CommandSender sender) {
        if (!(sender instanceof Player)) return false;

        boolean hasPlayerDataKey = ((Player) sender).hasMetadata(MetadataConstants.METADATA_KEY_PLAYER_DATA);

        if (!hasPlayerDataKey) {
            sender.sendMessage(LocaleLoader.getString("Commands.NotLoaded"));
        }

        return hasPlayerDataKey;
    }

    public static boolean isLoaded(CommandSender sender, PlayerProfile profile) {
        if (profile.isLoaded()) return true;
        sender.sendMessage(LocaleLoader.getString("Commands.NotLoaded"));
        return false;
    }

    public static boolean isInvalidInteger(CommandSender sender, String value) {
        if (StringUtils.isInt(value)) return false;

        sender.sendMessage("That is not a valid integer."); // TODO: Localize
        return true;
    }

    public static boolean isInvalidDouble(CommandSender sender, String value) {
        if (StringUtils.isDouble(value)) return false;

        sender.sendMessage("That is not a valid percentage."); // TODO: Localize
        return true;
    }

    public static boolean isInvalidSkill(CommandSender sender, String skillName) {
        if (SkillUtils.isSkill(skillName)) return false;

        sender.sendMessage(LocaleLoader.getString("Commands.Skill.Invalid"));
        return true;
    }

    public static boolean shouldEnableToggle(String arg) {
        return TRUE_OPTIONS.contains(arg.toLowerCase(Locale.ENGLISH));
    }

    public static boolean shouldDisableToggle(String arg) {
        return FALSE_OPTIONS.contains(arg.toLowerCase(Locale.ENGLISH));
    }

    /**
     * Print out details on Gathering skills. Only for online players.
     *
     * @param inspect The player to retrieve stats for
     * @param display The sender to display stats to
     */
    public static void printGatheringSkills(Player inspect, CommandSender display) {
        printGroupedSkillData(inspect, display, LocaleLoader.getString("Stats.Header.Gathering"), mcMMO.p.getSkillTools().GATHERING_SKILLS);
    }

    public static void printGatheringSkills(Player player) {
        printGatheringSkills(player, player);
    }

    /**
     * Print out details on Combat skills. Only for online players.
     *
     * @param inspect The player to retrieve stats for
     * @param display The sender to display stats to
     */
    public static void printCombatSkills(Player inspect, CommandSender display) {
        printGroupedSkillData(inspect, display, LocaleLoader.getString("Stats.Header.Combat"), mcMMO.p.getSkillTools().COMBAT_SKILLS);
    }

    public static void printCombatSkills(Player player) {
        printCombatSkills(player, player);
    }

    /**
     * Print out details on Misc skills. Only for online players.
     *
     * @param inspect The player to retrieve stats for
     * @param display The sender to display stats to
     */
    public static void printMiscSkills(Player inspect, CommandSender display) {
        printGroupedSkillData(inspect, display, LocaleLoader.getString("Stats.Header.Misc"), mcMMO.p.getSkillTools().getMiscSkills());
    }

    public static void printMiscSkills(Player player) {
        printMiscSkills(player, player);
    }

    public static @NotNull String displaySkill(PlayerProfile profile, PrimarySkillType skill) {
        if (SkillTools.isChildSkill(skill)) {
            return LocaleLoader.getString("Skills.ChildStats", LocaleLoader.getString(StringUtils.getCapitalized(skill.toString()) + ".Listener") + " ", StringUtils.formatNumber(profile.getSkillLevel(skill)));
        }
        if (profile.getSkillLevel(skill) == mcMMO.p.getSkillTools().getLevelCap(skill)) {
            return LocaleLoader.getString("Skills.Stats", LocaleLoader.getString(StringUtils.getCapitalized(skill.toString()) + ".Listener") + " ", StringUtils.formatNumber(profile.getSkillLevel(skill)), StringUtils.formatNumber(profile.getSkillXpLevel(skill)), LocaleLoader.getString("Skills.MaxXP"));
        }
        return LocaleLoader.getString("Skills.Stats", LocaleLoader.getString(StringUtils.getCapitalized(skill.toString()) + ".Listener") + " ", StringUtils.formatNumber(profile.getSkillLevel(skill)), StringUtils.formatNumber(profile.getSkillXpLevel(skill)), StringUtils.formatNumber(profile.getXpToLevel(skill)));
    }

    private static void printGroupedSkillData(Player inspectTarget, CommandSender display,
                                              String header, List<PrimarySkillType> skillGroup) {
        var mmoPlayer = UserManager.getPlayer(inspectTarget);
        if (mmoPlayer == null) return;

        PlayerProfile profile = mmoPlayer.getProfile();

        final List<String> displayData = new ArrayList<>();
        displayData.add(header);

        for (PrimarySkillType primarySkillType : skillGroup) {
            if (mcMMO.p.getSkillTools().doesPlayerHaveSkillPermission(inspectTarget, primarySkillType)) {
                displayData.add(displaySkill(profile, primarySkillType));
            }
        }

        int size = displayData.size();

        if (size > 1) {
            display.sendMessage(displayData.toArray(new String[size]));
        }
    }

    public static List<String> getOnlinePlayerNames(CommandSender sender) {
        Player player = sender instanceof Player p ? p : null;
        List<String> onlinePlayerNames = new ArrayList<>();

        for (Player onlinePlayer : mcMMO.p.getServer().getOnlinePlayers()) {
            if (player != null && player.canSee(onlinePlayer)) {
                onlinePlayerNames.add(onlinePlayer.getName());
            }
        }

        return onlinePlayerNames;
    }

    /**
     * Get a matched player name if one was found in the database.
     *
     * @param partialName Name to match
     * @return Matched name or {@code partialName} if no match was found
     */
    public static String getMatchedPlayerName(String partialName) {
        if (mcMMO.p.getGeneralConfig().getMatchOfflinePlayers()) {
            List<String> matches = matchPlayer(partialName);

            if (matches.size() == 1) {
                partialName = matches.get(0);
            }
        } else {
            Player player = mcMMO.p.getServer().getPlayer(partialName);

            if (player != null) {
                partialName = player.getName();
            }
        }

        return partialName;
    }

    /**
     * Attempts to match any player names with the given name, and returns a list of all possibly matches.
     * This list is not sorted in any particular order.
     * If an exact match is found, the returned list will only contain a single result.
     *
     * @param partialName Name to match
     * @return List of all possible names
     */
    private static List<String> matchPlayer(String partialName) {
        List<String> matchedPlayers = new ArrayList<>();

        for (OfflinePlayer offlinePlayer : mcMMO.p.getServer().getOfflinePlayers()) {
            String playerName = offlinePlayer.getName();

            if (playerName == null) { //Do null checking here to detect corrupted data before sending it throuogh .equals
                mcMMO.p.getLogger().warning("Player data file with UUID " + offlinePlayer.getUniqueId() + " is missing a player name. This may be a legacy file from before bukkit.lastKnownName. This should be okay to ignore.");
                continue; //Don't let an error here interrupt the loop
            }

            if (partialName.equalsIgnoreCase(playerName)) {
                // Exact match
                matchedPlayers.clear();
                matchedPlayers.add(playerName);
                break;
            }

            if (playerName.toLowerCase(Locale.ENGLISH).contains(partialName.toLowerCase(Locale.ENGLISH))) {
                // Partial match
                matchedPlayers.add(playerName);
            }
        }

        return matchedPlayers;
    }
}
