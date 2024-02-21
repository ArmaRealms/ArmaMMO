package com.gmail.nossr50.commands.skills;

import com.gmail.nossr50.datatypes.skills.PrimarySkillType;
import com.gmail.nossr50.locale.LocaleLoader;
import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.util.text.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class SkillGuideCommand implements CommandExecutor {
    private final String header;
    private final String guide;

    public SkillGuideCommand(PrimarySkillType skill) {
        header = LocaleLoader.getString("Guides.Header", mcMMO.p.getSkillTools().getLocalizedSkillName(skill));
        guide = LocaleLoader.getString("Guides." + StringUtils.getCapitalized(skill.toString()) + ".Section.0");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 1) {
            if (!args[0].equals("?")) {
                return false;
            }

            sender.sendMessage(header);
            sender.sendMessage(guide);
            return true;
        }

        return false;
    }

}
