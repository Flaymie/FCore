package dev.flaymie.fcore.api.event;

import dev.flaymie.fcore.FCore;
import dev.flaymie.fcore.api.annotation.Inject;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

/**
 * Абстрактный класс для слушателей событий
 * Предоставляет базовую функциональность для всех слушателей
 */
public abstract class AbstractEventListener implements Listener {
    
    @Inject
    protected FCore plugin;
    
    /**
     * Получает плагин
     * @return экземпляр плагина
     */
    public Plugin getPlugin() {
        return plugin;
    }
    
    /**
     * Вызывает событие через менеджер событий
     * @param event событие
     * @return true если событие не было отменено
     */
    protected boolean callEvent(FCoreEvent event) {
        if (plugin == null) {
            throw new IllegalStateException("Плагин не инициализирован");
        }
        return plugin.getEventManager().callEvent(event);
    }
    
    /**
     * Проверяет, инициализирован ли слушатель
     * @return true если слушатель инициализирован
     */
    public boolean isInitialized() {
        return plugin != null;
    }
} 