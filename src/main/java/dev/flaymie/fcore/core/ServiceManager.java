package dev.flaymie.fcore.core;

import dev.flaymie.fcore.FCore;
import dev.flaymie.fcore.api.service.FCoreService;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Менеджер сервисов FCore, управляет жизненным циклом сервисов
 */
public class ServiceManager {
    
    private final FCore plugin;
    private final Logger logger;
    private final Map<Class<? extends FCoreService>, FCoreService> services;
    
    public ServiceManager(FCore plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.services = new HashMap<>();
    }
    
    /**
     * Регистрирует сервис в менеджере
     * @param serviceClass класс сервиса
     * @param service экземпляр сервиса
     * @param <T> тип сервиса
     */
    public <T extends FCoreService> void registerService(Class<T> serviceClass, T service) {
        services.put(serviceClass, service);
        logger.info("Сервис " + service.getName() + " зарегистрирован");
    }
    
    /**
     * Получает сервис по его классу
     * @param serviceClass класс сервиса
     * @param <T> тип сервиса
     * @return экземпляр сервиса или null, если сервис не найден
     */
    @SuppressWarnings("unchecked")
    public <T extends FCoreService> T getService(Class<T> serviceClass) {
        return (T) services.get(serviceClass);
    }
    
    /**
     * Инициализирует все зарегистрированные сервисы
     */
    public void enableAllServices() {
        logger.info("Запуск сервисов...");
        for (FCoreService service : services.values()) {
            try {
                service.onEnable();
                logger.info("Сервис " + service.getName() + " запущен");
            } catch (Exception e) {
                logger.severe("Ошибка при запуске сервиса " + service.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Отключает все зарегистрированные сервисы
     */
    public void disableAllServices() {
        logger.info("Отключение сервисов...");
        for (FCoreService service : services.values()) {
            try {
                service.onDisable();
                logger.info("Сервис " + service.getName() + " отключен");
            } catch (Exception e) {
                logger.severe("Ошибка при отключении сервиса " + service.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        services.clear();
    }
    
    /**
     * Проверяет, зарегистрирован ли сервис
     * @param serviceClass класс сервиса
     * @return true, если сервис зарегистрирован
     */
    public boolean hasService(Class<? extends FCoreService> serviceClass) {
        return services.containsKey(serviceClass);
    }
    
    /**
     * Получает количество зарегистрированных сервисов
     * @return количество сервисов
     */
    public int getServicesCount() {
        return services.size();
    }
} 