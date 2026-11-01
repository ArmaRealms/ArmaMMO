package com.gmail.nossr50.config;

import java.io.File;

public class CustomItemSupportConfig extends BukkitConfig {
    public CustomItemSupportConfig(File dataFolder) {
        super("custom_item_support.yml", dataFolder);
        validate();
    }

    @Override
    protected void loadKeys() {

    }

    public boolean isCustomRepairAllowed() {
        return config.getBoolean(
                "Custom_Item_Support.Repair.Allow_Repair_On_Items_With_Custom_Model_Data", true);
    }

    /** Returns whether custom item repairs require a rank that preserves every enchantment. */
    public boolean isPerfectArcaneForgingRequired() {
        return config.getBoolean("Custom_Item_Support.Repair.Require_Perfect_Arcane_Forging", false);
    }

    public boolean isCustomSalvageAllowed() {
        return config.getBoolean(
                "Custom_Item_Support.Salvage.Allow_Salvage_On_Items_With_Custom_Model_Data", true);
    }

    /** Returns whether items with an explicit item model may be repaired. */
    public boolean isItemModelRepairAllowed() {
        return config.getBoolean(
                "Custom_Item_Support.Repair.Allow_Repair_On_Items_With_Custom_Item_Model", true);
    }

    /** Returns whether items with an explicit item model may be salvaged. */
    public boolean isItemModelSalvageAllowed() {
        return config.getBoolean(
                "Custom_Item_Support.Salvage.Allow_Salvage_On_Items_With_Custom_Item_Model", true);
    }
}
