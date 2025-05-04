package dev.flaymie.fcore.core;

/**
 * Интерфейс сервиса ядра
 */
public interface Service {
    
    /**
     * Вызывается при включении сервиса
     */
    void onEnable();
    
    /**
     * Вызывается при отключении сервиса
     */
    void onDisable();
} 