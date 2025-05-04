package dev.flaymie.fcore.core.security;

import java.util.ArrayList;
import java.util.List;

/**
 * Хранит результат проверки плагина системой безопасности
 */
public class PluginVerificationResult {
    
    private final String pluginName;
    private final List<String> issues;
    private String fcoreVersion;
    private PluginVerificationStatus status;
    private long timestamp;
    
    /**
     * Создает новый результат проверки плагина
     * @param pluginName имя проверяемого плагина
     */
    public PluginVerificationResult(String pluginName) {
        this.pluginName = pluginName;
        this.issues = new ArrayList<>();
        this.status = PluginVerificationStatus.UNKNOWN;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Получает имя плагина
     * @return имя плагина
     */
    public String getPluginName() {
        return pluginName;
    }
    
    /**
     * Получает список проблем, обнаруженных при проверке
     * @return список проблем
     */
    public List<String> getIssues() {
        return issues;
    }
    
    /**
     * Добавляет проблему в список
     * @param issue описание проблемы
     */
    public void addIssue(String issue) {
        this.issues.add(issue);
    }
    
    /**
     * Получает статус проверки плагина
     * @return статус проверки
     */
    public PluginVerificationStatus getStatus() {
        return status;
    }
    
    /**
     * Устанавливает статус проверки плагина
     * @param status новый статус
     */
    public void setStatus(PluginVerificationStatus status) {
        this.status = status;
    }
    
    /**
     * Получает время проверки
     * @return время проверки в миллисекундах
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * Устанавливает время проверки
     * @param timestamp время проверки в миллисекундах
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    /**
     * Получает версию FCore, с которой совместим плагин
     * @return версия FCore
     */
    public String getFcoreVersion() {
        return fcoreVersion;
    }
    
    /**
     * Устанавливает версию FCore, с которой совместим плагин
     * @param fcoreVersion версия FCore
     */
    public void setFcoreVersion(String fcoreVersion) {
        this.fcoreVersion = fcoreVersion;
    }
    
    /**
     * Проверяет, имеет ли результат проверки проблемы
     * @return true, если есть проблемы
     */
    public boolean hasIssues() {
        return !issues.isEmpty();
    }
    
    /**
     * Проверяет, прошел ли плагин верификацию
     * @return true, если плагин прошел проверку
     */
    public boolean isVerified() {
        return status == PluginVerificationStatus.VERIFIED;
    }
} 