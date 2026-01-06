package com.gmail.nossr50.util.blockmeta;

import com.gmail.nossr50.config.PersistentDataConfig;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public class ChunkManagerFactory {
    public static @NotNull ChunkManager getChunkManager() {

        if (PersistentDataConfig.getInstance().useBlockTracker()) {
            if (PersistentDataConfig.getInstance().usePlayerBlockTrackerHook()
                    && Bukkit.getPluginManager().isPluginEnabled("PlayerBlockTracker")) {
                return new PlayerBlockTrackerHook();
            }
            return new HashChunkManager();
        }

        return new NullChunkManager();
    }
}
