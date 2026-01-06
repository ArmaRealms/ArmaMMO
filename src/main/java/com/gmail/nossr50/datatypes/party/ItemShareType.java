package com.gmail.nossr50.datatypes.party;

import com.gmail.nossr50.locale.LocaleLoader;
import com.gmail.nossr50.util.ItemUtils;
import com.gmail.nossr50.util.text.StringUtils;
import org.bukkit.inventory.ItemStack;

public enum ItemShareType {
    LOOT("Saque"),
    MINING("Mineracao"),
    HERBALISM("Herbalismo"),
    WOODCUTTING("Lenhador"),
    MISC("Outros");
    private final String customName;

    ItemShareType(String customName) {
        this.customName = customName;
    }

    public static ItemShareType getShareType(String type) {
        for (ItemShareType shareType : values()) {
            if (shareType.toString().equalsIgnoreCase(type)) {
                return shareType;
            }
            if (shareType.customName.equalsIgnoreCase(type)) {
                return shareType;
            }
        }
        return null;
    }

    public static ItemShareType getShareType(ItemStack itemStack) {
        if (ItemUtils.isMobDrop(itemStack)) {
            return LOOT;
        }
        if (ItemUtils.isMiningDrop(itemStack)) {
            return MINING;
        }
        if (ItemUtils.isHerbalismDrop(itemStack)) {
            return HERBALISM;
        }
        if (ItemUtils.isWoodcuttingDrop(itemStack)) {
            return WOODCUTTING;
        }
        if (ItemUtils.isMiscDrop(itemStack)) {
            return MISC;
        }

        return null;
    }

    public String customName() {
        return this.customName;
    }

    public String getLocaleString() {
        return LocaleLoader.getString(
                "Party.ItemShare.Category." + StringUtils.getCapitalized(this.toString()));
    }
}
