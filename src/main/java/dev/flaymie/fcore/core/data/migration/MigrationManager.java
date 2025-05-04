package dev.flaymie.fcore.core.data.migration;

import dev.flaymie.fcore.FCore;
import dev.flaymie.fcore.api.service.FCoreService;
import dev.flaymie.fcore.core.data.orm.ConnectionManager;
import dev.flaymie.fcore.core.data.orm.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Менеджер миграций базы данных
 */
public class MigrationManager implements FCoreService {
    
    private final FCore plugin;
    private final Logger logger;
    private final ConnectionManager connectionManager;
    private final Map<Integer, Migration> migrations;
    
    public MigrationManager(FCore plugin, ConnectionManager connectionManager) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.connectionManager = connectionManager;
        this.migrations = new HashMap<>();
    }
    
    @Override
    public void onEnable() {
        logger.info("Инициализация менеджера миграций...");
        
        // Создаем таблицу миграций, если ее нет
        createMigrationsTable();
        
        // Регистрируем все миграции
        registerMigrations();
    }
    
    @Override
    public void onDisable() {
        logger.info("Отключение менеджера миграций...");
    }
    
    @Override
    public String getName() {
        return "MigrationManager";
    }
    
    /**
     * Создание таблицы для отслеживания миграций
     */
    private void createMigrationsTable() {
        try (Connection connection = connectionManager.getConnection()) {
            String createTableSql = 
                "CREATE TABLE IF NOT EXISTS fcore_migrations (" +
                "id INTEGER " + (connectionManager.isUseMysql() ? "AUTO_INCREMENT" : "PRIMARY KEY AUTOINCREMENT") + ", " +
                "version INTEGER NOT NULL, " +
                "name VARCHAR(100) NOT NULL, " +
                "batch INTEGER NOT NULL, " +
                "executed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                (connectionManager.isUseMysql() ? ", PRIMARY KEY (id)" : "") +
                ")" + (connectionManager.isUseMysql() ? " ENGINE=InnoDB DEFAULT CHARSET=utf8mb4" : "");
            
            try (PreparedStatement stmt = connection.prepareStatement(createTableSql)) {
                stmt.executeUpdate();
                logger.info("Таблица миграций проверена/создана");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Ошибка при создании таблицы миграций", e);
        }
    }
    
    /**
     * Регистрация миграции
     * @param migration миграция
     */
    public void registerMigration(Migration migration) {
        migrations.put(migration.getVersion(), migration);
    }
    
    /**
     * Регистрация всех миграций
     * Здесь нужно добавить все миграции в порядке возрастания версий
     */
    private void registerMigrations() {
        // Пример регистрации миграций (в проекте будут конкретные классы миграций)
        registerMigration(new CreateUsersTableMigration(connectionManager, logger));
        
        logger.info("Зарегистрировано миграций: " + migrations.size());
    }
    
    /**
     * Выполнение всех новых миграций
     * @return количество выполненных миграций
     */
    public int migrate() {
        try (Connection connection = connectionManager.getConnection()) {
            // Получаем выполненные миграции
            List<Integer> executedVersions = getExecutedMigrations();
            
            // Находим новые миграции
            List<Migration> pendingMigrations = new ArrayList<>();
            for (Migration migration : migrations.values()) {
                if (!executedVersions.contains(migration.getVersion())) {
                    pendingMigrations.add(migration);
                }
            }
            
            // Сортируем миграции по версии
            pendingMigrations.sort((m1, m2) -> Integer.compare(m1.getVersion(), m2.getVersion()));
            
            if (pendingMigrations.isEmpty()) {
                logger.info("Нет новых миграций для выполнения");
                return 0;
            }
            
            logger.info("Найдено " + pendingMigrations.size() + " новых миграций");
            
            // Получаем текущий batch
            int batch = getCurrentBatch(connection) + 1;
            
            // Выполняем каждую миграцию
            int migrationsExecuted = 0;
            for (Migration migration : pendingMigrations) {
                logger.info("Выполнение миграции: " + migration.getVersion() + " - " + migration.getName());
                
                try {
                    // Выполняем миграцию
                    boolean success = migration.up();
                    
                    if (success) {
                        // Записываем информацию о выполненной миграции
                        saveMigration(connection, migration, batch);
                        migrationsExecuted++;
                        logger.info("Миграция выполнена успешно");
                    } else {
                        logger.warning("Миграция не была выполнена");
                    }
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Ошибка при выполнении миграции: " + migration.getName(), e);
                    // При ошибке останавливаем процесс
                    break;
                }
            }
            
            return migrationsExecuted;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Ошибка при выполнении миграций", e);
            return 0;
        }
    }
    
    /**
     * Откат последнего пакета миграций
     * @return количество откаченных миграций
     */
    public int rollback() {
        try (Connection connection = connectionManager.getConnection()) {
            // Получаем последний batch
            int lastBatch = getCurrentBatch(connection);
            
            if (lastBatch <= 0) {
                logger.info("Нет миграций для отката");
                return 0;
            }
            
            // Получаем миграции для отката
            List<Migration> migrationsToRollback = getMigrationsForBatch(connection, lastBatch);
            
            if (migrationsToRollback.isEmpty()) {
                logger.info("Нет миграций для отката");
                return 0;
            }
            
            logger.info("Откат " + migrationsToRollback.size() + " миграций из batch " + lastBatch);
            
            // Откатываем каждую миграцию в обратном порядке
            int migrationsRolledBack = 0;
            for (int i = migrationsToRollback.size() - 1; i >= 0; i--) {
                Migration migration = migrationsToRollback.get(i);
                logger.info("Откат миграции: " + migration.getVersion() + " - " + migration.getName());
                
                try {
                    // Откатываем миграцию
                    boolean success = migration.down();
                    
                    if (success) {
                        // Удаляем информацию о миграции
                        deleteMigration(connection, migration.getVersion());
                        migrationsRolledBack++;
                        logger.info("Миграция откачена успешно");
                    } else {
                        logger.warning("Миграция не была откачена");
                    }
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Ошибка при откате миграции: " + migration.getName(), e);
                    // При ошибке останавливаем процесс
                    break;
                }
            }
            
            return migrationsRolledBack;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Ошибка при откате миграций", e);
            return 0;
        }
    }
    
    /**
     * Получение списка выполненных миграций
     * @return список версий выполненных миграций
     */
    private List<Integer> getExecutedMigrations() throws SQLException {
        List<Integer> versions = new ArrayList<>();
        
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement("SELECT version FROM fcore_migrations");
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                versions.add(rs.getInt("version"));
            }
        }
        
        return versions;
    }
    
    /**
     * Получение текущего batch
     * @param connection соединение с базой данных
     * @return номер последнего batch или 0
     */
    private int getCurrentBatch(Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT MAX(batch) as batch FROM fcore_migrations");
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt("batch");
            }
        }
        
        return 0;
    }
    
    /**
     * Получение миграций для указанного batch
     * @param connection соединение с базой данных
     * @param batch номер batch
     * @return список миграций
     */
    private List<Migration> getMigrationsForBatch(Connection connection, int batch) throws SQLException {
        List<Migration> result = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement("SELECT version FROM fcore_migrations WHERE batch = ?")) {
            stmt.setInt(1, batch);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int version = rs.getInt("version");
                    Migration migration = migrations.get(version);
                    
                    if (migration != null) {
                        result.add(migration);
                    }
                }
            }
        }
        
        return result;
    }
    
    /**
     * Сохранение информации о выполненной миграции
     * @param connection соединение с базой данных
     * @param migration миграция
     * @param batch номер batch
     */
    private void saveMigration(Connection connection, Migration migration, int batch) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO fcore_migrations (version, name, batch) VALUES (?, ?, ?)")) {
            
            stmt.setInt(1, migration.getVersion());
            stmt.setString(2, migration.getName());
            stmt.setInt(3, batch);
            
            stmt.executeUpdate();
        }
    }
    
    /**
     * Удаление информации о миграции
     * @param connection соединение с базой данных
     * @param version версия миграции
     */
    private void deleteMigration(Connection connection, int version) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
                "DELETE FROM fcore_migrations WHERE version = ?")) {
            
            stmt.setInt(1, version);
            stmt.executeUpdate();
        }
    }
} 