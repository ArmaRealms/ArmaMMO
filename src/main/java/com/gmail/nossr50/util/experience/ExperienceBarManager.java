package com.gmail.nossr50.util.experience;

import com.gmail.nossr50.config.experience.ExperienceConfig;
import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.datatypes.skills.PrimarySkillType;
import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.runnables.skills.ExperienceBarHideTask;
import com.gmail.nossr50.util.Misc;
import com.gmail.nossr50.util.player.NotificationManager;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;

/**
 * ExperienceBarManager handles displaying and updating mcMMO experience bars for players
 * Each ExperienceBarManager only manages a single player
 */
public class ExperienceBarManager {
    private final McMMOPlayer mcMMOPlayer;
    int delaySeconds = 3;

    private HashMap<PrimarySkillType, ExperienceBarWrapper> experienceBars;
    private HashMap<PrimarySkillType, ExperienceBarHideTask> experienceBarHideTaskHashMap;

    private HashSet<PrimarySkillType> alwaysVisible;
    private HashSet<PrimarySkillType> disabledBars;

    public ExperienceBarManager(final McMMOPlayer mcMMOPlayer) {
        this.mcMMOPlayer = mcMMOPlayer;
        init();
    }

    public void init() {
        //Init maps
        experienceBars = new HashMap<>();
        experienceBarHideTaskHashMap = new HashMap<>();

        //Init sets
        alwaysVisible = new HashSet<>();
        disabledBars = new HashSet<>();
    }

    public void updateExperienceBar(final PrimarySkillType primarySkillType) {
        final ExperienceConfig config = ExperienceConfig.getInstance();

        if (disabledBars.contains(primarySkillType) || !config.isExperienceBarsEnabled() || !config.isExperienceBarEnabled(primarySkillType)) {
            return;
        }

        // Get or Initialize Bar
        final ExperienceBarWrapper experienceBarWrapper = experienceBars.computeIfAbsent(primarySkillType,
                key -> new ExperienceBarWrapper(primarySkillType, mcMMOPlayer));

        // Update Progress and Show Bar
        experienceBarWrapper.setProgress(mcMMOPlayer.getProgressInCurrentSkillLevel(primarySkillType));
        experienceBarWrapper.showExperienceBar();

        //Setup Hide Bar Task
        cancelHideTask(primarySkillType);
        scheduleHideTask(primarySkillType, mcMMO.p);
    }

    /**
     * Cancels and forgets any pending hide task for the skill. Hide tasks only remove
     * themselves from the map when they actually run, so cancellation has to remove the entry
     * too or cancelled tasks linger in the map.
     */
    private void cancelHideTask(PrimarySkillType primarySkillType) {
        final ExperienceBarHideTask lingeringTask =
                experienceBarHideTaskHashMap.remove(primarySkillType);

        if (lingeringTask != null) {
            lingeringTask.cancel();
        }
    }

    private void scheduleHideTask(PrimarySkillType primarySkillType, Plugin plugin) {
        if (alwaysVisible.contains(primarySkillType)) {
            return;
        }

        final ExperienceBarHideTask experienceBarHideTask = new ExperienceBarHideTask(this, mcMMOPlayer, primarySkillType);
        mcMMO.p.getFoliaLib().getScheduler().runAtEntityLater(mcMMOPlayer.getPlayer(), experienceBarHideTask, (long) delaySeconds * Misc.TICK_CONVERSION_FACTOR);
        experienceBarHideTaskHashMap.put(primarySkillType, experienceBarHideTask);
    }

    public void hideExperienceBar(final PrimarySkillType primarySkillType) {
        if (experienceBars.containsKey(primarySkillType))
            experienceBars.get(primarySkillType).hideExperienceBar();
    }

    public void clearTask(final PrimarySkillType primarySkillType) {
        experienceBarHideTaskHashMap.remove(primarySkillType);
    }

    public void disableAllBars() {
        // Apply without the per-skill chat confirmations; the summary line covers them all
        for (PrimarySkillType primarySkillType : PrimarySkillType.values()) {
            applyBarSetting(XPBarSettingTarget.HIDE, primarySkillType);
        }

        NotificationManager.sendPlayerInformationChatOnlyPrefixed(mcMMOPlayer.getPlayer(), "Commands.XPBar.DisableAll");
    }

    public void xpBarSettingToggle(@NotNull XPBarSettingTarget settingTarget,
            @Nullable PrimarySkillType skillType) {
        applyBarSetting(settingTarget, skillType);
        informPlayer(settingTarget, skillType);
    }

    private void applyBarSetting(@NotNull XPBarSettingTarget settingTarget,
            @Nullable PrimarySkillType skillType) {
        switch (settingTarget) {
            case SHOW -> {
                disabledBars.remove(skillType);
                alwaysVisible.add(skillType);

                cancelHideTask(skillType);
                updateExperienceBar(skillType);
            }
            case HIDE -> {
                alwaysVisible.remove(skillType);
                disabledBars.add(skillType);

                cancelHideTask(skillType);
                hideExperienceBar(skillType);
            }
            case RESET -> resetBarSettings();
        }
    }

    private void resetBarSettings() {
        //Hide all currently permanent bars
        for (final PrimarySkillType permanent : alwaysVisible) {
            hideExperienceBar(permanent);
        }

        alwaysVisible.clear();
        disabledBars.clear();

        //Hide child skills by default
        disabledBars.add(PrimarySkillType.SALVAGE);
        disabledBars.add(PrimarySkillType.SMELTING);
    }

    private void informPlayer(@NotNull final ExperienceBarManager.@NotNull XPBarSettingTarget settingTarget, @Nullable final PrimarySkillType primarySkillType) {
        //Inform player of setting change
        if (settingTarget != XPBarSettingTarget.RESET) {
            NotificationManager.sendPlayerInformationChatOnlyPrefixed(mcMMOPlayer.getPlayer(), "Commands.XPBar.SettingChanged", mcMMO.p.getSkillTools().getLocalizedSkillName(primarySkillType), settingTarget.toString());
        } else {
            NotificationManager.sendPlayerInformationChatOnlyPrefixed(mcMMOPlayer.getPlayer(), "Commands.XPBar.Reset");
        }
    }

    public enum XPBarSettingTarget {SHOW, HIDE, RESET, DISABLE}
}
