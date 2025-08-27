package com.gmail.nossr50.api;

import com.gmail.nossr50.api.exceptions.InvalidFormulaTypeException;
import com.gmail.nossr50.api.exceptions.InvalidPlayerException;
import com.gmail.nossr50.api.exceptions.InvalidSkillException;
import com.gmail.nossr50.api.exceptions.InvalidXPGainReasonException;
import com.gmail.nossr50.api.exceptions.McMMOPlayerNotFoundException;
import com.gmail.nossr50.config.experience.ExperienceConfig;
import com.gmail.nossr50.datatypes.experience.FormulaType;
import com.gmail.nossr50.datatypes.experience.XPGainReason;
import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.datatypes.player.PlayerProfile;
import com.gmail.nossr50.datatypes.skills.PrimarySkillType;
import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.util.player.UserManager;
import com.gmail.nossr50.util.skills.CombatUtils;
import com.gmail.nossr50.util.skills.SkillTools;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.BlockState;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.UUID;

import static com.gmail.nossr50.datatypes.experience.XPGainReason.PVE;
import static com.gmail.nossr50.datatypes.experience.XPGainSource.CUSTOM;
import static com.gmail.nossr50.datatypes.experience.XPGainSource.SELF;

public final class ExperienceAPI {
    private ExperienceAPI() {
    }

    /**
     * Returns whether given string is a valid type of skill suitable for the other API calls in
     * this class.
     * </br>
     * This function is designed for API usage.
     *
     * @param skillType A string that may or may not be a skill
     * @return true if this is a valid mcMMO skill
     */
    public static boolean isValidSkillType(@NotNull final String skillType) {
        return mcMMO.p.getSkillTools().matchSkill(skillType) != null;
    }

    /**
     * Start the task that gives combat XP. Processes combat XP like mcMMO normally would, so mcMMO
     * will check whether the entity should reward XP when giving out the XP
     *
     * @param mmoPlayer        The attacking player
     * @param target           The defending entity
     * @param primarySkillType The skill being used
     * @param multiplier       final XP result will be multiplied by this
     * @deprecated Draft API
     */
    @Deprecated
    public static void addCombatXP(final McMMOPlayer mmoPlayer, final LivingEntity target,
                                   final PrimarySkillType primarySkillType,
                                   final double multiplier) {
        CombatUtils.processCombatXP(mmoPlayer, target, primarySkillType, multiplier);
    }

    /**
     * Start the task that gives combat XP. Processes combat XP like mcMMO normally would, so mcMMO
     * will check whether the entity should reward XP when giving out the XP
     *
     * @param mmoPlayer        The attacking player
     * @param target           The defending entity
     * @param primarySkillType The skill being used
     * @deprecated Draft API
     */
    @Deprecated
    public static void addCombatXP(final McMMOPlayer mmoPlayer, final LivingEntity target,
                                   final PrimarySkillType primarySkillType) {
        CombatUtils.processCombatXP(mmoPlayer, target, primarySkillType);
    }

    /**
     * Returns whether the given skill type string is both valid and not a child skill. (Child
     * skills have no XP of their own, and their level is derived from the parent(s).)
     * </br>
     * This function is designed for API usage.
     *
     * @param skillType the skill to check
     * @return true if this is a valid, non-child mcMMO skill
     */
    public static boolean isNonChildSkill(final String skillType) {
        final PrimarySkillType skill = mcMMO.p.getSkillTools().matchSkill(skillType);

        return skill != null && !SkillTools.isChildSkill(skill);
    }

    @Deprecated
    public static void addRawXP(final Player player, final String skillType, final int XP) {
        addRawXP(player, skillType, (float) XP);
    }

    /**
     * Adds raw XP to the player.
     * </br>
     * This function is designed for API usage.
     *
     * @param player    The player to add XP to
     * @param skillType The skill to add XP to
     * @param XP        The amount of XP to add
     * @throws InvalidSkillException if the given skill is not valid
     */
    @Deprecated
    public static void addRawXP(final Player player, final String skillType, final float XP) {
        addRawXP(player, skillType, XP, "UNKNOWN");
    }

    /**
     * Adds raw XP to the player.
     * </br>
     * This function is designed for API usage.
     *
     * @param player       The player to add XP to
     * @param skillType    The skill to add XP to
     * @param XP           The amount of XP to add
     * @param xpGainReason The reason to gain XP
     * @throws InvalidSkillException        if the given skill is not valid
     * @throws InvalidXPGainReasonException if the given xpGainReason is not valid
     */
    public static void addRawXP(final Player player, final String skillType, final float XP, final String xpGainReason) {
        addRawXP(player, skillType, XP, xpGainReason, false);
    }

    /**
     * Adds raw XP to the player.
     * </br>
     * This function is designed for API usage.
     *
     * @param player       The player to add XP to
     * @param skillType    The skill to add XP to
     * @param XP           The amount of XP to add
     * @param xpGainReason The reason to gain XP
     * @param isUnshared   true if the XP cannot be shared with party members
     * @throws InvalidSkillException        if the given skill is not valid
     * @throws InvalidXPGainReasonException if the given xpGainReason is not valid
     */
    public static void addRawXP(final Player player, final String skillType, final float XP, final String xpGainReason,
                                final boolean isUnshared) {
        if (isUnshared) {
            getPlayer(player).beginUnsharedXpGain(getSkillType(skillType), XP,
                    getXPGainReason(xpGainReason), CUSTOM);
            return;
        }

        getPlayer(player).applyXpGain(getSkillType(skillType), XP, getXPGainReason(xpGainReason),
                CUSTOM);
    }

    /**
     * Adds raw XP to an offline player.
     * </br>
     * This function is designed for API usage.
     *
     * @deprecated We're using float for our XP values now replaced by
     * {@link #addRawXPOffline(String name, String skillType, float XP)}
     */
    @Deprecated
    public static void addRawXPOffline(final String playerName, final String skillType, final int XP) {
        addRawXPOffline(playerName, skillType, (float) XP);
    }

    /**
     * Adds raw XP to an offline player.
     * </br>
     * This function is designed for API usage.
     *
     * @param playerName The player to add XP to
     * @param skillType  The skill to add XP to
     * @param XP         The amount of XP to add
     * @throws InvalidSkillException  if the given skill is not valid
     * @throws InvalidPlayerException if the given player does not exist in the database
     * @deprecated We're using uuids to get an offline player replaced by
     * {@link #addRawXPOffline(UUID uuid, String skillType, float XP)}
     */
    @Deprecated
    public static void addRawXPOffline(final String playerName, final String skillType, final float XP) {
        addOfflineXP(playerName, getSkillType(skillType), (int) Math.floor(XP));
    }

    /**
     * Adds raw XP to an offline player.
     * </br>
     * This function is designed for API usage.
     *
     * @param uuid      The UUID of player to add XP to
     * @param skillType The skill to add XP to
     * @param XP        The amount of XP to add
     * @throws InvalidSkillException  if the given skill is not valid
     * @throws InvalidPlayerException if the given player does not exist in the database
     */
    public static void addRawXPOffline(final UUID uuid, final String skillType, final float XP) {
        addOfflineXP(uuid, getSkillType(skillType), (int) Math.floor(XP));
    }

    /**
     * Adds XP to the player, calculates for XP Rate only.
     * </br>
     * This function is designed for API usage.
     *
     * @param player    The player to add XP to
     * @param skillType The skill to add XP to
     * @param XP        The amount of XP to add
     * @throws InvalidSkillException if the given skill is not valid
     */
    @Deprecated
    public static void addMultipliedXP(final Player player, final String skillType, final int XP) {
        addMultipliedXP(player, skillType, XP, "UNKNOWN");
    }

    /**
     * Adds XP to the player, calculates for XP Rate only.
     * </br>
     * This function is designed for API usage.
     *
     * @param player       The player to add XP to
     * @param skillType    The skill to add XP to
     * @param XP           The amount of XP to add
     * @param xpGainReason The reason to gain XP
     * @throws InvalidSkillException        if the given skill is not valid
     * @throws InvalidXPGainReasonException if the given xpGainReason is not valid
     */
    public static void addMultipliedXP(final Player player, final String skillType, final int XP,
                                       final String xpGainReason) {
        getPlayer(player).applyXpGain(
                getSkillType(skillType),
                (int) (XP * ExperienceConfig.getInstance().getExperienceGainsGlobalMultiplier()),
                getXPGainReason(xpGainReason), CUSTOM);
    }

    /**
     * Adds XP to an offline player, calculates for XP Rate only.
     * </br>
     * This function is designed for API usage.
     *
     * @param playerName The player to add XP to
     * @param skillType  The skill to add XP to
     * @param XP         The amount of XP to add
     * @throws InvalidSkillException  if the given skill is not valid
     * @throws InvalidPlayerException if the given player does not exist in the database
     */
    @Deprecated
    public static void addMultipliedXPOffline(final String playerName, final String skillType, final int XP) {
        addOfflineXP(
                playerName, getSkillType(skillType),
                (int) (XP * ExperienceConfig.getInstance().getExperienceGainsGlobalMultiplier()));
    }

    /**
     * Adds XP to the player, calculates for XP Rate and skill modifier.
     * </br>
     * This function is designed for API usage.
     *
     * @param player    The player to add XP to
     * @param skillType The skill to add XP to
     * @param XP        The amount of XP to add
     * @throws InvalidSkillException if the given skill is not valid
     */
    @Deprecated
    public static void addModifiedXP(final Player player, final String skillType, final int XP) {
        addModifiedXP(player, skillType, XP, "UNKNOWN");
    }

    /**
     * Adds XP to the player, calculates for XP Rate and skill modifier.
     * </br>
     * This function is designed for API usage.
     *
     * @param player       The player to add XP to
     * @param skillType    The skill to add XP to
     * @param XP           The amount of XP to add
     * @param xpGainReason The reason to gain XP
     * @throws InvalidSkillException        if the given skill is not valid
     * @throws InvalidXPGainReasonException if the given xpGainReason is not valid
     */
    public static void addModifiedXP(final Player player, final String skillType, final int XP, final String xpGainReason) {
        addModifiedXP(player, skillType, XP, xpGainReason, false);
    }

    /**
     * Adds XP to the player, calculates for XP Rate and skill modifier.
     * </br>
     * This function is designed for API usage.
     *
     * @param player       The player to add XP to
     * @param skillType    The skill to add XP to
     * @param XP           The amount of XP to add
     * @param xpGainReason The reason to gain XP
     * @param isUnshared   true if the XP cannot be shared with party members
     * @throws InvalidSkillException        if the given skill is not valid
     * @throws InvalidXPGainReasonException if the given xpGainReason is not valid
     */
    public static void addModifiedXP(final Player player, final String skillType, final int XP, final String xpGainReason,
                                     final boolean isUnshared) {
        final PrimarySkillType skill = getSkillType(skillType);

        final ExperienceConfig expConf = ExperienceConfig.getInstance();
        if (isUnshared) {
            getPlayer(player).beginUnsharedXpGain(
                    skill, (int) (XP / expConf.getFormulaSkillModifier(
                            skill) * expConf.getExperienceGainsGlobalMultiplier()),
                    getXPGainReason(xpGainReason), CUSTOM);
            return;
        }

        getPlayer(player).applyXpGain(
                skill, (int) (XP / expConf.getFormulaSkillModifier(
                        skill) * expConf.getExperienceGainsGlobalMultiplier()),
                getXPGainReason(xpGainReason), CUSTOM);
    }

    /**
     * Adds XP to an offline player, calculates for XP Rate and skill modifier.
     * </br>
     * This function is designed for API usage.
     *
     * @param playerName The player to add XP to
     * @param skillType  The skill to add XP to
     * @param XP         The amount of XP to add
     * @throws InvalidSkillException  if the given skill is not valid
     * @throws InvalidPlayerException if the given player does not exist in the database
     */
    @Deprecated
    public static void addModifiedXPOffline(final String playerName, final String skillType, final int XP) {
        final PrimarySkillType skill = getSkillType(skillType);

        addOfflineXP(
                playerName, skill,
                (int) (XP / ExperienceConfig.getInstance().getFormulaSkillModifier(
                        skill) * ExperienceConfig.getInstance()
                        .getExperienceGainsGlobalMultiplier()));
    }

    /**
     * Adds XP to the player, calculates for XP Rate, skill modifiers, perks, child skills, and
     * party sharing.
     * </br>
     * This function is designed for API usage.
     *
     * @param player    The player to add XP to
     * @param skillType The skill to add XP to
     * @param XP        The amount of XP to add
     * @throws InvalidSkillException if the given skill is not valid
     */
    @Deprecated
    public static void addXP(final Player player, final String skillType, final int XP) {
        addXP(player, skillType, XP, "UNKNOWN");
    }

    /**
     * Adds XP to the player, calculates for XP Rate, skill modifiers, perks, child skills, and
     * party sharing.
     * </br>
     * This function is designed for API usage.
     *
     * @param player       The player to add XP to
     * @param skillType    The skill to add XP to
     * @param XP           The amount of XP to add
     * @param xpGainReason The reason to gain XP
     * @throws InvalidSkillException        if the given skill is not valid
     * @throws InvalidXPGainReasonException if the given xpGainReason is not valid
     */
    public static void addXP(final Player player, final String skillType, final int XP, final String xpGainReason) {
        addXP(player, skillType, XP, xpGainReason, false);
    }

    /**
     * Adds XP to the player, calculates for XP Rate, skill modifiers, perks, child skills, and
     * party sharing.
     * </br>
     * This function is designed for API usage.
     *
     * @param player       The player to add XP to
     * @param skillType    The skill to add XP to
     * @param XP           The amount of XP to add
     * @param xpGainReason The reason to gain XP
     * @param isUnshared   true if the XP cannot be shared with party members
     * @throws InvalidSkillException        if the given skill is not valid
     * @throws InvalidXPGainReasonException if the given xpGainReason is not valid
     */
    public static void addXP(final Player player, final String skillType, final int XP, final String xpGainReason,
                             final boolean isUnshared) {
        if (isUnshared) {
            getPlayer(player).beginUnsharedXpGain(getSkillType(skillType), XP,
                    getXPGainReason(xpGainReason), CUSTOM);
            return;
        }

        getPlayer(player).beginXpGain(getSkillType(skillType), XP, getXPGainReason(xpGainReason),
                CUSTOM);
    }

    /**
     * Get the amount of XP a player has in a specific skill.
     * </br>
     * This function is designed for API usage.
     *
     * @param player    The player to get XP for
     * @param skillType The skill to get XP for
     * @return the amount of XP in a given skill
     * @throws InvalidSkillException         if the given skill is not valid
     * @throws UnsupportedOperationException if the given skill is a child skill
     */
    public static int getXP(final Player player, final String skillType) {
        return getPlayer(player).getSkillXpLevel(getNonChildSkillType(skillType));
    }

    /**
     * Get the amount of XP an offline player has in a specific skill.
     * </br>
     * This function is designed for API usage.
     *
     * @param playerName The player to get XP for
     * @param skillType  The skill to get XP for
     * @return the amount of XP in a given skill
     * @throws InvalidSkillException         if the given skill is not valid
     * @throws InvalidPlayerException        if the given player does not exist in the database
     * @throws UnsupportedOperationException if the given skill is a child skill
     */
    @Deprecated
    public static int getOfflineXP(final String playerName, final String skillType) {
        return getOfflineProfile(playerName).getSkillXpLevel(getNonChildSkillType(skillType));
    }

    /**
     * Get the amount of XP an offline player has in a specific skill.
     * </br>
     * This function is designed for API usage.
     *
     * @param uuid      The player to get XP for
     * @param skillType The skill to get XP for
     * @return the amount of XP in a given skill
     * @throws InvalidSkillException         if the given skill is not valid
     * @throws InvalidPlayerException        if the given player does not exist in the database
     * @throws UnsupportedOperationException if the given skill is a child skill
     */
    public static int getOfflineXP(final UUID uuid, final String skillType) {
        return getOfflineProfile(uuid).getSkillXpLevel(getNonChildSkillType(skillType));
    }

    /**
     * Get the amount of XP an offline player has in a specific skill.
     * </br>
     * This function is designed for API usage.
     *
     * @param offlinePlayer The player to get XP for
     * @param skillType     The skill to get XP for
     * @return the amount of XP in a given skill
     * @throws InvalidSkillException         if the given skill is not valid
     * @throws InvalidPlayerException        if the given player does not exist in the database
     * @throws UnsupportedOperationException if the given skill is a child skill
     */
    public static int getOfflineXP(@NotNull final OfflinePlayer offlinePlayer, @NotNull final String skillType)
            throws InvalidPlayerException {
        return getOfflineProfile(offlinePlayer).getSkillXpLevel(getNonChildSkillType(skillType));
    }

    /**
     * Get the raw amount of XP a player has in a specific skill.
     * </br>
     * This function is designed for API usage.
     *
     * @param player    The player to get XP for
     * @param skillType The skill to get XP for
     * @return the amount of XP in a given skill
     * @throws InvalidSkillException         if the given skill is not valid
     * @throws UnsupportedOperationException if the given skill is a child skill
     */
    public static float getXPRaw(final Player player, final String skillType) {
        return getPlayer(player).getSkillXpLevelRaw(getNonChildSkillType(skillType));
    }

    /**
     * Get the raw amount of XP an offline player has in a specific skill.
     * </br>
     * This function is designed for API usage.
     *
     * @param playerName The player to get XP for
     * @param skillType  The skill to get XP for
     * @return the amount of XP in a given skill
     * @throws InvalidSkillException         if the given skill is not valid
     * @throws InvalidPlayerException        if the given player does not exist in the database
     * @throws UnsupportedOperationException if the given skill is a child skill
     */
    @Deprecated
    public static float getOfflineXPRaw(final String playerName, final String skillType) {
        return getOfflineProfile(playerName).getSkillXpLevelRaw(getNonChildSkillType(skillType));
    }

    /**
     * Get the raw amount of XP an offline player has in a specific skill.
     * </br>
     * This function is designed for API usage.
     *
     * @param uuid      The player to get XP for
     * @param skillType The skill to get XP for
     * @return the amount of XP in a given skill
     * @throws InvalidSkillException         if the given skill is not valid
     * @throws InvalidPlayerException        if the given player does not exist in the database
     * @throws UnsupportedOperationException if the given skill is a child skill
     */
    public static float getOfflineXPRaw(final UUID uuid, final String skillType) {
        return getOfflineProfile(uuid).getSkillXpLevelRaw(getNonChildSkillType(skillType));
    }

    /**
     * Get the raw amount of XP an offline player has in a specific skill.
     * </br>
     * This function is designed for API usage.
     *
     * @param offlinePlayer The player to get XP for
     * @param skillType     The skill to get XP for
     * @return the amount of XP in a given skill
     * @throws InvalidSkillException         if the given skill is not valid
     * @throws InvalidPlayerException        if the given player does not exist in the database
     * @throws UnsupportedOperationException if the given skill is a child skill
     */
    public static float getOfflineXPRaw(@NotNull final OfflinePlayer offlinePlayer,
                                        @NotNull final String skillType)
            throws InvalidPlayerException, UnsupportedOperationException, InvalidSkillException {
        return getOfflineProfile(offlinePlayer).getSkillXpLevelRaw(getNonChildSkillType(skillType));
    }

    public static float getOfflineXPRaw(@NotNull final OfflinePlayer offlinePlayer,
                                        @NotNull final PrimarySkillType skillType)
            throws InvalidPlayerException, UnsupportedOperationException {
        if (SkillTools.isChildSkill(skillType)) {
            throw new UnsupportedOperationException();
        }

        return getOfflineProfile(offlinePlayer).getSkillXpLevelRaw(skillType);
    }

    /**
     * Get the total amount of XP needed to reach the next level.
     * </br>
     * This function is designed for API usage.
     *
     * @param player    The player to get the XP amount for
     * @param skillType The skill to get the XP amount for
     * @return the total amount of XP needed to reach the next level
     * @throws InvalidSkillException         if the given skill is not valid
     * @throws UnsupportedOperationException if the given skill is a child skill
     */
    public static int getXPToNextLevel(final Player player, final String skillType) {
        return getPlayer(player).getXpToLevel(getNonChildSkillType(skillType));
    }

    /**
     * Get the total amount of XP an offline player needs to reach the next level.
     * </br>
     * This function is designed for API usage.
     *
     * @param playerName The player to get XP for
     * @param skillType  The skill to get XP for
     * @return the total amount of XP needed to reach the next level
     * @throws InvalidSkillException         if the given skill is not valid
     * @throws InvalidPlayerException        if the given player does not exist in the database
     * @throws UnsupportedOperationException if the given skill is a child skill
     */
    @Deprecated
    public static int getOfflineXPToNextLevel(final String playerName, final String skillType) {
        return getOfflineProfile(playerName).getXpToLevel(getNonChildSkillType(skillType));
    }

    /**
     * Get the total amount of XP an offline player needs to reach the next level.
     * </br>
     * This function is designed for API usage.
     *
     * @param uuid      The player to get XP for
     * @param skillType The skill to get XP for
     * @return the total amount of XP needed to reach the next level
     * @throws InvalidSkillException         if the given skill is not valid
     * @throws InvalidPlayerException        if the given player does not exist in the database
     * @throws UnsupportedOperationException if the given skill is a child skill
     */
    public static int getOfflineXPToNextLevel(@NotNull final UUID uuid, @NotNull final String skillType) {
        return getOfflineProfile(uuid).getXpToLevel(getNonChildSkillType(skillType));
    }

    /**
     * Get the total amount of XP an offline player needs to reach the next level.
     * </br>
     * This function is designed for API usage.
     *
     * @param offlinePlayer The player to get XP for
     * @param skillType     The skill to get XP for
     * @return the total amount of XP needed to reach the next level
     * @throws InvalidSkillException         if the given skill is not valid
     * @throws InvalidPlayerException        if the given player does not exist in the database
     * @throws UnsupportedOperationException if the given skill is a child skill
     */
    public static int getOfflineXPToNextLevel(@NotNull final OfflinePlayer offlinePlayer,
                                              @NotNull final String skillType)
            throws UnsupportedOperationException, InvalidSkillException, InvalidPlayerException {
        return getOfflineProfile(offlinePlayer).getXpToLevel(getNonChildSkillType(skillType));
    }

    /**
     * Get the amount of XP remaining until the next level.
     * </br>
     * This function is designed for API usage.
     *
     * @param player    The player to get the XP amount for
     * @param skillType The skill to get the XP amount for
     * @return the amount of XP remaining until the next level
     * @throws InvalidSkillException         if the given skill is not valid
     * @throws UnsupportedOperationException if the given skill is a child skill
     */
    public static int getXPRemaining(final Player player, final String skillType) {
        final PrimarySkillType skill = getNonChildSkillType(skillType);

        final PlayerProfile profile = getPlayer(player).getProfile();

        return profile.getXpToLevel(skill) - profile.getSkillXpLevel(skill);
    }

    /**
     * Get the amount of XP an offline player has left before leveling up.
     * </br>
     * This function is designed for API usage.
     *
     * @param playerName The player to get XP for
     * @param skillType  The skill to get XP for
     * @return the amount of XP needed to reach the next level
     * @throws InvalidSkillException         if the given skill is not valid
     * @throws InvalidPlayerException        if the given player does not exist in the database
     * @throws UnsupportedOperationException if the given skill is a child skill
     */
    @Deprecated
    public static int getOfflineXPRemaining(final String playerName, final String skillType) {
        final PrimarySkillType skill = getNonChildSkillType(skillType);
        final PlayerProfile profile = getOfflineProfile(playerName);

        return profile.getXpToLevel(skill) - profile.getSkillXpLevel(skill);
    }

    /**
     * Get the amount of XP an offline player has left before leveling up.
     * </br>
     * This function is designed for API usage.
     *
     * @param uuid      The player to get XP for
     * @param skillType The skill to get XP for
     * @return the amount of XP needed to reach the next level
     * @throws InvalidSkillException         if the given skill is not valid
     * @throws InvalidPlayerException        if the given player does not exist in the database
     * @throws UnsupportedOperationException if the given skill is a child skill
     */
    public static float getOfflineXPRemaining(final UUID uuid, final String skillType) {
        final PrimarySkillType skill = getNonChildSkillType(skillType);
        final PlayerProfile profile = getOfflineProfile(uuid);

        return profile.getXpToLevel(skill) - profile.getSkillXpLevelRaw(skill);
    }

    /**
     * Get the amount of XP an offline player has left before leveling up.
     * </br>
     * This function is designed for API usage.
     *
     * @param offlinePlayer The player to get XP for
     * @param skillType     The skill to get XP for
     * @return the amount of XP needed to reach the next level
     * @throws InvalidSkillException         if the given skill is not valid
     * @throws InvalidPlayerException        if the given player does not exist in the database
     * @throws UnsupportedOperationException if the given skill is a child skill
     */
    public static float getOfflineXPRemaining(final OfflinePlayer offlinePlayer, final String skillType)
            throws InvalidSkillException, InvalidPlayerException, UnsupportedOperationException {
        final PrimarySkillType skill = getNonChildSkillType(skillType);
        final PlayerProfile profile = getOfflineProfile(offlinePlayer);

        return profile.getXpToLevel(skill) - profile.getSkillXpLevelRaw(skill);
    }

    /**
     * Add levels to a skill.
     * </br>
     * This function is designed for API usage.
     *
     * @param player    The player to add levels to
     * @param skillType Type of skill to add levels to
     * @param levels    Number of levels to add
     * @throws InvalidSkillException if the given skill is not valid
     */
    public static void addLevel(final Player player, final String skillType, final int levels) {
        getPlayer(player).addLevels(getSkillType(skillType), levels);
    }

    /**
     * Add levels to a skill for an offline player.
     * </br>
     * This function is designed for API usage.
     *
     * @param playerName The player to add levels to
     * @param skillType  Type of skill to add levels to
     * @param levels     Number of levels to add
     * @throws InvalidSkillException  if the given skill is not valid
     * @throws InvalidPlayerException if the given player does not exist in the database
     */
    @Deprecated
    public static void addLevelOffline(final String playerName, final String skillType, final int levels) {
        final PlayerProfile profile = getOfflineProfile(playerName);
        final PrimarySkillType skill = getSkillType(skillType);

        if (SkillTools.isChildSkill(skill)) {
            final var parentSkills = mcMMO.p.getSkillTools().getChildSkillParents(skill);

            for (final PrimarySkillType parentSkill : parentSkills) {
                profile.addLevels(parentSkill, (levels / parentSkills.size()));
            }

            profile.scheduleAsyncSave();
            return;
        }

        profile.addLevels(skill, levels);
        profile.scheduleAsyncSave();
    }

    /**
     * Add levels to a skill for an offline player.
     * </br>
     * This function is designed for API usage.
     *
     * @param uuid      The player to add levels to
     * @param skillType Type of skill to add levels to
     * @param levels    Number of levels to add
     * @throws InvalidSkillException  if the given skill is not valid
     * @throws InvalidPlayerException if the given player does not exist in the database
     */
    public static void addLevelOffline(final UUID uuid, final String skillType, final int levels) {
        final PlayerProfile profile = getOfflineProfile(uuid);
        final PrimarySkillType skill = getSkillType(skillType);

        if (SkillTools.isChildSkill(skill)) {
            final var parentSkills = mcMMO.p.getSkillTools().getChildSkillParents(skill);

            for (final PrimarySkillType parentSkill : parentSkills) {
                profile.addLevels(parentSkill, (levels / parentSkills.size()));
            }

            profile.scheduleAsyncSave();
            return;
        }

        profile.addLevels(skill, levels);
        profile.scheduleAsyncSave();
    }

    /**
     * Get the level a player has in a specific skill.
     * </br>
     * This function is designed for API usage.
     *
     * @param player    The player to get the level for
     * @param skillType The skill to get the level for
     * @return the level of a given skill
     * @throws InvalidSkillException if the given skill is not valid
     * @deprecated Use getLevel(Player player, PrimarySkillType skillType) instead
     */
    @Deprecated
    public static int getLevel(final Player player, final String skillType) {
        return getPlayer(player).getSkillLevel(getSkillType(skillType));
    }

    /**
     * Get the level a player has in a specific skill.
     * </br>
     * This function is designed for API usage.
     *
     * @param player    The player to get the level for
     * @param skillType The skill to get the level for
     * @return the level of a given skill
     * @throws InvalidSkillException if the given skill is not valid
     */
    public static int getLevel(final Player player, final PrimarySkillType skillType) {
        return getPlayer(player).getSkillLevel(skillType);
    }

    /**
     * Get the level an offline player has in a specific skill.
     * </br>
     * This function is designed for API usage.
     *
     * @param playerName The player to get the level for
     * @param skillType  The skill to get the level for
     * @return the level of a given skill
     * @throws InvalidSkillException  if the given skill is not valid
     * @throws InvalidPlayerException if the given player does not exist in the database
     */
    public static int getLevelOffline(final String playerName, final String skillType) {
        return getOfflineProfile(playerName).getSkillLevel(getSkillType(skillType));
    }

    /**
     * Get the level an offline player has in a specific skill.
     * </br>
     * This function is designed for API usage.
     *
     * @param uuid      The player to get the level for
     * @param skillType The skill to get the level for
     * @return the level of a given skill
     * @throws InvalidSkillException  if the given skill is not valid
     * @throws InvalidPlayerException if the given player does not exist in the database
     */
    public static int getLevelOffline(final UUID uuid, final String skillType) {
        return getOfflineProfile(uuid).getSkillLevel(getSkillType(skillType));
    }

    /**
     * Gets the power level of a player.
     * </br>
     * This function is designed for API usage.
     *
     * @param player The player to get the power level for
     * @return the power level of the player
     */
    public static int getPowerLevel(final Player player) {
        return getPlayer(player).getPowerLevel();
    }

    /**
     * Gets the power level of an offline player.
     * </br>
     * This function is designed for API usage.
     *
     * @param playerName The player to get the power level for
     * @return the power level of the player
     * @throws InvalidPlayerException if the given player does not exist in the database
     */
    @Deprecated
    public static int getPowerLevelOffline(final String playerName) {
        int powerLevel = 0;
        final PlayerProfile profile = getOfflineProfile(playerName);

        for (final PrimarySkillType type : SkillTools.NON_CHILD_SKILLS) {
            powerLevel += profile.getSkillLevel(type);
        }

        return powerLevel;
    }

    /**
     * Gets the power level of an offline player.
     * </br>
     * This function is designed for API usage.
     *
     * @param uuid The player to get the power level for
     * @return the power level of the player
     * @throws InvalidPlayerException if the given player does not exist in the database
     */
    public static int getPowerLevelOffline(final UUID uuid) {
        int powerLevel = 0;
        final PlayerProfile profile = getOfflineProfile(uuid);

        for (final PrimarySkillType type : SkillTools.NON_CHILD_SKILLS) {
            powerLevel += profile.getSkillLevel(type);
        }

        return powerLevel;
    }

    /**
     * Get the level cap of a specific skill.
     * </br>
     * This function is designed for API usage.
     *
     * @param skillType The skill to get the level cap for
     * @return the level cap of a given skill
     * @throws InvalidSkillException if the given skill is not valid
     */
    public static int getLevelCap(final String skillType) {
        return mcMMO.p.getSkillTools().getLevelCap(getSkillType(skillType));
    }

    /**
     * Get the power level cap.
     * </br>
     * This function is designed for API usage.
     *
     * @return the overall power level cap
     */
    public static int getPowerLevelCap() {
        return mcMMO.p.getGeneralConfig().getPowerLevelCap();
    }

    /**
     * Get the position on the leaderboard of a player.
     * </br>
     * This function is designed for API usage.
     *
     * @param playerName The name of the player to check
     * @param skillType  The skill to check
     * @return the position on the leaderboard
     * @throws InvalidSkillException         if the given skill is not valid
     * @throws InvalidPlayerException        if the given player does not exist in the database
     * @throws UnsupportedOperationException if the given skill is a child skill
     */
    @Deprecated
    public static int getPlayerRankSkill(final String playerName, final String skillType) {
        return mcMMO.getDatabaseManager()
                .readRank(mcMMO.p.getServer().getOfflinePlayer(playerName).getName()).get(
                        getNonChildSkillType(skillType));
    }

    /**
     * Get the position on the leaderboard of a player.
     * </br>
     * This function is designed for API usage.
     *
     * @param uuid      The name of the player to check
     * @param skillType The skill to check
     * @return the position on the leaderboard
     * @throws InvalidSkillException         if the given skill is not valid
     * @throws InvalidPlayerException        if the given player does not exist in the database
     * @throws UnsupportedOperationException if the given skill is a child skill
     */
    public static int getPlayerRankSkill(final UUID uuid, final String skillType) {
        return mcMMO.getDatabaseManager()
                .readRank(mcMMO.p.getServer().getOfflinePlayer(uuid).getName()).get(
                        getNonChildSkillType(skillType));
    }

    /**
     * Get the position on the power level leaderboard of a player.
     * </br>
     * This function is designed for API usage.
     *
     * @param playerName The name of the player to check
     * @return the position on the power level leaderboard
     * @throws InvalidPlayerException if the given player does not exist in the database
     */
    @Deprecated
    public static int getPlayerRankOverall(final String playerName) {
        return mcMMO.getDatabaseManager()
                .readRank(mcMMO.p.getServer().getOfflinePlayer(playerName).getName()).get(
                        null);
    }

    /**
     * Get the position on the power level leaderboard of a player.
     * </br>
     * This function is designed for API usage.
     *
     * @param uuid The name of the player to check
     * @return the position on the power level leaderboard
     * @throws InvalidPlayerException if the given player does not exist in the database
     */
    public static int getPlayerRankOverall(final UUID uuid) {
        return mcMMO.getDatabaseManager()
                .readRank(mcMMO.p.getServer().getOfflinePlayer(uuid).getName()).get(null);
    }

    /**
     * Sets the level of a player in a specific skill type.
     * </br>
     * This function is designed for API usage.
     *
     * @param player     The player to set the level of
     * @param skillType  The skill to set the level for
     * @param skillLevel The value to set the level to
     * @throws InvalidSkillException if the given skill is not valid
     */
    public static void setLevel(final Player player, final String skillType, final int skillLevel) {
        getPlayer(player).modifySkill(getSkillType(skillType), skillLevel);
    }

    /**
     * Sets the level of an offline player in a specific skill type.
     * </br>
     * This function is designed for API usage.
     *
     * @param playerName The player to set the level of
     * @param skillType  The skill to set the level for
     * @param skillLevel The statVal to set the level to
     * @throws InvalidSkillException  if the given skill is not valid
     * @throws InvalidPlayerException if the given player does not exist in the database
     */
    @Deprecated
    public static void setLevelOffline(final String playerName, final String skillType, final int skillLevel) {
        getOfflineProfile(playerName).modifySkill(getSkillType(skillType), skillLevel);
    }

    /**
     * Sets the level of an offline player in a specific skill type.
     * </br>
     * This function is designed for API usage.
     *
     * @param uuid       The player to set the level of
     * @param skillType  The skill to set the level for
     * @param skillLevel The statVal to set the level to
     * @throws InvalidSkillException  if the given skill is not valid
     * @throws InvalidPlayerException if the given player does not exist in the database
     */
    public static void setLevelOffline(final UUID uuid, final String skillType, final int skillLevel) {
        getOfflineProfile(uuid).modifySkill(getSkillType(skillType), skillLevel);
    }

    /**
     * Sets the XP of a player in a specific skill type.
     * </br>
     * This function is designed for API usage.
     *
     * @param player    The player to set the XP of
     * @param skillType The skill to set the XP for
     * @param newValue  The statVal to set the XP to
     * @throws InvalidSkillException         if the given skill is not valid
     * @throws UnsupportedOperationException if the given skill is a child skill
     */
    public static void setXP(final Player player, final String skillType, final int newValue) {
        getPlayer(player).setSkillXpLevel(getNonChildSkillType(skillType), newValue);
    }

    /**
     * Sets the XP of an offline player in a specific skill type.
     * </br>
     * This function is designed for API usage.
     *
     * @param playerName The player to set the XP of
     * @param skillType  The skill to set the XP for
     * @param newValue   The statVal to set the XP to
     * @throws InvalidSkillException         if the given skill is not valid
     * @throws InvalidPlayerException        if the given player does not exist in the database
     * @throws UnsupportedOperationException if the given skill is a child skill
     */
    @Deprecated
    public static void setXPOffline(final String playerName, final String skillType, final int newValue) {
        getOfflineProfile(playerName).setSkillXpLevel(getNonChildSkillType(skillType), newValue);
    }

    /**
     * Sets the XP of an offline player in a specific skill type.
     * </br>
     * This function is designed for API usage.
     *
     * @param uuid      The player to set the XP of
     * @param skillType The skill to set the XP for
     * @param newValue  The statVal to set the XP to
     * @throws InvalidSkillException         if the given skill is not valid
     * @throws InvalidPlayerException        if the given player does not exist in the database
     * @throws UnsupportedOperationException if the given skill is a child skill
     */
    public static void setXPOffline(final UUID uuid, final String skillType, final int newValue) {
        getOfflineProfile(uuid).setSkillXpLevel(getNonChildSkillType(skillType), newValue);
    }

    /**
     * Removes XP from a player in a specific skill type.
     * </br>
     * This function is designed for API usage.
     *
     * @param player    The player to change the XP of
     * @param skillType The skill to change the XP for
     * @param xp        The amount of XP to remove
     * @throws InvalidSkillException         if the given skill is not valid
     * @throws UnsupportedOperationException if the given skill is a child skill
     */
    public static void removeXP(final Player player, final String skillType, final int xp) {
        getPlayer(player).removeXp(getNonChildSkillType(skillType), xp);
    }

    /**
     * Removes XP from an offline player in a specific skill type.
     * </br>
     * This function is designed for API usage.
     *
     * @param playerName The player to change the XP of
     * @param skillType  The skill to change the XP for
     * @param xp         The amount of XP to remove
     * @throws InvalidSkillException         if the given skill is not valid
     * @throws InvalidPlayerException        if the given player does not exist in the database
     * @throws UnsupportedOperationException if the given skill is a child skill
     */
    @Deprecated
    public static void removeXPOffline(final String playerName, final String skillType, final int xp) {
        getOfflineProfile(playerName).removeXp(getNonChildSkillType(skillType), xp);
    }

    /**
     * Removes XP from an offline player in a specific skill type.
     * </br>
     * This function is designed for API usage.
     *
     * @param uuid      The player to change the XP of
     * @param skillType The skill to change the XP for
     * @param xp        The amount of XP to remove
     * @throws InvalidSkillException         if the given skill is not valid
     * @throws InvalidPlayerException        if the given player does not exist in the database
     * @throws UnsupportedOperationException if the given skill is a child skill
     */
    public static void removeXPOffline(final UUID uuid, final String skillType, final int xp) {
        getOfflineProfile(uuid).removeXp(getNonChildSkillType(skillType), xp);
    }

    /**
     * Check how much XP is needed for a specific level with the selected level curve.
     * </br>
     * This function is designed for API usage.
     *
     * @param level The level to get the amount of XP for
     * @throws InvalidFormulaTypeException if the given formulaType is not valid
     */
    public static int getXpNeededToLevel(final int level) {
        return mcMMO.getFormulaManager()
                .getXPtoNextLevel(level, ExperienceConfig.getInstance().getFormulaType());
    }

    /**
     * Check how much XP is needed for a specific level with the provided level curve.
     * </br>
     * This function is designed for API usage.
     *
     * @param level       The level to get the amount of XP for
     * @param formulaType The formula type to get the amount of XP for
     * @throws InvalidFormulaTypeException if the given formulaType is not valid
     */
    public static int getXpNeededToLevel(final int level, final String formulaType) {
        return mcMMO.getFormulaManager().getXPtoNextLevel(level, getFormulaType(formulaType));
    }

    /**
     * Will add the appropriate type of XP from the block to the player based on the material of the
     * blocks given
     *
     * @param blockStates the blocks to reward XP for
     * @param mmoPlayer   the target player
     */
    public static void addXpFromBlocks(final ArrayList<BlockState> blockStates, final McMMOPlayer mmoPlayer) {
        for (final BlockState bs : blockStates) {
            for (final PrimarySkillType skillType : PrimarySkillType.values()) {
                if (ExperienceConfig.getInstance().getXp(skillType, bs.getType()) > 0) {
                    mmoPlayer.applyXpGain(
                            skillType,
                            ExperienceConfig.getInstance().getXp(skillType, bs.getType()), PVE,
                            SELF);
                }
            }
        }
    }

    /**
     * Will add the appropriate type of XP from the block to the player based on the material of the
     * blocks given if it matches the given skillType
     *
     * @param blockStates the blocks to reward XP for
     * @param mmoPlayer   the target player
     * @param skillType   target primary skill
     */
    public static void addXpFromBlocksBySkill(final ArrayList<BlockState> blockStates,
                                              final McMMOPlayer mmoPlayer,
                                              final PrimarySkillType skillType) {
        for (final BlockState bs : blockStates) {
            if (ExperienceConfig.getInstance().getXp(skillType, bs.getType()) > 0) {
                mmoPlayer.applyXpGain(
                        skillType, ExperienceConfig.getInstance().getXp(skillType, bs.getType()),
                        PVE,
                        SELF);
            }
        }
    }

    /**
     * Will add the appropriate type of XP from the block to the player based on the material of the
     * blocks given
     *
     * @param blockState The target blockstate
     * @param mmoPlayer  The target player
     */
    public static void addXpFromBlock(final BlockState blockState, final McMMOPlayer mmoPlayer) {
        for (final PrimarySkillType skillType : PrimarySkillType.values()) {
            if (ExperienceConfig.getInstance().getXp(skillType, blockState.getType()) > 0) {
                mmoPlayer.applyXpGain(
                        skillType,
                        ExperienceConfig.getInstance().getXp(skillType, blockState.getType()),
                        PVE, SELF);
            }
        }
    }

    /**
     * Will add the appropriate type of XP from the block to the player based on the material of the
     * blocks given if it matches the given skillType
     *
     * @param blockState The target blockstate
     * @param mmoPlayer  The target player
     * @param skillType  target primary skill
     */
    public static void addXpFromBlockBySkill(final BlockState blockState, final McMMOPlayer mmoPlayer,
                                             final PrimarySkillType skillType) {
        if (ExperienceConfig.getInstance().getXp(skillType, blockState.getType()) > 0) {
            mmoPlayer.applyXpGain(
                    skillType,
                    ExperienceConfig.getInstance().getXp(skillType, blockState.getType()), PVE,
                    SELF);
        }
    }

    // Utility methods follow.
    private static void addOfflineXP(@NotNull final UUID playerUniqueId, @NotNull final PrimarySkillType skill,
                                     final int XP) {
        final PlayerProfile profile = getOfflineProfile(playerUniqueId);

        profile.addXp(skill, XP);
        profile.save(true);
    }

    private static void addOfflineXP(@NotNull final String playerName, @NotNull final PrimarySkillType skill,
                                     final int XP) {
        final PlayerProfile profile = getOfflineProfile(playerName);

        profile.addXp(skill, XP);
        profile.scheduleAsyncSave();
    }

    private static @NotNull PlayerProfile getOfflineProfile(@NotNull final UUID uuid)
            throws InvalidPlayerException {
        final PlayerProfile profile = mcMMO.getDatabaseManager().loadPlayerProfile(uuid);

        if (!profile.isLoaded()) {
            throw new InvalidPlayerException();
        }

        return profile;
    }

    private static @NotNull PlayerProfile getOfflineProfile(@NotNull final OfflinePlayer offlinePlayer)
            throws InvalidPlayerException {
        final PlayerProfile profile = mcMMO.getDatabaseManager().loadPlayerProfile(offlinePlayer);

        if (!profile.isLoaded()) {
            throw new InvalidPlayerException();
        }

        return profile;
    }

    private static @NotNull PlayerProfile getOfflineProfile(@NotNull final String playerName)
            throws InvalidPlayerException {
        final PlayerProfile profile = mcMMO.getDatabaseManager().loadPlayerProfile(playerName);

        if (!profile.isLoaded()) {
            throw new InvalidPlayerException();
        }

        return profile;
    }

    private static PrimarySkillType getSkillType(final String skillType) throws InvalidSkillException {
        final PrimarySkillType skill = mcMMO.p.getSkillTools().matchSkill(skillType);

        if (skill == null) {
            throw new InvalidSkillException();
        }

        return skill;
    }

    private static PrimarySkillType getNonChildSkillType(final String skillType)
            throws InvalidSkillException, UnsupportedOperationException {
        final PrimarySkillType skill = getSkillType(skillType);

        if (SkillTools.isChildSkill(skill)) {
            throw new UnsupportedOperationException("Child skills do not have XP");
        }

        return skill;
    }

    private static XPGainReason getXPGainReason(final String reason) throws InvalidXPGainReasonException {
        final XPGainReason xpGainReason = XPGainReason.getXPGainReason(reason);

        if (xpGainReason == null) {
            throw new InvalidXPGainReasonException();
        }

        return xpGainReason;
    }

    private static FormulaType getFormulaType(final String formula) throws InvalidFormulaTypeException {
        final FormulaType formulaType = FormulaType.getFormulaType(formula);

        if (formulaType == null) {
            throw new InvalidFormulaTypeException();
        }

        return formulaType;
    }

    /**
     * @param player target player
     * @return McMMOPlayer for that player if the profile is loaded, otherwise null
     * @throws McMMOPlayerNotFoundException
     * @deprecated Use UserManager::getPlayer(Player player) instead
     */
    @Deprecated(forRemoval = true)
    private static McMMOPlayer getPlayer(final Player player) throws McMMOPlayerNotFoundException {
        if (!UserManager.hasPlayerDataKey(player)) {
            throw new McMMOPlayerNotFoundException(player);
        }

        return UserManager.getPlayer(player);
    }
}
