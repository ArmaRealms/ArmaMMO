package com.gmail.nossr50.util;

import com.gmail.nossr50.mcMMO;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Reflection-based access to Paper-only APIs. All reflection is resolved once at class load and
 * cached. Unavailable methods use the documented fallback for each operation.
 * <p>
 * Why reflection? mcMMO compiles against the Spigot API for maximum compatibility. Paper-only
 * methods like {@code Player.lookAt()} are not available at compile time.
 */
public final class PaperUtil {

    private static final boolean IS_FOLIA;
    private static final boolean IS_PAPER;
    private static final Method GET_HOLDER_WITHOUT_SNAPSHOT;

    /**
     * Cached: {@code Player.lookAt(double, double, double, LookAnchor)}
     */
    private static final Method LOOK_AT_METHOD;

    /**
     * Cached: {@code LookAnchor.EYES} enum constant
     */
    private static final Object LOOK_ANCHOR_EYES;

    static {
        IS_FOLIA = classExists("io.papermc.paper.threadedregions.RegionizedServer");
        IS_PAPER = classExists("io.papermc.paper.entity.LookAnchor");

        Method lookAt = null;
        Object anchorEyes = null;

        if (IS_PAPER) {
            try {
                final Class<?> lookAnchorClass =
                        Class.forName("io.papermc.paper.entity.LookAnchor");
                anchorEyes = lookAnchorClass.getField("EYES").get(null);
                lookAt = Player.class.getMethod(
                        "lookAt", double.class, double.class, double.class, lookAnchorClass);
            } catch (final ReflectiveOperationException e) {
                mcMMO.p.getLogger().warning(
                        "Paper detected but Player.lookAt() could not be resolved — "
                                + "Daze will not change player look direction. " + e.getMessage());
                // lookAt and anchorEyes remain null — canLookAt() will return false
            }
        }

        LOOK_AT_METHOD = lookAt;
        LOOK_ANCHOR_EYES = anchorEyes;

        Method getHolder = null;
        try {
            getHolder = Inventory.class.getMethod("getHolder", boolean.class);
        } catch (final NoSuchMethodException ignored) {
            // Spigot only provides the snapshot-producing overload.
        }
        GET_HOLDER_WITHOUT_SNAPSHOT = getHolder;
    }

    private PaperUtil() {
    }

    /**
     * @return {@code true} if the server is running Folia
     */
    public static boolean isFolia() {
        return IS_FOLIA;
    }

    /**
     * @return {@code true} if the server is running Paper (or a Paper fork, including Folia)
     */
    public static boolean isPaper() {
        return IS_PAPER;
    }

    /**
     * @return whether the runtime inventory API can avoid creating a block-state snapshot
     */
    public static boolean canGetHolderWithoutSnapshot() {
        return GET_HOLDER_WITHOUT_SNAPSHOT != null;
    }

    /**
     * Gets the live holder on Paper, or the ordinary holder on Spigot.
     * Callers retaining a block state for later mutations must obtain a normal snapshot instead.
     *
     * @param inventory inventory whose holder is being inspected
     * @return the holder, or null for an inventory without one
     */
    public static @Nullable InventoryHolder getHolderWithoutSnapshot(final @NotNull Inventory inventory) {
        if (GET_HOLDER_WITHOUT_SNAPSHOT == null) {
            return inventory.getHolder();
        }
        try {
            return (InventoryHolder) GET_HOLDER_WITHOUT_SNAPSHOT.invoke(inventory, false);
        } catch (final IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Failed to get inventory holder without a snapshot", e);
        }
    }

    /**
     * @return {@code true} if we can call {@link #lookAt(Player, double, double, double)}
     * successfully (Paper or any Paper fork, including Folia)
     */
    public static boolean canLookAt() {
        return IS_PAPER && LOOK_AT_METHOD != null;
    }

    /**
     * Make a player look at the specified coordinates using Paper's
     * {@code Player.lookAt(x, y, z, LookAnchor.EYES)}.
     * <p>
     * This does <b>not</b> teleport the player — it only changes their view direction, avoiding
     * the race condition with death/world-change events that the old teleport approach caused.
     *
     * @param player the player whose view direction to change
     * @param x      target X coordinate
     * @param y      target Y coordinate
     * @param z      target Z coordinate
     */
    public static void lookAt(final @NotNull Player player,
                              final double x, final double y, final double z) {
        if (LOOK_AT_METHOD == null || LOOK_ANCHOR_EYES == null) {
            return;
        }
        try {
            LOOK_AT_METHOD.invoke(player, x, y, z, LOOK_ANCHOR_EYES);
        } catch (final IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to invoke Player.lookAt()", e);
        }
    }

    private static boolean classExists(final @NotNull String className) {
        try {
            Class.forName(className);
            return true;
        } catch (final ClassNotFoundException e) {
            return false;
        }
    }
}


