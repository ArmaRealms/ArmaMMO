package com.gmail.nossr50.util.platform;

import com.gmail.nossr50.mcMMO;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.logging.Logger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MinecraftGameVersionTest {

    private static @NotNull Stream<Arguments> getGameVersions() {
        /*
         * These samples were taken directly from the historical
         * data of CraftBukkit's pom.xml file:
         * https://hub.spigotmc.org/stash/projects/SPIGOT/repos/craftbukkit/browse/pom.xml
         *
         * We should be safe to assume that forks follow these conventions and do not mess
         * with this version number (Spigot, Paper and Tuinity do at least).
         */
        return Stream.of(
                Arguments.of("1.13.2-R0.1-SNAPSHOT", 1, 13, 2),
                Arguments.of("1.13-R0.2-SNAPSHOT", 1, 13, 0),
                Arguments.of("1.13.2-R0.1-SNAPSHOT", 1, 13, 2),
                Arguments.of("1.13-pre7-R0.1-SNAPSHOT", 1, 13, 0),
                Arguments.of("1.14-pre5-SNAPSHOT", 1, 14, 0),
                Arguments.of("1.15-R0.1-SNAPSHOT", 1, 15, 0),
                Arguments.of("1.16.5-R0.1-SNAPSHOT", 1, 16, 5),
                Arguments.of("1.17-R0.1-SNAPSHOT", 1, 17, 0)
        );
    }

    @Test
    void testAtLeast() {
        // TODO: Remove redundant tests
        final MinecraftGameVersion oneEightEight = new MinecraftGameVersion(1, 8, 8);
        final MinecraftGameVersion oneSixteenFive = new MinecraftGameVersion(1, 16, 5);
        final MinecraftGameVersion oneTwo = new MinecraftGameVersion(1, 2);

        // 1.8.8
        assertTrue(oneEightEight.isAtLeast(1, 8, 7));
        assertFalse(oneEightEight.isAtLeast(1, 9, 0));

        // 1.16.5
        assertTrue(oneSixteenFive.isAtLeast(1, 15, 2));
        assertFalse(oneSixteenFive.isAtLeast(1, 17, 0));

        // 1.2
        assertTrue(oneTwo.isAtLeast(1, 2, 0));

        // Test major version number
        final MinecraftGameVersion majorVersionTest = new MinecraftGameVersion(2, 0, 0);

        assertFalse(majorVersionTest.isAtLeast(3, 0, 0));
        assertFalse(majorVersionTest.isAtLeast(3, 1, 0));
        assertFalse(majorVersionTest.isAtLeast(3, 0, 2));

        assertTrue(majorVersionTest.isAtLeast(2, 0, 0));
        assertTrue(majorVersionTest.isAtLeast(1, 0, 0));

        // Test minor version number
        final MinecraftGameVersion minorVersionTest = new MinecraftGameVersion(0, 3, 0);

        assertFalse(minorVersionTest.isAtLeast(0, 4, 0));
        assertFalse(minorVersionTest.isAtLeast(1, 4, 0));
        assertFalse(minorVersionTest.isAtLeast(0, 4, 1));

        assertTrue(minorVersionTest.isAtLeast(0, 1, 0));
        assertTrue(minorVersionTest.isAtLeast(0, 2, 0));
        assertTrue(minorVersionTest.isAtLeast(0, 2, 1));
        assertTrue(minorVersionTest.isAtLeast(0, 3, 0));

        // Test patch version number

        final MinecraftGameVersion patchVersionTest = new MinecraftGameVersion(0, 0, 5);

        assertFalse(patchVersionTest.isAtLeast(1, 0, 0));
        assertFalse(patchVersionTest.isAtLeast(0, 0, 6));
        assertFalse(patchVersionTest.isAtLeast(0, 1, 4));
        assertFalse(patchVersionTest.isAtLeast(1, 1, 4));

        assertTrue(patchVersionTest.isAtLeast(0, 0, 1));
        assertTrue(patchVersionTest.isAtLeast(0, 0, 2));
        assertTrue(patchVersionTest.isAtLeast(0, 0, 3));
        assertTrue(patchVersionTest.isAtLeast(0, 0, 4));
        assertTrue(patchVersionTest.isAtLeast(0, 0, 5));
    }

    @MethodSource("getGameVersions")
    @ParameterizedTest(name = "Verify that \"{0}\" is recognized as {1}.{2}.{3}")
    void testVersionDetection(final String gameVersion, final int major, final int minor, final int patch) {
        /*
         * The platform manager checks for the type of server software,
         * we will just simulate some "Spigot" version here, so that the test can
         * continue successfully.
         */
        final String serverSoftwareVersion =
                "git-Spigot-12345-abcdef (MC: " + major + '.' + minor + '.' + patch + ')';

        // Set up a mock plugin for logging.
        final mcMMO plugin = Mockito.mock(mcMMO.class);
        Mockito.when(plugin.getName()).thenReturn("mcMMO");
        Mockito.when(plugin.getLogger()).thenReturn(Logger.getLogger("mcMMO"));
        mcMMO.p = plugin;

        try (final MockedStatic<Bukkit> bukkit = Mockito.mockStatic(Bukkit.class)) {
            // Inject our own Bukkit versions
            bukkit.when(Bukkit::getVersion).thenReturn(serverSoftwareVersion);
            bukkit.when(Bukkit::getBukkitVersion).thenReturn(gameVersion);

            final PlatformManager manager = new PlatformManager();
            final Platform platform = manager.getPlatform();
            final MinecraftGameVersion minecraftVersion = platform.getGameVersion();

            assertEquals(major, minecraftVersion.getMajorVersion().asInt());
            assertEquals(minor, minecraftVersion.getMinorVersion().asInt());
            assertEquals(patch, minecraftVersion.getPatchVersion().asInt());
        } finally {
            mcMMO.p = null;
        }
    }

}
