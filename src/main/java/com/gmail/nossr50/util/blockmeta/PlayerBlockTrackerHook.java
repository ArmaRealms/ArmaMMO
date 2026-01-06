package com.gmail.nossr50.util.blockmeta;

import com.gestankbratwurst.playerblocktracker.PlayerBlockTracker;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.jetbrains.annotations.NotNull;


public class PlayerBlockTrackerHook implements ChunkManager {

    /**
     * Check to see if a given {@link org.bukkit.block.Block} is ineligible for rewards. This is a location-based
     * lookup, and the other properties of the {@link org.bukkit.block.Block} do not matter.
     *
     * @param block Block to check
     * @return true if the given block should not give rewards, false if otherwise
     */
    @Override
    public boolean isIneligible(@NotNull final Block block) {
        return PlayerBlockTracker.isTracked(block);
    }

    /**
     * Check to see if a given {@link org.bukkit.block.Block} is eligible for rewards. This is a location-based
     * lookup, and the other properties of the {@link org.bukkit.block.Block} do not matter.
     *
     * @param block Block to check
     * @return true if the given block should give rewards, false if otherwise
     */
    @Override
    public boolean isEligible(@NotNull final Block block) {
        return !PlayerBlockTracker.isTracked(block);
    }

    /**
     * Check to see if a given {@link org.bukkit.block.BlockState} is eligible for rewards. This is a location-based
     * lookup, and the other properties of the {@link org.bukkit.block.BlockState} do not matter.
     *
     * @param blockState BlockState to check
     * @return true if the given BlockState location is set to true, false if otherwise
     */
    @Override
    public boolean isEligible(@NotNull final BlockState blockState) {
        return PlayerBlockTracker.isTracked(blockState.getBlock());
    }

    /**
     * Check to see if a given {@link org.bukkit.block.BlockState} is ineligible for rewards. This is a
     * location-based lookup, and the other properties of the {@link org.bukkit.block.BlockState} do not matter.
     *
     * @param blockState BlockState to check
     * @return true if the given BlockState location is set to true, false if otherwise
     */
    @Override
    public boolean isIneligible(@NotNull final BlockState blockState) {
        return !PlayerBlockTracker.isTracked(blockState.getBlock());
    }

    /**
     * Set a given {@link org.bukkit.block.Block} as ineligible for rewards. This is a location-based lookup, and the
     * other properties of the {@link org.bukkit.block.Block} do not matter.
     *
     * @param block block whose location to set as ineligible
     */
    @Override
    public void setIneligible(@NotNull final Block block) {
        if (!PlayerBlockTracker.isTracked(block)) {
            PlayerBlockTracker.track(block);
        }
    }

    /**
     * Set a given BlockState location to true
     *
     * @param blockState BlockState location to set
     */
    @Override
    public void setIneligible(@NotNull final BlockState blockState) {
        if (!PlayerBlockTracker.isTracked(blockState.getBlock())) {
            PlayerBlockTracker.track(blockState.getBlock());
        }
    }

    /**
     * Set a given {@link org.bukkit.block.Block} as eligible for rewards. This is a location-based lookup, and the
     * other properties of the {@link org.bukkit.block.Block} do not matter.
     *
     * @param block block whose location to set as eligible
     */
    @Override
    public void setEligible(@NotNull final Block block) {
        if (PlayerBlockTracker.isTracked(block)) {
            PlayerBlockTracker.unTrack(block);
        }
    }

    /**
     * Set a given BlockState location to false
     *
     * @param blockState BlockState location to set
     */
    @Override
    public void setEligible(@NotNull final BlockState blockState) {
        if (PlayerBlockTracker.isTracked(blockState.getBlock())) {
            PlayerBlockTracker.unTrack(blockState.getBlock());
        }
    }

    @Override
    public void closeAll() {
        // No action needed
    }

    @Override
    public void chunkUnloaded(final int cx, final int cz, @NotNull final World world) {
        // No action needed
    }

    @Override
    public void unloadWorld(@NotNull final World world) {
        // No action needed
    }
}
