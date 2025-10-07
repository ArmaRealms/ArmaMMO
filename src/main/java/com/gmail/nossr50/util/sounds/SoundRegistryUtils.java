package com.gmail.nossr50.util.sounds;

import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.util.AttributeMapper;
import com.gmail.nossr50.util.LogUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static java.lang.String.format;

public final class SoundRegistryUtils {

    public static final String PAPER_SOUND_REGISTRY_FIELD = "SOUND_EVENT";
    public static final String SPIGOT_SOUND_REGISTRY_FIELD = "SOUNDS";
    public static final String METHOD_GET_OR_THROW_NAME = "getOrThrow";
    public static final String METHOD_GET_NAME = "get";
    private static Method registryLookup;
    private static Object soundReg;

    static {
        boolean foundRegistry = false;
        final Class<?> registry;
        try {
            registry = Class.forName(AttributeMapper.ORG_BUKKIT_REGISTRY);
            try {
                // First check for Paper's sound registry, held by field SOUND_EVENT
                soundReg = registry.getField(PAPER_SOUND_REGISTRY_FIELD).get(null);
                foundRegistry = true;
            } catch (final NoSuchFieldException | IllegalAccessException e) {
                try {
                    soundReg = registry.getField(SPIGOT_SOUND_REGISTRY_FIELD);
                    foundRegistry = true;
                } catch (final NoSuchFieldException ex) {
                    // ignored
                }
            }
        } catch (final ClassNotFoundException e) {
            // ignored
        }

        if (foundRegistry) {
            try {
                // getOrThrow isn't in all API versions, but we use it if it exists
                registryLookup = soundReg.getClass().getMethod(METHOD_GET_OR_THROW_NAME,
                        NamespacedKey.class);
            } catch (final NoSuchMethodException e) {
                try {
                    registryLookup = soundReg.getClass().getMethod(METHOD_GET_NAME,
                            NamespacedKey.class);
                } catch (final NoSuchMethodException ex) {
                    // ignored exception
                    registryLookup = null;
                }
            }
        }
    }

    public static boolean useLegacyLookup() {
        return registryLookup == null;
    }

    public static @Nullable Sound getSound(final String id, final String fallBackId) {
        if (registryLookup != null) {
            try {
                return (Sound) registryLookup.invoke(soundReg, NamespacedKey.fromString(id));
            } catch (final InvocationTargetException | IllegalAccessException
                           | IllegalArgumentException e) {
                if (fallBackId != null) {
                    LogUtils.debug(mcMMO.p.getLogger(),
                            format("Could not find sound with ID '%s', trying fallback ID '%s'", id,
                                    fallBackId));
                    try {
                        return (Sound) registryLookup.invoke(soundReg,
                                NamespacedKey.fromString(fallBackId));
                    } catch (final IllegalAccessException | InvocationTargetException ex) {
                        mcMMO.p.getLogger().severe(format("Could not find sound with ID %s,"
                                + " fallback ID of %s also failed.", id, fallBackId));
                    }
                } else {
                    mcMMO.p.getLogger().severe(format("Could not find sound with ID %s.", id));
                }
                throw new RuntimeException(e);
            }
        }
        return null;
    }
}
