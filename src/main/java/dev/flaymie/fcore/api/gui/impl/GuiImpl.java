package dev.flaymie.fcore.api.gui.impl;

import dev.flaymie.fcore.api.gui.ClickHandler;
import dev.flaymie.fcore.api.gui.Gui;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class GuiImpl implements Gui {
    
    protected final Inventory inventory;
    protected final String title;
    protected final int rows;
    protected final Map<Integer, ClickHandler> clickHandlers;
    protected final Set<UUID> viewers;
    protected boolean canPlayerMoveItems;
    
    public GuiImpl(String title, int rows) {
        this.title = title;
        this.rows = Math.max(1, Math.min(6, rows));
        this.inventory = Bukkit.createInventory(null, rows * 9, title);
        this.clickHandlers = new HashMap<>();
        this.viewers = new HashSet<>();
        this.canPlayerMoveItems = false;
    }
    
    @Override
    public void open(Player player) {
        if (player == null) return;
        
        player.openInventory(inventory);
        viewers.add(player.getUniqueId());
    }
    
    @Override
    public void close(Player player) {
        if (player == null) return;
        
        if (player.getOpenInventory().getTopInventory().equals(inventory)) {
            player.closeInventory();
        }
        
        viewers.remove(player.getUniqueId());
    }
    
    @Override
    public void close(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            close(player);
        } else {
            viewers.remove(uuid);
        }
    }
    
    @Override
    public Gui setItem(int slot, ItemStack item) {
        if (slot >= 0 && slot < inventory.getSize()) {
            inventory.setItem(slot, item);
        }
        return this;
    }
    
    @Override
    public Gui setItem(int slot, ItemStack item, ClickHandler clickHandler) {
        setItem(slot, item);
        setClickHandler(slot, clickHandler);
        return this;
    }
    
    @Override
    public Gui setItem(int row, int column, ItemStack item) {
        return setItem(row * 9 + column, item);
    }
    
    @Override
    public Gui setItem(int row, int column, ItemStack item, ClickHandler clickHandler) {
        return setItem(row * 9 + column, item, clickHandler);
    }
    
    @Override
    public ItemStack getItem(int slot) {
        if (slot >= 0 && slot < inventory.getSize()) {
            return inventory.getItem(slot);
        }
        return null;
    }
    
    @Override
    public Gui setClickHandler(int slot, ClickHandler clickHandler) {
        if (slot >= 0 && slot < inventory.getSize()) {
            if (clickHandler == null) {
                clickHandlers.remove(slot);
            } else {
                clickHandlers.put(slot, clickHandler);
            }
        }
        return this;
    }
    
    @Override
    public boolean hasClickHandler(int slot) {
        return clickHandlers.containsKey(slot);
    }
    
    @Override
    public int getRows() {
        return rows;
    }
    
    @Override
    public String getTitle() {
        return title;
    }
    
    @Override
    public Inventory getInventory() {
        return inventory;
    }
    
    @Override
    public Gui update() {
        viewers.forEach(uuid -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.updateInventory();
            }
        });
        return this;
    }
    
    @Override
    public Gui fillEmptySlots(ItemStack item) {
        if (item == null) return this;
        
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, item);
            }
        }
        
        return this;
    }
    
    @Override
    public void handleClick(InventoryClickEvent event) {
        if (event == null) return;
        
        event.setCancelled(!canPlayerMoveItems);
        
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= inventory.getSize()) return;
        
        ClickHandler handler = clickHandlers.get(slot);
        if (handler != null) {
            handler.onClick(event);
        }
    }
    
    @Override
    public int getSize() {
        return inventory.getSize();
    }
    
    @Override
    public boolean canPlayerMoveItems() {
        return canPlayerMoveItems;
    }
    
    @Override
    public Gui setCanPlayerMoveItems(boolean canPlayerMoveItems) {
        this.canPlayerMoveItems = canPlayerMoveItems;
        return this;
    }
} 