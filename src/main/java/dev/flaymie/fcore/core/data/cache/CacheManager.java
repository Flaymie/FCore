package dev.flaymie.fcore.core.data.cache;

import dev.flaymie.fcore.FCore;
import dev.flaymie.fcore.api.service.FCoreService;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Менеджер кэширования данных
 */
public class CacheManager implements FCoreService {
    
    private final FCore plugin;
    private final Logger logger;
    
    // Хранилище кэша по типам
    private final Map<String, Cache<?>> caches;
    
    // Настройки
    private long defaultExpirationTime = 300; // 5 минут
    private int cleanupInterval = 60; // 1 минута
    private boolean enabled = true;
    
    public CacheManager(FCore plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.caches = new ConcurrentHashMap<>();
    }
    
    @Override
    public void onEnable() {
        logger.info("Инициализация менеджера кэширования...");
        
        // Запускаем задачу очистки старых данных
        startCleanupTask();
    }
    
    @Override
    public void onDisable() {
        logger.info("Отключение менеджера кэширования...");
        
        // Очищаем все кэши
        clearAll();
    }
    
    @Override
    public String getName() {
        return "CacheManager";
    }
    
    /**
     * Получение объекта из кэша
     * @param key ключ
     * @param <T> тип объекта
     * @return объект из кэша или null
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        if (!enabled) {
            return null;
        }
        
        // Получаем кэш по имени
        Cache<T> cache = (Cache<T>) caches.get(getCacheKey(key));
        if (cache == null) {
            return null;
        }
        
        // Проверяем устаревшие данные
        if (cache.isExpired()) {
            caches.remove(getCacheKey(key));
            return null;
        }
        
        return cache.getValue();
    }
    
    /**
     * Получение объекта из кэша по типу
     * @param type тип кэша
     * @param key ключ
     * @param <T> тип объекта
     * @return объект из кэша или null
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String type, String key) {
        return get(type + ":" + key);
    }
    
    /**
     * Сохранение объекта в кэш
     * @param key ключ
     * @param value значение
     * @param <T> тип объекта
     */
    public <T> void put(String key, T value) {
        put(key, value, defaultExpirationTime);
    }
    
    /**
     * Сохранение объекта в кэш с указанием времени жизни
     * @param key ключ
     * @param value значение
     * @param expirationTime время жизни в секундах
     * @param <T> тип объекта
     */
    public <T> void put(String key, T value, long expirationTime) {
        if (!enabled) {
            return;
        }
        
        Cache<T> cache = new Cache<>(value, expirationTime);
        caches.put(getCacheKey(key), cache);
    }
    
    /**
     * Сохранение объекта в кэш по типу
     * @param type тип кэша
     * @param key ключ
     * @param value значение
     * @param <T> тип объекта
     */
    public <T> void put(String type, String key, T value) {
        put(type + ":" + key, value);
    }
    
    /**
     * Сохранение объекта в кэш по типу с указанием времени жизни
     * @param type тип кэша
     * @param key ключ
     * @param value значение
     * @param expirationTime время жизни в секундах
     * @param <T> тип объекта
     */
    public <T> void put(String type, String key, T value, long expirationTime) {
        put(type + ":" + key, value, expirationTime);
    }
    
    /**
     * Удаление объекта из кэша
     * @param key ключ
     * @return true, если объект был удален
     */
    public boolean remove(String key) {
        return caches.remove(getCacheKey(key)) != null;
    }
    
    /**
     * Удаление объекта из кэша по типу
     * @param type тип кэша
     * @param key ключ
     * @return true, если объект был удален
     */
    public boolean remove(String type, String key) {
        return remove(type + ":" + key);
    }
    
    /**
     * Проверка наличия объекта в кэше
     * @param key ключ
     * @return true, если объект есть в кэше
     */
    public boolean contains(String key) {
        return get(key) != null;
    }
    
    /**
     * Проверка наличия объекта в кэше по типу
     * @param type тип кэша
     * @param key ключ
     * @return true, если объект есть в кэше
     */
    public boolean contains(String type, String key) {
        return contains(type + ":" + key);
    }
    
    /**
     * Очистка всего кэша
     */
    public void clearAll() {
        caches.clear();
    }
    
    /**
     * Очистка кэша по типу
     * @param type тип кэша
     */
    public void clearType(String type) {
        String prefix = type + ":";
        caches.entrySet().removeIf(entry -> entry.getKey().startsWith(prefix));
    }
    
    /**
     * Запуск задачи очистки устаревших данных
     */
    private void startCleanupTask() {
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            if (!enabled) {
                return;
            }
            
            int removed = 0;
            for (Map.Entry<String, Cache<?>> entry : new HashMap<>(caches).entrySet()) {
                if (entry.getValue().isExpired()) {
                    caches.remove(entry.getKey());
                    removed++;
                }
            }
            
            // Выводим в лог только если что-то было удалено
            if (removed > 0 && plugin.getCoreConfig().isDebugMode()) {
                logger.info("Удалено " + removed + " устаревших записей из кэша");
            }
        }, cleanupInterval * 20L, cleanupInterval * 20L);
    }
    
    /**
     * Преобразует ключ для хранения в Map
     * @param key исходный ключ
     * @return преобразованный ключ
     */
    private String getCacheKey(String key) {
        return key.toLowerCase();
    }
    
    /**
     * Получение времени жизни по умолчанию
     * @return время жизни в секундах
     */
    public long getDefaultExpirationTime() {
        return defaultExpirationTime;
    }
    
    /**
     * Установка времени жизни по умолчанию
     * @param defaultExpirationTime время жизни в секундах
     */
    public void setDefaultExpirationTime(long defaultExpirationTime) {
        this.defaultExpirationTime = defaultExpirationTime;
    }
    
    /**
     * Получение интервала очистки
     * @return интервал в секундах
     */
    public int getCleanupInterval() {
        return cleanupInterval;
    }
    
    /**
     * Установка интервала очистки
     * @param cleanupInterval интервал в секундах
     */
    public void setCleanupInterval(int cleanupInterval) {
        this.cleanupInterval = cleanupInterval;
    }
    
    /**
     * Проверка, включен ли кэш
     * @return true, если кэш включен
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Включение/выключение кэша
     * @param enabled true - включить, false - выключить
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    /**
     * Класс для хранения объекта в кэше
     * @param <T> тип объекта
     */
    private static class Cache<T> {
        private final T value;
        private final long expirationTime;
        
        public Cache(T value, long expirationTimeInSeconds) {
            this.value = value;
            this.expirationTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(expirationTimeInSeconds);
        }
        
        public T getValue() {
            return value;
        }
        
        public boolean isExpired() {
            return System.currentTimeMillis() > expirationTime;
        }
    }
} 