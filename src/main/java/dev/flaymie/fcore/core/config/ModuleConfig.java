package dev.flaymie.fcore.core.config;

import dev.flaymie.fcore.core.data.config.ConfigFile;
import dev.flaymie.fcore.core.data.config.ConfigSection;
import dev.flaymie.fcore.core.data.config.ConfigValue;

/**
 * Пример конфигурации модуля с использованием системы аннотаций
 */
@ConfigFile("modules/example.yml")
public class ModuleConfig {
    
    @ConfigValue("enabled")
    private boolean enabled = true;
    
    @ConfigValue("name")
    private String name = "Example Module";
    
    @ConfigValue("version")
    private String version = "1.0.0";
    
    @ConfigSection("settings")
    private Settings settings = new Settings();
    
    @ConfigSection("database")
    private Database database = new Database();
    
    /**
     * Настройки модуля
     */
    public static class Settings {
        
        @ConfigValue("debug-mode")
        private boolean debugMode = false;
        
        @ConfigValue("auto-save")
        private boolean autoSave = true;
        
        @ConfigValue("save-interval")
        private int saveInterval = 300;
        
        public boolean isDebugMode() {
            return debugMode;
        }
        
        public void setDebugMode(boolean debugMode) {
            this.debugMode = debugMode;
        }
        
        public boolean isAutoSave() {
            return autoSave;
        }
        
        public void setAutoSave(boolean autoSave) {
            this.autoSave = autoSave;
        }
        
        public int getSaveInterval() {
            return saveInterval;
        }
        
        public void setSaveInterval(int saveInterval) {
            this.saveInterval = saveInterval;
        }
    }
    
    /**
     * Настройки базы данных модуля
     */
    public static class Database {
        
        @ConfigValue("use-global")
        private boolean useGlobal = true;
        
        @ConfigValue("table-prefix")
        private String tablePrefix = "example_";
        
        public boolean isUseGlobal() {
            return useGlobal;
        }
        
        public void setUseGlobal(boolean useGlobal) {
            this.useGlobal = useGlobal;
        }
        
        public String getTablePrefix() {
            return tablePrefix;
        }
        
        public void setTablePrefix(String tablePrefix) {
            this.tablePrefix = tablePrefix;
        }
    }
    
    // Геттеры и сеттеры
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public Settings getSettings() {
        return settings;
    }
    
    public void setSettings(Settings settings) {
        this.settings = settings;
    }
    
    public Database getDatabase() {
        return database;
    }
    
    public void setDatabase(Database database) {
        this.database = database;
    }
} 