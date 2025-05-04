package dev.flaymie.fcore.api.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * Интерфейс для GUI
 */
public interface Gui {
    
    /**
     * Открывает инвентарь для игрока
     * @param player игрок
     */
    void open(Player player);
    
    /**
     * Закрывает инвентарь для игрока
     * @param player игрок
     */
    void close(Player player);
    
    /**
     * Закрывает инвентарь по UUID
     * @param uuid UUID игрока
     */
    void close(UUID uuid);
    
    /**
     * Обновляет содержимое GUI
     * @return текущий GUI
     */
    Gui update();
    
    /**
     * Устанавливает предмет в слот
     * @param slot слот
     * @param item предмет
     * @return текущий GUI
     */
    Gui setItem(int slot, ItemStack item);
    
    /**
     * Устанавливает предмет в слот с обработчиком клика
     * @param slot слот
     * @param item предмет
     * @param clickHandler обработчик клика
     * @return текущий GUI
     */
    Gui setItem(int slot, ItemStack item, ClickHandler clickHandler);
    
    /**
     * Устанавливает предмет в слот по координатам
     * @param row строка (0-5)
     * @param column колонка (0-8)
     * @param item предмет
     * @return текущий GUI
     */
    Gui setItem(int row, int column, ItemStack item);
    
    /**
     * Устанавливает предмет в слот по координатам с обработчиком клика
     * @param row строка (0-5)
     * @param column колонка (0-8)
     * @param item предмет
     * @param clickHandler обработчик клика
     * @return текущий GUI
     */
    Gui setItem(int row, int column, ItemStack item, ClickHandler clickHandler);
    
    /**
     * Заполняет все пустые слоты указанным предметом
     * @param item предмет-заполнитель
     * @return текущий GUI
     */
    Gui fillEmptySlots(ItemStack item);
    
    /**
     * Получает предмет из слота
     * @param slot слот
     * @return предмет
     */
    ItemStack getItem(int slot);
    
    /**
     * Устанавливает обработчик клика для слота
     * @param slot слот
     * @param clickHandler обработчик клика
     * @return текущий GUI
     */
    Gui setClickHandler(int slot, ClickHandler clickHandler);
    
    /**
     * Получает количество строк
     * @return количество строк
     */
    int getRows();
    
    /**
     * Получает название инвентаря
     * @return название
     */
    String getTitle();
    
    /**
     * Получает инвентарь
     * @return инвентарь
     */
    Inventory getInventory();
    
    /**
     * Устанавливает, может ли игрок перемещать предметы
     * @param canPlayerMoveItems true, если может
     * @return текущий GUI
     */
    Gui setCanPlayerMoveItems(boolean canPlayerMoveItems);
    
    /**
     * Проверяет, существует ли обработчик для указанного слота
     * @param slot номер слота
     * @return true, если есть обработчик
     */
    boolean hasClickHandler(int slot);
    
    /**
     * Обрабатывает событие клика по инвентарю
     * @param event событие клика
     */
    void handleClick(InventoryClickEvent event);
    
    /**
     * Получает размер GUI (в слотах)
     * @return размер
     */
    int getSize();
    
    /**
     * Проверяет, может ли игрок перемещать предметы
     * @return true, если может
     */
    boolean canPlayerMoveItems();
} 