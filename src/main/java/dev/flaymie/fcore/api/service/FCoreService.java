package dev.flaymie.fcore.api.service;

/**
 * Интерфейс для сервисов ядра FCore
 */
public interface FCoreService {
    
    /**
     * Вызывается при включении сервиса
     */
    void onEnable();
    
    /**
     * Вызывается при отключении сервиса
     */
    void onDisable();
    
    /**
     * Получает имя сервиса
     * @return имя сервиса
     */
    String getName();
} 