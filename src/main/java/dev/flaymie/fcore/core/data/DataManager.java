package dev.flaymie.fcore.core.data;

import dev.flaymie.fcore.FCore;
import dev.flaymie.fcore.api.service.FCoreService;
import dev.flaymie.fcore.core.data.cache.CacheManager;
import dev.flaymie.fcore.core.data.config.ConfigManager;
import dev.flaymie.fcore.core.data.migration.MigrationManager;
import dev.flaymie.fcore.core.data.orm.ConnectionManager;
import dev.flaymie.fcore.core.data.orm.Database;
import dev.flaymie.fcore.core.data.user.UserManager;

import java.util.logging.Logger;

/**
 * Основной менеджер данных, объединяющий все подсистемы работы с данными
 */
public class DataManager implements FCoreService {
    
    private final FCore plugin;
    private final Logger logger;
    
    // Компоненты системы данных
    private ConfigManager configManager;
    private ConnectionManager connectionManager;
    private Database database;
    private CacheManager cacheManager;
    private MigrationManager migrationManager;
    private UserManager userManager;
    
    public DataManager(FCore plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }
    
    @Override
    public void onEnable() {
        logger.info("Инициализация менеджера данных...");
        
        // Инициализируем компоненты в правильном порядке
        initConfigManager();
        initCacheManager();
        initConnectionManager();
        initDatabase();
        initMigrationManager();
        initUserManager();
        
        // Запускаем миграции при запуске
        runMigrations();
        
        logger.info("Менеджер данных успешно инициализирован");
    }
    
    @Override
    public void onDisable() {
        logger.info("Отключение менеджера данных...");
        
        // Отключаем компоненты в обратном порядке
        if (userManager != null) {
            userManager.onDisable();
        }
        
        if (migrationManager != null) {
            migrationManager.onDisable();
        }
        
        if (database != null) {
            database.onDisable();
        }
        
        if (connectionManager != null) {
            connectionManager.onDisable();
        }
        
        if (cacheManager != null) {
            cacheManager.onDisable();
        }
        
        if (configManager != null) {
            configManager.onDisable();
        }
        
        logger.info("Менеджер данных успешно отключен");
    }
    
    @Override
    public String getName() {
        return "DataManager";
    }
    
    /**
     * Инициализация менеджера конфигураций
     */
    private void initConfigManager() {
        configManager = new ConfigManager(plugin);
        configManager.onEnable();
        plugin.getServiceManager().registerService(ConfigManager.class, configManager);
        plugin.getDependencyContainer().registerSingleton(ConfigManager.class, configManager);
    }
    
    /**
     * Инициализация менеджера кэширования
     */
    private void initCacheManager() {
        cacheManager = new CacheManager(plugin);
        cacheManager.onEnable();
        plugin.getServiceManager().registerService(CacheManager.class, cacheManager);
        plugin.getDependencyContainer().registerSingleton(CacheManager.class, cacheManager);
    }
    
    /**
     * Инициализация менеджера соединений с БД
     */
    private void initConnectionManager() {
        connectionManager = new ConnectionManager(plugin, configManager);
        connectionManager.onEnable();
        plugin.getServiceManager().registerService(ConnectionManager.class, connectionManager);
        plugin.getDependencyContainer().registerSingleton(ConnectionManager.class, connectionManager);
    }
    
    /**
     * Инициализация ОРМ
     */
    private void initDatabase() {
        database = new Database(plugin, connectionManager, cacheManager);
        database.onEnable();
        plugin.getServiceManager().registerService(Database.class, database);
        plugin.getDependencyContainer().registerSingleton(Database.class, database);
    }
    
    /**
     * Инициализация менеджера миграций
     */
    private void initMigrationManager() {
        migrationManager = new MigrationManager(plugin, connectionManager);
        migrationManager.onEnable();
        plugin.getServiceManager().registerService(MigrationManager.class, migrationManager);
        plugin.getDependencyContainer().registerSingleton(MigrationManager.class, migrationManager);
    }
    
    /**
     * Инициализация менеджера пользователей
     */
    private void initUserManager() {
        userManager = new UserManager(plugin);
        userManager.onEnable();
        plugin.getServiceManager().registerService(UserManager.class, userManager);
        plugin.getDependencyContainer().registerSingleton(UserManager.class, userManager);
    }
    
    /**
     * Запуск миграций при старте
     */
    private void runMigrations() {
        try {
            int migrationsCount = migrationManager.migrate();
            if (migrationsCount > 0) {
                logger.info("Выполнено " + migrationsCount + " миграций базы данных");
            }
        } catch (Exception e) {
            logger.severe("Ошибка при выполнении миграций: " + e.getMessage());
        }
    }
    
    /**
     * Получение менеджера конфигураций
     * @return менеджер конфигураций
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    /**
     * Получение менеджера соединений с БД
     * @return менеджер соединений
     */
    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }
    
    /**
     * Получение менеджера баз данных (ORM)
     * @return менеджер БД
     */
    public Database getDatabase() {
        return database;
    }
    
    /**
     * Получение менеджера кэширования
     * @return менеджер кэширования
     */
    public CacheManager getCacheManager() {
        return cacheManager;
    }
    
    /**
     * Получение менеджера миграций
     * @return менеджер миграций
     */
    public MigrationManager getMigrationManager() {
        return migrationManager;
    }
    
    /**
     * Получение менеджера пользователей
     * @return менеджер пользователей
     */
    public UserManager getUserManager() {
        return userManager;
    }
}