package dev.flaymie.fcore.core.data.orm;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.flaymie.fcore.FCore;
import dev.flaymie.fcore.api.service.FCoreService;
import dev.flaymie.fcore.core.data.config.ConfigManager;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Менеджер соединений с базой данных
 */
public class ConnectionManager implements FCoreService {
    
    private final FCore plugin;
    private final Logger logger;
    private final ConfigManager configManager;
    private DatabaseConfig config;
    private HikariDataSource dataSource;
    private boolean useMysql;
    
    public ConnectionManager(FCore plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.configManager = configManager;
    }
    
    @Override
    public void onEnable() {
        logger.info("Инициализация менеджера соединений с БД...");
        
        // Загружаем конфигурацию базы данных
        config = configManager.load(DatabaseConfig.class);
        
        // Определяем какой тип БД использовать
        useMysql = config.isMysqlEnabled();
        
        // Инициализируем соединение
        initializeDataSource();
        
        logger.info("Используется " + (useMysql ? "MySQL" : "SQLite") + " база данных");
    }
    
    @Override
    public void onDisable() {
        // Закрываем соединения при отключении
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("Соединение с базой данных закрыто");
        }
    }
    
    @Override
    public String getName() {
        return "ConnectionManager";
    }
    
    /**
     * Инициализирует источник данных (пул соединений)
     */
    private void initializeDataSource() {
        HikariConfig hikariConfig = new HikariConfig();
        
        // Настраиваем HikariCP в зависимости от типа базы данных
        if (useMysql) {
            // MySQL настройки
            hikariConfig.setJdbcUrl("jdbc:mysql://" + config.getHost() + ":" + config.getPort() + 
                                   "/" + config.getDatabase() + 
                                   "?useSSL=" + config.isUseSSL() +
                                   "&useUnicode=true&characterEncoding=utf8");
            hikariConfig.setUsername(config.getUsername());
            hikariConfig.setPassword(config.getPassword());
            hikariConfig.setDriverClassName("com.mysql.jdbc.Driver");
        } else {
            // SQLite настройки
            File dbFile = new File(plugin.getDataFolder(), config.getSqliteFile());
            
            // Создаем директорию если она не существует
            if (!dbFile.getParentFile().exists()) {
                dbFile.getParentFile().mkdirs();
            }
            
            hikariConfig.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
            hikariConfig.setDriverClassName("org.sqlite.JDBC");
        }
        
        // Общие настройки HikariCP
        hikariConfig.setPoolName("FCore-HikariPool");
        hikariConfig.setMaximumPoolSize(config.getPoolSize());
        hikariConfig.setConnectionTimeout(config.getConnectionTimeout());
        hikariConfig.setLeakDetectionThreshold(60000);
        
        // Отображение SQL запросов в лог
        if (config.isShowSql()) {
            hikariConfig.setConnectionTestQuery("SELECT 1");
        }
        
        try {
            // Создаем пул соединений
            dataSource = new HikariDataSource(hikariConfig);
            
            // Тестируем соединение
            try (Connection conn = dataSource.getConnection()) {
                logger.info("Соединение с базой данных установлено успешно");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Ошибка при инициализации соединения с базой данных", e);
            throw new RuntimeException("Не удалось установить соединение с базой данных: " + e.getMessage(), e);
        }
    }
    
    /**
     * Возвращает соединение с базой данных из пула
     * @return объект Connection
     * @throws SQLException при ошибке получения соединения
     */
    public Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            throw new SQLException("DataSource не инициализирован или закрыт");
        }
        return dataSource.getConnection();
    }
    
    /**
     * Проверяет используется ли MySQL
     * @return true, если используется MySQL, false для SQLite
     */
    public boolean isUseMysql() {
        return useMysql;
    }
} 