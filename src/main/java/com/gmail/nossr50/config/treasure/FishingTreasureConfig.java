package com.gmail.nossr50.config.treasure;

import com.gmail.nossr50.config.BukkitConfig;
import com.gmail.nossr50.datatypes.treasure.EnchantmentTreasure;
import com.gmail.nossr50.datatypes.treasure.FishingTreasure;
import com.gmail.nossr50.datatypes.treasure.FishingTreasureBook;
import com.gmail.nossr50.datatypes.treasure.Rarity;
import com.gmail.nossr50.datatypes.treasure.ShakeTreasure;
import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.util.EnchantmentUtils;
import com.gmail.nossr50.util.LogUtils;
import com.gmail.nossr50.util.PotionUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.gmail.nossr50.util.PotionUtil.matchPotionType;

public class FishingTreasureConfig extends BukkitConfig {

    public static final String FILENAME = "fishing_treasures.yml";
    private static FishingTreasureConfig instance;

    public @NotNull HashMap<Rarity, List<FishingTreasure>> fishingRewards = new HashMap<>();
    public @NotNull HashMap<Rarity, List<EnchantmentTreasure>> fishingEnchantments = new HashMap<>();
    public @NotNull HashMap<EntityType, List<ShakeTreasure>> shakeMap = new HashMap<>();

    private FishingTreasureConfig() {
        super(FILENAME, false);
        loadKeys();
        validate();
    }

    public static FishingTreasureConfig getInstance() {
        if (instance == null) {
            instance = new FishingTreasureConfig();
        }

        return instance;
    }

    @Override
    protected boolean validateKeys() {
        // Validate all the settings!
        final List<String> reason = new ArrayList<>();
        final ConfigurationSection enchantment_drop_rates = config.getConfigurationSection(
                "Enchantment_Drop_Rates");

        if (enchantment_drop_rates != null) {
            for (final String tier : enchantment_drop_rates.getKeys(false)) {
                double totalEnchantDropRate = 0;
                double totalItemDropRate = 0;

                for (final Rarity rarity : Rarity.values()) {
                    final double enchantDropRate = config.getDouble(
                            "Enchantment_Drop_Rates." + tier + "." + rarity.toString());
                    final double itemDropRate = config.getDouble(
                            "Item_Drop_Rates." + tier + "." + rarity);

                    if ((enchantDropRate < 0.0 || enchantDropRate > 100.0)) {
                        reason.add(
                                "The enchant drop rate for " + tier + " items that are " + rarity
                                        + "should be between 0.0 and 100.0!");
                    }

                    if (itemDropRate < 0.0 || itemDropRate > 100.0) {
                        reason.add(
                                "The item drop rate for " + tier + " items that are " + rarity
                                        + "should be between 0.0 and 100.0!");
                    }

                    totalEnchantDropRate += enchantDropRate;
                    totalItemDropRate += itemDropRate;
                }

                if (totalEnchantDropRate < 0 || totalEnchantDropRate > 100.0) {
                    reason.add("The total enchant drop rate for " + tier
                            + " should be between 0.0 and 100.0!");
                }

                if (totalItemDropRate < 0 || totalItemDropRate > 100.0) {
                    reason.add("The total item drop rate for " + tier
                            + " should be between 0.0 and 100.0!");
                }
            }
        } else {
            mcMMO.p.getLogger().warning(
                    "Your fishing treasures config is empty, is this intentional? Delete it to regenerate.");
        }

        return noErrorsInConfig(reason);
    }

    @Override
    protected void loadKeys() {
        if (config.getConfigurationSection("Treasures") != null) {
            backup();
            return;
        }

        loadTreasures("Fishing");
        loadEnchantments();

        for (final EntityType entity : EntityType.values()) {
            if (entity.isAlive()) {
                loadTreasures("Shake." + entity);
            }
        }
    }

    private void loadTreasures(@NotNull final String type) {
        final boolean isFishing = type.equals("Fishing");
        final boolean isShake = type.contains("Shake");

        final ConfigurationSection treasureSection = config.getConfigurationSection(type);

        if (treasureSection == null) {
            return;
        }

        // Initialize fishing HashMap
        for (final Rarity rarity : Rarity.values()) {
            if (!fishingRewards.containsKey(rarity)) {
                fishingRewards.put(rarity, (new ArrayList<>()));
            }
        }

        for (final String treasureName : treasureSection.getKeys(false)) {
            // Validate all the things!
            final List<String> reason = new ArrayList<>();

            final String[] treasureInfo = treasureName.split("[|]");
            final String materialName = treasureInfo[0];

            /*
             * Material, Amount, and Data
             */
            final Material material;

            if (materialName.contains("INVENTORY")) {
                // Use magic material BEDROCK to know that we're grabbing something from the inventory and not a normal treasure
                addShakeTreasure(
                        new ShakeTreasure(
                                new ItemStack(Material.BEDROCK, 1, (byte) 0), 1,
                                getInventoryStealDropChance(), getInventoryStealDropLevel()),
                        EntityType.PLAYER);
                continue;
            } else {
                material = Material.matchMaterial(materialName);
            }

            int amount = config.getInt(type + "." + treasureName + ".Amount");
            final short data = (treasureInfo.length == 2) ? Short.parseShort(treasureInfo[1])
                    : (short) config.getInt(
                    type + "." + treasureName + ".Data");

            if (material == null) {
                reason.add("Cannot find matching item type in this version of MC, skipping - "
                        + materialName);
                continue;
            }

            if (amount <= 0) {
                amount = 1;
            }

            if (material.isBlock() && (data > 127 || data < -128)) {
                reason.add("Data of " + treasureName + " is invalid! " + data);
            }

            /*
             * XP, Drop Chance, and Drop Level
             */

            final int xp = config.getInt(type + "." + treasureName + ".XP");
            final double dropChance = config.getDouble(type + "." + treasureName + ".Drop_Chance");
            final int dropLevel = config.getInt(type + "." + treasureName + ".Drop_Level");

            if (xp < 0) {
                reason.add(treasureName + " has an invalid XP value: " + xp);
            }

            if (dropChance < 0.0D) {
                reason.add(treasureName + " has an invalid Drop_Chance: " + dropChance);
            }

            if (dropLevel < 0) {
                reason.add("Fishing Config: " + treasureName + " has an invalid Drop_Level: "
                        + dropLevel);
            }

            /*
             * Specific Types
             */
            Rarity rarity = null;

            if (isFishing) {
                final String rarityStr = config.getString(type + "." + treasureName + ".Rarity");

                if (rarityStr != null) {
                    rarity = Rarity.getRarity(rarityStr);
                } else {
                    mcMMO.p.getLogger().severe(
                            "Please edit your config and add a Rarity definition for - "
                                    + treasureName);
                    mcMMO.p.getLogger().severe("Skipping this treasure until rarity is defined - "
                            + treasureName);
                    continue;
                }
            }

            /*
             * Itemstack
             */
            ItemStack item = null;

            String customName = null;

            if (hasCustomName(type, treasureName)) {
                customName = config.getString(type + "." + treasureName + ".Custom_Name");
            }

            if (materialName.contains("POTION")) {
                // Update for 1.20.5

                final Material mat = Material.matchMaterial(materialName);
                if (mat == null) {
                    reason.add("Potion format for " + FILENAME + " has changed");
                    continue;
                } else {
                    item = new ItemStack(mat, amount, data);
                    final PotionMeta potionMeta = (PotionMeta) item.getItemMeta();

                    if (potionMeta == null) {
                        mcMMO.p.getLogger().severe(
                                "FishingConfig: Item meta when adding potion to fishing treasure was null,"
                                        + " contact the mcMMO devs!");
                        reason.add(
                                "FishingConfig: Item meta when adding potion to fishing treasure was null");
                        continue;
                    }

                    final String potionTypeStr;
                    potionTypeStr = config.getString(
                            type + "." + treasureName + ".PotionData.PotionType", "WATER");
                    final boolean extended = config.getBoolean(
                            type + "." + treasureName + ".PotionData.Extended", false);
                    final boolean upgraded = config.getBoolean(
                            type + "." + treasureName + ".PotionData.Upgraded", false);
                    final PotionType potionType = matchPotionType(potionTypeStr, extended,
                            upgraded);

                    if (potionType == null) {
                        reason.add(
                                "FishingConfig: Could not derive potion type from: " + potionTypeStr
                                        + ", " + extended + ", " + upgraded);
                        continue;
                    }

                    // Set the base potion type
                    // NOTE: Upgraded/Extended are ignored in 1.20.5 and later
                    PotionUtil.setBasePotionType(potionMeta, potionType, upgraded, extended);

                    if (customName != null) {
                        potionMeta.setDisplayName(
                                ChatColor.translateAlternateColorCodes('&', customName));
                    }

                    if (config.contains(type + "." + treasureName + ".Lore")) {
                        final List<String> lore = new ArrayList<>();
                        for (final String s : config.getStringList(type + "." + treasureName + ".Lore")) {
                            lore.add(ChatColor.translateAlternateColorCodes('&', s));
                        }
                        potionMeta.setLore(lore);
                    }
                    item.setItemMeta(potionMeta);
                }
            } else if (material == Material.ENCHANTED_BOOK) {
                //If any whitelisted enchants exist we use whitelist-based matching
                item = new ItemStack(material, 1);
                final ItemMeta itemMeta = item.getItemMeta();

                final List<String> allowedEnchantsList = config.getStringList(
                        type + "." + treasureName + ".Enchantments_Whitelist");
                final List<String> disallowedEnchantsList = config.getStringList(
                        type + "." + treasureName + ".Enchantments_Blacklist");

                final Set<Enchantment> blackListedEnchants = new HashSet<>();
                final Set<Enchantment> whiteListedEnchants = new HashSet<>();

                matchAndFillSet(disallowedEnchantsList, blackListedEnchants);
                matchAndFillSet(allowedEnchantsList, whiteListedEnchants);

                if (customName != null && itemMeta != null) {
                    itemMeta.setDisplayName(
                            ChatColor.translateAlternateColorCodes('&', customName));
                    item.setItemMeta(itemMeta);
                }

                final FishingTreasureBook fishingTreasureBook = new FishingTreasureBook(
                        item, xp, blackListedEnchants,
                        whiteListedEnchants);
                addFishingTreasure(rarity, fishingTreasureBook);
                //TODO: Add book support for shake
                continue; //The code in this whole file is a disaster, ignore this hacky solution :P
            } else {
                item = new ItemStack(material, amount, data);

                if (customName != null) {
                    final ItemMeta itemMeta = item.getItemMeta();
                    itemMeta.setDisplayName(
                            ChatColor.translateAlternateColorCodes('&', customName));
                    item.setItemMeta(itemMeta);
                }

                if (config.contains(type + "." + treasureName + ".Lore")) {
                    final ItemMeta itemMeta = item.getItemMeta();
                    final List<String> lore = new ArrayList<>();
                    for (final String s : config.getStringList(type + "." + treasureName + ".Lore")) {
                        lore.add(ChatColor.translateAlternateColorCodes('&', s));
                    }
                    itemMeta.setLore(lore);
                    item.setItemMeta(itemMeta);
                }
            }

            if (noErrorsInConfig(reason)) {
                if (isFishing) {
                    addFishingTreasure(rarity, new FishingTreasure(item, xp));
                } else if (isShake) {
                    final ShakeTreasure shakeTreasure = new ShakeTreasure(item, xp, dropChance,
                            dropLevel);

                    final EntityType entityType = EntityType.valueOf(type.substring(6));
                    addShakeTreasure(shakeTreasure, entityType);
                }
            }
        }
    }

    private void addShakeTreasure(@NotNull final ShakeTreasure shakeTreasure,
                                  @NotNull final EntityType entityType) {
        if (!shakeMap.containsKey(entityType)) {
            shakeMap.put(entityType, new ArrayList<>());
        }
        shakeMap.get(entityType).add(shakeTreasure);
    }

    private void addFishingTreasure(@NotNull final Rarity rarity,
                                    @NotNull final FishingTreasure fishingTreasure) {
        fishingRewards.get(rarity).add(fishingTreasure);
    }

    private boolean hasCustomName(@NotNull final String type, @NotNull final String treasureName) {
        return config.contains(type + "." + treasureName + ".Custom_Name");
    }

    /**
     * Matches enchantments on a list (user provided string) to known enchantments in the Spigot API
     * Any matches are added to the passed set
     *
     * @param enchantListStr the users string list of enchantments
     * @param permissiveList the permissive list of enchantments
     */
    private void matchAndFillSet(@NotNull final List<String> enchantListStr,
                                 @NotNull final Set<Enchantment> permissiveList) {
        if (enchantListStr.isEmpty()) {
            return;
        }

        for (final String str : enchantListStr) {
            boolean foundMatch = false;
            for (final Enchantment enchantment : Enchantment.values()) {
                if (enchantment.getKey().getKey().equalsIgnoreCase(str)) {
                    permissiveList.add(enchantment);
                    foundMatch = true;
                    break;
                }
            }

            if (!foundMatch) {
                LogUtils.debug(
                        mcMMO.p.getLogger(),
                        "[Fishing Treasure Init] Could not find any enchantments which matched the user defined enchantment named: "
                                + str);
            }
        }
    }

    private void loadEnchantments() {
        for (final Rarity rarity : Rarity.values()) {
            if (!fishingEnchantments.containsKey(rarity)) {
                fishingEnchantments.put(rarity, (new ArrayList<>()));
            }

            final ConfigurationSection enchantmentSection = config.getConfigurationSection(
                    "Enchantments_Rarity." + rarity.toString());

            if (enchantmentSection == null) {
                return;
            }

            for (final String enchantmentName : enchantmentSection.getKeys(false)) {
                final int level = config.getInt("Enchantments_Rarity." + rarity + "." + enchantmentName);
                final Enchantment enchantment = EnchantmentUtils.getByName(enchantmentName);

                if (enchantment == null) {
                    mcMMO.p.getLogger().info(
                            "Skipping invalid enchantment in '" + FILENAME + "', named:"
                                    + enchantmentName);
                    continue;
                }

                fishingEnchantments.get(rarity).add(new EnchantmentTreasure(enchantment, level));
            }
        }
    }

    public boolean getInventoryStealEnabled() {
        return config.contains("Shake.PLAYER.INVENTORY");
    }

    public boolean getInventoryStealStacks() {
        return config.getBoolean("Shake.PLAYER.INVENTORY.Whole_Stacks");
    }

    public double getInventoryStealDropChance() {
        return config.getDouble("Shake.PLAYER.INVENTORY.Drop_Chance");
    }

    public int getInventoryStealDropLevel() {
        return config.getInt("Shake.PLAYER.INVENTORY.Drop_Level");
    }

    public double getItemDropRate(final int tier, @NotNull final Rarity rarity) {
        return config.getDouble("Item_Drop_Rates.Tier_" + tier + "." + rarity);
    }

    public double getEnchantmentDropRate(final int tier, @NotNull final Rarity rarity) {
        return config.getDouble("Enchantment_Drop_Rates.Tier_" + tier + "." + rarity);
    }
}
