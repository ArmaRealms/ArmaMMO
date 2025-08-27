package com.gmail.nossr50.util;

import com.gmail.nossr50.mcMMO;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.gmail.nossr50.util.MetadataService.NSK_SUPER_ABILITY_BOOSTED_ITEM;

public final class ItemMetadataUtils {

    public static final @NotNull String LEGACY_ABILITY_TOOL_LORE = "mcMMO Ability Tool";

    private ItemMetadataUtils() {
        // private ctor
    }

    public static void setSuperAbilityBoostedItem(@NotNull final ItemStack itemStack,
                                                  final int originalDigSpeed) {
        if (itemStack.getItemMeta() == null) {
            mcMMO.p.getLogger()
                    .severe("Can not assign persistent data to an item with null item metadata");
            return;
        }

        final ItemMeta itemMeta = itemStack.getItemMeta();
        final PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();

        dataContainer.set(NSK_SUPER_ABILITY_BOOSTED_ITEM, PersistentDataType.INTEGER,
                originalDigSpeed);

        itemStack.setItemMeta(itemMeta);
    }

    public static boolean isSuperAbilityBoosted(@NotNull final ItemStack itemStack) {
        if (itemStack.getItemMeta() == null) {
            return false;
        }

        final ItemMeta itemMeta = itemStack.getItemMeta();
        //Get container from entity
        final PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();

        //If this value isn't null, then the tool can be considered dig speed boosted
        final Integer boostValue = dataContainer.get(NSK_SUPER_ABILITY_BOOSTED_ITEM,
                PersistentDataType.INTEGER);

        return boostValue != null;
    }

    public static int getSuperAbilityToolOriginalDigSpeed(@NotNull final ItemStack itemStack) {
        //Get container from entity
        final ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta == null) {
            return 0;
        }

        final PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();

        if (dataContainer.get(NSK_SUPER_ABILITY_BOOSTED_ITEM, PersistentDataType.INTEGER) == null) {
            mcMMO.p.getLogger().severe("Value should never be null for a boosted item");
            return 0;
        } else {
            //Too lazy to make a custom data type for this stuff
            final Integer boostValue = dataContainer.get(NSK_SUPER_ABILITY_BOOSTED_ITEM,
                    PersistentDataType.INTEGER);
            return Math.max(boostValue, 0);
        }
    }

    public static void removeBonusDigSpeedOnSuperAbilityTool(@NotNull final ItemStack itemStack) {
        final int originalSpeed = getSuperAbilityToolOriginalDigSpeed(itemStack);
        final ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta != null) {
            //TODO: can be optimized
            if (itemMeta.hasEnchant(mcMMO.p.getEnchantmentMapper().getEfficiency())) {
                itemMeta.removeEnchant(mcMMO.p.getEnchantmentMapper().getEfficiency());
            }

            if (originalSpeed > 0) {
                itemMeta.addEnchant(mcMMO.p.getEnchantmentMapper().getEfficiency(), originalSpeed,
                        true);
            }

            final PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
            dataContainer.remove(NSK_SUPER_ABILITY_BOOSTED_ITEM); //Remove persistent data

            //TODO: needed?
            itemStack.setItemMeta(itemMeta);
        }
    }

    public static boolean isLegacyAbilityTool(@NotNull final ItemStack itemStack) {
        final ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta == null) {
            return false;
        }

        final List<String> lore = itemMeta.getLore();

        if (lore == null || lore.isEmpty()) {
            return false;
        }

        return lore.contains(LEGACY_ABILITY_TOOL_LORE);
    }
}
