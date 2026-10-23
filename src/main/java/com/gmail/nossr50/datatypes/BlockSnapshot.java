package com.gmail.nossr50.datatypes;

import org.bukkit.Material;
import org.bukkit.block.Block;

/**
 * Contains a snapshot of a block at a specific moment in time Used to check before/after type
 * stuff
 */
public record BlockSnapshot(Material oldType, Block blockRef) {

    public boolean hasChangedType() {
        return oldType != blockRef.getState().getType();
    }
}
