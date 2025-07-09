package com.gmail.nossr50.commands.player;

import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.datatypes.skills.SuperAbilityType;
import com.gmail.nossr50.locale.LocaleLoader;
import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.util.commands.CommandUtils;
import com.gmail.nossr50.util.player.UserManager;
import com.gmail.nossr50.util.scoreboards.ScoreboardManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MccooldownCommand implements TabExecutor {
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

            if (mcMMO.p.getGeneralConfig().getScoreboardsEnabled() && mcMMO.p.getGeneralConfig().getCooldownUseBoard()) {
                ScoreboardManager.enablePlayerCooldownScoreboard(player);

                if (!mcMMO.p.getGeneralConfig().getCooldownUseChat()) {
                    return true;
                }
            }

            McMMOPlayer mmoPlayer = UserManager.getPlayer(player);
            if (mmoPlayer == null) {
                player.sendMessage(LocaleLoader.getString("Profile.PendingLoad"));
                return true;
            }

            player.sendMessage(LocaleLoader.getString("Commands.Cooldowns.Header"));
            player.sendMessage(LocaleLoader.getString("mcMMO.NoSkillNote"));

            for (SuperAbilityType ability : SuperAbilityType.values()) {
                if (!ability.getPermissions(player)) {
                    continue;
                }

                int seconds = mmoPlayer.calculateTimeRemaining(ability);

                if (seconds <= 0) {
                    player.sendMessage(LocaleLoader.getString("Commands.Cooldowns.Row.Y", ability.getLocalizedName()));
                } else {
                    player.sendMessage(LocaleLoader.getString("Commands.Cooldowns.Row.N", ability.getLocalizedName(), seconds));
                }
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
