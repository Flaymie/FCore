package dev.flaymie.fcore.core.data.migration;

import dev.flaymie.fcore.core.data.orm.ConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Абстрактный класс для миграций базы данных
 */
public abstract class AbstractMigration implements Migration {
    
    protected final ConnectionManager connectionManager;
    protected final Logger logger;
    
    public AbstractMigration(ConnectionManager connectionManager, Logger logger) {
        this.connectionManager = connectionManager;
        this.logger = logger;
    }
    
    /**
     * Выполнение SQL запроса
     * @param sql SQL запрос
     * @return true, если запрос выполнен успешно
     */
    protected boolean executeQuery(String sql) {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Ошибка при выполнении SQL запроса: " + sql, e);
            return false;
        }
    }
    
    /**
     * Выполнение SQL запроса с параметрами
     * @param sql SQL запрос
     * @param params параметры запроса
     * @return true, если запрос выполнен успешно
     */
    protected boolean executeQuery(String sql, Object... params) {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            // Устанавливаем параметры
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Ошибка при выполнении SQL запроса с параметрами: " + sql, e);
            return false;
        }
    }
    
    /**
     * Проверка существования таблицы в базе данных
     * @param tableName имя таблицы
     * @return true, если таблица существует
     */
    protected boolean tableExists(String tableName) {
        try (Connection connection = connectionManager.getConnection()) {
            return connection.getMetaData()
                    .getTables(null, null, tableName, null)
                    .next();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Ошибка при проверке существования таблицы: " + tableName, e);
            return false;
        }
    }
} 