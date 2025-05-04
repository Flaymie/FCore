package dev.flaymie.fcore.core.data.migration;

import dev.flaymie.fcore.core.data.orm.ConnectionManager;

import java.util.logging.Logger;

/**
 * Миграция для создания таблицы пользователей
 */
public class CreateUsersTableMigration extends AbstractMigration {
    
    public CreateUsersTableMigration(ConnectionManager connectionManager, Logger logger) {
        super(connectionManager, logger);
    }
    
    @Override
    public int getVersion() {
        return 20230101; // Формат: YYYYMMDD
    }
    
    @Override
    public String getName() {
        return "create_users_table";
    }
    
    @Override
    public boolean up() {
        // Проверка на существование таблицы
        if (tableExists("fcore_users")) {
            logger.info("Таблица fcore_users уже существует, пропускаем миграцию");
            return true;
        }
        
        // SQL для создания таблицы (с учетом типа БД)
        String createTableSQL = 
            "CREATE TABLE fcore_users (" +
            "id INTEGER " + (connectionManager.isUseMysql() ? "AUTO_INCREMENT" : "PRIMARY KEY AUTOINCREMENT") + ", " +
            "uuid VARCHAR(36) NOT NULL, " +
            "username VARCHAR(16) NOT NULL, " +
            "last_login TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "balance DOUBLE DEFAULT 0, " +
            "status VARCHAR(20) DEFAULT 'active', " +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            (connectionManager.isUseMysql() ? ", PRIMARY KEY (id), UNIQUE INDEX idx_uuid (uuid)" : ", UNIQUE (uuid)") +
            ")" + (connectionManager.isUseMysql() ? " ENGINE=InnoDB DEFAULT CHARSET=utf8mb4" : "");
        
        // Выполняем запрос
        boolean success = executeQuery(createTableSQL);
        
        // Создаем индекс для username (отдельно для SQLite)
        if (success && !connectionManager.isUseMysql()) {
            success = executeQuery("CREATE INDEX idx_username ON fcore_users (username)");
        }
        
        return success;
    }
    
    @Override
    public boolean down() {
        // Удаляем таблицу
        return executeQuery("DROP TABLE IF EXISTS fcore_users");
    }
} 