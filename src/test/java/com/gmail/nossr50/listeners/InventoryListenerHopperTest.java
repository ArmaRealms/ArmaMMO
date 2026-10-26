package com.gmail.nossr50.listeners;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gmail.nossr50.MMOTestEnvironment;
import com.gmail.nossr50.config.WorldBlacklist;
import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.runnables.skills.AlchemyBrewCheckTask;
import com.gmail.nossr50.runnables.skills.AlchemyBrewTask;
import com.gmail.nossr50.skills.alchemy.Alchemy;
import com.gmail.nossr50.skills.alchemy.AlchemyManager;
import com.gmail.nossr50.skills.alchemy.AlchemyPotionBrewer;
import com.gmail.nossr50.util.ContainerMetadataUtils;
import com.gmail.nossr50.util.LegacyBrewerInventory;
import com.gmail.nossr50.util.PaperUtil;
import com.gmail.nossr50.util.player.UserManager;
import java.util.logging.Logger;
import java.util.stream.Stream;
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
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
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
        paper.when(() -> PaperUtil.canGetHolderWithoutSnapshot(destination)).thenReturn(true);
        paper.when(() -> PaperUtil.getHolderWithoutSnapshot(destination)).thenReturn(liveHolder);
        brewer = mockStatic(AlchemyPotionBrewer.class);
        owners = mockStatic(ContainerMetadataUtils.class);
    }

    @AfterEach
    void tearDown() {
        Alchemy.brewingStandMap.clear();
        owners.close();
        brewer.close();
        paper.close();
        cleanUpStaticMocks();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void disabledHopperAlchemyShouldNotCopyOrSchedule(final boolean paperRuntime) {
        useRuntime(paperRuntime);
        when(generalConfig.getEnabledForHoppers()).thenReturn(false);

        listener.onInventoryMoveItemEvent(event);

        verify(destination, times(paperRuntime ? 0 : 1)).getHolder();
        owners.verifyNoInteractions();
        brewer.verifyNoInteractions();
        verify(event, never()).setCancelled(true);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void invalidIngredientShouldNotCopyOrSchedule(final boolean paperRuntime) {
        useRuntime(paperRuntime);
        listener.onInventoryMoveItemEvent(event);

        verify(destination, times(paperRuntime ? 0 : 1)).getHolder();
        brewer.verify(() -> AlchemyPotionBrewer.isValidIngredientByLevel(1, item));
        brewer.verify(() -> AlchemyPotionBrewer.scheduleCheck(any()), never());
        verify(event, never()).setCancelled(true);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void blockedIngredientShouldStillCancelWhenHopperAlchemyIsDisabled(final boolean paperRuntime) {
        useRuntime(paperRuntime);
        when(generalConfig.getEnabledForHoppers()).thenReturn(false);
        when(generalConfig.getPreventHopperTransferIngredients()).thenReturn(true);

        listener.onInventoryMoveItemEvent(event);

        verify(event).setCancelled(true);
        verify(destination, times(paperRuntime ? 0 : 1)).getHolder();
        brewer.verifyNoInteractions();
    }

    @ParameterizedTest
    @MethodSource("potionTransfers")
    void blockedPotionsShouldCancelWithoutExtraCopies(final Material type, final boolean paperRuntime) {
        useRuntime(paperRuntime);
        when(item.getType()).thenReturn(type);
        when(generalConfig.getPreventHopperTransferBottles()).thenReturn(true);

        listener.onInventoryMoveItemEvent(event);

        verify(event).setCancelled(true);
        verify(destination, times(paperRuntime ? 0 : 1)).getHolder();
        brewer.verifyNoInteractions();
    }

    @ParameterizedTest
    @MethodSource("potionTransfers")
    void ingredientRestrictionShouldNotBlockPotions(final Material type, final boolean paperRuntime) {
        useRuntime(paperRuntime);
        when(item.getType()).thenReturn(type);
        when(generalConfig.getPreventHopperTransferIngredients()).thenReturn(true);

        listener.onInventoryMoveItemEvent(event);

        verify(event, never()).setCancelled(true);
        verify(destination, times(paperRuntime ? 0 : 1)).getHolder();
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
        paper.when(() -> PaperUtil.canGetHolderWithoutSnapshot(destination)).thenReturn(false);
        paper.when(() -> PaperUtil.getHolderWithoutSnapshot(destination)).thenAnswer(call -> destination.getHolder());
        brewer.when(() -> AlchemyPotionBrewer.isValidIngredientByLevel(1, item)).thenReturn(true);

        listener.onInventoryMoveItemEvent(event);

        verify(destination).getHolder();
        owners.verify(() -> ContainerMetadataUtils.getContainerOwner(snapshot));
        brewer.verify(() -> AlchemyPotionBrewer.scheduleCheck(snapshot));
    }

    @Test
    void legacySpigotInventoryShouldReuseItsSnapshotEvenOnPaper() {
        final LegacyBrewerInventory legacy = new LegacyBrewerInventory(destination);
        when(event.getDestination()).thenReturn(legacy);
        paper.when(PaperUtil::canGetHolderWithoutSnapshot).thenCallRealMethod();
        paper.when(() -> PaperUtil.canGetHolderWithoutSnapshot(legacy)).thenCallRealMethod();
        paper.when(() -> PaperUtil.getHolderWithoutSnapshot(legacy)).thenCallRealMethod();
        brewer.when(() -> AlchemyPotionBrewer.isValidIngredientByLevel(1, item)).thenReturn(true);

        listener.onInventoryMoveItemEvent(event);

        verify(destination).getHolder();
        owners.verify(() -> ContainerMetadataUtils.getContainerOwner(snapshot));
        brewer.verify(() -> AlchemyPotionBrewer.scheduleCheck(snapshot));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void scheduledAlchemyShouldRetainSnapshotFuelAndMutateOnlyTheSnapshot(final boolean paperRuntime) {
        useRuntime(paperRuntime);
        final Location location = new Location(world, 1, 2, 3);
        final ItemStack[] contents = new ItemStack[5];
        contents[0] = mock(ItemStack.class);
        contents[Alchemy.INGREDIENT_SLOT] = item;
        when(destination.getContents()).thenAnswer(call -> contents.clone());
        when(snapshot.getInventory()).thenReturn(destination);
        when(snapshot.getLocation()).thenReturn(location);
        when(snapshot.getType()).thenReturn(Material.BREWING_STAND);
        when(snapshot.getFuelLevel()).thenReturn(5);
        when(snapshot.getBrewingTime()).thenReturn(-1);
        when(liveHolder.getFuelLevel()).thenReturn(0);
        owners.when(() -> ContainerMetadataUtils.getContainerOwner(snapshot)).thenReturn(mock(OfflinePlayer.class));
        brewer.when(() -> AlchemyPotionBrewer.isValidIngredientByLevel(1, item)).thenReturn(true);
        brewer.when(() -> AlchemyPotionBrewer.isValidBrew(eq(1), any(ItemStack[].class))).thenReturn(true);
        brewer.when(() -> AlchemyPotionBrewer.scheduleCheck(snapshot)).thenCallRealMethod();

        listener.onInventoryMoveItemEvent(event);

        final ArgumentCaptor<AlchemyBrewCheckTask> check = ArgumentCaptor.forClass(AlchemyBrewCheckTask.class);
        verify(foliaLib.getScheduler()).runAtLocation(eq(location), check.capture());
        check.getValue().run();
        final AlchemyBrewTask task = Alchemy.brewingStandMap.get(location);
        assertThat(task).isNotNull();
        task.run();
        verify(snapshot).setFuelLevel(4);
        verify(snapshot).setBrewingTime(399);
        verify(liveHolder, never()).setFuelLevel(anyInt());
        verify(liveHolder, never()).setBrewingTime(anyInt());

        // Removing the ingredient between checks must still cancel the existing brew.
        contents[Alchemy.INGREDIENT_SLOT] = null;
        check.getValue().run();
        verify(snapshot).setBrewingTime(-1);
        assertThat(Alchemy.brewingStandMap).doesNotContainKey(location);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void onlineOwnerWithoutLoadedPlayerDataShouldKeepTierOne(final boolean paperRuntime) {
        useRuntime(paperRuntime);
        final BrewingStand holder = paperRuntime ? liveHolder : snapshot;
        when(player.getPlayer()).thenReturn(player);
        owners.when(() -> ContainerMetadataUtils.getContainerOwner(holder)).thenReturn(player);
        mockedUserManager.when(() -> UserManager.getPlayer(player)).thenReturn(null);
        brewer.when(() -> AlchemyPotionBrewer.isValidIngredientByLevel(1, item)).thenReturn(true);

        listener.onInventoryMoveItemEvent(event);

        brewer.verify(() -> AlchemyPotionBrewer.scheduleCheck(snapshot));
        verify(destination).getHolder();
    }

    private void useRuntime(final boolean paperRuntime) {
        paper.when(() -> PaperUtil.canGetHolderWithoutSnapshot(destination)).thenReturn(paperRuntime);
        if (!paperRuntime) {
            paper.when(() -> PaperUtil.getHolderWithoutSnapshot(destination))
                    .thenAnswer(call -> destination.getHolder());
        }
    }

    private static Stream<Arguments> potionTransfers() {
        return Stream.of(Material.POTION, Material.SPLASH_POTION, Material.LINGERING_POTION)
                .flatMap(type -> Stream.of(Arguments.of(type, true), Arguments.of(type, false)));
    }

    @Test
    void nonBrewingDestinationShouldNotResolveAHolder() {
        when(event.getDestination()).thenReturn(mock(Inventory.class));

        listener.onInventoryMoveItemEvent(event);

        paper.verifyNoInteractions();
        brewer.verifyNoInteractions();
        verify(event, never()).setCancelled(true);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void virtualBrewerWithoutAHolderShouldNotBeCancelled(final boolean paperRuntime) {
        useRuntime(paperRuntime);
        if (paperRuntime) {
            paper.when(() -> PaperUtil.getHolderWithoutSnapshot(destination)).thenReturn(null);
        }
        when(destination.getHolder()).thenReturn(null);
        when(generalConfig.getPreventHopperTransferIngredients()).thenReturn(true);

        listener.onInventoryMoveItemEvent(event);

        verify(event, never()).setCancelled(true);
        verify(destination, times(paperRuntime ? 0 : 1)).getHolder();
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
