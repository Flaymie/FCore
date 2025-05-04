package dev.flaymie.fcore.core.data.migration;

/**
 * Интерфейс для миграций базы данных
 */
public interface Migration {
    
    /**
     * Получение версии миграции
     * @return версия миграции (обычно в формате YYYYMMDD)
     */
    int getVersion();
    
    /**
     * Получение имени миграции
     * @return имя миграции
     */
    String getName();
    
    /**
     * Выполнение миграции
     * @return true, если миграция выполнена успешно
     */
    boolean up();
    
    /**
     * Откат миграции
     * @return true, если миграция откачена успешно
     */
    boolean down();
} 