package dev.flaymie.fcore.api.permission;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Строитель для групп прав с поддержкой цепочек вызовов
 */
public class PermissionBuilder {
    
    private final PermissionManager permissionManager;
    private final Map<String, PermissionGroup> groups;
    private PermissionGroup currentGroup;
    
    /**
     * Создает строитель прав
     * @param permissionManager менеджер прав
     */
    public PermissionBuilder(PermissionManager permissionManager) {
        this.permissionManager = permissionManager;
        this.groups = new HashMap<>();
    }
    
    /**
     * Начинает определение группы прав
     * @param name название группы
     * @return строитель группы
     */
    public GroupBuilder group(String name) {
        // Если группа уже существует в менеджере, используем её
        PermissionGroup existingGroup = permissionManager.getGroup(name);
        if (existingGroup != null) {
            currentGroup = existingGroup;
            groups.put(name, existingGroup);
        } else {
            // Иначе создаем новую группу
            currentGroup = new PermissionGroup(name);
            groups.put(name, currentGroup);
        }
        
        return new GroupBuilder(this, currentGroup);
    }
    
    /**
     * Регистрирует все определенные группы в менеджере прав
     */
    public void register() {
        for (PermissionGroup group : groups.values()) {
            permissionManager.registerGroup(group);
        }
    }
    
    /**
     * Строитель для конкретной группы прав
     */
    public static class GroupBuilder {
        
        private final PermissionBuilder parent;
        private final PermissionGroup group;
        
        /**
         * Создает строитель группы
         * @param parent родительский строитель
         * @param group группа прав
         */
        public GroupBuilder(PermissionBuilder parent, PermissionGroup group) {
            this.parent = parent;
            this.group = group;
        }
        
        /**
         * Добавляет право в группу
         * @param permission право
         * @return строитель группы
         */
        public GroupBuilder add(String permission) {
            group.addPermission(permission, true);
            return this;
        }
        
        /**
         * Добавляет отрицательное право в группу
         * @param permission право
         * @return строитель группы
         */
        public GroupBuilder deny(String permission) {
            group.addPermission(permission, false);
            return this;
        }
        
        /**
         * Добавляет право с явным указанием значения
         * @param permission право
         * @param value значение
         * @return строитель группы
         */
        public GroupBuilder permission(String permission, boolean value) {
            group.addPermission(permission, value);
            return this;
        }
        
        /**
         * Добавляет контекстное право
         * @param permission право
         * @param value значение
         * @param contextPredicate предикат контекста
         * @return строитель группы
         */
        public GroupBuilder permission(String permission, boolean value, Predicate<PermissionContext> contextPredicate) {
            group.addContextualPermission(permission, value, contextPredicate);
            return this;
        }
        
        /**
         * Добавляет временное право
         * @param permission право
         * @param value значение
         * @param durationMillis длительность в миллисекундах
         * @return строитель группы
         */
        public GroupBuilder temporary(String permission, boolean value, long durationMillis) {
            long expireTime = System.currentTimeMillis() + durationMillis;
            group.addTemporaryPermission(permission, value, expireTime);
            return this;
        }
        
        /**
         * Добавляет родительскую группу
         * @param parentName название родительской группы
         * @return строитель группы
         */
        public GroupBuilder parent(String parentName) {
            group.addParent(parentName);
            return this;
        }
        
        /**
         * Добавляет все дочерние права от указанного корня
         * @param basePermission базовое право (например, fcore.command.*)
         * @return строитель группы
         */
        public GroupBuilder addChildren(String basePermission) {
            if (!basePermission.endsWith(".*")) {
                basePermission = basePermission + ".*";
            }
            group.addPermission(basePermission, true);
            return this;
        }
        
        /**
         * Переключается на определение другой группы прав
         * @param name название группы
         * @return строитель новой группы
         */
        public GroupBuilder group(String name) {
            return parent.group(name);
        }
        
        /**
         * Завершает определение групп и регистрирует их
         */
        public void register() {
            parent.register();
        }
    }
} 