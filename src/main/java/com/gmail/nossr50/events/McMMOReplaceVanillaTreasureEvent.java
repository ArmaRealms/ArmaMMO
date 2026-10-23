package com.gmail.nossr50.events;

import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class McMMOReplaceVanillaTreasureEvent extends Event {
    /**
     * Rest of file is required boilerplate for custom events
     **/
    private static final @NotNull HandlerList handlers = new HandlerList();
    private final @NotNull Item originalItem;
    private final @Nullable Player causingPlayer;
    private @NotNull ItemStack replacementItemStack;

    public McMMOReplaceVanillaTreasureEvent(@NotNull Item originalItem,
                                            @NotNull ItemStack replacementItemStack) {
        this(originalItem, replacementItemStack, null);
    }

    public McMMOReplaceVanillaTreasureEvent(@NotNull Item originalItem,
                                            @NotNull ItemStack replacementItemStack, @Nullable Player causingPlayer) {
        this.originalItem = originalItem;
        this.replacementItemStack = replacementItemStack;
        this.causingPlayer = causingPlayer;
    }

    public static @NotNull HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public @NotNull ItemStack getReplacementItemStack() {
        return replacementItemStack;
    }

    public void setReplacementItemStack(@NotNull ItemStack replacementItemStack) {
        this.replacementItemStack = replacementItemStack;
    }

    public @Nullable Player getCausingPlayer() {
        return causingPlayer;
    }

    public @NotNull Item getOriginalItem() {
        return originalItem;
    }
}
