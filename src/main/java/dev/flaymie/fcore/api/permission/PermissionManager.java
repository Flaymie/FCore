package dev.flaymie.fcore.api.permission;

import dev.flaymie.fcore.api.service.FCoreService;
import org.bukkit.entity.Player;

/**
 * Интерфейс для управления правами
 */
public interface PermissionManager extends FCoreService {
    
    /**
     * Проверяет наличие права у игрока
     * @param player игрок
     * @param permission право
     * @return true, если у игрока есть право
     */
    boolean hasPermission(Player player, String permission);
    
    /**
     * Проверяет наличие права у игрока с учетом контекста
     * @param player игрок
     * @param permission право
     * @param context контекст проверки права
     * @return true, если у игрока есть право
     */
    boolean hasPermission(Player player, String permission, PermissionContext context);
    
    /**
     * Регистрирует группу прав
     * @param group группа прав
     */
    void registerGroup(PermissionGroup group);
    
    /**
     * Добавляет право для игрока
     * @param player игрок
     * @param permission право
     */
    void addPermission(Player player, String permission);
    
    /**
     * Удаляет право у игрока
     * @param player игрок
     * @param permission право
     */
    void removePermission(Player player, String permission);
    
    /**
     * Добавляет игрока в группу
     * @param player игрок
     * @param groupName название группы
     */
    void addToGroup(Player player, String groupName);
    
    /**
     * Удаляет игрока из группы
     * @param player игрок
     * @param groupName название группы
     */
    void removeFromGroup(Player player, String groupName);
    
    /**
     * Возвращает билдер для создания групп прав
     * @return билдер групп
     */
    PermissionBuilder permissions();
    
    /**
     * Сохраняет все права в конфигурацию
     */
    void savePermissions();
    
    /**
     * Загружает права из конфигурации
     */
    void loadPermissions();
    
    /**
     * Проверяет наличие группы
     * @param groupName название группы
     * @return true, если группа существует
     */
    boolean groupExists(String groupName);
    
    /**
     * Получает группу по названию
     * @param groupName название группы
     * @return группа или null, если не найдена
     */
    PermissionGroup getGroup(String groupName);
} 