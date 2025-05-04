package dev.flaymie.fcore.api.gui;

import org.bukkit.inventory.ItemStack;

/**
 * Интерфейс для билдера GUI
 */
public interface GuiBuilder {
    
    /**
     * Устанавливает название GUI
     * @param title название
     * @return текущий билдер
     */
    GuiBuilder title(String title);
    
    /**
     * Устанавливает количество рядов в GUI (от 1 до 6)
     * @param rows количество рядов
     * @return текущий билдер
     */
    GuiBuilder rows(int rows);
    
    /**
     * Добавляет предмет в указанный слот
     * @param slot номер слота
     * @param item предмет
     * @return текущий билдер
     */
    GuiBuilder item(int slot, ItemStack item);
    
    /**
     * Добавляет предмет в указанный слот с обработчиком кликов
     * @param slot номер слота
     * @param item предмет
     * @param clickHandler обработчик кликов
     * @return текущий билдер
     */
    GuiBuilder item(int slot, ItemStack item, ClickHandler clickHandler);
    
    /**
     * Добавляет предмет на указанную позицию (ряд, колонка)
     * @param row ряд (начиная с 0)
     * @param col колонка (начиная с 0)
     * @param item предмет
     * @return текущий билдер
     */
    GuiBuilder item(int row, int col, ItemStack item);
    
    /**
     * Добавляет предмет на указанную позицию (ряд, колонка) с обработчиком кликов
     * @param row ряд (начиная с 0)
     * @param col колонка (начиная с 0)
     * @param item предмет
     * @param clickHandler обработчик кликов
     * @return текущий билдер
     */
    GuiBuilder item(int row, int col, ItemStack item, ClickHandler clickHandler);
    
    /**
     * Заполняет рамку GUI указанным предметом
     * @param item предмет для рамки
     * @return текущий билдер
     */
    GuiBuilder border(ItemStack item);
    
    /**
     * Заполняет рамку GUI указанным предметом с обработчиком кликов
     * @param item предмет для рамки
     * @param clickHandler обработчик кликов
     * @return текущий билдер
     */
    GuiBuilder border(ItemStack item, ClickHandler clickHandler);
    
    /**
     * Заполняет все пустые слоты указанным предметом
     * @param item предмет-заполнитель
     * @return текущий билдер
     */
    GuiBuilder fillEmpty(ItemStack item);
    
    /**
     * Заполняет все пустые слоты указанным предметом с обработчиком кликов
     * @param item предмет-заполнитель
     * @param clickHandler обработчик кликов
     * @return текущий билдер
     */
    GuiBuilder fillEmpty(ItemStack item, ClickHandler clickHandler);
    
    /**
     * Устанавливает, можно ли перемещать предметы в GUI
     * @param canMove true, если можно
     * @return текущий билдер
     */
    GuiBuilder canPlayerMoveItems(boolean canMove);
    
    /**
     * Добавляет обработчик, который будет вызван при закрытии GUI
     * @param closeHandler обработчик закрытия
     * @return текущий билдер
     */
    GuiBuilder onClose(CloseHandler closeHandler);
    
    /**
     * Создает GUI на основе настроек билдера
     * @return готовый GUI
     */
    Gui build();
} 