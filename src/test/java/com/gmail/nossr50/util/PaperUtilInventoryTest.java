package com.gmail.nossr50.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import org.bukkit.block.BrewingStand;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.junit.jupiter.api.Test;

/** Tests run against Spigot by default and against Paper when that API is on the test classpath. */
class PaperUtilInventoryTest {
    /** Exposes the Paper overload to tests compiled against Spigot. */
    public interface SnapshotInventory extends Inventory {
        InventoryHolder getHolder(boolean useSnapshot);
    }

    @Test
    void spigotShouldUseTheOrdinaryHolderExactlyOnce() {
        assumeFalse(PaperUtil.canGetHolderWithoutSnapshot());
        final Inventory inventory = mock(Inventory.class);
        final InventoryHolder holder = mock(InventoryHolder.class);
        when(inventory.getHolder()).thenReturn(holder);

        assertThat(PaperUtil.getHolderWithoutSnapshot(inventory)).isSameAs(holder);
        verify(inventory).getHolder();
    }

    @Test
    void spigotInventoryImplementationShouldWorkOnEitherRuntime() {
        final BrewerInventory delegate = mock(BrewerInventory.class);
        final BrewingStand holder = mock(BrewingStand.class);
        when(delegate.getHolder()).thenReturn(holder);
        final LegacyBrewerInventory inventory = new LegacyBrewerInventory(delegate);

        assertThat(PaperUtil.canGetHolderWithoutSnapshot(inventory)).isFalse();
        assertThat(PaperUtil.getHolderWithoutSnapshot(inventory)).isSameAs(holder);
        verify(delegate).getHolder();
    }

    @Test
    void paperShouldRequestTheLiveHolderWithoutCallingTheSnapshotOverload() {
        assumeTrue(PaperUtil.canGetHolderWithoutSnapshot());
        final SnapshotInventory inventory = mock(SnapshotInventory.class);
        final InventoryHolder holder = mock(InventoryHolder.class);
        when(inventory.getHolder(false)).thenReturn(holder);

        assertThat(PaperUtil.getHolderWithoutSnapshot(inventory)).isSameAs(holder);
        verify(inventory).getHolder(false);
        verify(inventory, never()).getHolder();
    }

    @Test
    void paperShouldPreserveNullHolders() {
        assumeTrue(PaperUtil.canGetHolderWithoutSnapshot());
        final SnapshotInventory inventory = mock(SnapshotInventory.class);

        assertThat(PaperUtil.getHolderWithoutSnapshot(inventory)).isNull();
        verify(inventory, never()).getHolder();
    }

    @Test
    void paperInvocationFailureShouldNotSilentlyFallbackToACopy() {
        assumeTrue(PaperUtil.canGetHolderWithoutSnapshot());
        final SnapshotInventory inventory = mock(SnapshotInventory.class);
        final IllegalStateException failure = new IllegalStateException("Holder unavailable");
        when(inventory.getHolder(false)).thenThrow(failure);

        assertThatThrownBy(() -> PaperUtil.getHolderWithoutSnapshot(inventory))
                .isInstanceOf(IllegalStateException.class)
                .hasCauseInstanceOf(InvocationTargetException.class)
                .hasRootCause(failure);
        verify(inventory, never()).getHolder();
    }
}
