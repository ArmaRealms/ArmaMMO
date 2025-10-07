package com.gmail.nossr50.util.experience;

import com.gmail.nossr50.config.experience.ExperienceConfig;
import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.datatypes.skills.PrimarySkillType;
import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.runnables.skills.ExperienceBarHideTask;
import com.gmail.nossr50.util.Misc;
import com.gmail.nossr50.util.player.NotificationManager;
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

        // Cancel any existing Hide Task
        final ExperienceBarHideTask existingTask = experienceBarHideTaskHashMap.remove(primarySkillType);
        if (existingTask != null) {
            existingTask.cancel();
        }

        // Schedule new Hide Task
        scheduleHideTask(primarySkillType);
    }

    private void scheduleHideTask(final PrimarySkillType primarySkillType) {
        if (alwaysVisible.contains(primarySkillType)) return;

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
        for (final PrimarySkillType primarySkillType : PrimarySkillType.values()) {
            xpBarSettingToggle(XPBarSettingTarget.HIDE, primarySkillType);
        }

        NotificationManager.sendPlayerInformationChatOnlyPrefixed(mcMMOPlayer.getPlayer(), "Commands.XPBar.DisableAll");
    }

    public void xpBarSettingToggle(@NotNull final XPBarSettingTarget settingTarget, @Nullable final PrimarySkillType skillType) {
        switch (settingTarget) {
            case SHOW -> {
                disabledBars.remove(skillType);
                alwaysVisible.add(skillType);

                //Remove lingering tasks
                if (experienceBarHideTaskHashMap.containsKey(skillType)) {
                    experienceBarHideTaskHashMap.get(skillType).cancel();
                }

                updateExperienceBar(skillType);
            }
            case HIDE -> {
                alwaysVisible.remove(skillType);
                disabledBars.add(skillType);

                //Remove lingering tasks
                if (experienceBarHideTaskHashMap.containsKey(skillType)) {
                    experienceBarHideTaskHashMap.get(skillType).cancel();
                }

                hideExperienceBar(skillType);
            }
            case RESET -> resetBarSettings();
        }

        informPlayer(settingTarget, skillType);
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
