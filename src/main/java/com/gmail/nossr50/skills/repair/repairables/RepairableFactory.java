package com.gmail.nossr50.skills.repair.repairables;

import com.gmail.nossr50.datatypes.skills.ItemType;
import com.gmail.nossr50.datatypes.skills.MaterialType;
import org.bukkit.Material;

public class RepairableFactory {
    public static Repairable getRepairable(final Material itemMaterial, final Material repairMaterial,
                                           final short maximumDurability) {
        return getRepairable(itemMaterial, repairMaterial, null, 0, maximumDurability,
                ItemType.OTHER, MaterialType.OTHER, 1);
    }

    public static Repairable getRepairable(final Material itemMaterial, final Material repairMaterial,
                                           final int minimumLevel, final short maximumDurability, final ItemType repairItemType,
                                           final MaterialType repairMaterialType, final double xpMultiplier) {
        return getRepairable(itemMaterial, repairMaterial, null, minimumLevel, maximumDurability,
                repairItemType, repairMaterialType, xpMultiplier);
    }

    public static Repairable getRepairable(final Material itemMaterial, final Material repairMaterial,
                                           final String repairMaterialPrettyName,
                                           final int minimumLevel, final short maximumDurability, final ItemType repairItemType,
                                           final MaterialType repairMaterialType, final double xpMultiplier) {
        // TODO: Add in loading from config what type of repairable we want.
        return new SimpleRepairable(itemMaterial, repairMaterial, repairMaterialPrettyName,
                minimumLevel, maximumDurability, repairItemType, repairMaterialType, xpMultiplier);
    }

    public static Repairable getRepairable(final Material itemMaterial, final Material repairMaterial,
                                           final String repairMaterialPrettyName,
                                           final int minimumLevel, final short maximumDurability, final ItemType repairItemType,
                                           final MaterialType repairMaterialType, final double xpMultiplier, final int minQuantity) {
        // TODO: Add in loading from config what type of repairable we want.
        return new SimpleRepairable(itemMaterial, repairMaterial, repairMaterialPrettyName,
                minimumLevel, maximumDurability, repairItemType, repairMaterialType, xpMultiplier,
                minQuantity);
    }
}
