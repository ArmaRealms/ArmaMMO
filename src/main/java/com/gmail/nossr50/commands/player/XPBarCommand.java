package com.gmail.nossr50.commands.player;

import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.datatypes.skills.PrimarySkillType;
import com.gmail.nossr50.locale.LocaleLoader;
import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.util.experience.ExperienceBarManager;
import com.gmail.nossr50.util.player.NotificationManager;
import com.gmail.nossr50.util.player.UserManager;
import com.gmail.nossr50.util.skills.SkillUtils;
import com.gmail.nossr50.util.text.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class XPBarCommand implements TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(LocaleLoader.getString("Commands.NoConsole"));
            return false;
        }

        McMMOPlayer mmoPlayer = UserManager.getPlayer(player);
        if (mmoPlayer == null) {
            NotificationManager.sendPlayerInformationChatOnlyPrefixed(player, "Profile.PendingLoad");
            return false;
        }

        if (args.length == 0) {
            return false;
        }

        if (args.length < 2) {
            String option = args[0];
            if (option.equalsIgnoreCase(ExperienceBarManager.XPBarSettingTarget.RESET.toString())) {
                mmoPlayer.getExperienceBarManager().xpBarSettingToggle(ExperienceBarManager.XPBarSettingTarget.RESET, null);
                return true;
            }

            if (option.equalsIgnoreCase(ExperienceBarManager.XPBarSettingTarget.DISABLE.toString())) {
                mmoPlayer.getExperienceBarManager().disableAllBars();
                return true;
            }

            return false;
        }

        //Per skill Settings path
        if (args.length == 2) {
            String skillName = args[1];
            if (!SkillUtils.isSkill(skillName)) {
                return false;
            }

            PrimarySkillType targetSkill = mcMMO.p.getSkillTools().matchSkill(skillName);

            //Target setting
            String option = args[0].toLowerCase();

            ExperienceBarManager.XPBarSettingTarget settingTarget = getSettingTarget(option);
            if (settingTarget != null && settingTarget != ExperienceBarManager.XPBarSettingTarget.RESET) {
                //Change setting
                mmoPlayer.getExperienceBarManager().xpBarSettingToggle(settingTarget, targetSkill);
                return true;
            }
        }

        return false;
    }

    private @Nullable ExperienceBarManager.XPBarSettingTarget getSettingTarget(String string) {
        switch (string.toLowerCase()) {
            case "hide":
                return ExperienceBarManager.XPBarSettingTarget.HIDE;
            case "show":
                return ExperienceBarManager.XPBarSettingTarget.SHOW;
            case "reset":
                return ExperienceBarManager.XPBarSettingTarget.RESET;
            case "disable":
                return ExperienceBarManager.XPBarSettingTarget.DISABLE;
        }

        return null;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        switch (args.length) {
            case 1 -> {
                List<String> options = new ArrayList<>();

                for (ExperienceBarManager.XPBarSettingTarget settingTarget : ExperienceBarManager.XPBarSettingTarget.values()) {
                    options.add(StringUtils.getCapitalized(settingTarget.toString()));
                }

                return options.stream().filter(s -> s.startsWith(args[0])).toList();
            }
            case 2 -> {
                if (!args[0].equalsIgnoreCase(ExperienceBarManager.XPBarSettingTarget.RESET.toString())) {
                    mcMMO.p.getSkillTools().LOCALIZED_SKILL_NAMES.stream().filter(s -> s.startsWith(args[1])).toList();
                }
            }
            default -> {
                return List.of();
            }
        }

        return List.of();
    }
}
