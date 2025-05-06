package dev.flaymie.fcore.integration.placeholderapi;

import dev.flaymie.fcore.FCore;
import dev.flaymie.fcore.api.service.FCoreService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.function.Function;
import java.util.logging.Logger;

/**
 * Менеджер интеграции с PlaceholderAPI
 */
public class PlaceholderAPIManager implements FCoreService {
    
    private final FCore plugin;
    private final Logger logger;
    private FCorePlaceholderExpansion expansion;
    private boolean enabled = false;
    
    public PlaceholderAPIManager(FCore plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }
    
    @Override
    public void onEnable() {
        if (!isPlaceholderAPIInstalled()) {
            logger.info("PlaceholderAPI не обнаружен, интеграция отключена");
            return;
        }
        
        try {
            // Проверяем, что класс PlaceholderExpansion доступен
            Class.forName("me.clip.placeholderapi.expansion.PlaceholderExpansion");
            
        // Создаем и регистрируем расширение
        expansion = new FCorePlaceholderExpansion(plugin);
        
        if (expansion.register()) {
            logger.info("Интеграция с PlaceholderAPI включена");
            enabled = true;
        } else {
            logger.warning("Не удалось зарегистрировать расширение PlaceholderAPI");
            }
        } catch (ClassNotFoundException e) {
            logger.warning("Класс PlaceholderExpansion не найден, интеграция отключена");
        } catch (Exception e) {
            logger.severe("Ошибка при инициализации интеграции с PlaceholderAPI: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public void onDisable() {
        if (enabled && expansion != null) {
            // Отменяем регистрацию расширения
            expansion.unregister();
            logger.info("Интеграция с PlaceholderAPI отключена");
            enabled = false;
        }
    }
    
    @Override
    public String getName() {
        return "PlaceholderAPIManager";
    }
    
    /**
     * Проверяет, установлен ли PlaceholderAPI
     * @return true, если PlaceholderAPI установлен
     */
    public boolean isPlaceholderAPIInstalled() {
        return Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
    }
    
    /**
     * Проверяет, включена ли интеграция с PlaceholderAPI
     * @return true, если интеграция включена
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Регистрирует новый плейсхолдер
     * @param key ключ плейсхолдера (без %fcore_%)
     * @param function функция, возвращающая значение плейсхолдера
     * @return true, если плейсхолдер успешно зарегистрирован
     */
    public boolean registerPlaceholder(String key, Function<Player, String> function) {
        if (!enabled || expansion == null) {
            return false;
        }
        
        expansion.registerPlaceholder(key, function);
        logger.info("Зарегистрирован плейсхолдер: fcore_" + key);
        return true;
    }
    
    /**
     * Форматирует текст с использованием PlaceholderAPI для указанного игрока
     * @param player игрок
     * @param text текст для форматирования
     * @return отформатированный текст или исходный текст, если PlaceholderAPI недоступен
     */
    public String setPlaceholders(Player player, String text) {
        if (!enabled || text == null) {
            return text;
        }
        
        try {
            return me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, text);
        } catch (Exception e) {
            logger.warning("Ошибка при форматировании плейсхолдеров: " + e.getMessage());
            return text;
        }
    }
} 