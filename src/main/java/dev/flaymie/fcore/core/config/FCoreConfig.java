package dev.flaymie.fcore.core.config;

import dev.flaymie.fcore.FCore;
import dev.flaymie.fcore.api.service.FCoreService;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Управление конфигурацией ядра
 */
public class FCoreConfig implements FCoreService {

    private final FCore plugin;
    private final Logger logger;
    
    private File configFile;
    private FileConfiguration config;
    
    // Настройки ядра
    private boolean debugMode;
    private String locale;
    private boolean autoScanServices;
    private String serverName;
    
    public FCoreConfig(FCore plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }
    
    @Override
    public void onEnable() {
        // Создаем конфигурацию по умолчанию
        plugin.saveDefaultConfig();
        
        // Загружаем конфигурацию
        loadConfig();
        
        logger.info("Конфигурация загружена");
    }
    
    @Override
    public void onDisable() {
        // Сохраняем конфигурацию при отключении
        saveConfig();
        
        logger.info("Конфигурация сохранена");
    }
    
    @Override
    public String getName() {
        return "FCoreConfig";
    }
    
    /**
     * Загружает конфигурацию из файла
     */
    public void loadConfig() {
        config = plugin.getConfig();
        
        // Загружаем основные настройки
        debugMode = config.getBoolean("core.debug", false);
        locale = config.getString("core.locale", "ru_RU");
        autoScanServices = config.getBoolean("core.auto-scan-services", true);
        serverName = config.getString("core.server-name", "MinecraftServer");
        
        logger.info("Режим отладки: " + (debugMode ? "включен" : "выключен"));
        logger.info("Локализация: " + locale);
    }
    
    /**
     * Сохраняет конфигурацию в файл
     */
    public void saveConfig() {
        // Обновляем значения в конфигурации
        config.set("core.debug", debugMode);
        config.set("core.locale", locale);
        config.set("core.auto-scan-services", autoScanServices);
        config.set("core.server-name", serverName);
        
        try {
            config.save(new File(plugin.getDataFolder(), "config.yml"));
        } catch (IOException e) {
            logger.severe("Не удалось сохранить конфигурацию: " + e.getMessage());
        }
    }
    
    /**
     * Создает файл конфигурации, если он не существует
     * @param fileName имя файла
     * @return объект FileConfiguration
     */
    public FileConfiguration createConfig(String fileName) {
        File file = new File(plugin.getDataFolder(), fileName);
        
        if (!file.exists()) {
            // Копируем файл из ресурсов, если он там есть
            if (plugin.getResource(fileName) != null) {
                plugin.saveResource(fileName, false);
            } else {
                // Иначе создаем пустой файл
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    logger.severe("Не удалось создать файл " + fileName + ": " + e.getMessage());
                }
            }
        }
        
        return YamlConfiguration.loadConfiguration(file);
    }
    
    /**
     * Получение режима отладки
     * @return true, если режим отладки включен
     */
    public boolean isDebugMode() {
        return debugMode;
    }
    
    /**
     * Установка режима отладки
     * @param debugMode новое значение
     */
    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }
    
    /**
     * Получение локализации
     * @return код локализации
     */
    public String getLocale() {
        return locale;
    }
    
    /**
     * Установка локализации
     * @param locale новое значение
     */
    public void setLocale(String locale) {
        this.locale = locale;
    }
    
    /**
     * Проверка автоматического сканирования сервисов
     * @return true, если автоматическое сканирование включено
     */
    public boolean isAutoScanServices() {
        return autoScanServices;
    }
    
    /**
     * Получение имени сервера
     * @return имя сервера
     */
    public String getServerName() {
        return serverName;
    }
    
    /**
     * Установка имени сервера
     * @param serverName новое значение
     */
    public void setServerName(String serverName) {
        this.serverName = serverName;
    }
} 