package com.gmail.nossr50.events.skills.fishing;

import static java.util.Objects.requireNonNull;
import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.util.player.UserManager;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class McMMOPlayerMagicHunterEvent extends McMMOPlayerFishingTreasureEvent {
    private final Map<Enchantment, Integer> enchants = new HashMap<>();

    @Deprecated(forRemoval = true, since = "2.2.010")
    public McMMOPlayerMagicHunterEvent(@NotNull final Player player, @NotNull final ItemStack treasure, final int xp,
                                       @NotNull final Map<Enchantment, Integer> enchants) {
        this(requireNonNull(UserManager.getPlayer(player)), treasure, xp, enchants);
    }

    public McMMOPlayerMagicHunterEvent(@NotNull final McMMOPlayer mmoPlayer, @NotNull final ItemStack treasure,
                                       final int xp, @NotNull final Map<Enchantment, Integer> enchants) {
        super(mmoPlayer, treasure, xp);
        requireNonNull(enchants, "enchants cannot be null");
        this.enchants.putAll(enchants);
    }

    public @NotNull Map<Enchantment, Integer> getEnchantments() {
        return enchants;
    }
}
