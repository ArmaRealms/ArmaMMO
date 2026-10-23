package com.gmail.nossr50.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.gmail.nossr50.MMOTestEnvironment;
import com.gmail.nossr50.mcMMO;
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CustomItemSupportConfigTest extends MMOTestEnvironment {
    @BeforeEach
    void setUp() {
        mockBaseEnvironment(Logger.getLogger(CustomItemSupportConfigTest.class.getName()));
        when(mcMMO.p.getResource("custom_item_support.yml")).thenAnswer(call ->
                getClass().getResourceAsStream("/custom_item_support.yml"));
    }

    @AfterEach
    void tearDown() {
        cleanUpStaticMocks();
    }

    @Test
    void missingModelOptionsShouldKeepExistingServersEnabled() throws IOException {
        // Given - an existing config with only the legacy model-data options
        Files.writeString(testDataFolder.toPath().resolve("custom_item_support.yml"), """
                Custom_Item_Support:
                    Repair:
                        Allow_Repair_On_Items_With_Custom_Model_Data: false
                    Salvage:
                        Allow_Salvage_On_Items_With_Custom_Model_Data: false
                """);

        // When - loading the old config and copying new defaults
        final CustomItemSupportConfig config = new CustomItemSupportConfig(testDataFolder);

        // Then - legacy choices stay intact and new model options default to allowing use
        assertThat(config.isCustomRepairAllowed()).isFalse();
        assertThat(config.isCustomSalvageAllowed()).isFalse();
        assertThat(config.isItemModelRepairAllowed()).isTrue();
        assertThat(config.isItemModelSalvageAllowed()).isTrue();
    }

    @Test
    void explicitModelOptionsShouldBeLoadedIndependently() throws IOException {
        // Given - repair is blocked while salvage is allowed for item models
        Files.writeString(testDataFolder.toPath().resolve("custom_item_support.yml"), """
                Custom_Item_Support:
                    Repair:
                        Allow_Repair_On_Items_With_Custom_Item_Model: false
                    Salvage:
                        Allow_Salvage_On_Items_With_Custom_Item_Model: true
                """);

        // When - loading the config
        final CustomItemSupportConfig config = new CustomItemSupportConfig(testDataFolder);

        // Then - each operation respects its own option
        assertThat(config.isItemModelRepairAllowed()).isFalse();
        assertThat(config.isItemModelSalvageAllowed()).isTrue();
    }
}
