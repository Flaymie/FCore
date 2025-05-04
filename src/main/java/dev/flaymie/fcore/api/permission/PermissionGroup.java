package dev.flaymie.fcore.api.permission;

import java.util.*;
import java.util.function.Predicate;

/**
 * Группа прав с поддержкой наследования и условий
 */
public class PermissionGroup {
    
    private final String name;
    private final Map<String, Boolean> permissions;
    private final Map<String, Predicate<PermissionContext>> contextualPermissions;
    private final Set<String> parents;
    private final Map<String, Long> temporaryPermissions;
    
    /**
     * Создает группу прав
     * @param name название группы
     */
    public PermissionGroup(String name) {
        this.name = name;
        this.permissions = new HashMap<>();
        this.contextualPermissions = new HashMap<>();
        this.parents = new HashSet<>();
        this.temporaryPermissions = new HashMap<>();
    }
    
    /**
     * Возвращает название группы
     * @return название группы
     */
    public String getName() {
        return name;
    }
    
    /**
     * Добавляет право в группу
     * @param permission право
     * @param value значение (true/false)
     */
    public void addPermission(String permission, boolean value) {
        permissions.put(permission, value);
    }
    
    /**
     * Добавляет контекстное право в группу
     * @param permission право
     * @param value значение (true/false)
     * @param contextPredicate предикат проверки контекста
     */
    public void addContextualPermission(String permission, boolean value, Predicate<PermissionContext> contextPredicate) {
        permissions.put(permission, value);
        if (contextPredicate != null) {
            contextualPermissions.put(permission, contextPredicate);
        }
    }
    
    /**
     * Добавляет временное право в группу
     * @param permission право
     * @param value значение (true/false)
     * @param expireTime время истечения в миллисекундах
     */
    public void addTemporaryPermission(String permission, boolean value, long expireTime) {
        permissions.put(permission, value);
        temporaryPermissions.put(permission, expireTime);
    }
    
    /**
     * Удаляет право из группы
     * @param permission право
     */
    public void removePermission(String permission) {
        permissions.remove(permission);
        contextualPermissions.remove(permission);
        temporaryPermissions.remove(permission);
    }
    
    /**
     * Проверяет наличие права в группе
     * @param permission право
     * @return true, если право есть и оно положительное, false иначе
     */
    public boolean hasPermission(String permission) {
        // Проверяем точное соответствие
        if (permissions.containsKey(permission)) {
            return permissions.get(permission);
        }
        
        // Проверяем права с подстановкой (node.*)
        for (Map.Entry<String, Boolean> entry : permissions.entrySet()) {
            String key = entry.getKey();
            Boolean value = entry.getValue();
            
            if (key.endsWith(".*") && permission.startsWith(key.substring(0, key.length() - 2))) {
                return value;
            }
        }
        
        return false;
    }
    
    /**
     * Проверяет наличие права в группе с учетом контекста
     * @param permission право
     * @param context контекст
     * @return true, если право есть, оно положительное и удовлетворяет контексту, false иначе
     */
    public boolean hasPermission(String permission, PermissionContext context) {
        // Если для права есть контекстное условие, проверяем его
        if (contextualPermissions.containsKey(permission)) {
            Predicate<PermissionContext> predicate = contextualPermissions.get(permission);
            if (!predicate.test(context)) {
                return false; // Контекст не выполняется
            }
        }
        
        // Проверяем, не истекло ли временное право
        if (temporaryPermissions.containsKey(permission)) {
            long expireTime = temporaryPermissions.get(permission);
            if (System.currentTimeMillis() > expireTime) {
                // Право истекло, удаляем его
                removePermission(permission);
                return false;
            }
        }
        
        // Проверяем само право
        return hasPermission(permission);
    }
    
    /**
     * Добавляет родительскую группу
     * @param parent название родительской группы
     */
    public void addParent(String parent) {
        parents.add(parent);
    }
    
    /**
     * Удаляет родительскую группу
     * @param parent название родительской группы
     */
    public void removeParent(String parent) {
        parents.remove(parent);
    }
    
    /**
     * Возвращает список родительских групп
     * @return список родительских групп
     */
    public Set<String> getParents() {
        return Collections.unmodifiableSet(parents);
    }
    
    /**
     * Возвращает все права группы
     * @return карта прав
     */
    public Map<String, Boolean> getPermissions() {
        return Collections.unmodifiableMap(permissions);
    }
    
    /**
     * Возвращает контекстные условия для прав
     * @return карта контекстных условий
     */
    public Map<String, Predicate<PermissionContext>> getContextualPermissions() {
        return Collections.unmodifiableMap(contextualPermissions);
    }
    
    /**
     * Возвращает временные права группы
     * @return карта временных прав
     */
    public Map<String, Long> getTemporaryPermissions() {
        return Collections.unmodifiableMap(temporaryPermissions);
    }
} 