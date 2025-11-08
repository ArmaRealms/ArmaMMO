package com.gmail.nossr50.datatypes.skills.alchemy;

import static com.gmail.nossr50.util.PotionUtil.isLong;
import static com.gmail.nossr50.util.PotionUtil.isPotionTypeWater;
import static com.gmail.nossr50.util.PotionUtil.isStrong;
import com.gmail.nossr50.util.PotionUtil;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;

public enum PotionStage {
    FIVE(5),
    FOUR(4),
    THREE(3),
    TWO(2),
    ONE(1);

    int numerical;

    PotionStage(final int numerical) {
        this.numerical = numerical;
    }

    private static PotionStage getPotionStageNumerical(final int numerical) {
        for (final PotionStage potionStage : values()) {
            if (numerical >= potionStage.toNumerical()) {
                return potionStage;
            }
        }

        return ONE;
    }

    public static PotionStage getPotionStage(final AlchemyPotion input, final AlchemyPotion output) {
        PotionStage outputPotionStage = getPotionStage(output);
        final PotionStage inputPotionStage = getPotionStage(input);
        if (!isWaterBottle(input) && inputPotionStage == outputPotionStage) {
            outputPotionStage = PotionStage.FIVE;
        }

        return outputPotionStage;
    }

    private static boolean isWaterBottle(final AlchemyPotion alchemyPotion) {
        return isPotionTypeWater(alchemyPotion.getAlchemyPotionMeta());
    }

    public static PotionStage getPotionStage(final AlchemyPotion alchemyPotion) {
        final PotionMeta potionMeta = alchemyPotion.getAlchemyPotionMeta();

        int stage = 1;

        // Check if potion has an effect of any sort
        if (!potionMeta.getCustomEffects().isEmpty()
                || PotionUtil.hasBasePotionEffects(potionMeta)) {
            stage++;
        }

        // Check if potion has a glowstone dust amplifier
        // Else check if the potion has a custom effect with an amplifier added by mcMMO
        if (isStrong(potionMeta)) {
            stage++;
        } else if (!potionMeta.getCustomEffects().isEmpty()) {
            for (final PotionEffect effect : potionMeta.getCustomEffects()) {
                if (effect.getAmplifier() > 0) {
                    stage++;
                    break;
                }
            }
        }

        // Check if potion has a redstone dust amplifier
        if (isLong(potionMeta)) {
            stage++;
        }

        // Check if potion has a gunpowder amplifier
        if (alchemyPotion.isSplash() || alchemyPotion.isLingering()) {
            stage++;
        }

        return PotionStage.getPotionStageNumerical(stage);
    }

    public int toNumerical() {
        return numerical;
    }
}
