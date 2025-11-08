package com.gmail.nossr50.datatypes.treasure;

import com.google.common.base.Objects;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;

public record EnchantmentWrapper(@NotNull Enchantment enchantment, int enchantmentLevel) {

    @Override
    public String toString() {
        return "EnchantmentWrapper{" +
                "enchantment=" + enchantment +
                ", enchantmentLevel=" + enchantmentLevel +
                '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final EnchantmentWrapper that = (EnchantmentWrapper) o;
        return enchantmentLevel == that.enchantmentLevel && Objects.equal(enchantment,
                that.enchantment);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(enchantment, enchantmentLevel);
    }
}
