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
    private final McMMOPlayer mmoPlayer;
    int delaySeconds = 3;

    private HashMap<PrimarySkillType, ExperienceBarWrapper> experienceBars;
    private HashMap<PrimarySkillType, ExperienceBarHideTask> experienceBarHideTaskHashMap;

    private HashSet<PrimarySkillType> alwaysVisible;
    private HashSet<PrimarySkillType> disabledBars;

    public ExperienceBarManager(McMMOPlayer mmoPlayer) {
        this.mmoPlayer = mmoPlayer;
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

    public void updateExperienceBar(PrimarySkillType primarySkillType) {
        ExperienceConfig config = ExperienceConfig.getInstance();

        if (disabledBars.contains(primarySkillType) || !config.isExperienceBarsEnabled() || !config.isExperienceBarEnabled(primarySkillType)) {
            return;
        }

        // Get or Initialize Bar
        ExperienceBarWrapper experienceBarWrapper = experienceBars.computeIfAbsent(primarySkillType,
                key -> new ExperienceBarWrapper(primarySkillType, mmoPlayer));

        // Update Progress and Show Bar
        experienceBarWrapper.setProgress(mmoPlayer.getProgressInCurrentSkillLevel(primarySkillType));
        experienceBarWrapper.showExperienceBar();

        // Cancel any existing Hide Task
        ExperienceBarHideTask existingTask = experienceBarHideTaskHashMap.remove(primarySkillType);
        if (existingTask != null) {
            existingTask.cancel();
        }

        // Schedule new Hide Task
        scheduleHideTask(primarySkillType);
    }


    private void scheduleHideTask(PrimarySkillType primarySkillType) {
        if (alwaysVisible.contains(primarySkillType)) return;

        ExperienceBarHideTask experienceBarHideTask = new ExperienceBarHideTask(this, mmoPlayer, primarySkillType);
        mcMMO.p.getFoliaLib().getScheduler().runAtEntityLater(mmoPlayer.getPlayer(), experienceBarHideTask, (long) delaySeconds * Misc.TICK_CONVERSION_FACTOR);
        experienceBarHideTaskHashMap.put(primarySkillType, experienceBarHideTask);
    }

    public void hideExperienceBar(PrimarySkillType primarySkillType) {
        if (experienceBars.containsKey(primarySkillType))
            experienceBars.get(primarySkillType).hideExperienceBar();
    }

    public void clearTask(PrimarySkillType primarySkillType) {
        experienceBarHideTaskHashMap.remove(primarySkillType);
    }

    public void disableAllBars() {
        for (PrimarySkillType primarySkillType : PrimarySkillType.values()) {
            xpBarSettingToggle(XPBarSettingTarget.HIDE, primarySkillType);
        }

        NotificationManager.sendPlayerInformationChatOnlyPrefixed(mmoPlayer.getPlayer(), "Commands.XPBar.DisableAll");
    }

    public void xpBarSettingToggle(@NotNull XPBarSettingTarget settingTarget, @Nullable PrimarySkillType skillType) {
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
        for (PrimarySkillType permanent : alwaysVisible) {
            hideExperienceBar(permanent);
        }

        alwaysVisible.clear();
        disabledBars.clear();

        //Hide child skills by default
        disabledBars.add(PrimarySkillType.SALVAGE);
        disabledBars.add(PrimarySkillType.SMELTING);
    }

    private void informPlayer(@NotNull ExperienceBarManager.@NotNull XPBarSettingTarget settingTarget, @Nullable PrimarySkillType primarySkillType) {
        //Inform player of setting change
        if (settingTarget != XPBarSettingTarget.RESET) {
            NotificationManager.sendPlayerInformationChatOnlyPrefixed(mmoPlayer.getPlayer(), "Commands.XPBar.SettingChanged", mcMMO.p.getSkillTools().getLocalizedSkillName(primarySkillType), settingTarget.toString());
        } else {
            NotificationManager.sendPlayerInformationChatOnlyPrefixed(mmoPlayer.getPlayer(), "Commands.XPBar.Reset");
        }
    }

    public enum XPBarSettingTarget {SHOW, HIDE, RESET, DISABLE}
}
