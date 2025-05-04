package dev.flaymie.fcore.core.permission;

import dev.flaymie.fcore.FCore;
import dev.flaymie.fcore.api.permission.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.logging.Logger;

/**
 * Реализация менеджера прав
 */
public class PermissionManagerImpl implements PermissionManager {
    
    private final FCore plugin;
    private final Logger logger;
    private final Map<String, PermissionGroup> groups;
    private final Map<UUID, UserPermissionHolder> userPermissions;
    private final File permissionsFile;
    private FileConfiguration permissionsConfig;
    
    /**
     * Создает менеджер прав
     * @param plugin экземпляр плагина
     */
    public PermissionManagerImpl(FCore plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.groups = new ConcurrentHashMap<>();
        this.userPermissions = new ConcurrentHashMap<>();
        this.permissionsFile = new File(plugin.getDataFolder(), "permissions.yml");
    }
    
    @Override
    public void onEnable() {
        // Загружаем конфигурацию с правами
        loadPermissions();
        logger.info("Менеджер прав инициализирован");
    }
    
    @Override
    public void onDisable() {
        // Сохраняем права в конфигурацию
        savePermissions();
        
        // Очищаем коллекции
        groups.clear();
        userPermissions.clear();
        
        logger.info("Менеджер прав отключен");
    }
    
    @Override
    public String getName() {
        return "PermissionManager";
    }
    
    @Override
    public boolean hasPermission(Player player, String permission) {
        // Если у игрока есть права оператора, разрешаем все
        if (player.isOp()) {
            return true;
        }
        
        // Получаем хранилище прав игрока
        UserPermissionHolder holder = getUserPermissions(player);
        
        // Проверяем прямые права игрока
        if (holder.hasPermission(permission)) {
            return true;
        }
        
        // Проверяем права групп игрока
        for (String groupName : holder.getGroups()) {
            PermissionGroup group = groups.get(groupName);
            if (group != null && hasGroupPermission(group, permission, new HashSet<>())) {
                return true;
            }
        }
        
        // Передаем проверку в базовую систему прав Bukkit
        return player.hasPermission(permission);
    }
    
    @Override
    public boolean hasPermission(Player player, String permission, PermissionContext context) {
        // Если у игрока есть права оператора, разрешаем все
        if (player.isOp()) {
            return true;
        }
        
        // Получаем хранилище прав игрока
        UserPermissionHolder holder = getUserPermissions(player);
        
        // Проверяем прямые права игрока с учетом контекста
        if (holder.hasPermission(permission, context)) {
            return true;
        }
        
        // Проверяем права групп игрока с учетом контекста
        for (String groupName : holder.getGroups()) {
            PermissionGroup group = groups.get(groupName);
            if (group != null && hasGroupPermission(group, permission, context, new HashSet<>())) {
                return true;
            }
        }
        
        // Если контекстные права не найдены, проверяем обычные права
        return hasPermission(player, permission);
    }
    
    @Override
    public void registerGroup(PermissionGroup group) {
        groups.put(group.getName(), group);
        logger.info("Зарегистрирована группа прав: " + group.getName());
    }
    
    @Override
    public void addPermission(Player player, String permission) {
        UserPermissionHolder holder = getUserPermissions(player);
        holder.addPermission(permission, true);
    }
    
    @Override
    public void removePermission(Player player, String permission) {
        UserPermissionHolder holder = getUserPermissions(player);
        holder.removePermission(permission);
    }
    
    @Override
    public void addToGroup(Player player, String groupName) {
        if (!groups.containsKey(groupName)) {
            logger.warning("Попытка добавить игрока " + player.getName() + " в несуществующую группу " + groupName);
            return;
        }
        
        UserPermissionHolder holder = getUserPermissions(player);
        holder.addGroup(groupName);
    }
    
    @Override
    public void removeFromGroup(Player player, String groupName) {
        UserPermissionHolder holder = getUserPermissions(player);
        holder.removeGroup(groupName);
    }
    
    @Override
    public PermissionBuilder permissions() {
        return new PermissionBuilder(this);
    }
    
    @Override
    public void savePermissions() {
        // Если файл не существует, создаем его
        if (!permissionsFile.exists()) {
            try {
                permissionsFile.createNewFile();
            } catch (IOException e) {
                logger.severe("Не удалось создать файл permissions.yml: " + e.getMessage());
                return;
            }
        }
        
        permissionsConfig = YamlConfiguration.loadConfiguration(permissionsFile);
        
        // Сохраняем группы
        ConfigurationSection groupsSection = permissionsConfig.createSection("groups");
        
        for (PermissionGroup group : groups.values()) {
            ConfigurationSection groupSection = groupsSection.createSection(group.getName());
            
            // Сохраняем права группы
            Map<String, Boolean> permissions = group.getPermissions();
            if (!permissions.isEmpty()) {
                ConfigurationSection permsSection = groupSection.createSection("permissions");
                for (Map.Entry<String, Boolean> entry : permissions.entrySet()) {
                    permsSection.set(entry.getKey(), entry.getValue());
                }
            }
            
            // Сохраняем родительские группы
            Set<String> parents = group.getParents();
            if (!parents.isEmpty()) {
                groupSection.set("parents", new ArrayList<>(parents));
            }
            
            // Сохраняем временные права
            Map<String, Long> temporaryPermissions = group.getTemporaryPermissions();
            if (!temporaryPermissions.isEmpty()) {
                ConfigurationSection tempSection = groupSection.createSection("temporary");
                for (Map.Entry<String, Long> entry : temporaryPermissions.entrySet()) {
                    tempSection.set(entry.getKey(), entry.getValue());
                }
            }
        }
        
        // Сохраняем пользовательские права
        ConfigurationSection usersSection = permissionsConfig.createSection("users");
        
        for (UserPermissionHolder holder : userPermissions.values()) {
            ConfigurationSection userSection = usersSection.createSection(holder.getPlayerId().toString());
            
            userSection.set("name", holder.getPlayerName());
            
            // Сохраняем права пользователя
            Map<String, Boolean> permissions = holder.getPermissions();
            if (!permissions.isEmpty()) {
                ConfigurationSection permsSection = userSection.createSection("permissions");
                for (Map.Entry<String, Boolean> entry : permissions.entrySet()) {
                    permsSection.set(entry.getKey(), entry.getValue());
                }
            }
            
            // Сохраняем группы пользователя
            Set<String> userGroups = holder.getGroups();
            if (!userGroups.isEmpty()) {
                userSection.set("groups", new ArrayList<>(userGroups));
            }
            
            // Временные права сохраняются аналогично
        }
        
        try {
            permissionsConfig.save(permissionsFile);
            logger.info("Права сохранены в " + permissionsFile.getName());
        } catch (IOException e) {
            logger.severe("Не удалось сохранить права: " + e.getMessage());
        }
    }
    
    @Override
    public void loadPermissions() {
        // Очищаем текущие данные
        groups.clear();
        userPermissions.clear();
        
        // Если файл не существует, создаем пустую конфигурацию
        if (!permissionsFile.exists()) {
            permissionsConfig = new YamlConfiguration();
            // Создаем секции по умолчанию
            permissionsConfig.createSection("groups");
            permissionsConfig.createSection("users");
            return;
        }
        
        // Загружаем конфигурацию
        permissionsConfig = YamlConfiguration.loadConfiguration(permissionsFile);
        
        // Загружаем группы
        ConfigurationSection groupsSection = permissionsConfig.getConfigurationSection("groups");
        if (groupsSection != null) {
            for (String groupName : groupsSection.getKeys(false)) {
                ConfigurationSection groupSection = groupsSection.getConfigurationSection(groupName);
                if (groupSection == null) continue;
                
                PermissionGroup group = new PermissionGroup(groupName);
                
                // Загружаем права группы
                ConfigurationSection permsSection = groupSection.getConfigurationSection("permissions");
                if (permsSection != null) {
                    for (String permission : permsSection.getKeys(false)) {
                        boolean value = permsSection.getBoolean(permission);
                        group.addPermission(permission, value);
                    }
                }
                
                // Загружаем родительские группы
                List<String> parents = groupSection.getStringList("parents");
                for (String parent : parents) {
                    group.addParent(parent);
                }
                
                // Загружаем временные права
                ConfigurationSection tempSection = groupSection.getConfigurationSection("temporary");
                if (tempSection != null) {
                    for (String permission : tempSection.getKeys(false)) {
                        long expireTime = tempSection.getLong(permission);
                        if (expireTime > System.currentTimeMillis()) {
                            group.addTemporaryPermission(permission, true, expireTime);
                        }
                    }
                }
                
                // Регистрируем группу
                groups.put(groupName, group);
            }
        }
        
        logger.info("Загружено " + groups.size() + " групп прав");
    }
    
    @Override
    public boolean groupExists(String groupName) {
        return groups.containsKey(groupName);
    }
    
    @Override
    public PermissionGroup getGroup(String groupName) {
        return groups.get(groupName);
    }
    
    /**
     * Получает или создает хранилище прав для игрока
     * @param player игрок
     * @return хранилище прав
     */
    private UserPermissionHolder getUserPermissions(Player player) {
        return userPermissions.computeIfAbsent(player.getUniqueId(), id -> new UserPermissionHolder(player));
    }
    
    /**
     * Проверяет наличие права в группе с учетом наследования
     * @param group группа прав
     * @param permission право
     * @param checked список проверенных групп для избежания циклов
     * @return true, если право найдено
     */
    private boolean hasGroupPermission(PermissionGroup group, String permission, Set<String> checked) {
        // Защита от циклических зависимостей
        if (checked.contains(group.getName())) {
            return false;
        }
        
        checked.add(group.getName());
        
        // Проверяем право напрямую в группе
        if (group.hasPermission(permission)) {
            return true;
        }
        
        // Проверяем родительские группы
        for (String parentName : group.getParents()) {
            PermissionGroup parent = groups.get(parentName);
            if (parent != null && hasGroupPermission(parent, permission, checked)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Проверяет наличие права в группе с учетом наследования и контекста
     * @param group группа прав
     * @param permission право
     * @param context контекст
     * @param checked список проверенных групп для избежания циклов
     * @return true, если право найдено
     */
    private boolean hasGroupPermission(PermissionGroup group, String permission, PermissionContext context, Set<String> checked) {
        // Защита от циклических зависимостей
        if (checked.contains(group.getName())) {
            return false;
        }
        
        checked.add(group.getName());
        
        // Проверяем право напрямую в группе с учетом контекста
        if (group.hasPermission(permission, context)) {
            return true;
        }
        
        // Проверяем родительские группы
        for (String parentName : group.getParents()) {
            PermissionGroup parent = groups.get(parentName);
            if (parent != null && hasGroupPermission(parent, permission, context, checked)) {
                return true;
            }
        }
        
        return false;
    }
} 