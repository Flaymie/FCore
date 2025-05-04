package dev.flaymie.fcore.core.data.user;

import dev.flaymie.fcore.FCore;
import dev.flaymie.fcore.api.service.FCoreService;
import dev.flaymie.fcore.core.data.cache.CacheManager;
import dev.flaymie.fcore.core.data.orm.Database;
import dev.flaymie.fcore.core.data.orm.UserData;
import org.bukkit.entity.Player;

import java.util.Date;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Менеджер для работы с данными пользователей
 */
public class UserManager implements FCoreService {
    
    private final FCore plugin;
    private final Logger logger;
    private final Database database;
    private final CacheManager cacheManager;
    
    public UserManager(FCore plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.database = plugin.getDataManager().getDatabase();
        this.cacheManager = plugin.getDataManager().getCacheManager();
    }
    
    @Override
    public void onEnable() {
        logger.info("Инициализация менеджера пользователей...");
    }
    
    @Override
    public void onDisable() {
        logger.info("Отключение менеджера пользователей...");
        
        // Сохраняем все данные при отключении
        saveAll();
    }
    
    @Override
    public String getName() {
        return "UserManager";
    }
    
    /**
     * Загружает данные пользователя по UUID
     * @param uuid UUID игрока
     * @return данные пользователя или null
     */
    public UserData loadUser(UUID uuid) {
        // Проверяем кэш
        UserData userData = (UserData) cacheManager.get("users", uuid.toString());
        if (userData != null) {
            return userData;
        }
        
        // Загружаем из базы данных
        userData = database.findAll(UserData.class, "uuid = ?", uuid.toString())
                .stream()
                .findFirst()
                .orElse(null);
        
        if (userData != null) {
            // Добавляем в кэш
            cacheManager.put("users", uuid.toString(), userData);
        }
        
        return userData;
    }
    
    /**
     * Загружает данные пользователя по имени
     * @param username имя игрока
     * @return данные пользователя или null
     */
    public UserData loadUserByName(String username) {
        return database.findAll(UserData.class, "username = ?", username)
                .stream()
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Загружает или создает данные пользователя
     * @param player игрок
     * @return данные пользователя
     */
    public UserData getOrCreateUser(Player player) {
        UUID uuid = player.getUniqueId();
        UserData userData = loadUser(uuid);
        
        if (userData == null) {
            // Создаем нового пользователя
            userData = new UserData(uuid, player.getName());
            database.save(userData);
            
            // Добавляем в кэш
            cacheManager.put("users", uuid.toString(), userData);
            
            logger.info("Создан новый пользователь: " + player.getName());
        } else {
            // Обновляем последний вход и имя (на случай если оно изменилось)
            userData.setLastLogin(new Date());
            userData.setUsername(player.getName());
            database.save(userData);
        }
        
        return userData;
    }
    
    /**
     * Сохраняет данные пользователя
     * @param userData данные пользователя
     * @return true, если сохранение успешно
     */
    public boolean saveUser(UserData userData) {
        boolean success = database.save(userData);
        
        if (success) {
            // Обновляем кэш
            cacheManager.put("users", userData.getUuid(), userData);
        }
        
        return success;
    }
    
    /**
     * Удаляет данные пользователя
     * @param uuid UUID игрока
     * @return true, если удаление успешно
     */
    public boolean deleteUser(UUID uuid) {
        UserData userData = loadUser(uuid);
        
        if (userData != null) {
            boolean success = database.delete(userData);
            
            if (success) {
                // Удаляем из кэша
                cacheManager.remove("users", uuid.toString());
            }
            
            return success;
        }
        
        return false;
    }
    
    /**
     * Сохраняет все данные пользователей из кэша
     */
    public void saveAll() {
        // В будущем здесь может быть более эффективная реализация для массового сохранения
        logger.info("Сохранение всех данных пользователей...");
    }
} 