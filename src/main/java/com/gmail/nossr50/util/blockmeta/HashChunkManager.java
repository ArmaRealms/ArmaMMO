package com.gmail.nossr50.util.blockmeta;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class HashChunkManager implements ChunkManager {
    private final HashMap<CoordinateKey, McMMOSimpleRegionFile> regionMap = new HashMap<>(); // Tracks active regions
    private final HashMap<CoordinateKey, HashSet<CoordinateKey>> chunkUsageMap = new HashMap<>(); // Tracks active chunks by region
    private final HashMap<CoordinateKey, ChunkStore> chunkMap = new HashMap<>(); // Tracks active chunks

    @Override
    public synchronized void closeAll() {
        // Save all dirty chunkstores
        for (final ChunkStore chunkStore : chunkMap.values()) {
            if (!chunkStore.isDirty()) {
                continue;
            }
            final World world = Bukkit.getWorld(chunkStore.getWorldId());
            if (world == null) {
                continue; // Oh well
            }
            writeChunkStore(world, chunkStore);
        }
        // Clear in memory chunks
        chunkMap.clear();
        chunkUsageMap.clear();
        // Close all region files
        for (final McMMOSimpleRegionFile rf : regionMap.values()) {
            rf.close();
        }
        regionMap.clear();
    }

    private synchronized @Nullable ChunkStore readChunkStore(@NotNull final World world, final int cx, final int cz)
            throws IOException {
        final McMMOSimpleRegionFile rf = getWriteableSimpleRegionFile(world, cx, cz);
        try (final DataInputStream in = rf.getInputStream(cx, cz)) { // Get input stream for chunk
            if (in == null) {
                return null; // No chunk
            }
            return BitSetChunkStore.Serialization.readChunkStore(in); // Read in the chunkstore
        }
    }

    private synchronized void writeChunkStore(@NotNull final World world, @NotNull final ChunkStore data) {
        if (!data.isDirty()) {
            return; // Don't save unchanged data
        }
        try {
            final McMMOSimpleRegionFile rf = getWriteableSimpleRegionFile(world, data.getChunkX(),
                    data.getChunkZ());
            try (final DataOutputStream out = rf.getOutputStream(data.getChunkX(), data.getChunkZ())) {
                BitSetChunkStore.Serialization.writeChunkStore(out, data);
            }
            data.setDirty(false);
        } catch (final IOException e) {
            throw new RuntimeException(
                    "Unable to write chunk meta data for " + data.getChunkX() + ", "
                            + data.getChunkZ(), e);
        }
    }

    private synchronized @NotNull McMMOSimpleRegionFile getWriteableSimpleRegionFile(
            @NotNull final World world, final int cx, final int cz) {
        final CoordinateKey regionKey = toRegionKey(world.getUID(), cx, cz);

        return regionMap.computeIfAbsent(regionKey, k -> {
            final File regionFile = getRegionFile(world, regionKey);
            regionFile.getParentFile().mkdirs();
            return new McMMOSimpleRegionFile(regionFile, regionKey.x, regionKey.z);
        });
    }

    private @NotNull File getRegionFile(@NotNull final World world, @NotNull final CoordinateKey regionKey) {
        if (world.getUID() != regionKey.worldID) {
            throw new IllegalArgumentException();
        }
        return new File(new File(world.getWorldFolder(), "mcmmo_regions"),
                "mcmmo_" + regionKey.x + "_" + regionKey.z + "_.mcm");
    }

    private @Nullable ChunkStore loadChunk(final int cx, final int cz, @NotNull final World world) {
        try {
            return readChunkStore(world, cx, cz);
        } catch (final Exception ignored) {
        }

        return null;
    }

    private void unloadChunk(final int cx, final int cz, @NotNull final World world) {
        final CoordinateKey chunkKey = toChunkKey(world.getUID(), cx, cz);
        final ChunkStore chunkStore = chunkMap.remove(chunkKey); // Remove from chunk map
        if (chunkStore == null) {
            return;
        }

        if (chunkStore.isDirty()) {
            writeChunkStore(world, chunkStore);
        }

        final CoordinateKey regionKey = toRegionKey(world.getUID(), cx, cz);
        final HashSet<CoordinateKey> chunkKeys = chunkUsageMap.get(regionKey);
        chunkKeys.remove(chunkKey); // remove from region file in-use set
        // If it was last chunk in the region, close the region file and remove it from memory
        if (chunkKeys.isEmpty()) {
            chunkUsageMap.remove(regionKey);
            regionMap.remove(regionKey).close();
        }
    }

    @Override
    public synchronized void chunkUnloaded(final int cx, final int cz, @NotNull final World world) {
        unloadChunk(cx, cz, world);
    }

    @Override
    public synchronized void unloadWorld(@NotNull final World world) {
        final UUID wID = world.getUID();

        // Save and remove all the chunks
        final List<CoordinateKey> chunkKeys = new ArrayList<>(chunkMap.keySet());
        for (final CoordinateKey chunkKey : chunkKeys) {
            if (!wID.equals(chunkKey.worldID)) {
                continue;
            }
            final ChunkStore chunkStore = chunkMap.remove(chunkKey);
            if (!chunkStore.isDirty()) {
                continue;
            }
            try {
                writeChunkStore(world, chunkStore);
            } catch (final Exception ignore) {
            }
        }
        // Clear all the region files
        final List<CoordinateKey> regionKeys = new ArrayList<>(regionMap.keySet());
        for (final CoordinateKey regionKey : regionKeys) {
            if (!wID.equals(regionKey.worldID)) {
                continue;
            }
            regionMap.remove(regionKey).close();
            chunkUsageMap.remove(regionKey);
        }
    }

    private synchronized boolean isIneligible(final int x, final int y, final int z, @NotNull final World world) {
        final CoordinateKey chunkKey = blockCoordinateToChunkKey(world.getUID(), x, y, z);

        // Get chunk, load from file if necessary
        // Get/Load/Create chunkstore
        final ChunkStore check = chunkMap.computeIfAbsent(chunkKey, k -> {
            // Load from file
            final ChunkStore loaded = loadChunk(chunkKey.x, chunkKey.z, world);
            if (loaded != null) {
                chunkUsageMap.computeIfAbsent(toRegionKey(chunkKey.worldID, chunkKey.x, chunkKey.z),
                        j -> new HashSet<>()).add(chunkKey);
                return loaded;
            }
            // Mark chunk in-use for region tracking
            chunkUsageMap.computeIfAbsent(toRegionKey(chunkKey.worldID, chunkKey.x, chunkKey.z),
                    j -> new HashSet<>()).add(chunkKey);
            // Create a new chunkstore
            return new BitSetChunkStore(world, chunkKey.x, chunkKey.z);
        });

        final int ix = Math.abs(x) % 16;
        final int iz = Math.abs(z) % 16;

        return check.isTrue(ix, y, iz);
    }

    @Override
    public synchronized boolean isIneligible(@NotNull final Block block) {
        return isIneligible(block.getX(), block.getY(), block.getZ(), block.getWorld());
    }

    @Override
    public synchronized boolean isIneligible(@NotNull final BlockState blockState) {
        return isIneligible(blockState.getX(), blockState.getY(), blockState.getZ(),
                blockState.getWorld());
    }

    @Override
    public synchronized boolean isEligible(@NotNull final Block block) {
        return !isIneligible(block);
    }

    @Override
    public synchronized boolean isEligible(@NotNull final BlockState blockState) {
        return !isIneligible(blockState);
    }

    @Override
    public synchronized void setIneligible(@NotNull final Block block) {
        set(block.getX(), block.getY(), block.getZ(), block.getWorld(), true);
    }

    @Override
    public synchronized void setIneligible(@NotNull final BlockState blockState) {
        set(blockState.getX(), blockState.getY(), blockState.getZ(), blockState.getWorld(), true);
    }

    @Override
    public synchronized void setEligible(@NotNull final Block block) {
        set(block.getX(), block.getY(), block.getZ(), block.getWorld(), false);
    }

    @Override
    public synchronized void setEligible(@NotNull final BlockState blockState) {
        set(blockState.getX(), blockState.getY(), blockState.getZ(), blockState.getWorld(), false);
    }

    private synchronized void set(final int x, final int y, final int z, @NotNull final World world, final boolean value) {
        final CoordinateKey chunkKey = blockCoordinateToChunkKey(world.getUID(), x, y, z);

        // Get/Load/Create chunkstore
        final ChunkStore cStore = chunkMap.computeIfAbsent(chunkKey, k -> {
            // Load from file
            final ChunkStore loaded = loadChunk(chunkKey.x, chunkKey.z, world);
            if (loaded != null) {
                chunkUsageMap.computeIfAbsent(toRegionKey(chunkKey.worldID, chunkKey.x, chunkKey.z),
                        j -> new HashSet<>()).add(chunkKey);
                return loaded;
            }
            // Mark chunk in-use for region tracking
            chunkUsageMap.computeIfAbsent(toRegionKey(chunkKey.worldID, chunkKey.x, chunkKey.z),
                    j -> new HashSet<>()).add(chunkKey);
            // Create a new chunkstore
            return new BitSetChunkStore(world, chunkKey.x, chunkKey.z);
        });

        // Get block offset (offset from chunk corner)
        final int ix = Math.abs(x) % 16;
        final int iz = Math.abs(z) % 16;

        // Set chunk store value
        cStore.set(ix, y, iz, value);
    }

    private @NotNull CoordinateKey blockCoordinateToChunkKey(@NotNull final UUID worldUid, final int x, final int y,
                                                             final int z) {
        return toChunkKey(worldUid, x >> 4, z >> 4);
    }

    private @NotNull CoordinateKey toChunkKey(@NotNull final UUID worldUid, final int cx, final int cz) {
        return new CoordinateKey(worldUid, cx, cz);
    }

    private @NotNull CoordinateKey toRegionKey(@NotNull final UUID worldUid, final int cx, final int cz) {
        // Compute region index (32x32 chunk regions)
        final int rx = cx >> 5;
        final int rz = cz >> 5;
        return new CoordinateKey(worldUid, rx, rz);
    }

    private static final class CoordinateKey {
        public final @NotNull UUID worldID;
        public final int x;
        public final int z;

        private CoordinateKey(@NotNull final UUID worldID, final int x, final int z) {
            this.worldID = worldID;
            this.x = x;
            this.z = z;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final CoordinateKey coordinateKey = (CoordinateKey) o;
            return x == coordinateKey.x &&
                    z == coordinateKey.z &&
                    worldID.equals(coordinateKey.worldID);
        }

        @Override
        public int hashCode() {
            return Objects.hash(worldID, x, z);
        }
    }
}
