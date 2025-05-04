package dev.flaymie.fcore.core.action;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Контекст выполнения действий
 * Используется для передачи данных между действиями в цепочке
 */
public class ActionContext {
    
    private final Map<String, Object> data;
    private final Map<String, Object> globalData;
    private final ActionContext parentContext;
    
    /**
     * Создает пустой контекст
     */
    public ActionContext() {
        this(null);
    }
    
    /**
     * Создает контекст с родительским контекстом
     *
     * @param parentContext родительский контекст
     */
    public ActionContext(ActionContext parentContext) {
        this.data = new HashMap<>();
        this.globalData = new HashMap<>();
        this.parentContext = parentContext;
    }
    
    /**
     * Устанавливает значение в локальном контексте
     *
     * @param key ключ
     * @param value значение
     * @return текущий контекст
     */
    public ActionContext set(String key, Object value) {
        data.put(key, value);
        return this;
    }
    
    /**
     * Устанавливает значение в глобальном контексте
     *
     * @param key ключ
     * @param value значение
     * @return текущий контекст
     */
    public ActionContext setGlobal(String key, Object value) {
        globalData.put(key, value);
        
        // Пробрасываем в родительский контекст если есть
        if (parentContext != null) {
            parentContext.setGlobal(key, value);
        }
        
        return this;
    }
    
    /**
     * Получает значение из контекста
     *
     * @param key ключ
     * @param <T> тип значения
     * @return значение или Optional.empty() если значение не найдено
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String key) {
        if (data.containsKey(key)) {
            return Optional.ofNullable((T) data.get(key));
        }
        
        if (globalData.containsKey(key)) {
            return Optional.ofNullable((T) globalData.get(key));
        }
        
        if (parentContext != null) {
            return parentContext.get(key);
        }
        
        return Optional.empty();
    }
    
    /**
     * Получает значение из контекста или возвращает значение по умолчанию
     *
     * @param key ключ
     * @param defaultValue значение по умолчанию
     * @param <T> тип значения
     * @return значение или defaultValue если значение не найдено
     */
    @SuppressWarnings("unchecked")
    public <T> T getOrDefault(String key, T defaultValue) {
        Optional<T> value = this.get(key);
        return value.orElse(defaultValue);
    }
    
    /**
     * Проверяет, содержит ли контекст значение по указанному ключу
     *
     * @param key ключ
     * @return true если значение найдено, иначе false
     */
    public boolean has(String key) {
        return data.containsKey(key) || 
               globalData.containsKey(key) || 
               (parentContext != null && parentContext.has(key));
    }
    
    /**
     * Удаляет значение из локального контекста
     *
     * @param key ключ
     * @return текущий контекст
     */
    public ActionContext remove(String key) {
        data.remove(key);
        return this;
    }
    
    /**
     * Удаляет значение из глобального контекста
     *
     * @param key ключ
     * @return текущий контекст
     */
    public ActionContext removeGlobal(String key) {
        globalData.remove(key);
        
        if (parentContext != null) {
            parentContext.removeGlobal(key);
        }
        
        return this;
    }
    
    /**
     * Создает новый контекст, наследующий текущий
     *
     * @return новый контекст с ссылкой на текущий как родительский
     */
    public ActionContext createChild() {
        return new ActionContext(this);
    }
} 