package dev.flaymie.fcore.api.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;

/**
 * Интерфейс для обработки закрытия GUI
 */
@FunctionalInterface
public interface CloseHandler {
    
    /**
     * Вызывается при закрытии GUI
     * @param event оригинальное событие закрытия инвентаря
     * @param player игрок, закрывший GUI
     * @param gui GUI, которое было закрыто
     */
    void onClose(InventoryCloseEvent event, Player player, Gui gui);
} 