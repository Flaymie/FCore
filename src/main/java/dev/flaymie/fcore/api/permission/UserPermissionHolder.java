package dev.flaymie.fcore.api.permission;

import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.Predicate;

/**
 * Класс для хранения пользовательских прав
 */
public class UserPermissionHolder {
    
    private final UUID playerId;
    private final String playerName;
    private final Map<String, Boolean> permissions;
    private final Map<String, Predicate<PermissionContext>> contextualPermissions;
    private final Map<String, Long> temporaryPermissions;
    private final Set<String> groups;
    
    /**
     * Создает хранилище прав игрока
     * @param player игрок
     */
    public UserPermissionHolder(Player player) {
        this.playerId = player.getUniqueId();
        this.playerName = player.getName();
        this.permissions = new HashMap<>();
        this.contextualPermissions = new HashMap<>();
        this.temporaryPermissions = new HashMap<>();
        this.groups = new HashSet<>();
    }
    
    /**
     * Загружает хранилище прав из карты значений
     * @param player игрок
     * @param data карта с данными
     */
    public UserPermissionHolder(Player player, Map<String, Object> data) {
        this(player);
        
        // Загружаем права
        if (data.containsKey("permissions")) {
            @SuppressWarnings("unchecked")
            Map<String, Boolean> perms = (Map<String, Boolean>) data.get("permissions");
            permissions.putAll(perms);
        }
        
        // Загружаем группы
        if (data.containsKey("groups")) {
            @SuppressWarnings("unchecked")
            List<String> groupList = (List<String>) data.get("groups");
            groups.addAll(groupList);
        }
        
        // Загружаем временные права
        if (data.containsKey("temporary")) {
            @SuppressWarnings("unchecked")
            Map<String, Long> tempPerms = (Map<String, Long>) data.get("temporary");
            temporaryPermissions.putAll(tempPerms);
        }
    }
    
    /**
     * Возвращает ID игрока
     * @return ID игрока
     */
    public UUID getPlayerId() {
        return playerId;
    }
    
    /**
     * Возвращает имя игрока
     * @return имя игрока
     */
    public String getPlayerName() {
        return playerName;
    }
    
    /**
     * Добавляет право игроку
     * @param permission право
     * @param value значение (true/false)
     */
    public void addPermission(String permission, boolean value) {
        permissions.put(permission, value);
    }
    
    /**
     * Добавляет контекстное право игроку
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
     * Добавляет временное право игроку
     * @param permission право
     * @param value значение (true/false)
     * @param expireTime время истечения в миллисекундах
     */
    public void addTemporaryPermission(String permission, boolean value, long expireTime) {
        permissions.put(permission, value);
        temporaryPermissions.put(permission, expireTime);
    }
    
    /**
     * Удаляет право у игрока
     * @param permission право
     */
    public void removePermission(String permission) {
        permissions.remove(permission);
        contextualPermissions.remove(permission);
        temporaryPermissions.remove(permission);
    }
    
    /**
     * Проверяет наличие права у игрока
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
     * Проверяет наличие права у игрока с учетом контекста
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
     * Добавляет игрока в группу
     * @param group название группы
     */
    public void addGroup(String group) {
        groups.add(group);
    }
    
    /**
     * Удаляет игрока из группы
     * @param group название группы
     */
    public void removeGroup(String group) {
        groups.remove(group);
    }
    
    /**
     * Проверяет, входит ли игрок в группу
     * @param group название группы
     * @return true, если игрок в группе
     */
    public boolean inGroup(String group) {
        return groups.contains(group);
    }
    
    /**
     * Возвращает список групп игрока
     * @return список групп
     */
    public Set<String> getGroups() {
        return Collections.unmodifiableSet(groups);
    }
    
    /**
     * Возвращает все права игрока
     * @return карта прав
     */
    public Map<String, Boolean> getPermissions() {
        return Collections.unmodifiableMap(permissions);
    }
    
    /**
     * Конвертирует права игрока в карту для сохранения
     * @return карта данных
     */
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        
        result.put("uuid", playerId.toString());
        result.put("name", playerName);
        result.put("permissions", new HashMap<>(permissions));
        result.put("groups", new ArrayList<>(groups));
        result.put("temporary", new HashMap<>(temporaryPermissions));
        
        return result;
    }
} 