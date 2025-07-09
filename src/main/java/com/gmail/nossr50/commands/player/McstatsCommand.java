package com.gmail.nossr50.commands.player;

import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.locale.LocaleLoader;
import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.util.commands.CommandUtils;
import com.gmail.nossr50.util.player.UserManager;
import com.gmail.nossr50.util.scoreboards.ScoreboardManager;
import com.gmail.nossr50.util.text.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class McstatsCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(LocaleLoader.getString("Commands.NoConsole"));
            return true;
        }

        if (!CommandUtils.hasPlayerDataKey(sender)) {
            return true;
        }

        if (args.length == 0) {
            McMMOPlayer mmoPlayer = UserManager.getPlayer(player);
            if (mmoPlayer == null) {
                sender.sendMessage(LocaleLoader.getString("Profile.PendingLoad"));
                return true;
            }

            if (mcMMO.p.getGeneralConfig().getStatsUseBoard() && mcMMO.p.getGeneralConfig().getScoreboardsEnabled()) {
                ScoreboardManager.enablePlayerStatsScoreboard(player);

                if (!mcMMO.p.getGeneralConfig().getStatsUseChat()) {
                    return true;
                }
            }

            player.sendMessage(LocaleLoader.getString("Stats.Own.Stats"));
            player.sendMessage(LocaleLoader.getString("mcMMO.NoSkillNote"));

            CommandUtils.printGatheringSkills(player);
            CommandUtils.printCombatSkills(player);
            CommandUtils.printMiscSkills(player);

            int powerLevelCap = mcMMO.p.getGeneralConfig().getPowerLevelCap();

            if (powerLevelCap != Integer.MAX_VALUE) {
                player.sendMessage(LocaleLoader.getString("Commands.PowerLevel.Capped", StringUtils.formatNumber(mmoPlayer.getPowerLevel()), powerLevelCap));
            } else {
                player.sendMessage(LocaleLoader.getString("Commands.PowerLevel", StringUtils.formatNumber(mmoPlayer.getPowerLevel())));
            }

            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        return List.of();
    }
}
