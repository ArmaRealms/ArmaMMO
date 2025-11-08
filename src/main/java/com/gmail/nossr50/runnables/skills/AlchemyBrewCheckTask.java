package com.gmail.nossr50.runnables.skills;

import static com.gmail.nossr50.skills.alchemy.AlchemyPotionBrewer.isValidBrew;
import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.skills.alchemy.Alchemy;
import com.gmail.nossr50.util.CancellableRunnable;
import com.gmail.nossr50.util.ContainerMetadataUtils;
import com.gmail.nossr50.util.player.UserManager;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.BrewingStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class AlchemyBrewCheckTask extends CancellableRunnable {
    private final BrewingStand brewingStand;
    private final ItemStack[] oldInventory;

    @Deprecated(forRemoval = true, since = "2.2.010")
    public AlchemyBrewCheckTask(@Nullable final Player ignored, final BrewingStand brewingStand) {
        this(brewingStand);
    }

    public AlchemyBrewCheckTask(@NotNull final BrewingStand brewingStand) {
        this.brewingStand = brewingStand;
        this.oldInventory = Arrays.copyOfRange(brewingStand.getInventory().getContents(), 0, 4);
    }

    @Override
    public void run() {
        final OfflinePlayer offlinePlayer = ContainerMetadataUtils.getContainerOwner(brewingStand);
        int ingredientLevel = 1;
        if (offlinePlayer != null && offlinePlayer.isOnline()) {
            final McMMOPlayer mmoPlayer = UserManager.getPlayer(offlinePlayer.getPlayer());
            if (mmoPlayer != null) {
                ingredientLevel = mmoPlayer.getAlchemyManager().getTier();
            }
        }
        final Location location = brewingStand.getLocation();
        final ItemStack[] newInventory = Arrays.copyOfRange(
                brewingStand.getInventory().getContents(), 0, 4);
        final boolean validBrew =
                brewingStand.getFuelLevel() > 0 && isValidBrew(ingredientLevel, newInventory);

        if (Alchemy.brewingStandMap.containsKey(location)) {
            if (oldInventory[Alchemy.INGREDIENT_SLOT] == null
                    || newInventory[Alchemy.INGREDIENT_SLOT] == null
                    || !oldInventory[Alchemy.INGREDIENT_SLOT].isSimilar(
                    newInventory[Alchemy.INGREDIENT_SLOT])
                    || !validBrew) {
                Alchemy.brewingStandMap.get(location).cancelBrew();
            }
        } else if (validBrew) {
            Alchemy.brewingStandMap.put(location, new AlchemyBrewTask(brewingStand));
        }
    }
}
