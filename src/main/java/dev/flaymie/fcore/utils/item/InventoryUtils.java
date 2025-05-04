package dev.flaymie.fcore.utils.item;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Утилиты для работы с инвентарем
 */
public class InventoryUtils {

    /**
     * Проверяет, есть ли в инвентаре свободные слоты
     * @param inventory инвентарь для проверки
     * @return true, если есть хотя бы один свободный слот
     */
    public static boolean hasEmptySlot(Inventory inventory) {
        if (inventory == null) return false;
        
        return inventory.firstEmpty() != -1;
    }
    
    /**
     * Подсчитывает количество свободных слотов в инвентаре
     * @param inventory инвентарь для проверки
     * @return количество свободных слотов
     */
    public static int countEmptySlots(Inventory inventory) {
        if (inventory == null) return 0;
        
        int count = 0;
        for (ItemStack item : inventory.getContents()) {
            if (item == null || item.getType() == Material.AIR) {
                count++;
            }
        }
        
        return count;
    }
    
    /**
     * Проверяет, есть ли в инвентаре игрока указанный предмет
     * @param player игрок
     * @param material материал предмета
     * @param amount количество (по умолчанию 1)
     * @return true, если предмет найден
     */
    public static boolean hasItem(Player player, Material material, int amount) {
        if (player == null || material == null) return false;
        
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                count += item.getAmount();
                if (count >= amount) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Проверяет, есть ли в инвентаре игрока указанный предмет
     * @param player игрок
     * @param material материал предмета
     * @return true, если предмет найден
     */
    public static boolean hasItem(Player player, Material material) {
        return hasItem(player, material, 1);
    }
    
    /**
     * Подсчитывает количество предметов указанного материала в инвентаре
     * @param inventory инвентарь
     * @param material материал предмета
     * @return количество предметов
     */
    public static int countItems(Inventory inventory, Material material) {
        if (inventory == null || material == null) return 0;
        
        int count = 0;
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() == material) {
                count += item.getAmount();
            }
        }
        
        return count;
    }
    
    /**
     * Удаляет указанное количество предметов из инвентаря
     * @param player игрок
     * @param material материал предмета
     * @param amount количество для удаления
     * @return true, если удаление успешно
     */
    public static boolean removeItems(Player player, Material material, int amount) {
        if (player == null || material == null || amount <= 0) return false;
        if (!hasItem(player, material, amount)) return false;
        
        PlayerInventory inventory = player.getInventory();
        int remaining = amount;
        
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.getType() == material) {
                if (item.getAmount() <= remaining) {
                    remaining -= item.getAmount();
                    inventory.setItem(i, null);
                } else {
                    item.setAmount(item.getAmount() - remaining);
                    remaining = 0;
                }
                
                if (remaining <= 0) {
                    break;
                }
            }
        }
        
        player.updateInventory();
        return true;
    }
    
    /**
     * Добавляет предмет в инвентарь, если есть место
     * @param player игрок
     * @param itemStack предмет
     * @return true, если предмет добавлен
     */
    public static boolean addItem(Player player, ItemStack itemStack) {
        if (player == null || itemStack == null) return false;
        
        PlayerInventory inventory = player.getInventory();
        Map<Integer, ItemStack> leftover = inventory.addItem(itemStack);
        
        // Если ничего не осталось - значит всё добавлено
        boolean success = leftover.isEmpty();
        
        player.updateInventory();
        return success;
    }
    
    /**
     * Пытается добавить предмет в инвентарь. Если не получается, дропает на землю
     * @param player игрок
     * @param itemStack предмет
     */
    public static void addItemOrDrop(Player player, ItemStack itemStack) {
        if (player == null || itemStack == null) return;
        
        if (!addItem(player, itemStack)) {
            player.getWorld().dropItem(player.getLocation(), itemStack);
        }
    }
    
    /**
     * Заполняет пустые слоты инвентаря указанным предметом
     * @param inventory инвентарь
     * @param itemStack предмет-заполнитель
     */
    public static void fillEmptySlots(Inventory inventory, ItemStack itemStack) {
        if (inventory == null || itemStack == null) return;
        
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack current = inventory.getItem(i);
            if (current == null || current.getType() == Material.AIR) {
                inventory.setItem(i, itemStack.clone());
            }
        }
    }
    
    /**
     * Очищает все слоты инвентаря игрока, включая броню
     * @param player игрок
     */
    public static void clearAll(Player player) {
        if (player == null) return;
        
        PlayerInventory inventory = player.getInventory();
        inventory.clear();
        
        // Очищаем слоты брони
        inventory.setHelmet(null);
        inventory.setChestplate(null);
        inventory.setLeggings(null);
        inventory.setBoots(null);
        
        // Для версий 1.9+, очищаем оффхенд
        inventory.setItemInOffHand(null);
        
        player.updateInventory();
    }
    
    /**
     * Сортирует предметы в инвентаре по материалу
     * @param inventory инвентарь
     */
    public static void sortByMaterial(Inventory inventory) {
        if (inventory == null) return;
        
        // Собираем все предметы
        List<ItemStack> items = new ArrayList<>();
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                items.add(item.clone());
            }
        }
        
        // Сортируем по имени материала
        items.sort((a, b) -> a.getType().name().compareTo(b.getType().name()));
        
        // Очищаем инвентарь
        inventory.clear();
        
        // Добавляем отсортированные предметы обратно
        for (ItemStack item : items) {
            inventory.addItem(item);
        }
    }
    
    /**
     * Проверяет, может ли предмет быть добавлен в инвентарь
     * @param inventory инвентарь
     * @param itemStack предмет
     * @return true, если предмет может быть добавлен
     */
    public static boolean canAddItem(Inventory inventory, ItemStack itemStack) {
        if (inventory == null || itemStack == null) return false;
        
        // Создаём копию инвентаря
        Inventory clone = inventory.getHolder().getInventory();
        
        // Пытаемся добавить предмет
        Map<Integer, ItemStack> leftover = clone.addItem(itemStack.clone());
        
        // Если ничего не осталось, значит всё может быть добавлено
        return leftover.isEmpty();
    }
} 