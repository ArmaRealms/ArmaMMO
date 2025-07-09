package com.gmail.nossr50.skills.excavation;

import com.gmail.nossr50.api.ItemSpawnReason;
import com.gmail.nossr50.config.experience.ExperienceConfig;
import com.gmail.nossr50.datatypes.experience.XPGainReason;
import com.gmail.nossr50.datatypes.experience.XPGainSource;
import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.datatypes.skills.PrimarySkillType;
import com.gmail.nossr50.datatypes.skills.SubSkillType;
import com.gmail.nossr50.datatypes.treasure.ExcavationTreasure;
import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.skills.SkillManager;
import com.gmail.nossr50.util.ItemUtils;
import com.gmail.nossr50.util.Misc;
import com.gmail.nossr50.util.Permissions;
import com.gmail.nossr50.util.random.ProbabilityUtil;
import com.gmail.nossr50.util.skills.RankUtils;
import com.gmail.nossr50.util.skills.SkillUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.List;

import static java.util.Objects.requireNonNull;

public class ExcavationManager extends SkillManager {
    public ExcavationManager(final McMMOPlayer mmoPlayer) {
        super(mmoPlayer, PrimarySkillType.EXCAVATION);
    }

    /**
     * Process treasure drops & XP gain for Excavation.
     *
     * @param blockState The {@link BlockState} to check ability activation for
     */
    @Deprecated(forRemoval = true, since = "2.2.024")
    public void excavationBlockCheck(final BlockState blockState) {
        excavationBlockCheck(blockState.getBlock());
    }

    public void excavationBlockCheck(final Block block) {
        final int xp = ExperienceConfig.getInstance().getXp(PrimarySkillType.EXCAVATION, block.getType());
        requireNonNull(block, "excavationBlockCheck: block cannot be null");
        if (Permissions.isSubSkillEnabled(getPlayer(), SubSkillType.EXCAVATION_ARCHAEOLOGY)) {
            final List<ExcavationTreasure> treasures = getTreasures(block);

            if (!treasures.isEmpty()) {
                final int skillLevel = getSkillLevel();
                final Location centerOfBlock = Misc.getBlockCenter(block);

                for (final ExcavationTreasure treasure : treasures) {
                    if (skillLevel >= treasure.getDropLevel()
                            && ProbabilityUtil.isStaticSkillRNGSuccessful(
                            PrimarySkillType.EXCAVATION, mmoPlayer, treasure.getDropProbability())) {
                        processExcavationBonusesOnBlock(treasure, centerOfBlock);
                    }
                }
            }
        }

        applyXpGain(xp, XPGainReason.PVE, XPGainSource.SELF);
    }

    @Deprecated(forRemoval = true, since = "2.2.024")
    public List<ExcavationTreasure> getTreasures(@NotNull final BlockState blockState) {
        requireNonNull(blockState, "blockState cannot be null");
        return getTreasures(blockState.getBlock());
    }

    public List<ExcavationTreasure> getTreasures(@NotNull final Block block) {
        requireNonNull(block, "block cannot be null");
        return Excavation.getTreasures(block);
    }

    @VisibleForTesting
    @Deprecated(forRemoval = true, since = "2.2.024")
    public void processExcavationBonusesOnBlock(final BlockState ignored, final ExcavationTreasure treasure, final Location location) {
        processExcavationBonusesOnBlock(treasure, location);
    }

    public void processExcavationBonusesOnBlock(final ExcavationTreasure treasure, final Location location) {
        //Spawn Vanilla XP orbs if a dice roll succeeds
        if (ProbabilityUtil.isStaticSkillRNGSuccessful(
                PrimarySkillType.EXCAVATION, mmoPlayer, getArchaelogyExperienceOrbChance())) {
            Misc.spawnExperienceOrb(location, getExperienceOrbsReward());
        }

        int xp = 0;
        xp += treasure.getXp();
        ItemUtils.spawnItem(getPlayer(), location, treasure.getDrop(), ItemSpawnReason.EXCAVATION_TREASURE);
        if (xp > 0) {
            applyXpGain(xp, XPGainReason.PVE, XPGainSource.SELF);
        }
    }

    public int getExperienceOrbsReward() {
        return getArchaeologyRank();
    }

    public double getArchaelogyExperienceOrbChance() {
        return getArchaeologyRank() * 2.0;
    }

    public int getArchaeologyRank() {
        return RankUtils.getRank(getPlayer(), SubSkillType.EXCAVATION_ARCHAEOLOGY);
    }

    public void printExcavationDebug(final Player player, final Block block) {
        if (Permissions.isSubSkillEnabled(getPlayer(), SubSkillType.EXCAVATION_ARCHAEOLOGY)) {
            final List<ExcavationTreasure> treasures = Excavation.getTreasures(block);

            if (!treasures.isEmpty()) {
                for (final ExcavationTreasure treasure : treasures) {
                    player.sendMessage("|||||||||||||||||||||||||||||||||");
                    player.sendMessage("[mcMMO DEBUG] Treasure found: (" + treasure.getDrop().getType() + ")");
                    player.sendMessage("[mcMMO DEBUG] Drop Chance for Treasure: " + treasure.getDropChance());
                    player.sendMessage("[mcMMO DEBUG] Skill Level Required: " + treasure.getDropLevel());
                    player.sendMessage("[mcMMO DEBUG] XP for Treasure: " + treasure.getXp());
                }
            } else {
                player.sendMessage("[mcMMO DEBUG] No treasures found for this block.");
            }
        }
    }

    /**
     * Process the Giga Drill Breaker ability.
     *
     * @param block The {@link Block} to check ability activation for
     */
    public void gigaDrillBreaker(final Block block) {
        excavationBlockCheck(block);
        excavationBlockCheck(block);

        SkillUtils.handleDurabilityChange(getPlayer().getInventory().getItemInMainHand(),
                mcMMO.p.getGeneralConfig().getAbilityToolDamage());
    }
}
