package dev.flaymie.fcore.core.action;

import org.bukkit.entity.Player;

/**
 * Функциональный интерфейс для выполнения произвольного кода
 */
@FunctionalInterface
public interface ActionRunnable {
    
    /**
     * Выполняет код для конкретного игрока
     *
     * @param player игрок
     */
    void run(Player player);
} 