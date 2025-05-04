package dev.flaymie.fcore.core.action.trigger;

import dev.flaymie.fcore.core.action.Action;
import org.bukkit.entity.Player;

/**
 * Интерфейс триггера для автоматического запуска действий
 */
public interface ActionTrigger {
    
    /**
     * Активирует триггер, привязывая к нему действие
     *
     * @param action действие, которое будет выполнено при срабатывании триггера
     */
    void activate(Action action);
    
    /**
     * Проверяет, активен ли триггер
     *
     * @return true, если триггер активен, иначе false
     */
    boolean isActive();
    
    /**
     * Деактивирует триггер
     */
    void deactivate();
    
    /**
     * Получает тип триггера
     *
     * @return тип триггера
     */
    String getType();
    
    /**
     * Получает уникальный идентификатор триггера
     *
     * @return ID триггера
     */
    String getId();
    
    /**
     * Проверяет, соответствует ли игрок условиям триггера
     *
     * @param player игрок для проверки
     * @return true, если игрок соответствует условиям триггера, иначе false
     */
    boolean matchesPlayer(Player player);
} 