package dev.flaymie.fcore.api.gui;

import dev.flaymie.fcore.FCore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Менеджер для управления GUI
 */
public class GuiManager implements Listener {
    
    private static GuiManager instance;
    
    private final Map<UUID, Gui> playerGuis;
    private final Map<Inventory, Gui> inventoryGuis;
    
    private GuiManager() {
        this.playerGuis = new ConcurrentHashMap<>();
        this.inventoryGuis = new ConcurrentHashMap<>();
        
        // Регистрация обработчика событий
        Bukkit.getPluginManager().registerEvents(this, FCore.getInstance());
    }
    
    /**
     * Получает экземпляр менеджера GUI
     * @return экземпляр менеджера
     */
    public static GuiManager getInstance() {
        if (instance == null) {
            instance = new GuiManager();
        }
        return instance;
    }
    
    /**
     * Регистрирует GUI для игрока
     * @param player игрок
     * @param gui GUI
     */
    public void registerGui(Player player, Gui gui) {
        UUID uuid = player.getUniqueId();
        playerGuis.put(uuid, gui);
        inventoryGuis.put(gui.getInventory(), gui);
    }
    
    /**
     * Удаляет регистрацию GUI для игрока
     * @param player игрок
     */
    public void unregisterGui(Player player) {
        UUID uuid = player.getUniqueId();
        Gui gui = playerGuis.remove(uuid);
        if (gui != null) {
            inventoryGuis.remove(gui.getInventory());
        }
    }
    
    /**
     * Получает GUI для игрока
     * @param player игрок
     * @return GUI или null, если не найден
     */
    public Gui getGui(Player player) {
        return playerGuis.get(player.getUniqueId());
    }
    
    /**
     * Проверяет, имеет ли игрок открытый GUI
     * @param player игрок
     * @return true, если имеет
     */
    public boolean hasGui(Player player) {
        return playerGuis.containsKey(player.getUniqueId());
    }
    
    /**
     * Открывает GUI для игрока
     * @param player игрок
     * @param gui GUI для открытия
     */
    public void openGui(Player player, Gui gui) {
        gui.open(player);
        registerGui(player, gui);
    }
    
    /**
     * Закрывает открытый GUI для игрока
     * @param player игрок
     */
    public void closeGui(Player player) {
        Gui gui = getGui(player);
        if (gui != null) {
            gui.close(player);
            unregisterGui(player);
        }
    }
    
    /**
     * Обработчик клика в инвентаре
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Inventory inventory = event.getInventory();
        Gui gui = inventoryGuis.get(inventory);
        
        if (gui != null) {
            gui.handleClick(event);
        }
    }
    
    /**
     * Обработчик перетаскивания в инвентаре
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Inventory inventory = event.getInventory();
        Gui gui = inventoryGuis.get(inventory);
        
        if (gui != null && !gui.canPlayerMoveItems()) {
            event.setCancelled(true);
        }
    }
    
    /**
     * Обработчик закрытия инвентаря
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        
        Player player = (Player) event.getPlayer();
        Inventory inventory = event.getInventory();
        
        Gui gui = inventoryGuis.get(inventory);
        if (gui != null) {
            unregisterGui(player);
        }
    }
    
    /**
     * Обработчик выхода игрока
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        unregisterGui(player);
    }
} 