package dev.flaymie.fcore.api.gui;

import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * Интерфейс для обработки кликов в GUI
 */
@FunctionalInterface
public interface ClickHandler {
    
    /**
     * Обрабатывает клик по предмету в инвентаре
     * @param event событие клика
     */
    void onClick(InventoryClickEvent event);
} 