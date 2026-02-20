package com.gmail.nossr50.util.skills;

import static com.gmail.nossr50.util.ItemMetadataUtils.isLegacyAbilityTool;
import static com.gmail.nossr50.util.ItemMetadataUtils.isSuperAbilityBoosted;
import static com.gmail.nossr50.util.ItemMetadataUtils.removeBonusDigSpeedOnSuperAbilityTool;
import static com.gmail.nossr50.util.PotionEffectUtil.getHastePotionEffectType;
import com.gmail.nossr50.config.HiddenConfig;
import com.gmail.nossr50.datatypes.experience.XPGainReason;
import com.gmail.nossr50.datatypes.experience.XPGainSource;
import com.gmail.nossr50.datatypes.interactions.NotificationType;
import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.datatypes.skills.PrimarySkillType;
import com.gmail.nossr50.datatypes.skills.SubSkillType;
import com.gmail.nossr50.datatypes.skills.SuperAbilityType;
import com.gmail.nossr50.locale.LocaleLoader;
import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.util.ItemMetadataUtils;
import com.gmail.nossr50.util.ItemUtils;
import com.gmail.nossr50.util.Misc;
import com.gmail.nossr50.util.Permissions;
import com.gmail.nossr50.util.player.NotificationManager;
import com.gmail.nossr50.util.player.UserManager;
import com.gmail.nossr50.util.text.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public final class SkillUtils {
    /**
     * This is a static utility class, therefore we don't want any instances of this class. Making
     * the constructor private prevents accidents like that.
     */
    private SkillUtils() {
    }

    public static void applyXpGain(final McMMOPlayer mmoPlayer, final PrimarySkillType skill, final float xp,
                                   final XPGainReason xpGainReason) {
        mmoPlayer.beginXpGain(skill, xp, xpGainReason, XPGainSource.SELF);
    }

    public static void applyXpGain(final McMMOPlayer mmoPlayer, final PrimarySkillType skill, final float xp,
                                   final XPGainReason xpGainReason, final XPGainSource xpGainSource) {
        mmoPlayer.beginXpGain(skill, xp, xpGainReason, xpGainSource);
    }

    /*
     * Skill Stat Calculations
     */

    public static String[] calculateLengthDisplayValues(final Player player, final float skillValue,
                                                        final PrimarySkillType skill) {
        final int maxLength = mcMMO.p.getSkillTools()
                .getSuperAbilityMaxLength(mcMMO.p.getSkillTools().getSuperAbility(skill));
        final int abilityLengthVar = mcMMO.p.getAdvancedConfig().getAbilityLength();
        final int abilityLengthCap = mcMMO.p.getAdvancedConfig().getAbilityLengthCap();

        int length;

        if (abilityLengthCap > 0) {
            length = (int) Math.min(abilityLengthCap, 2 + (skillValue / abilityLengthVar));
        } else {
            length = 2 + (int) (skillValue / abilityLengthVar);
        }

        final int enduranceLength = PerksUtils.handleActivationPerks(player, length, maxLength);

        if (maxLength != 0) {
            length = Math.min(length, maxLength);
        }

        return new String[]{String.valueOf(length), String.valueOf(enduranceLength)};
    }

    /*
     * Others
     */

    public static int handleFoodSkills(final Player player, final int eventFoodLevel,
                                       final SubSkillType subSkillType) {
        final int curRank = RankUtils.getRank(player, subSkillType);

        final int currentFoodLevel = player.getFoodLevel();
        int foodChange = eventFoodLevel - currentFoodLevel;

        foodChange += curRank;

        return currentFoodLevel + foodChange;
    }

    /**
     * Calculate the time remaining until the cooldown expires.
     *
     * @param deactivatedTimeStamp Time of deactivation
     * @param cooldown             The length of the cooldown
     * @param player               The Player to check for cooldown perks
     * @return the number of seconds remaining before the cooldown expires
     */
    public static int calculateTimeLeft(final long deactivatedTimeStamp, final int cooldown, final Player player) {
        return (int) (((deactivatedTimeStamp + (PerksUtils.handleCooldownPerks(player, cooldown)
                * Misc.TIME_CONVERSION_FACTOR)) - System.currentTimeMillis())
                / Misc.TIME_CONVERSION_FACTOR);
    }

    /**
     * Check if the cooldown has expired. This does NOT account for cooldown perks!
     *
     * @param deactivatedTimeStamp Time of deactivation in seconds
     * @param cooldown             The length of the cooldown in seconds
     * @return true if the cooldown is expired
     */
    public static boolean cooldownExpired(final long deactivatedTimeStamp, final int cooldown) {
        return System.currentTimeMillis()
                >= (deactivatedTimeStamp + cooldown) * Misc.TIME_CONVERSION_FACTOR;
    }

    /**
     * Checks if the given string represents a valid skill
     *
     * @param skillName The name of the skill to check
     * @return true if this is a valid skill, false otherwise
     */
    public static boolean isSkill(final String skillName) {
        return mcMMO.p.getGeneralConfig().getLocale().equalsIgnoreCase("en_US") ?
                mcMMO.p.getSkillTools().matchSkill(skillName) != null : isLocalizedSkill(skillName);
    }

    public static void sendSkillMessage(final Player player, final NotificationType notificationType,
                                        final String key) {
        final Location location = player.getLocation();

        for (final Player otherPlayer : player.getWorld().getPlayers()) {
            if (otherPlayer != player && Misc.isNear(location, otherPlayer.getLocation(),
                    Misc.SKILL_MESSAGE_MAX_SENDING_DISTANCE)) {
                NotificationManager.sendNearbyPlayersInformation(otherPlayer, notificationType, key,
                        player.getName());
            }
        }
    }

    public static void handleAbilitySpeedIncrease(final Player player) {
        if (HiddenConfig.getInstance().useEnchantmentBuffs()) {
            final ItemStack heldItem = player.getInventory().getItemInMainHand();

            if (heldItem == null) {
                return;
            }

            if (!ItemUtils.canBeSuperAbilityDigBoosted(heldItem)) {
                return;
            }

            final int originalDigSpeed = heldItem.getEnchantmentLevel(
                    mcMMO.p.getEnchantmentMapper().getEfficiency());
            ItemUtils.addDigSpeedToItem(heldItem,
                    heldItem.getEnchantmentLevel(mcMMO.p.getEnchantmentMapper().getEfficiency()));

            //1.13.2+ will have persistent metadata for this item
            ItemMetadataUtils.setSuperAbilityBoostedItem(heldItem, originalDigSpeed);
        } else {
            int duration = 0;
            int amplifier = 0;

            if (player.hasPotionEffect(getHastePotionEffectType())) {
                for (final PotionEffect effect : player.getActivePotionEffects()) {
                    if (effect.getType() == getHastePotionEffectType()) {
                        duration = effect.getDuration();
                        amplifier = effect.getAmplifier();
                        break;
                    }
                }
            }

            final McMMOPlayer mmoPlayer = UserManager.getPlayer(player);

            //Not Loaded
            if (mmoPlayer == null) {
                return;
            }

            final PrimarySkillType skill = mmoPlayer.getAbilityMode(SuperAbilityType.SUPER_BREAKER)
                    ? PrimarySkillType.MINING : PrimarySkillType.EXCAVATION;

            final int abilityLengthVar = mcMMO.p.getAdvancedConfig().getAbilityLength();
            final int abilityLengthCap = mcMMO.p.getAdvancedConfig().getAbilityLengthCap();

            final int ticks;

            if (abilityLengthCap > 0) {
                ticks = PerksUtils.handleActivationPerks(player, Math.min(abilityLengthCap,
                                2 + (mmoPlayer.getSkillLevel(skill) / abilityLengthVar)),
                        mcMMO.p.getSkillTools().getSuperAbilityMaxLength(
                                mcMMO.p.getSkillTools().getSuperAbility(skill)))
                        * Misc.TICK_CONVERSION_FACTOR;
            } else {
                ticks = PerksUtils.handleActivationPerks(player,
                        2 + ((mmoPlayer.getSkillLevel(skill)) / abilityLengthVar),
                        mcMMO.p.getSkillTools().getSuperAbilityMaxLength(
                                mcMMO.p.getSkillTools().getSuperAbility(skill)))
                        * Misc.TICK_CONVERSION_FACTOR;
            }

            final PotionEffect abilityBuff = new PotionEffect(getHastePotionEffectType(),
                    duration + ticks, amplifier + 10);
            player.addPotionEffect(abilityBuff, true);
        }
    }

    public static void removeAbilityBoostsFromInventory(@NotNull final Player player) {
        for (final ItemStack itemStack : player.getInventory().getContents()) {
            removeAbilityBuff(itemStack);
        }
    }

    public static void removeAbilityBuff(@Nullable final ItemStack itemStack) {
        if (itemStack == null) {
            return;
        }

        if (!ItemUtils.canBeSuperAbilityDigBoosted(itemStack)) {
            return;
        }

        //1.13.2+ will have persistent metadata for this itemStack
        if (isLegacyAbilityTool(itemStack)) {
            final ItemMeta itemMeta = itemStack.getItemMeta();

            if (itemMeta != null) {
                // This is safe to call without prior checks.
                itemMeta.removeEnchant(mcMMO.p.getEnchantmentMapper().getEfficiency());

                itemStack.setItemMeta(itemMeta);
                ItemUtils.removeAbilityLore(itemStack);
            }
        }

        if (isSuperAbilityBoosted(itemStack)) {
            removeBonusDigSpeedOnSuperAbilityTool(itemStack);
        }
    }

    public static void handleDurabilityChange(final ItemStack itemStack, final int durabilityModifier) {
        handleDurabilityChange(itemStack, durabilityModifier, 1.0);
    }

    /**
     * Modify the durability of an ItemStack, using Tools specific formula for unbreaking enchant
     * damage reduction
     *
     * @param itemStack          The ItemStack which durability should be modified
     * @param durabilityModifier the amount to modify the durability by
     * @param maxDamageModifier  the amount to adjust the max damage by
     */
    public static void handleDurabilityChange(final ItemStack itemStack, double durabilityModifier,
                                              final double maxDamageModifier) {
        if (itemStack.hasItemMeta() && itemStack.getItemMeta().isUnbreakable()) {
            return;
        }

        final Material type = itemStack.getType();
        final short maxDurability =
                mcMMO.getRepairableManager().isRepairable(type) ? mcMMO.getRepairableManager()
                        .getRepairable(type).getMaximumDurability() : type.getMaxDurability();
        durabilityModifier = (int) Math.min(durabilityModifier / (
                        itemStack.getEnchantmentLevel(mcMMO.p.getEnchantmentMapper().getUnbreaking()) + 1),
                maxDurability * maxDamageModifier);

        itemStack.setDurability(
                (short) Math.min(itemStack.getDurability() + durabilityModifier, maxDurability));
    }

    private static boolean isLocalizedSkill(final String skillName) {
        for (final PrimarySkillType skill : PrimarySkillType.values()) {
            if (skillName.equalsIgnoreCase(LocaleLoader.getString(
                    StringUtils.getCapitalized(skill.toString()) + ".SkillName"))) {
                return true;
            }
        }

        return false;
    }

    /**
     * Modify the durability of an ItemStack, using Armor specific formula for unbreaking enchant
     * damage reduction
     *
     * @param itemStack          The ItemStack which durability should be modified
     * @param durabilityModifier the amount to modify the durability by
     * @param maxDamageModifier  the amount to adjust the max damage by
     */
    public static void handleArmorDurabilityChange(final ItemStack itemStack, double durabilityModifier,
                                                   final double maxDamageModifier) {
        if (itemStack.hasItemMeta() && itemStack.getItemMeta().isUnbreakable()) {
            return;
        }

        final Material type = itemStack.getType();
        final short maxDurability =
                mcMMO.getRepairableManager().isRepairable(type) ? mcMMO.getRepairableManager()
                        .getRepairable(type).getMaximumDurability() : type.getMaxDurability();
        durabilityModifier = (int) Math.min(durabilityModifier * (0.6 + 0.4 / (
                        itemStack.getEnchantmentLevel(mcMMO.p.getEnchantmentMapper().getUnbreaking()) + 1)),
                maxDurability * maxDamageModifier);

        itemStack.setDurability(
                (short) Math.min(itemStack.getDurability() + durabilityModifier, maxDurability));
    }

    @Nullable
    public static Material getRepairAndSalvageItem(@NotNull final ItemStack inHand) {
        if (ItemUtils.isPrismarineTool(inHand)) {
            return Material.PRISMARINE_CRYSTALS;
        } else if (ItemUtils.isNetheriteTool(inHand) || ItemUtils.isNetheriteArmor(inHand)) {
            return Material.NETHERITE_SCRAP;
        } else if (ItemUtils.isDiamondTool(inHand) || ItemUtils.isDiamondArmor(inHand)) {
            return Material.DIAMOND;
        } else if (ItemUtils.isGoldTool(inHand) || ItemUtils.isGoldArmor(inHand)) {
            return Material.GOLD_INGOT;
        } else if (ItemUtils.isIronTool(inHand) || ItemUtils.isIronArmor(inHand)) {
            return Material.IRON_INGOT;
        } else if (ItemUtils.isStoneTool(inHand)) {
            return Material.COBBLESTONE;
        } else if (ItemUtils.isWoodTool(inHand)) {
            return Material.OAK_WOOD;
        } else if (ItemUtils.isLeatherArmor(inHand)) {
            return Material.LEATHER;
        } else if (ItemUtils.isStringTool(inHand)) {
            return Material.STRING;
        } else {
            return null;
        }
    }

    public static int getRepairAndSalvageQuantities(final ItemStack item) {
        return getRepairAndSalvageQuantities(item.getType(), getRepairAndSalvageItem(item));
    }

    public static int getRepairAndSalvageQuantities(final Material itemMaterial,
                                                    final Material recipeMaterial) {
        int quantity = 0;

        if (mcMMO.getMaterialMapStore().isPrismarineTool(itemMaterial)) {
            return 16;
        }

        if (mcMMO.getMaterialMapStore().isNetheriteTool(itemMaterial)
                || mcMMO.getMaterialMapStore().isNetheriteArmor(itemMaterial)) {
            //One netherite bar requires 4 netherite scraps
            return 4;
        }

        final ItemStack recipeItem = recipeMaterial != null ? new ItemStack(recipeMaterial) : null;

        for (final Iterator<? extends Recipe> recipeIterator = Bukkit.getServer().recipeIterator();
             recipeIterator.hasNext(); ) {
            final Recipe bukkitRecipe = recipeIterator.next();

            if (bukkitRecipe.getResult().getType() != itemMaterial) {
                continue;
            }

            if (bukkitRecipe instanceof ShapelessRecipe shapelessRecipe) {
                for (RecipeChoice ingredient : shapelessRecipe.getChoiceList()) {
                    if (ingredient != null && recipeItem != null && ingredient.test(recipeItem)) {
                        quantity += 1;
                    }
                }
            } else if (bukkitRecipe instanceof ShapedRecipe shapedRecipe) {
                for (RecipeChoice ingredient : shapedRecipe.getChoiceMap()
                        .values()) {
                    if (ingredient != null && recipeItem != null && ingredient.test(recipeItem)) {
                        quantity += 1;
                    }
                }
            }
        }

        return quantity;
    }

    /**
     * Checks if a player can use a skill
     *
     * @param player       target player
     * @param subSkillType target subskill
     * @return true if the player has permission and has the skill unlocked
     */
    public static boolean canUseSubskill(final Player player, @NotNull final SubSkillType subSkillType) {
        return Permissions.isSubSkillEnabled(player, subSkillType) && RankUtils.hasUnlockedSubskill(
                player, subSkillType);
    }
}
