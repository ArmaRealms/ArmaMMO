package com.gmail.nossr50.datatypes.mods;

import org.bukkit.inventory.ItemStack;

public record CustomEntity(double xpMultiplier, boolean canBeTamed, int tamingXP, boolean canBeSummoned,
                           ItemStack callOfTheWildItem, int callOfTheWildAmount) {
}
