package dev.flaymie.fcore.api.plugin;

import dev.flaymie.fcore.FCore;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Базовый класс для плагинов FCore
 */
public abstract class FCorePlugin {
    
    protected final FCore core;
    protected final String name;
    protected final String version;
    
    private File configFile;
    private FileConfiguration config;
    private boolean isEnabled = false;
    
    /**
     * Конструктор плагина
     * @param core экземпляр FCore
     * @param name имя плагина
     * @param version версия плагина
     */
    public FCorePlugin(FCore core, String name, String version) {
        this.core = core;
        this.name = name;
        this.version = version;
    }
    
    /**
     * Вызывается при загрузке плагина
     */
    public void onLoad() {
        // Создаем директорию для плагина
        File dataFolder = new File(core.getDataFolder(), "plugins/" + name);
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        
        // Загружаем конфигурацию
        configFile = new File(dataFolder, "config.yml");
        
        // Если конфиг не существует, создаем его
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                
                // Если есть ресурс config.yml в jar, копируем его
                InputStream defaultConfig = getClass().getResourceAsStream("/fcore_plugin/" + name + "/config.yml");
                if (defaultConfig != null) {
                    YamlConfiguration defaultConfigYaml = YamlConfiguration.loadConfiguration(
                            new InputStreamReader(defaultConfig, StandardCharsets.UTF_8));
                    defaultConfigYaml.save(configFile);
                }
            } catch (IOException e) {
                core.getLogger().severe("Не удалось создать конфигурацию для плагина " + name + ": " + e.getMessage());
            }
        }
        
        // Загружаем конфигурацию
        config = YamlConfiguration.loadConfiguration(configFile);
    }
    
    /**
     * Вызывается при включении плагина
     */
    public abstract void onEnable();
    
    /**
     * Вызывается при отключении плагина
     */
    public abstract void onDisable();
    
    /**
     * Получает имя плагина
     * @return имя плагина
     */
    public String getName() {
        return name;
    }
    
    /**
     * Получает версию плагина
     * @return версия плагина
     */
    public String getVersion() {
        return version;
    }
    
    /**
     * Получает конфигурацию плагина
     * @return конфигурация плагина
     */
    public FileConfiguration getConfig() {
        return config;
    }
    
    /**
     * Сохраняет конфигурацию плагина
     */
    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            core.getLogger().severe("Не удалось сохранить конфигурацию плагина " + name + ": " + e.getMessage());
        }
    }
    
    /**
     * Проверяет, включен ли плагин
     * @return true, если плагин включен
     */
    public boolean isEnabled() {
        return isEnabled;
    }
    
    /**
     * Включает плагин
     */
    void enable() {
        if (!isEnabled) {
            onEnable();
            isEnabled = true;
            core.getLogger().info("Плагин " + name + " v" + version + " включен");
        }
    }
    
    /**
     * Отключает плагин
     */
    void disable() {
        if (isEnabled) {
            onDisable();
            isEnabled = false;
            core.getLogger().info("Плагин " + name + " отключен");
        }
    }
} 