package com.gmail.nossr50.events.skills.abilities;

import static java.util.Objects.requireNonNull;
import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.datatypes.skills.PrimarySkillType;
import com.gmail.nossr50.util.player.UserManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class McMMOPlayerAbilityDeactivateEvent extends McMMOPlayerAbilityEvent {
    @Deprecated(forRemoval = true, since = "2.2.010")
    public McMMOPlayerAbilityDeactivateEvent(@NotNull final Player player,
                                             @NotNull final PrimarySkillType skill) {
        this(requireNonNull(UserManager.getPlayer(player)), skill);
    }

    public McMMOPlayerAbilityDeactivateEvent(@NotNull final McMMOPlayer mmoPlayer,
                                             @NotNull final PrimarySkillType skill) {
        super(mmoPlayer, skill);
    }
}
