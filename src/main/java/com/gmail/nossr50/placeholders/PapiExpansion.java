package com.gmail.nossr50.placeholders;

import com.gmail.nossr50.api.ExperienceAPI;
import com.gmail.nossr50.config.experience.ExperienceConfig;
import com.gmail.nossr50.datatypes.database.PlayerStat;
import com.gmail.nossr50.datatypes.party.Party;
import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.datatypes.skills.PrimarySkillType;
import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.util.Permissions;
import com.gmail.nossr50.util.player.UserManager;
import com.gmail.nossr50.util.text.StringUtils;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PapiExpansion extends PlaceholderExpansion {
    private final ExperienceConfig experienceConfig = ExperienceConfig.getInstance();
    public static final String SKILL_LEVEL = "level_";
    public static final String SKILL_EXP_NEEDED = "xp_needed_";
    public static final String SKILL_EXP_REMAINING = "xp_remaining_";
    public static final String SKILL_EXP = "xp_";
    public static final String SKILL_RANK = "rank_";
    public static final String SKILL_EXP_RATE = "xprate_";
    public static final String POWER_LEVEL = "power_level";
    public static final String POWER_LEVEL_CAP = "power_level_cap";
    public static final String IN_PARTY = "in_party";
    public static final String PARTY_NAME = "party_name";
    public static final String IS_PARTY_LEADER = "is_party_leader";
    public static final String PARTY_LEADER = "party_leader";
    public static final String PARTY_SIZE = "party_size";
    public static final String EXP_RATE = "xprate";
    public static final String IS_EXP_EVENT_ACTIVE = "is_xp_event_active";
    public static final String MCABILITY = "mcability";
    public static final String MCNOTIFY = "mcnotify";
    public static final String TOP_POWER_LEVEL = "top_power_";
    public static final String NAME = "name";
    public static final String LEVEL = "level";
    public List<PlayerStat> topPowerLevel = getTopPowerLevel();

    public PapiExpansion() {
    }

    @Override
    public String getIdentifier() {
        return "mcmmo";
    }

    @Override
    public String getAuthor() {
        return "mcMMO Dev Team";
    }

    @Override
    public String getVersion() {
        return mcMMO.p.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String getRequiredPlugin() {
        return "mcMMO";
    }

    @Override
    @Nullable
    public String onPlaceholderRequest(final Player player, @NotNull final String params) {
        // Non player-specific placeholders
        if (params.equalsIgnoreCase(IS_EXP_EVENT_ACTIVE)) {
            return mcMMO.p.isXPEventEnabled() ? PlaceholderAPIPlugin.booleanTrue() : PlaceholderAPIPlugin.booleanFalse();
        } else if (params.equalsIgnoreCase(EXP_RATE)) {
            return String.valueOf(experienceConfig.getExperienceGainsGlobalMultiplier());
        } else if (params.equalsIgnoreCase(POWER_LEVEL_CAP)) {
            return StringUtils.formatNumber(mcMMO.p.getGeneralConfig().getPowerLevelCap());
        }

        if (params.startsWith(TOP_POWER_LEVEL)) {
            String sub = params.substring(TOP_POWER_LEVEL.length());
            String[] split = sub.split("_");
            if (split.length != 2) {
                return "top param not valid. Use top_power_name_0 or top_power_level_0.";
            }
            String type = split[0];
            String rank = split[1];
            if (type.equalsIgnoreCase(NAME)) {
                if (Integer.parseInt(rank) >= topPowerLevel.size()) {
                    return "No player found at that rank.";
                }
                return topPowerLevel.get(Integer.parseInt(rank)).name;
            } else if (type.equalsIgnoreCase(LEVEL)) {
                if (Integer.parseInt(rank) >= topPowerLevel.size()) {
                    return "No player found at that rank.";
                }
                return StringUtils.formatNumber(topPowerLevel.get(Integer.parseInt(rank)).statVal);
            }
        }

        final McMMOPlayer user = UserManager.getPlayer(player);
        if (user == null) return null;

        //Player configuration
        if (params.equalsIgnoreCase(MCABILITY)) {
            return user.getAbilityUse() ? PlaceholderAPIPlugin.booleanTrue() : PlaceholderAPIPlugin.booleanFalse();
        }
        if (params.equalsIgnoreCase(MCNOTIFY)) {
            return user.useChatNotifications() ? PlaceholderAPIPlugin.booleanTrue() : PlaceholderAPIPlugin.booleanFalse();
        }

        if (params.startsWith(SKILL_LEVEL)) {
            PrimarySkillType skill = PrimarySkillType.valueOf(params.substring(SKILL_LEVEL.length()).toUpperCase());
            return skill == null ? null : StringUtils.formatNumber(user.getSkillLevel(skill));
        } else if (params.startsWith(SKILL_EXP_NEEDED)) {
            PrimarySkillType skill = PrimarySkillType.valueOf(params.substring(SKILL_EXP_NEEDED.length()).toUpperCase());
            return skill == null ? null : StringUtils.formatNumber(user.getXpToLevel(skill));
        } else if (params.startsWith(SKILL_EXP_REMAINING)) {
            PrimarySkillType skill = PrimarySkillType.valueOf(params.substring(SKILL_EXP_REMAINING.length()).toUpperCase());
            return skill == null ? null : StringUtils.formatNumber(user.getXpToLevel(skill) - user.getSkillXpLevel(skill));
        } else if (params.startsWith(SKILL_EXP)) {
            PrimarySkillType skill = PrimarySkillType.valueOf(params.substring(SKILL_EXP.length()).toUpperCase());
            return skill == null ? null : StringUtils.formatNumber(user.getSkillXpLevel(skill));
        } else if (params.startsWith(SKILL_RANK)) {
            try {
                return StringUtils.formatNumber(ExperienceAPI.getPlayerRankSkill(player.getUniqueId(), StringUtils.getCapitalized(params.substring(SKILL_RANK.length()))));
            } catch (Exception ex) {
                return null;
            }
        } else if (params.startsWith(SKILL_EXP_RATE)) {
            PrimarySkillType skill = PrimarySkillType.valueOf(params.substring(SKILL_EXP_RATE.length()).toUpperCase());
            if (skill == null) return null;
            double modifier = 1.0F;
            if (Permissions.customXpBoost(player, skill))
                modifier = experienceConfig.getCustomXpPerkBoost();
            else if (Permissions.quadrupleXp(player, skill))
                modifier = 4;
            else if (Permissions.tripleXp(player, skill))
                modifier = 3;
            else if (Permissions.doubleAndOneHalfXp(player, skill))
                modifier = 2.5;
            else if (Permissions.doubleXp(player, skill))
                modifier = 2;
            else if (Permissions.oneAndOneHalfXp(player, skill))
                modifier = 1.5;
            else if (Permissions.oneAndOneTenthXp(player, skill))
                modifier = 1.1;
            return String.valueOf(modifier);
        } else if (params.equalsIgnoreCase(POWER_LEVEL)) {
            return StringUtils.formatNumber(user.getPowerLevel());
        }

        //Party placeholders
        final Party party = user.getParty();

        if (params.equalsIgnoreCase(IN_PARTY)) {
            return (party == null) ? PlaceholderAPIPlugin.booleanFalse() : PlaceholderAPIPlugin.booleanTrue();
        } else if (params.equalsIgnoreCase(PARTY_NAME)) {
            return (party == null) ? "" : party.getName();
        } else if (params.equalsIgnoreCase(IS_PARTY_LEADER)) {
            if (party == null) return "";
            return party.getLeader().getPlayerName().equals(player.getName()) ? PlaceholderAPIPlugin.booleanTrue() : PlaceholderAPIPlugin.booleanFalse();
        } else if (params.equalsIgnoreCase(PARTY_LEADER)) {
            return (party == null) ? "" : party.getLeader().getPlayerName();
        } else if (params.equalsIgnoreCase(PARTY_SIZE)) {
            return (party == null) ? "" : StringUtils.formatNumber(party.getMembers().size());
        }

        return null;
    }

    @NotNull
    private List<PlayerStat> getTopPowerLevel() {
        return mcMMO.getDatabaseManager().readLeaderboard(null, 1, 10);
    }

}
