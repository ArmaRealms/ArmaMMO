package com.gmail.nossr50.listeners;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gmail.nossr50.MMOTestEnvironment;
import com.gmail.nossr50.config.WorldBlacklist;
import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.skills.alchemy.AlchemyManager;
import com.gmail.nossr50.skills.alchemy.AlchemyPotionBrewer;
import com.gmail.nossr50.util.ContainerMetadataUtils;
import com.gmail.nossr50.util.PaperUtil;
import com.gmail.nossr50.util.player.UserManager;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.BrewingStand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.MockedStatic;

class InventoryListenerHopperTest extends MMOTestEnvironment {
    private InventoryListener listener;
    private InventoryMoveItemEvent event;
    private BrewerInventory destination;
    private BrewingStand liveHolder;
    private BrewingStand snapshot;
    private ItemStack item;
    private MockedStatic<PaperUtil> paper;
    private MockedStatic<AlchemyPotionBrewer> brewer;
    private MockedStatic<ContainerMetadataUtils> owners;

    @BeforeEach
    void setUp() {
        mockBaseEnvironment(Logger.getLogger(getClass().getName()));
        listener = new InventoryListener(mcMMO.p);
        event = mock(InventoryMoveItemEvent.class);
        destination = mock(BrewerInventory.class);
        liveHolder = mock(BrewingStand.class);
        snapshot = mock(BrewingStand.class);
        item = mock(ItemStack.class);
        when(item.getType()).thenReturn(Material.NETHER_WART);
        when(event.getSource()).thenReturn(mock(Inventory.class));
        when(event.getDestination()).thenReturn(destination);
        when(event.getItem()).thenReturn(item);
        when(destination.getHolder()).thenReturn(snapshot);
        when(generalConfig.getEnabledForHoppers()).thenReturn(true);
        paper = mockStatic(PaperUtil.class);
        paper.when(PaperUtil::canGetHolderWithoutSnapshot).thenReturn(true);
        paper.when(() -> PaperUtil.getHolderWithoutSnapshot(destination)).thenReturn(liveHolder);
        brewer = mockStatic(AlchemyPotionBrewer.class);
        owners = mockStatic(ContainerMetadataUtils.class);
    }

    @AfterEach
    void tearDown() {
        owners.close();
        brewer.close();
        paper.close();
        cleanUpStaticMocks();
    }

    @Test
    void disabledHopperAlchemyShouldNotCopyOrSchedule() {
        when(generalConfig.getEnabledForHoppers()).thenReturn(false);

        listener.onInventoryMoveItemEvent(event);

        verify(destination, never()).getHolder();
        owners.verifyNoInteractions();
        brewer.verifyNoInteractions();
        verify(event, never()).setCancelled(true);
    }

    @Test
    void invalidIngredientShouldNotCopyOrSchedule() {
        listener.onInventoryMoveItemEvent(event);

        verify(destination, never()).getHolder();
        brewer.verify(() -> AlchemyPotionBrewer.isValidIngredientByLevel(1, item));
        brewer.verify(() -> AlchemyPotionBrewer.scheduleCheck(any()), never());
        verify(event, never()).setCancelled(true);
    }

    @Test
    void blockedIngredientShouldStillCancelWhenHopperAlchemyIsDisabled() {
        when(generalConfig.getEnabledForHoppers()).thenReturn(false);
        when(generalConfig.getPreventHopperTransferIngredients()).thenReturn(true);

        listener.onInventoryMoveItemEvent(event);

        verify(event).setCancelled(true);
        verify(destination, never()).getHolder();
        brewer.verifyNoInteractions();
    }

    @ParameterizedTest
    @EnumSource(value = Material.class, names = {"POTION", "SPLASH_POTION", "LINGERING_POTION"})
    void blockedPotionsShouldCancelWithoutCopying(final Material type) {
        when(item.getType()).thenReturn(type);
        when(generalConfig.getPreventHopperTransferBottles()).thenReturn(true);

        listener.onInventoryMoveItemEvent(event);

        verify(event).setCancelled(true);
        verify(destination, never()).getHolder();
        brewer.verifyNoInteractions();
    }

    @ParameterizedTest
    @EnumSource(value = Material.class, names = {"POTION", "SPLASH_POTION", "LINGERING_POTION"})
    void ingredientRestrictionShouldNotBlockPotions(final Material type) {
        when(item.getType()).thenReturn(type);
        when(generalConfig.getPreventHopperTransferIngredients()).thenReturn(true);

        listener.onInventoryMoveItemEvent(event);

        verify(event, never()).setCancelled(true);
        verify(destination, never()).getHolder();
    }

    @Test
    void validIngredientShouldScheduleTheSnapshotRatherThanTheLiveHolder() {
        brewer.when(() -> AlchemyPotionBrewer.isValidIngredientByLevel(1, item)).thenReturn(true);

        listener.onInventoryMoveItemEvent(event);

        owners.verify(() -> ContainerMetadataUtils.getContainerOwner(liveHolder));
        verify(destination).getHolder();
        brewer.verify(() -> AlchemyPotionBrewer.scheduleCheck(snapshot));
        brewer.verify(() -> AlchemyPotionBrewer.scheduleCheck(liveHolder), never());
        verify(event, never()).setCancelled(true);
    }

    @Test
    void onlineOwnerShouldKeepTheirAlchemyTier() {
        final AlchemyManager manager = mock(AlchemyManager.class);
        when(player.getPlayer()).thenReturn(player);
        owners.when(() -> ContainerMetadataUtils.getContainerOwner(liveHolder)).thenReturn(player);
        mockedUserManager.when(() -> UserManager.getPlayer(player)).thenReturn(mmoPlayer);
        doReturn(manager).when(mmoPlayer).getAlchemyManager();
        when(manager.getTier()).thenReturn(4);
        brewer.when(() -> AlchemyPotionBrewer.isValidIngredientByLevel(4, item)).thenReturn(true);

        listener.onInventoryMoveItemEvent(event);

        brewer.verify(() -> AlchemyPotionBrewer.isValidIngredientByLevel(4, item));
        brewer.verify(() -> AlchemyPotionBrewer.scheduleCheck(snapshot));
    }

    @Test
    void offlineOwnerShouldKeepTheDefaultTier() {
        final OfflinePlayer owner = mock(OfflinePlayer.class);
        owners.when(() -> ContainerMetadataUtils.getContainerOwner(liveHolder)).thenReturn(owner);
        brewer.when(() -> AlchemyPotionBrewer.isValidIngredientByLevel(1, item)).thenReturn(true);

        listener.onInventoryMoveItemEvent(event);

        brewer.verify(() -> AlchemyPotionBrewer.scheduleCheck(snapshot));
    }

    @Test
    void spigotShouldReuseItsExistingSnapshot() {
        paper.when(PaperUtil::canGetHolderWithoutSnapshot).thenReturn(false);
        paper.when(() -> PaperUtil.getHolderWithoutSnapshot(destination)).thenAnswer(call -> destination.getHolder());
        brewer.when(() -> AlchemyPotionBrewer.isValidIngredientByLevel(1, item)).thenReturn(true);

        listener.onInventoryMoveItemEvent(event);

        verify(destination).getHolder();
        owners.verify(() -> ContainerMetadataUtils.getContainerOwner(snapshot));
        brewer.verify(() -> AlchemyPotionBrewer.scheduleCheck(snapshot));
    }

    @Test
    void virtualBrewerWithoutAHolderShouldNotBeCancelled() {
        paper.when(() -> PaperUtil.getHolderWithoutSnapshot(destination)).thenReturn(null);
        when(destination.getHolder()).thenReturn(null);
        when(generalConfig.getPreventHopperTransferIngredients()).thenReturn(true);

        listener.onInventoryMoveItemEvent(event);

        verify(event, never()).setCancelled(true);
        verify(destination, never()).getHolder();
        brewer.verifyNoInteractions();
    }

    @Test
    void blacklistedSourceShouldBeIgnoredBeforeResolvingTheHolder() {
        final Location location = new Location(world, 1, 2, 3);
        when(event.getSource().getLocation()).thenReturn(location);
        try (MockedStatic<WorldBlacklist> blacklist = mockStatic(WorldBlacklist.class)) {
            blacklist.when(() -> WorldBlacklist.isWorldBlacklisted(world)).thenReturn(true);

            listener.onInventoryMoveItemEvent(event);

            paper.verifyNoInteractions();
            verify(destination, never()).getHolder();
            brewer.verifyNoInteractions();
        }
    }

    @Test
    void listenerShouldContinueIgnoringCancelledEvents() throws Exception {
        final var handler = InventoryListener.class
                .getMethod("onInventoryMoveItemEvent", InventoryMoveItemEvent.class)
                .getAnnotation(EventHandler.class);
        assertThat(handler.ignoreCancelled()).isTrue();
    }
}
