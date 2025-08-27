package com.gmail.nossr50.util;

import com.gmail.nossr50.api.ItemSpawnReason;
import com.gmail.nossr50.config.experience.ExperienceConfig;
import com.gmail.nossr50.config.party.ItemWeightConfig;
import com.gmail.nossr50.datatypes.treasure.EnchantmentWrapper;
import com.gmail.nossr50.datatypes.treasure.FishingTreasureBook;
import com.gmail.nossr50.events.items.McMMOItemSpawnEvent;
import com.gmail.nossr50.locale.LocaleLoader;
import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.skills.smelting.Smelting;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

public final class ItemUtils {
    // Reflection for setItemName only available in newer APIs
    private static final Method setItemName;

    static {
        setItemName = getSetItemName();
    }

    private ItemUtils() {
        // private constructor
    }

    private static Method getSetItemName() {
        try {
            return ItemMeta.class.getMethod("setItemName", String.class);
        } catch (final NoSuchMethodException e) {
            return null;
        }
    }

    /**
     * Sets the item name using the new API if available or falls back to the old API.
     *
     * @param itemMeta The item meta to set the name on
     * @param name     The name to set
     */
    public static void setItemName(final ItemMeta itemMeta, final String name) {
        if (setItemName != null) {
            setItemNameModern(itemMeta, name);
        } else {
            itemMeta.setDisplayName(ChatColor.RESET + name);
        }
    }

    private static void setItemNameModern(final ItemMeta itemMeta, final String name) {
        try {
            setItemName.invoke(itemMeta, name);
        } catch (final IllegalAccessException | InvocationTargetException e) {
            mcMMO.p.getLogger().severe("Failed to set item name: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Checks if the item is a bow.
     *
     * @param item Item to check
     * @return true if the item is a bow, false otherwise
     */
    // TODO: Unit tests
    public static boolean isBow(@NotNull final ItemStack item) {
        return mcMMO.getMaterialMapStore().isBow(item.getType().getKey().getKey());
    }

    /**
     * Exhaustive lookup for a Material by name.
     * <p>
     * This method will first try a normal lookup, then a legacy lookup, then a lookup by ENUM name,
     * and finally a lookup by ENUM name with legacy name.
     *
     * @param materialName The name of the material to lookup
     * @return The Material if found, or null if not found
     */
    public static @Nullable Material exhaustiveMaterialLookup(@NotNull final String materialName) {
        requireNonNull(materialName, "materialName cannot be null");

        // First try a normal lookup
        Material material = Material.matchMaterial(materialName);

        // If that fails, try a legacy lookup
        if (material == null) {
            material = Material.matchMaterial(materialName, true);
        }

        // try to match to Material ENUM
        if (material == null) {
            material = Material.getMaterial(materialName.toUpperCase(Locale.ENGLISH));
        }

        // try to match to Material ENUM with legacy name
        if (material == null) {
            material = Material.getMaterial(materialName.toUpperCase(Locale.ENGLISH), true);
        }
        return material;
    }

    /**
     * Checks if a player has an item in their inventory or offhand.
     *
     * @param player   Player to check
     * @param material Material to check for
     * @return true if the player has the item in their inventory or offhand, false otherwise
     */
    public static boolean hasItemIncludingOffHand(final Player player, final Material material) {
        // Checks main inventory / item bar
        final boolean containsInMain = player.getInventory().contains(material);

        if (containsInMain) {
            return true;
        }

        return player.getInventory().getItemInOffHand().getType() == material;
    }

    /**
     * Removes an item from a player's inventory, including their offhand.
     *
     * @param player   Player to remove the item from
     * @param material Material to remove
     * @param amount   Amount of the material to remove
     */
    public static void removeItemIncludingOffHand(@NotNull final Player player,
                                                  @NotNull final Material material, final int amount) {
        // Checks main inventory / item bar
        if (player.getInventory().contains(material)) {
            player.getInventory().removeItem(new ItemStack(material, amount));
            return;
        }

        // Check off-hand
        final ItemStack offHandItem = player.getInventory().getItemInOffHand();
        if (offHandItem.getType() == material) {
            final int newAmount = offHandItem.getAmount() - amount;
            if (newAmount > 0) {
                offHandItem.setAmount(newAmount);
            } else {
                player.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
            }
        }
    }

    // TODO: Unit tests
    public static boolean isCrossbow(@NotNull final ItemStack item) {
        return mcMMO.getMaterialMapStore().isCrossbow(item.getType().getKey().getKey());
    }

    // TODO: Unit tests
    public static boolean isTrident(@NotNull final ItemStack item) {
        return mcMMO.getMaterialMapStore().isTrident(item.getType().getKey().getKey());
    }

    public static boolean isMace(@NotNull final ItemStack item) {
        return mcMMO.getMaterialMapStore().isMace(item.getType().getKey().getKey());
    }

    public static boolean hasItemInEitherHand(@NotNull final Player player, final Material material) {
        return player.getInventory().getItemInMainHand().getType() == material
                || player.getInventory().getItemInOffHand().getType() == material;
    }

    public static boolean doesPlayerHaveEnchantmentOnArmor(@NotNull final Player player,
                                                           @NotNull final String enchantmentByName) {
        final Enchantment enchantment = getEnchantment(enchantmentByName);

        if (enchantment == null) {
            return false;
        }

        return doesPlayerHaveEnchantmentOnArmor(player, enchantment);
    }

    public static boolean doesPlayerHaveEnchantmentOnArmor(@NotNull final Player player,
                                                           @NotNull final Enchantment enchantment) {
        for (final ItemStack itemStack : player.getInventory().getArmorContents()) {
            if (itemStack != null) {
                if (hasEnchantment(itemStack, enchantment)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean doesPlayerHaveEnchantmentOnArmorOrHands(@NotNull final Player player,
                                                                  @NotNull final String enchantmentName) {
        final Enchantment enchantment = getEnchantment(enchantmentName);

        if (enchantment == null) {
            return false;
        }

        return doesPlayerHaveEnchantmentOnArmorOrHands(player, enchantment);
    }

    public static boolean doesPlayerHaveEnchantmentOnArmorOrHands(@NotNull final Player player,
                                                                  @NotNull final Enchantment enchantment) {
        if (doesPlayerHaveEnchantmentOnArmor(player, enchantment)) {
            return true;
        }

        return doesPlayerHaveEnchantmentInHands(player, enchantment);
    }

    public static boolean doesPlayerHaveEnchantmentInHands(@NotNull final Player player,
                                                           @NotNull final NamespacedKey enchantmentNameKey) {
        final Enchantment enchantment = Enchantment.getByKey(enchantmentNameKey);

        if (enchantment == null) {
            return false;
        }

        return doesPlayerHaveEnchantmentInHands(player, enchantment);
    }

    public static boolean doesPlayerHaveEnchantmentInHands(@NotNull final Player player,
                                                           @NotNull final String enchantmentName) {
        final Enchantment enchantment = getEnchantment(enchantmentName);

        if (enchantment == null) {
            return false;
        }

        return doesPlayerHaveEnchantmentInHands(player, enchantment);
    }

    public static boolean doesPlayerHaveEnchantmentInHands(@NotNull final Player player,
                                                           @NotNull final Enchantment enchantment) {
        return hasEnchantment(player.getInventory().getItemInMainHand(), enchantment) ||
                hasEnchantment(player.getInventory().getItemInOffHand(), enchantment);
    }

    public static boolean hasEnchantment(@NotNull final ItemStack itemStack,
                                         @NotNull final Enchantment enchantment) {
        if (itemStack.getItemMeta() != null) {
            return itemStack.getItemMeta().hasEnchant(enchantment);
        }

        return false;
    }

    public static @Nullable Enchantment getEnchantment(@NotNull final String enchantmentName) {
        for (final Enchantment enchantment : Enchantment.values()) {
            if (enchantment.getKey().getKey().equalsIgnoreCase(enchantmentName)) {
                return enchantment;
            }
        }

        return null;
    }

    /**
     * Checks if the item is a sword.
     *
     * @param item Item to check
     * @return true if the item is a sword, false otherwise
     */
    public static boolean isSword(@NotNull final ItemStack item) {
        return mcMMO.getMaterialMapStore().isSword(item.getType().getKey().getKey());
    }

    /**
     * Checks if the item is a hoe.
     *
     * @param item Item to check
     * @return true if the item is a hoe, false otherwise
     */
    public static boolean isHoe(@NotNull final ItemStack item) {
        return mcMMO.getMaterialMapStore().isHoe(item.getType().getKey().getKey());
    }

    /**
     * Checks if the item is a shovel.
     *
     * @param item Item to check
     * @return true if the item is a shovel, false otherwise
     */
    public static boolean isShovel(@NotNull final ItemStack item) {
        return mcMMO.getMaterialMapStore().isShovel(item.getType().getKey().getKey());
    }

    /**
     * Checks if the item is an axe.
     *
     * @param item Item to check
     * @return true if the item is an axe, false otherwise
     */
    public static boolean isAxe(@NotNull final ItemStack item) {
        return mcMMO.getMaterialMapStore().isAxe(item.getType().getKey().getKey());
    }

    /**
     * Checks if the item is a pickaxe.
     *
     * @param item Item to check
     * @return true if the item is a pickaxe, false otherwise
     */
    public static boolean isPickaxe(@NotNull final ItemStack item) {
        return mcMMO.getMaterialMapStore().isPickAxe(item.getType().getKey().getKey());
    }

    /**
     * Checks if the item counts as unarmed.
     *
     * @param item Item to check
     * @return true if the item counts as unarmed, false otherwise
     */
    public static boolean isUnarmed(final ItemStack item) {
        if (mcMMO.p.getGeneralConfig().getUnarmedItemsAsUnarmed()) {
            return !isMinecraftTool(item);
        }

        return item.getType() == Material.AIR;
    }

    /**
     * Checks to see if an item is a wearable armor piece.
     *
     * @param item Item to check
     * @return true if the item is armor, false otherwise
     */
    public static boolean isArmor(final ItemStack item) {
        return mcMMO.getMaterialMapStore().isArmor(item.getType());
    }

    /**
     * Checks to see if an item is a leather armor piece.
     *
     * @param item Item to check
     * @return true if the item is leather armor, false otherwise
     */
    public static boolean isLeatherArmor(final ItemStack item) {
        return mcMMO.getMaterialMapStore().isLeatherArmor(item.getType());
    }

    /**
     * Checks to see if an item is a gold armor piece.
     *
     * @param item Item to check
     * @return true if the item is gold armor, false otherwise
     */
    public static boolean isGoldArmor(final ItemStack item) {
        return mcMMO.getMaterialMapStore().isGoldArmor(item.getType().getKey().getKey());
    }

    /**
     * Checks to see if an item is an iron armor piece.
     *
     * @param item Item to check
     * @return true if the item is iron armor, false otherwise
     */
    public static boolean isIronArmor(final ItemStack item) {
        return mcMMO.getMaterialMapStore().isIronArmor(item.getType().getKey().getKey());
    }

    /**
     * Checks to see if an item is a diamond armor piece.
     *
     * @param item Item to check
     * @return true if the item is diamond armor, false otherwise
     */
    public static boolean isDiamondArmor(final ItemStack item) {
        return mcMMO.getMaterialMapStore().isDiamondArmor(item.getType().getKey().getKey());
    }

    public static boolean isNetheriteArmor(final ItemStack itemStack) {
        return mcMMO.getMaterialMapStore().isNetheriteArmor(itemStack.getType().getKey().getKey());
    }

    public static boolean isNetheriteTool(final ItemStack itemStack) {
        return mcMMO.getMaterialMapStore().isNetheriteTool(itemStack.getType().getKey().getKey());
    }

    /**
     * Checks to see if an item is a chainmail armor piece.
     *
     * @param item Item to check
     * @return true if the item is chainmail armor, false otherwise
     */
    public static boolean isChainmailArmor(final ItemStack item) {
        return mcMMO.getMaterialMapStore().isChainmailArmor(item.getType().getKey().getKey());
    }

    /**
     * Checks to see if an item is a *vanilla* tool.
     *
     * @param item Item to check
     * @return true if the item is a tool, false otherwise
     */
    public static boolean isMinecraftTool(final ItemStack item) {
        return mcMMO.getMaterialMapStore().isTool(item.getType().getKey().getKey());
    }

    /**
     * Checks to see if an item is a stone tool.
     *
     * @param item Item to check
     * @return true if the item is a stone tool, false otherwise
     */
    public static boolean isStoneTool(final ItemStack item) {
        return mcMMO.getMaterialMapStore().isStoneTool(item.getType().getKey().getKey());
    }

    /**
     * Checks to see if an item is a wooden tool.
     *
     * @param item Item to check
     * @return true if the item is a wooden tool, false otherwise
     */
    public static boolean isWoodTool(final ItemStack item) {
        return mcMMO.getMaterialMapStore().isWoodTool(item.getType().getKey().getKey());
    }

    /**
     * Checks to see if an item is a string tool.
     *
     * @param item Item to check
     * @return true if the item is a string tool, false otherwise
     */
    public static boolean isStringTool(final ItemStack item) {
        return mcMMO.getMaterialMapStore().isStringTool(item.getType().getKey().getKey());
    }

    public static boolean isPrismarineTool(final ItemStack item) {
        return mcMMO.getMaterialMapStore().isPrismarineTool(item.getType().getKey().getKey());
    }

    /**
     * Checks to see if an item is a gold tool.
     *
     * @param item Item to check
     * @return true if the item is a stone tool, false otherwise
     */
    public static boolean isGoldTool(final ItemStack item) {
        return mcMMO.getMaterialMapStore().isGoldTool(item.getType().getKey().getKey());
    }

    /**
     * Checks to see if an item is an iron tool.
     *
     * @param item Item to check
     * @return true if the item is an iron tool, false otherwise
     */
    public static boolean isIronTool(final ItemStack item) {
        return mcMMO.getMaterialMapStore().isIronTool(item.getType().getKey().getKey());
    }

    /**
     * Checks to see if an item is a diamond tool.
     *
     * @param item Item to check
     * @return true if the item is a diamond tool, false otherwise
     */
    public static boolean isDiamondTool(final ItemStack item) {
        return mcMMO.getMaterialMapStore().isDiamondTool(item.getType().getKey().getKey());
    }

    /**
     * Checks to see if an item is enchantable.
     *
     * @param item Item to check
     * @return true if the item is enchantable, false otherwise
     */
    public static boolean isEnchantable(final ItemStack item) {
        return mcMMO.getMaterialMapStore().isEnchantable(item.getType().getKey().getKey());
    }

    public static boolean isSmeltable(final ItemStack item) {
        return item != null && Smelting.getSmeltXP(item) >= 1;
    }

    public static boolean isSmelted(final ItemStack item) {
        if (item == null) {
            return false;
        }

        for (final Recipe recipe : mcMMO.p.getServer().getRecipesFor(item)) {
            if (recipe instanceof FurnaceRecipe
                    && ((FurnaceRecipe) recipe).getInput().getType().isBlock()
                    && MaterialUtils.isOre(((FurnaceRecipe) recipe).getInput().getType())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if an item is sharable.
     *
     * @param item Item that will get shared
     * @return True if the item can be shared.
     */
    public static boolean isSharable(final ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }

        return isMiningDrop(item)
                || isWoodcuttingDrop(item)
                || isMobDrop(item)
                || isHerbalismDrop(item)
                || isMiscDrop(item);
    }

    /**
     * Checks to see if an item is a mining drop.
     *
     * @param item Item to check
     * @return true if the item is a mining drop, false otherwise
     */
    public static boolean isMiningDrop(final ItemStack item) {
        //TODO: 1.14 This needs to be updated
        return switch (item.getType()) { // Should we also have Glowing Redstone Ore here?
            // Should we also have Glowstone here?
            case COAL, COAL_ORE, DIAMOND, DIAMOND_ORE, EMERALD, EMERALD_ORE, GOLD_ORE, IRON_ORE,
                 LAPIS_ORE,
                 REDSTONE_ORE, REDSTONE, GLOWSTONE_DUST, QUARTZ, NETHER_QUARTZ_ORE, LAPIS_LAZULI -> true;
            default -> false;
        };
    }

    /**
     * Checks to see if an item is a herbalism drop.
     *
     * @param item Item to check
     * @return true if the item is a herbalism drop, false otherwise
     */
    public static boolean isHerbalismDrop(final ItemStack item) {
        //TODO: 1.14 This needs to be updated
        return switch (item.getType().getKey().getKey().toLowerCase()) {
            case "wheat", "wheat_seeds", "carrot", "chorus_fruit", "chorus_flower", "potato",
                 "beetroot", "beetroots",
                 "beetroot_seeds", "nether_wart", "brown_mushroom", "red_mushroom", "rose_bush",
                 "dandelion", "cactus",
                 "sugar_cane", "melon", "melon_seeds", "pumpkin", "pumpkin_seeds", "lily_pad",
                 "vine", "tall_grass",
                 "cocoa_beans" -> true;
            default -> false;
        };
    }


    /**
     * Checks to see if an item is a mob drop.
     *
     * @param item Item to check
     * @return true if the item is a mob drop, false otherwise
     */
    public static boolean isMobDrop(final ItemStack item) {
        //TODO: 1.14 This needs to be updated
        return switch (item.getType()) {
            case STRING, FEATHER, CHICKEN, COOKED_CHICKEN, LEATHER, BEEF, COOKED_BEEF, PORKCHOP,
                 COOKED_PORKCHOP,
                 WHITE_WOOL, BLACK_WOOL, BLUE_WOOL, BROWN_WOOL, CYAN_WOOL, GRAY_WOOL, GREEN_WOOL,
                 LIGHT_BLUE_WOOL,
                 LIGHT_GRAY_WOOL, LIME_WOOL, MAGENTA_WOOL, ORANGE_WOOL, PINK_WOOL, PURPLE_WOOL,
                 RED_WOOL, YELLOW_WOOL,
                 IRON_INGOT, SNOWBALL, BLAZE_ROD, SPIDER_EYE, GUNPOWDER, ENDER_PEARL, GHAST_TEAR,
                 MAGMA_CREAM, BONE,
                 ARROW, SLIME_BALL, NETHER_STAR, ROTTEN_FLESH, GOLD_NUGGET, EGG, ROSE_BUSH, COAL -> true;
            default -> false;
        };
    }

    /**
     * Checks to see if an item is a woodcutting drop.
     *
     * @param item Item to check
     * @return true if the item is a woodcutting drop, false otherwise
     */
    public static boolean isWoodcuttingDrop(final ItemStack item) {
        return switch (item.getType().toString()) {
            case "ACACIA_LOG", "BIRCH_LOG", "DARK_OAK_LOG", "PALE_OAK_LOG", "JUNGLE_LOG", "OAK_LOG",
                 "SPRUCE_LOG",
                 "STRIPPED_ACACIA_LOG", "STRIPPED_BIRCH_LOG", "STRIPPED_DARK_OAK_LOG",
                 "STRIPPED_PALE_OAK_LOG",
                 "STRIPPED_JUNGLE_LOG", "STRIPPED_OAK_LOG", "STRIPPED_SPRUCE_LOG",
                 "STRIPPED_MANGROVE_LOG",
                 "ACACIA_SAPLING", "SPRUCE_SAPLING", "BIRCH_SAPLING", "DARK_OAK_SAPLING",
                 "PALE_OAK_SAPLING",
                 "JUNGLE_SAPLING", "OAK_SAPLING", "ACACIA_LEAVES", "BIRCH_LEAVES",
                 "DARK_OAK_LEAVES", "PALE_OAK_LEAVES",
                 "JUNGLE_LEAVES", "OAK_LEAVES", "SPRUCE_LEAVES", "BEE_NEST", "APPLE" -> true;
            default -> false;
        };
    }

    /**
     * Checks to see if an item is a miscellaneous drop. These items are read from the config file
     *
     * @param item Item to check
     * @return true if the item is a miscellaneous drop, false otherwise
     */
    public static boolean isMiscDrop(final ItemStack item) {
        return ItemWeightConfig.getInstance().getMiscItems().contains(item.getType());
    }

    // TODO: This is used exclusively for Chimaera Wing... should revisit this sometime
    public static boolean isMcMMOItem(final ItemStack item) {
        if (!item.hasItemMeta()) {
            return false;
        }

        final ItemMeta itemMeta = item.getItemMeta();

        if (itemMeta == null) {
            return false;
        }

        return itemMeta.getLore() != null
                && itemMeta.getLore().contains("mcMMO Item");
    }

    public static boolean isChimaeraWing(final ItemStack item) {
        if (!isMcMMOItem(item)) {
            return false;
        }

        final ItemMeta itemMeta = item.getItemMeta();

        if (itemMeta == null) {
            return false;
        }

        return itemMeta.hasDisplayName() && itemMeta.getDisplayName()
                .equals(ChatColor.GOLD + LocaleLoader.getString("Item.ChimaeraWing.Name"));
    }

    public static void removeAbilityLore(@NotNull final ItemStack itemStack) {
        final ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta == null) {
            return;
        }

        if (itemMeta.hasLore()) {
            final List<String> itemLore = itemMeta.getLore();

            if (itemLore == null) {
                return;
            }

            if (itemLore.remove("mcMMO Ability Tool")) {
                itemMeta.setLore(itemLore);
                itemStack.setItemMeta(itemMeta);
            }
        }
    }

    public static void addDigSpeedToItem(@NotNull final ItemStack itemStack,
                                         final int existingEnchantLevel) {
        final ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta == null) {
            return;
        }

        itemMeta.addEnchant(mcMMO.p.getEnchantmentMapper().getEfficiency(),
                existingEnchantLevel + mcMMO.p.getAdvancedConfig().getEnchantBuff(), true);
        itemStack.setItemMeta(itemMeta);
    }

    public static boolean canBeSuperAbilityDigBoosted(@NotNull final ItemStack itemStack) {
        return isShovel(itemStack) || isPickaxe(itemStack);
    }

    public static @NotNull ItemStack createEnchantBook(
            @NotNull final FishingTreasureBook fishingTreasureBook) {
        final ItemStack itemStack = fishingTreasureBook.getDrop().clone();
        final EnchantmentWrapper enchantmentWrapper = getRandomEnchantment(
                fishingTreasureBook.getLegalEnchantments());
        final ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta == null) {
            return itemStack;
        }

        final EnchantmentStorageMeta enchantmentStorageMeta = (EnchantmentStorageMeta) itemMeta;
        enchantmentStorageMeta.addStoredEnchant(
                enchantmentWrapper.enchantment(),
                enchantmentWrapper.enchantmentLevel(),
                ExperienceConfig.getInstance().allowUnsafeEnchantments());
        itemStack.setItemMeta(enchantmentStorageMeta);
        return itemStack;
    }

    public static @NotNull EnchantmentWrapper getRandomEnchantment(
            @NotNull final List<EnchantmentWrapper> enchantmentWrappers) {
        Collections.shuffle(enchantmentWrappers, Misc.getRandom());

        final int randomIndex = Misc.getRandom().nextInt(enchantmentWrappers.size());
        return enchantmentWrappers.get(randomIndex);
    }

    /**
     * Drop items at a given location.
     *
     * @param location   The location to drop the items at
     * @param itemStacks The items to drop
     */
    public static void spawnItems(@Nullable final Player player,
                                  @NotNull final Location location,
                                  @NotNull final Collection<ItemStack> itemStacks,
                                  @NotNull final ItemSpawnReason itemSpawnReason) {
        for (final ItemStack is : itemStacks) {
            spawnItem(player, location, is, itemSpawnReason);
        }
    }

    /**
     * Drop items at a given location.
     *
     * @param player          player to drop the items for
     * @param location        The location to drop the items at
     * @param itemStacks      The items to drop
     * @param blackList       The items to skip
     * @param itemSpawnReason the reason for the item drop
     */
    public static void spawnItems(@Nullable final Player player,
                                  @NotNull final Location location,
                                  @NotNull final Collection<ItemStack> itemStacks,
                                  @NotNull final Collection<Material> blackList,
                                  @NotNull final ItemSpawnReason itemSpawnReason) {
        for (final ItemStack is : itemStacks) {
            // Skip blacklisted items
            if (blackList.contains(is.getType())) {
                continue;
            }
            spawnItem(player, location, is, itemSpawnReason);
        }
    }

    /**
     * Drop items at a given location.
     *
     * @param location The location to drop the items at
     * @param is       The items to drop
     * @param quantity The amount of items to drop
     */
    public static void spawnItems(@Nullable final Player player,
                                  @NotNull final Location location,
                                  @NotNull final ItemStack is,
                                  final int quantity,
                                  @NotNull final ItemSpawnReason itemSpawnReason) {
        for (int i = 0; i < quantity; i++) {
            spawnItem(player, location, is, itemSpawnReason);
        }
    }

    /**
     * Drop an item at a given location.
     *
     * @param location        The location to drop the item at
     * @param itemStack       The item to drop
     * @param itemSpawnReason the reason for the item drop
     * @return Dropped Item entity or null if invalid or cancelled
     */
    public static @Nullable Item spawnItem(@Nullable final Player player,
                                           @NotNull final Location location,
                                           @NotNull final ItemStack itemStack,
                                           @NotNull final ItemSpawnReason itemSpawnReason) {
        if (itemStack.getType() == Material.AIR || location.getWorld() == null) {
            return null;
        }

        // We can't get the item until we spawn it and we want to make it cancellable, so we have a custom event.
        final McMMOItemSpawnEvent event = new McMMOItemSpawnEvent(location, itemStack,
                itemSpawnReason, player);
        mcMMO.p.getServer().getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return null;
        }

        return location.getWorld().dropItem(location, event.getItemStack());
    }

    /**
     * Drop an item at a given location.
     *
     * @param location        The location to drop the item at
     * @param itemStack       The item to drop
     * @param itemSpawnReason the reason for the item drop
     * @return Dropped Item entity or null if invalid or cancelled
     */
    public static @Nullable Item spawnItemNaturally(@Nullable final Player player,
                                                    @NotNull final Location location,
                                                    @NotNull final ItemStack itemStack,
                                                    @NotNull final ItemSpawnReason itemSpawnReason) {
        if (itemStack.getType() == Material.AIR || location.getWorld() == null) {
            return null;
        }

        // We can't get the item until we spawn it and we want to make it cancellable, so we have a custom event.
        final McMMOItemSpawnEvent event = new McMMOItemSpawnEvent(location, itemStack,
                itemSpawnReason, player);
        mcMMO.p.getServer().getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return null;
        }

        return location.getWorld().dropItemNaturally(location, event.getItemStack());
    }

    /**
     * Drop items at a given location.
     *
     * @param fromLocation The location to drop the items at
     * @param is           The items to drop
     * @param speed        the speed that the item should travel
     * @param quantity     The amount of items to drop
     */
    public static void spawnItemsTowardsLocation(@Nullable final Player player,
                                                 @NotNull final Location fromLocation,
                                                 @NotNull final Location toLocation,
                                                 @NotNull final ItemStack is,
                                                 final int quantity,
                                                 final double speed,
                                                 @NotNull final ItemSpawnReason itemSpawnReason) {
        for (int i = 0; i < quantity; i++) {
            spawnItemTowardsLocation(player, fromLocation, toLocation, is, speed, itemSpawnReason);
        }
    }

    /**
     * Drop an item at a given location. This method is fairly expensive as it creates clones of
     * everything passed to itself since they are mutable objects
     *
     * @param fromLocation The location to drop the item at
     * @param toLocation   The location the item will travel towards
     * @param itemToSpawn  The item to spawn
     * @param speed        the speed that the item should travel
     * @return Dropped Item entity or null if invalid or cancelled
     */
    public static @Nullable Item spawnItemTowardsLocation(@Nullable final Player player,
                                                          @NotNull final Location fromLocation,
                                                          @NotNull final Location toLocation,
                                                          @NotNull final ItemStack itemToSpawn,
                                                          final double speed,
                                                          @NotNull final ItemSpawnReason itemSpawnReason) {
        if (itemToSpawn.getType() == Material.AIR) {
            return null;
        }

        //Work with fresh copies of everything
        ItemStack clonedItem = itemToSpawn.clone();
        final Location spawnLocation = fromLocation.clone();
        final Location targetLocation = toLocation.clone();

        if (spawnLocation.getWorld() == null) {
            return null;
        }

        // We can't get the item until we spawn it and we want to make it cancellable, so we have a custom event.
        final McMMOItemSpawnEvent event = new McMMOItemSpawnEvent(spawnLocation, clonedItem,
                itemSpawnReason, player);
        mcMMO.p.getServer().getPluginManager().callEvent(event);
        clonedItem = event.getItemStack();

        //Something cancelled the event so back out
        if (event.isCancelled()) {
            return null;
        }

        //Use the item from the event
        final Item spawnedItem = spawnLocation.getWorld().dropItem(spawnLocation, clonedItem);
        final Vector vecFrom = spawnLocation.clone().toVector().clone();
        final Vector vecTo = targetLocation.clone().toVector().clone();

        //Vector which is pointing towards out target location
        Vector direction = vecTo.subtract(vecFrom).normalize();

        //Modify the speed of the vector
        direction = direction.multiply(speed);
        spawnedItem.setVelocity(direction);
        return spawnedItem;
    }

    public static void spawnItemsFromCollection(@NotNull final Player player,
                                                @NotNull final Location location,
                                                @NotNull final Collection<ItemStack> drops,
                                                @NotNull final ItemSpawnReason itemSpawnReason) {
        requireNonNull(drops, "drops cannot be null");
        for (final ItemStack drop : drops) {
            spawnItem(player, location, drop, itemSpawnReason);
        }
    }

    /**
     * Drops only the first n items in a collection Size should always be a positive integer above
     * 0
     *
     * @param location  target drop location
     * @param drops     collection to iterate over
     * @param sizeLimit the number of drops to process
     */
    public static void spawnItemsFromCollection(@Nullable final Player player,
                                                @NotNull final Location location,
                                                @NotNull final Collection<ItemStack> drops,
                                                @NotNull final ItemSpawnReason itemSpawnReason,
                                                final int sizeLimit) {
        // TODO: This doesn't make much sense, unit test time?
        final ItemStack[] arrayDrops = drops.toArray(new ItemStack[0]);

        for (int i = 0; i < sizeLimit - 1; i++) {
            spawnItem(player, location, arrayDrops[i], itemSpawnReason);
        }
    }

    /**
     * Spawn items form a collection if conditions are met. Each item is tested against the
     * condition and spawned if it passes.
     *
     * @param potentialItemDrops The collection of items to iterate over, each one is tested and
     *                           spawned if the predicate is true
     * @param predicate          The predicate to test the item against
     * @param itemSpawnReason    The reason for the item drop
     * @param spawnLocation      The location to spawn the item at
     * @param player             The player to spawn the item for
     */
    public static void spawnItemsConditionally(@NotNull final Collection<ItemStack> potentialItemDrops,
                                               @NotNull final Predicate<ItemStack> predicate,
                                               @NotNull final ItemSpawnReason itemSpawnReason,
                                               @NotNull final Location spawnLocation,
                                               @NotNull final Player player) {
        potentialItemDrops.stream()
                .filter(predicate)
                .forEach(itemStack -> spawnItem(player, spawnLocation, itemStack, itemSpawnReason));
    }
}
