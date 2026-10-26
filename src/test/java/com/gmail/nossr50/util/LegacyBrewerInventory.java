package com.gmail.nossr50.util;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BrewingStand;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.ItemStack;

/** A concrete third-party inventory compiled against Spigot, without Paper's additional overload. */
public final class LegacyBrewerInventory implements BrewerInventory {
    private final BrewerInventory delegate;

    public LegacyBrewerInventory(final BrewerInventory delegate) {
        this.delegate = delegate;
    }

    @Override public int getSize() { return delegate.getSize(); }
    @Override public int getMaxStackSize() { return delegate.getMaxStackSize(); }
    @Override public void setMaxStackSize(int size) { delegate.setMaxStackSize(size); }
    @Override public ItemStack getItem(int index) { return delegate.getItem(index); }
    @Override public void setItem(int index, ItemStack item) { delegate.setItem(index, item); }
    @Override public HashMap<Integer, ItemStack> addItem(ItemStack... items) { return delegate.addItem(items); }
    @Override public HashMap<Integer, ItemStack> removeItem(ItemStack... items) { return delegate.removeItem(items); }
    @Override public ItemStack[] getContents() { return delegate.getContents(); }
    @Override public void setContents(ItemStack[] items) { delegate.setContents(items); }
    @Override public ItemStack[] getStorageContents() { return delegate.getStorageContents(); }
    @Override public void setStorageContents(ItemStack[] items) { delegate.setStorageContents(items); }
    @Override public boolean contains(Material material) { return delegate.contains(material); }
    @Override public boolean contains(ItemStack item) { return delegate.contains(item); }
    @Override public boolean contains(Material material, int amount) { return delegate.contains(material, amount); }
    @Override public boolean contains(ItemStack item, int amount) { return delegate.contains(item, amount); }
    @Override public boolean containsAtLeast(ItemStack item, int amount) { return delegate.containsAtLeast(item, amount); }
    @Override public HashMap<Integer, ? extends ItemStack> all(Material material) { return delegate.all(material); }
    @Override public HashMap<Integer, ? extends ItemStack> all(ItemStack item) { return delegate.all(item); }
    @Override public int first(Material material) { return delegate.first(material); }
    @Override public int first(ItemStack item) { return delegate.first(item); }
    @Override public int firstEmpty() { return delegate.firstEmpty(); }
    @Override public boolean isEmpty() { return delegate.isEmpty(); }
    @Override public void remove(Material material) { delegate.remove(material); }
    @Override public void remove(ItemStack item) { delegate.remove(item); }
    @Override public void clear(int index) { delegate.clear(index); }
    @Override public void clear() { delegate.clear(); }
    @Override public List<HumanEntity> getViewers() { return delegate.getViewers(); }
    @Override public InventoryType getType() { return delegate.getType(); }
    @Override public BrewingStand getHolder() { return delegate.getHolder(); }
    @Override public ListIterator<ItemStack> iterator() { return delegate.iterator(); }
    @Override public ListIterator<ItemStack> iterator(int index) { return delegate.iterator(index); }
    @Override public Location getLocation() { return delegate.getLocation(); }
    @Override public ItemStack getIngredient() { return delegate.getIngredient(); }
    @Override public void setIngredient(ItemStack item) { delegate.setIngredient(item); }
    @Override public ItemStack getFuel() { return delegate.getFuel(); }
    @Override public void setFuel(ItemStack item) { delegate.setFuel(item); }
}
