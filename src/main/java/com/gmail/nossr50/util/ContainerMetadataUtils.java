package com.gmail.nossr50.util;

import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.util.player.UserManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

import static com.gmail.nossr50.util.MetadataService.NSK_CONTAINER_UUID_LEAST_SIG;
import static com.gmail.nossr50.util.MetadataService.NSK_CONTAINER_UUID_MOST_SIG;

public class ContainerMetadataUtils {

    public static void changeContainerOwnership(@Nullable final BlockState blockState,
                                                @Nullable final Player player) {
        // no-op when the blockState is null or player is null
        if (blockState == null || player == null) {
            return;
        }

        final McMMOPlayer mmoPlayer = UserManager.getPlayer(player);

        /*
            Debug output
         */
        printOwnershipGainDebug(blockState, mmoPlayer);
        printOwnershipLossDebug(blockState);
        setOwner(blockState, player.getUniqueId());
    }

    public static void printOwnershipGainDebug(@NotNull final BlockState blockState,
                                               @Nullable final McMMOPlayer mmoPlayer) {
        if (mmoPlayer != null && mmoPlayer.isDebugMode()) {
            mmoPlayer.getPlayer().sendMessage("Container ownership " +
                    ChatColor.GREEN + "gained " + ChatColor.RESET +
                    "at location: " + blockState.getLocation());
        }
    }

    public static void printOwnershipLossDebug(final BlockState blockState) {
        final OfflinePlayer containerOwner = getContainerOwner(blockState);

        if (containerOwner != null && containerOwner.isOnline()) {
            final McMMOPlayer mmoContainerOwner = UserManager.getPlayer(containerOwner.getPlayer());

            if (mmoContainerOwner != null) {
                if (mmoContainerOwner.isDebugMode()) {
                    mmoContainerOwner.getPlayer().sendMessage("Container ownership " +
                            ChatColor.RED + "lost " + ChatColor.RESET +
                            "at location: " + blockState.getLocation());
                }
            }
        }
    }

    public static @Nullable OfflinePlayer getContainerOwner(final BlockState container) {
        if (container instanceof final PersistentDataHolder persistentDataHolder) {
            final UUID uuid = getOwner(persistentDataHolder);

            if (uuid != null) {
                return Bukkit.getOfflinePlayer(uuid);
            }
        }

        return null;
    }

    public static boolean isContainerOwned(final BlockState blockState) {
        return getContainerOwner(blockState) != null;
    }

    public static void processContainerOwnership(final BlockState blockState, final Player player) {
        // no-op when the blockState is null or player is null
        if (blockState == null || player == null) {
            return;
        }

        if (getContainerOwner(blockState) != null) {
            if (getContainerOwner(blockState).getUniqueId().equals(player.getUniqueId())) {
                return;
            }
        }

        changeContainerOwnership(blockState, player);
    }

    public static @Nullable UUID getOwner(@NotNull final PersistentDataHolder persistentDataHolder) {
        //Get container from entity
        final PersistentDataContainer dataContainer = persistentDataHolder.getPersistentDataContainer();

        //Too lazy to make a custom data type for this stuff
        final Long mostSigBits = dataContainer.get(NSK_CONTAINER_UUID_MOST_SIG,
                PersistentDataType.LONG);
        final Long leastSigBits = dataContainer.get(NSK_CONTAINER_UUID_LEAST_SIG,
                PersistentDataType.LONG);

        if (mostSigBits != null && leastSigBits != null) {
            return new UUID(mostSigBits, leastSigBits);
        } else {
            return null;
        }
    }

    public static void setOwner(@NotNull final BlockState blockState, @NotNull final UUID uuid) {
        final PersistentDataContainer dataContainer = ((PersistentDataHolder) blockState).getPersistentDataContainer();

        dataContainer.set(NSK_CONTAINER_UUID_MOST_SIG, PersistentDataType.LONG,
                uuid.getMostSignificantBits());
        dataContainer.set(NSK_CONTAINER_UUID_LEAST_SIG, PersistentDataType.LONG,
                uuid.getLeastSignificantBits());

        blockState.update();
    }
}
