package com.gmail.nossr50.util.skills;

import com.gmail.nossr50.config.experience.ExperienceConfig;
import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.datatypes.skills.PrimarySkillType;
import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.skills.acrobatics.AcrobaticsManager;
import com.gmail.nossr50.skills.swords.SwordsManager;
import com.gmail.nossr50.skills.taming.TamingManager;
import com.gmail.nossr50.util.ItemUtils;
import com.gmail.nossr50.util.Misc;
import com.gmail.nossr50.util.player.UserManager;
import org.bukkit.Material;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Wolf;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;

public class CombatProcessor {
    private static final ExperienceConfig experienceConfig = ExperienceConfig.getInstance();
    
    private CombatProcessor() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Apply combat modifiers and process and XP gain.
     *
     * @param event The event to run the combat checks on.
     */
    public static void processCombatAttack(@NotNull EntityDamageByEntityEvent event, @NotNull Entity painSourceRoot, @NotNull LivingEntity target) {
        Entity painSource = event.getDamager();

        if (target instanceof Player player) {
            if (experienceConfig.isNPCInteractionPrevented() && (Misc.isNPCEntityExcludingVillagers(target))) return;
            if (!UserManager.hasPlayerDataKey(player)) return;

            McMMOPlayer mcMMOPlayer = UserManager.getPlayer(player);
            if (mcMMOPlayer == null) return;

            AcrobaticsManager acrobaticsManager = mcMMOPlayer.getAcrobaticsManager();
            if (acrobaticsManager.canDodge(target)) {
                event.setDamage(acrobaticsManager.dodgeCheck(painSourceRoot, event.getDamage()));
            }

            if (ItemUtils.isSword(player.getInventory().getItemInMainHand())) {
                if (!mcMMO.p.getSkillTools().canCombatSkillsTrigger(PrimarySkillType.SWORDS, target)) {
                    return;
                }

                SwordsManager swordsManager = mcMMOPlayer.getSwordsManager();
                if (swordsManager.canUseCounterAttack(painSource)) {
                    swordsManager.counterAttackChecks((LivingEntity) painSource, event.getDamage());
                }
            }
        }

        if (painSourceRoot instanceof Player player && painSource instanceof Player) {
            if (!UserManager.hasPlayerDataKey(player)) return;

            ItemStack heldItem = player.getInventory().getItemInMainHand();

            if (target instanceof Tameable tamedEntity) {
                if (heldItem.getType() == Material.BONE) {
                    McMMOPlayer mcMMOPlayer = UserManager.getPlayer(player);
                    if (mcMMOPlayer == null) return;
                    TamingManager tamingManager = mcMMOPlayer.getTamingManager();

                    if (tamingManager.canUseBeastLore()) {
                        tamingManager.beastLore(tamedEntity);
                        event.setCancelled(true);
                        return;
                    }
                }

                if (CombatUtils.isFriendlyPet(player, tamedEntity)) return;
            }

            if (ItemUtils.isSword(heldItem)) {
                if (!mcMMO.p.getSkillTools().canCombatSkillsTrigger(PrimarySkillType.SWORDS, target)) return;

                if (mcMMO.p.getSkillTools().doesPlayerHaveSkillPermission(player, PrimarySkillType.SWORDS)) {
                    CombatUtils.processSwordCombat(target, player, event);
                }
            } else if (ItemUtils.isAxe(heldItem)) {
                if (!mcMMO.p.getSkillTools().canCombatSkillsTrigger(PrimarySkillType.AXES, target)) return;

                if (mcMMO.p.getSkillTools().doesPlayerHaveSkillPermission(player, PrimarySkillType.AXES)) {
                    CombatUtils.processAxeCombat(target, player, event);
                }
            } else if (ItemUtils.isUnarmed(heldItem)) {
                if (!mcMMO.p.getSkillTools().canCombatSkillsTrigger(PrimarySkillType.UNARMED, target)) return;

                if (mcMMO.p.getSkillTools().doesPlayerHaveSkillPermission(player, PrimarySkillType.UNARMED)) {
                    CombatUtils.processUnarmedCombat(target, player, event);
                }
            }
        } else if (painSource instanceof Wolf wolf) {
            AnimalTamer tamer = wolf.getOwner();

            if (tamer instanceof Player master && mcMMO.p.getSkillTools().canCombatSkillsTrigger(PrimarySkillType.TAMING, target) && (!Misc.isNPCEntityExcludingVillagers(master) && mcMMO.p.getSkillTools().doesPlayerHaveSkillPermission(master, PrimarySkillType.TAMING))) {
                CombatUtils.processTamingCombat(target, master, wolf, event);
            }
        } else if (painSource instanceof Projectile arrow) {
            ProjectileSource projectileSource = arrow.getShooter();

            if (projectileSource instanceof Player player && mcMMO.p.getSkillTools().canCombatSkillsTrigger(PrimarySkillType.ARCHERY, target)) {

                if (!Misc.isNPCEntityExcludingVillagers(player) && mcMMO.p.getSkillTools().doesPlayerHaveSkillPermission(player, PrimarySkillType.ARCHERY)) {
                    CombatUtils.processArcheryCombat(target, player, event, arrow);
                } else {
                    //Cleanup Arrow
                    CombatUtils.cleanupArrowMetadata(arrow);
                }

                if (target.getType() != EntityType.CREEPER && !Misc.isNPCEntityExcludingVillagers(player) && mcMMO.p.getSkillTools().doesPlayerHaveSkillPermission(player, PrimarySkillType.TAMING)) {
                    McMMOPlayer mcMMOPlayer = UserManager.getPlayer(player);
                    if (mcMMOPlayer == null) return;

                    TamingManager tamingManager = mcMMOPlayer.getTamingManager();
                    tamingManager.attackTarget(target);
                }
            }
        }
    }

}

