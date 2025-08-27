package com.gmail.nossr50.skills.swords;

import com.gmail.nossr50.datatypes.interactions.NotificationType;
import com.gmail.nossr50.datatypes.meta.RuptureTaskMeta;
import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.datatypes.skills.PrimarySkillType;
import com.gmail.nossr50.datatypes.skills.SubSkillType;
import com.gmail.nossr50.datatypes.skills.SuperAbilityType;
import com.gmail.nossr50.datatypes.skills.ToolType;
import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.runnables.skills.RuptureTask;
import com.gmail.nossr50.skills.SkillManager;
import com.gmail.nossr50.util.MetadataConstants;
import com.gmail.nossr50.util.Permissions;
import com.gmail.nossr50.util.player.NotificationManager;
import com.gmail.nossr50.util.random.ProbabilityUtil;
import com.gmail.nossr50.util.skills.CombatUtils;
import com.gmail.nossr50.util.skills.RankUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SwordsManager extends SkillManager {
    public SwordsManager(final McMMOPlayer mcMMOPlayer) {
        super(mcMMOPlayer, PrimarySkillType.SWORDS);
    }

    public boolean canActivateAbility() {
        return mmoPlayer.getToolPreparationMode(ToolType.SWORD) && Permissions.serratedStrikes(getPlayer());
    }

    public boolean canUseStab() {
        return Permissions.isSubSkillEnabled(getPlayer(), SubSkillType.SWORDS_STAB) && RankUtils.hasUnlockedSubskill(getPlayer(), SubSkillType.SWORDS_STAB);
    }

    public boolean canUseRupture() {
        return Permissions.isSubSkillEnabled(getPlayer(), SubSkillType.SWORDS_RUPTURE) && RankUtils.hasUnlockedSubskill(getPlayer(), SubSkillType.SWORDS_RUPTURE);
    }

    public boolean canUseCounterAttack(final Entity target) {
        if (!RankUtils.hasUnlockedSubskill(getPlayer(), SubSkillType.SWORDS_COUNTER_ATTACK))
            return false;

        return target instanceof LivingEntity && Permissions.isSubSkillEnabled(getPlayer(), SubSkillType.SWORDS_COUNTER_ATTACK);
    }

    public boolean canUseSerratedStrike() {
        if (!RankUtils.hasUnlockedSubskill(getPlayer(), SubSkillType.SWORDS_SERRATED_STRIKES))
            return false;

        return mmoPlayer.getAbilityMode(SuperAbilityType.SERRATED_STRIKES);
    }

    /**
     * Check for Bleed effect.
     *
     * @param target The defending entity
     */
    public void processRupture(@NotNull final LivingEntity target) {
        if (!canUseRupture())
            return;

        if (target.hasMetadata(MetadataConstants.METADATA_KEY_RUPTURE)) {
            final RuptureTaskMeta ruptureTaskMeta = (RuptureTaskMeta) target.getMetadata(MetadataConstants.METADATA_KEY_RUPTURE).get(0);

            if (mmoPlayer.isDebugMode()) {
                mmoPlayer.getPlayer().sendMessage("Rupture task ongoing for target " + target);
                mmoPlayer.getPlayer().sendMessage(ruptureTaskMeta.getRuptureTimerTask().toString());
            }

            ruptureTaskMeta.getRuptureTimerTask().refreshRupture();
            return; //Don't apply bleed
        }

        final double ruptureOdds = mcMMO.p.getAdvancedConfig().getRuptureChanceToApplyOnHit(getRuptureRank())
                * mmoPlayer.getAttackStrength();
        if (ProbabilityUtil.isStaticSkillRNGSuccessful(PrimarySkillType.SWORDS, mmoPlayer, ruptureOdds)) {

            if (target instanceof final Player defender) {

                //Don't start or add to a bleed if they are blocking
                if (defender.isBlocking())
                    return;

                if (NotificationManager.doesPlayerUseNotifications(defender)) {
                    NotificationManager.sendPlayerInformation(defender, NotificationType.SUBSKILL_MESSAGE, "Swords.Combat.Bleeding.Started");
                }
            }

            final RuptureTask ruptureTask = new RuptureTask(mmoPlayer, target,
                    mcMMO.p.getAdvancedConfig().getRuptureTickDamage(target instanceof Player, getRuptureRank()));

            final RuptureTaskMeta ruptureTaskMeta = new RuptureTaskMeta(mcMMO.p, ruptureTask);

            mcMMO.p.getFoliaLib().getScheduler().runAtEntityTimer(target, ruptureTask, 1, 1);
            target.setMetadata(MetadataConstants.METADATA_KEY_RUPTURE, ruptureTaskMeta);
        }
    }

    private int getRuptureRank() {
        return RankUtils.getRank(getPlayer(), SubSkillType.SWORDS_RUPTURE);
    }

    public double getStabDamage() {
        final int rank = RankUtils.getRank(getPlayer(), SubSkillType.SWORDS_STAB);

        if (rank > 0) {
            final double baseDamage = mcMMO.p.getAdvancedConfig().getStabBaseDamage();
            final double rankMultiplier = mcMMO.p.getAdvancedConfig().getStabPerRankMultiplier();
            return (baseDamage + (rank * rankMultiplier));
        }

        return 0;
    }

    /**
     * Handle the effects of the Counter Attack ability
     *
     * @param attacker The {@link LivingEntity} being affected by the ability
     * @param damage   The amount of damage initially dealt by the event
     */
    public void counterAttackChecks(@NotNull final LivingEntity attacker, final double damage) {
        if (ProbabilityUtil.isSkillRNGSuccessful(SubSkillType.SWORDS_COUNTER_ATTACK, mmoPlayer)) {
            CombatUtils.safeDealDamage(attacker, damage / Swords.counterAttackModifier, getPlayer());

            NotificationManager.sendPlayerInformation(getPlayer(),
                    NotificationType.SUBSKILL_MESSAGE, "Swords.Combat.Countered");

            if (attacker instanceof final Player player) {
                NotificationManager.sendPlayerInformation(player, NotificationType.SUBSKILL_MESSAGE, "Swords.Combat.Counter.Hit");
            }
        }
    }

    /**
     * Handle the effects of the Serrated Strikes ability
     *
     * @param target The {@link LivingEntity} being affected by the ability
     * @param damage The amount of damage initially dealt by the event
     */
    public void serratedStrikes(@NotNull final LivingEntity target, final double damage) {
        CombatUtils.applyAbilityAoE(getPlayer(), target, damage / Swords.serratedStrikesModifier, skill);
    }
}
