package dev.flaymie.fcore.core.action.debug;

/**
 * Запись в логе отладки действий
 */
public class ActionDebugEntry {
    
    private final long timestamp;
    private final String message;
    
    /**
     * Создает новую запись в логе отладки
     *
     * @param timestamp время создания записи
     * @param message сообщение
     */
    public ActionDebugEntry(long timestamp, String message) {
        this.timestamp = timestamp;
        this.message = message;
    }
    
    /**
     * Получает время создания записи
     *
     * @return время создания записи
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * Получает сообщение записи
     *
     * @return сообщение
     */
    public String getMessage() {
        return message;
    }
} 