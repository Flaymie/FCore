package dev.flaymie.fcore.core.security;

/**
 * Хранит информацию о зависимости плагина от FCore
 */
public class DependencyInfo {
    
    private final String pluginName;
    private final String fileName;
    private boolean hasFCoreDependency;
    private String fileHash;
    private long lastCheckTime;
    
    /**
     * Создает новый объект информации о зависимости
     * @param pluginName имя плагина
     * @param fileName имя файла плагина
     */
    public DependencyInfo(String pluginName, String fileName) {
        this.pluginName = pluginName;
        this.fileName = fileName;
        this.lastCheckTime = System.currentTimeMillis();
    }
    
    /**
     * Получает имя плагина
     * @return имя плагина
     */
    public String getPluginName() {
        return pluginName;
    }
    
    /**
     * Получает имя файла плагина
     * @return имя файла плагина
     */
    public String getFileName() {
        return fileName;
    }
    
    /**
     * Проверяет, имеет ли плагин зависимость от FCore
     * @return true, если плагин зависит от FCore
     */
    public boolean hasFCoreDependency() {
        return hasFCoreDependency;
    }
    
    /**
     * Устанавливает зависимость плагина от FCore
     * @param hasFCoreDependency true, если плагин зависит от FCore
     */
    public void setHasFCoreDependency(boolean hasFCoreDependency) {
        this.hasFCoreDependency = hasFCoreDependency;
    }
    
    /**
     * Получает хеш файла плагина
     * @return хеш файла
     */
    public String getFileHash() {
        return fileHash;
    }
    
    /**
     * Устанавливает хеш файла плагина
     * @param fileHash хеш файла
     */
    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }
    
    /**
     * Получает время последней проверки плагина
     * @return время последней проверки в миллисекундах
     */
    public long getLastCheckTime() {
        return lastCheckTime;
    }
    
    /**
     * Устанавливает время последней проверки плагина
     * @param lastCheckTime время последней проверки в миллисекундах
     */
    public void setLastCheckTime(long lastCheckTime) {
        this.lastCheckTime = lastCheckTime;
    }
    
    /**
     * Обновляет время последней проверки плагина
     */
    public void updateLastCheckTime() {
        this.lastCheckTime = System.currentTimeMillis();
    }
} 