package dev.flaymie.fcore.core.data.orm;

import dev.flaymie.fcore.FCore;
import dev.flaymie.fcore.api.service.FCoreService;
import dev.flaymie.fcore.core.data.cache.CacheManager;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Класс для работы с базой данных (простой ORM)
 */
public class Database implements FCoreService {
    
    private final FCore plugin;
    private final Logger logger;
    private final ConnectionManager connectionManager;
    private final CacheManager cacheManager;
    private final Map<Class<?>, EntityInfo> entityInfoCache = new HashMap<>();
    
    public Database(FCore plugin, ConnectionManager connectionManager, CacheManager cacheManager) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.connectionManager = connectionManager;
        this.cacheManager = cacheManager;
    }
    
    @Override
    public void onEnable() {
        logger.info("Инициализация базы данных...");
    }
    
    @Override
    public void onDisable() {
        logger.info("Отключение базы данных...");
    }
    
    @Override
    public String getName() {
        return "Database";
    }
    
    /**
     * Сохраняет объект в базу данных
     * @param entity объект для сохранения
     * @param <T> тип объекта
     * @return true, если сохранение успешно
     */
    public <T> boolean save(T entity) {
        try {
            // Получаем информацию о сущности
            EntityInfo entityInfo = getEntityInfo(entity.getClass());
            String tableName = entityInfo.getTableName();
            
            // Проверяем существование таблицы
            ensureTableExists(entityInfo);
            
            // Получаем значение первичного ключа
            Field idField = entityInfo.getIdField();
            idField.setAccessible(true);
            Object idValue = idField.get(entity);
            
            // Определяем операцию - вставка или обновление
            boolean isInsert = idValue == null || 
                              (idValue instanceof Number && ((Number) idValue).longValue() == 0) ||
                              (idValue instanceof String && ((String) idValue).isEmpty());
            
            Connection connection = null;
            PreparedStatement statement = null;
            
            try {
                connection = connectionManager.getConnection();
                
                if (isInsert) {
                    // Выполняем вставку
                    String sql = generateInsertSQL(entityInfo);
                    statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                    
                    // Устанавливаем параметры
                    int paramIndex = 1;
                    for (Field field : entityInfo.getFields()) {
                        if (field != idField || !entityInfo.isAutoIncrement()) {
                            field.setAccessible(true);
                            Object value = field.get(entity);
                            setParameter(statement, paramIndex++, value);
                        }
                    }
                    
                    // Выполняем запрос
                    statement.executeUpdate();
                    
                    // Получаем сгенерированный ID
                    if (entityInfo.isAutoIncrement()) {
                        ResultSet generatedKeys = statement.getGeneratedKeys();
                        if (generatedKeys.next()) {
                            Object generatedId = generatedKeys.getObject(1);
                            idField.set(entity, convertToFieldType(generatedId, idField.getType()));
                        }
                    }
                } else {
                    // Выполняем обновление
                    String sql = generateUpdateSQL(entityInfo);
                    statement = connection.prepareStatement(sql);
                    
                    // Устанавливаем параметры
                    int paramIndex = 1;
                    for (Field field : entityInfo.getFields()) {
                        if (field != idField) {
                            field.setAccessible(true);
                            Object value = field.get(entity);
                            setParameter(statement, paramIndex++, value);
                        }
                    }
                    
                    // Добавляем ID в where
                    setParameter(statement, paramIndex, idValue);
                    
                    // Выполняем запрос
                    statement.executeUpdate();
                }
                
                // Обновляем кэш
                cacheManager.put(entity.getClass().getName() + ":" + idValue, entity);
                
                return true;
            } finally {
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ошибка при сохранении в базу данных", e);
            return false;
        }
    }
    
    /**
     * Загружает объект из базы данных по первичному ключу
     * @param entityClass класс сущности
     * @param id значение первичного ключа
     * @param <T> тип объекта
     * @param <ID> тип первичного ключа
     * @return загруженный объект или null
     */
    public <T, ID> T find(Class<T> entityClass, ID id) {
        // Проверяем кэш
        String cacheKey = entityClass.getName() + ":" + id;
        T cachedEntity = (T) cacheManager.get(cacheKey);
        if (cachedEntity != null) {
            return cachedEntity;
        }
        
        try {
            // Получаем информацию о сущности
            EntityInfo entityInfo = getEntityInfo(entityClass);
            String tableName = entityInfo.getTableName();
            
            // Формируем SQL запрос
            String sql = "SELECT * FROM " + tableName + " WHERE " + 
                        entityInfo.getIdColumn() + " = ?";
            
            Connection connection = null;
            PreparedStatement statement = null;
            ResultSet resultSet = null;
            
            try {
                connection = connectionManager.getConnection();
                statement = connection.prepareStatement(sql);
                
                // Устанавливаем ID как параметр
                setParameter(statement, 1, id);
                
                // Выполняем запрос
                resultSet = statement.executeQuery();
                
                // Преобразуем результат в объект
                if (resultSet.next()) {
                    T entity = mapResultSetToEntity(resultSet, entityClass, entityInfo);
                    
                    // Сохраняем в кэш
                    cacheManager.put(cacheKey, entity);
                    
                    return entity;
                }
                
                return null;
            } finally {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ошибка при загрузке из базы данных", e);
            return null;
        }
    }
    
    /**
     * Удаляет объект из базы данных
     * @param entity объект для удаления
     * @param <T> тип объекта
     * @return true, если удаление успешно
     */
    public <T> boolean delete(T entity) {
        try {
            // Получаем информацию о сущности
            EntityInfo entityInfo = getEntityInfo(entity.getClass());
            String tableName = entityInfo.getTableName();
            
            // Получаем значение первичного ключа
            Field idField = entityInfo.getIdField();
            idField.setAccessible(true);
            Object idValue = idField.get(entity);
            
            if (idValue == null) {
                return false;
            }
            
            // Формируем SQL запрос
            String sql = "DELETE FROM " + tableName + " WHERE " + 
                        entityInfo.getIdColumn() + " = ?";
            
            Connection connection = null;
            PreparedStatement statement = null;
            
            try {
                connection = connectionManager.getConnection();
                statement = connection.prepareStatement(sql);
                
                // Устанавливаем ID как параметр
                setParameter(statement, 1, idValue);
                
                // Выполняем запрос
                int rowsAffected = statement.executeUpdate();
                
                // Удаляем из кэша
                cacheManager.remove(entity.getClass().getName() + ":" + idValue);
                
                return rowsAffected > 0;
            } finally {
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ошибка при удалении из базы данных", e);
            return false;
        }
    }
    
    /**
     * Выполняет произвольный SQL запрос
     * @param sql запрос SQL
     * @param params параметры запроса
     * @return результат запроса или -1 при ошибке
     */
    public int executeUpdate(String sql, Object... params) {
        Connection connection = null;
        PreparedStatement statement = null;
        
        try {
            connection = connectionManager.getConnection();
            statement = connection.prepareStatement(sql);
            
            // Устанавливаем параметры
            for (int i = 0; i < params.length; i++) {
                setParameter(statement, i + 1, params[i]);
            }
            
            // Выполняем запрос
            return statement.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Ошибка при выполнении SQL запроса", e);
            return -1;
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Ошибка при закрытии соединения", e);
            }
        }
    }
    
    /**
     * Загружает список объектов по условию
     * @param entityClass класс сущности
     * @param whereClause условие WHERE (без слова WHERE)
     * @param params параметры для условия
     * @param <T> тип объекта
     * @return список объектов
     */
    public <T> List<T> findAll(Class<T> entityClass, String whereClause, Object... params) {
        try {
            // Получаем информацию о сущности
            EntityInfo entityInfo = getEntityInfo(entityClass);
            String tableName = entityInfo.getTableName();
            
            // Формируем SQL запрос
            String sql = "SELECT * FROM " + tableName;
            if (whereClause != null && !whereClause.isEmpty()) {
                sql += " WHERE " + whereClause;
            }
            
            Connection connection = null;
            PreparedStatement statement = null;
            ResultSet resultSet = null;
            
            try {
                connection = connectionManager.getConnection();
                statement = connection.prepareStatement(sql);
                
                // Устанавливаем параметры
                for (int i = 0; i < params.length; i++) {
                    setParameter(statement, i + 1, params[i]);
                }
                
                // Выполняем запрос
                resultSet = statement.executeQuery();
                
                // Создаем список результатов
                List<T> resultList = new ArrayList<>();
                
                // Преобразуем каждую строку в объект
                while (resultSet.next()) {
                    T entity = mapResultSetToEntity(resultSet, entityClass, entityInfo);
                    
                    // Добавляем в список
                    resultList.add(entity);
                    
                    // Сохраняем в кэш
                    Field idField = entityInfo.getIdField();
                    idField.setAccessible(true);
                    Object idValue = idField.get(entity);
                    cacheManager.put(entityClass.getName() + ":" + idValue, entity);
                }
                
                return resultList;
            } finally {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ошибка при загрузке списка из базы данных", e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Создает таблицу для сущности, если она не существует
     * @param entityInfo информация о сущности
     * @throws SQLException при ошибке создания таблицы
     */
    private void ensureTableExists(EntityInfo entityInfo) throws SQLException {
        String tableName = entityInfo.getTableName();
        
        // Проверяем существует ли таблица
        boolean tableExists = false;
        Connection connection = null;
        
        try {
            connection = connectionManager.getConnection();
            DatabaseMetaData metaData = connection.getMetaData();
            
            try (ResultSet tables = metaData.getTables(null, null, tableName, null)) {
                tableExists = tables.next();
            }
            
            // Если таблица не существует, создаем ее
            if (!tableExists) {
                String createTableSql = generateCreateTableSQL(entityInfo);
                try (Statement statement = connection.createStatement()) {
                    statement.executeUpdate(createTableSql);
                    logger.info("Создана таблица: " + tableName);
                }
            }
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }
    
    /**
     * Генерирует SQL для создания таблицы
     * @param entityInfo информация о сущности
     * @return SQL запрос
     */
    private String generateCreateTableSQL(EntityInfo entityInfo) {
        boolean isMysql = connectionManager.isUseMysql();
        StringBuilder sql = new StringBuilder();
        
        sql.append("CREATE TABLE IF NOT EXISTS ").append(entityInfo.getTableName()).append(" (");
        
        // Добавляем колонки
        List<String> columns = new ArrayList<>();
        
        // Добавляем первичный ключ
        Field idField = entityInfo.getIdField();
        Column idColumn = idField.getAnnotation(Column.class);
        String idColumnName = entityInfo.getIdColumn();
        String idType;
        
        // Определяем тип данных для ID
        if (idColumn != null && !idColumn.type().isEmpty()) {
            idType = idColumn.type();
        } else {
            idType = getSqlTypeForJavaType(idField.getType(), isMysql);
        }
        
        // Добавляем автоинкремент если нужно
        String idDef = idColumnName + " " + idType;
        if (entityInfo.isAutoIncrement()) {
            idDef += isMysql ? " AUTO_INCREMENT" : " PRIMARY KEY AUTOINCREMENT";
        }
        
        columns.add(idDef);
        
        // Добавляем остальные колонки
        for (Field field : entityInfo.getFields()) {
            if (field != idField) {
                Column column = field.getAnnotation(Column.class);
                if (column != null) {
                    String columnName = column.value();
                    String columnType;
                    
                    // Определяем тип данных для колонки
                    if (!column.type().isEmpty()) {
                        columnType = column.type();
                    } else {
                        columnType = getSqlTypeForJavaType(field.getType(), isMysql);
                    }
                    
                    // Добавляем размер для строк
                    if ((columnType.equals("VARCHAR") || columnType.equals("CHAR")) && column.length() > 0) {
                        columnType += "(" + column.length() + ")";
                    }
                    
                    // Добавляем NOT NULL если нужно
                    String columnDef = columnName + " " + columnType;
                    if (column.notNull()) {
                        columnDef += " NOT NULL";
                    }
                    
                    columns.add(columnDef);
                }
            }
        }
        
        // Собираем все колонки
        sql.append(String.join(", ", columns));
        
        // Добавляем первичный ключ если не автоинкремент
        if (!entityInfo.isAutoIncrement()) {
            sql.append(", PRIMARY KEY (").append(idColumnName).append(")");
        }
        
        sql.append(")");
        
        // Добавляем специфичные для MySQL опции
        if (isMysql) {
            sql.append(" ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        }
        
        return sql.toString();
    }
    
    /**
     * Генерирует SQL для вставки
     * @param entityInfo информация о сущности
     * @return SQL запрос
     */
    private String generateInsertSQL(EntityInfo entityInfo) {
        StringBuilder sql = new StringBuilder();
        StringBuilder values = new StringBuilder();
        
        sql.append("INSERT INTO ").append(entityInfo.getTableName()).append(" (");
        
        boolean first = true;
        for (Field field : entityInfo.getFields()) {
            Column column = field.getAnnotation(Column.class);
            Id id = field.getAnnotation(Id.class);
            
            // Пропускаем автоинкрементные ID
            if (id != null && id.autoIncrement()) {
                continue;
            }
            
            if (column != null) {
                if (!first) {
                    sql.append(", ");
                    values.append(", ");
                }
                sql.append(column.value());
                values.append("?");
                first = false;
            }
        }
        
        sql.append(") VALUES (").append(values).append(")");
        
        return sql.toString();
    }
    
    /**
     * Генерирует SQL для обновления
     * @param entityInfo информация о сущности
     * @return SQL запрос
     */
    private String generateUpdateSQL(EntityInfo entityInfo) {
        StringBuilder sql = new StringBuilder();
        
        sql.append("UPDATE ").append(entityInfo.getTableName()).append(" SET ");
        
        boolean first = true;
        for (Field field : entityInfo.getFields()) {
            if (field != entityInfo.getIdField()) {
                Column column = field.getAnnotation(Column.class);
                if (column != null) {
                    if (!first) {
                        sql.append(", ");
                    }
                    sql.append(column.value()).append(" = ?");
                    first = false;
                }
            }
        }
        
        sql.append(" WHERE ").append(entityInfo.getIdColumn()).append(" = ?");
        
        return sql.toString();
    }
    
    /**
     * Преобразует ResultSet в объект сущности
     * @param resultSet результаты запроса
     * @param entityClass класс сущности
     * @param entityInfo информация о сущности
     * @param <T> тип объекта
     * @return созданный объект
     */
    private <T> T mapResultSetToEntity(ResultSet resultSet, Class<T> entityClass, EntityInfo entityInfo) 
            throws SQLException, InstantiationException, IllegalAccessException, NoSuchMethodException, java.lang.reflect.InvocationTargetException {
        
        // Создаем новый экземпляр
        T entity = entityClass.getDeclaredConstructor().newInstance();
        
        // Заполняем поля
        for (Field field : entityInfo.getFields()) {
            field.setAccessible(true);
            
            Column column = field.getAnnotation(Column.class);
            if (column != null) {
                String columnName = column.value();
                Object value = resultSet.getObject(columnName);
                
                // Преобразуем значение в нужный тип
                field.set(entity, convertToFieldType(value, field.getType()));
            }
        }
        
        return entity;
    }
    
    /**
     * Устанавливает параметр в PreparedStatement
     * @param statement запрос
     * @param index индекс параметра (начиная с 1)
     * @param value значение параметра
     * @throws SQLException при ошибке установки параметра
     */
    private void setParameter(PreparedStatement statement, int index, Object value) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.NULL);
        } else if (value instanceof String) {
            statement.setString(index, (String) value);
        } else if (value instanceof Integer) {
            statement.setInt(index, (Integer) value);
        } else if (value instanceof Long) {
            statement.setLong(index, (Long) value);
        } else if (value instanceof Double) {
            statement.setDouble(index, (Double) value);
        } else if (value instanceof Float) {
            statement.setFloat(index, (Float) value);
        } else if (value instanceof Boolean) {
            statement.setBoolean(index, (Boolean) value);
        } else if (value instanceof java.util.Date) {
            statement.setTimestamp(index, new Timestamp(((java.util.Date) value).getTime()));
        } else if (value instanceof byte[]) {
            statement.setBytes(index, (byte[]) value);
        } else if (value instanceof UUID) {
            statement.setString(index, value.toString());
        } else {
            statement.setObject(index, value);
        }
    }
    
    /**
     * Преобразует значение из БД в нужный тип Java
     * @param value значение из БД
     * @param targetType целевой тип Java
     * @return преобразованное значение
     */
    private Object convertToFieldType(Object value, Class<?> targetType) {
        if (value == null) {
            return null;
        }
        
        if (targetType.isInstance(value)) {
            return value;
        }
        
        // Преобразуем примитивные типы
        if (targetType == int.class || targetType == Integer.class) {
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
            return Integer.parseInt(value.toString());
        } else if (targetType == long.class || targetType == Long.class) {
            if (value instanceof Number) {
                return ((Number) value).longValue();
            }
            return Long.parseLong(value.toString());
        } else if (targetType == double.class || targetType == Double.class) {
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }
            return Double.parseDouble(value.toString());
        } else if (targetType == float.class || targetType == Float.class) {
            if (value instanceof Number) {
                return ((Number) value).floatValue();
            }
            return Float.parseFloat(value.toString());
        } else if (targetType == boolean.class || targetType == Boolean.class) {
            if (value instanceof Number) {
                return ((Number) value).intValue() != 0;
            }
            return Boolean.parseBoolean(value.toString());
        } else if (targetType == String.class) {
            return value.toString();
        } else if (targetType == UUID.class && value instanceof String) {
            return UUID.fromString((String) value);
        } else if (targetType == java.util.Date.class && value instanceof Timestamp) {
            return new java.util.Date(((Timestamp) value).getTime());
        }
        
        // Если не удалось конвертировать
        return value;
    }
    
    /**
     * Возвращает SQL тип для Java типа
     * @param javaType тип Java
     * @param isMysql флаг использования MySQL
     * @return SQL тип данных
     */
    private String getSqlTypeForJavaType(Class<?> javaType, boolean isMysql) {
        if (javaType == String.class) {
            return "VARCHAR(255)";
        } else if (javaType == int.class || javaType == Integer.class) {
            return "INT";
        } else if (javaType == long.class || javaType == Long.class) {
            return "BIGINT";
        } else if (javaType == double.class || javaType == Double.class) {
            return "DOUBLE";
        } else if (javaType == float.class || javaType == Float.class) {
            return "FLOAT";
        } else if (javaType == boolean.class || javaType == Boolean.class) {
            return isMysql ? "TINYINT(1)" : "INTEGER";
        } else if (javaType == java.util.Date.class) {
            return "TIMESTAMP";
        } else if (javaType == UUID.class) {
            return "VARCHAR(36)";
        } else if (javaType == byte[].class) {
            return "BLOB";
        }
        
        // По умолчанию используем TEXT
        return "TEXT";
    }
    
    /**
     * Получает информацию о сущности из кэша или создает новую
     * @param entityClass класс сущности
     * @return информация о сущности
     */
    private EntityInfo getEntityInfo(Class<?> entityClass) {
        // Проверяем кэш
        EntityInfo entityInfo = entityInfoCache.get(entityClass);
        if (entityInfo != null) {
            return entityInfo;
        }
        
        // Проверяем наличие аннотации @Entity
        if (!entityClass.isAnnotationPresent(Entity.class)) {
            throw new IllegalArgumentException("Class " + entityClass.getName() + " must be annotated with @Entity");
        }
        
        Entity entityAnnotation = entityClass.getAnnotation(Entity.class);
        String tableName = entityAnnotation.value();
        
        // Ищем первичный ключ
        Field idField = null;
        String idColumn = null;
        boolean autoIncrement = false;
        
        for (Field field : entityClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class)) {
                idField = field;
                
                Id idAnnotation = field.getAnnotation(Id.class);
                autoIncrement = idAnnotation.autoIncrement();
                
                // Получаем имя колонки для ID
                if (field.isAnnotationPresent(Column.class)) {
                    Column column = field.getAnnotation(Column.class);
                    idColumn = column.value();
                } else {
                    idColumn = field.getName();
                }
                
                break;
            }
        }
        
        if (idField == null) {
            throw new IllegalArgumentException("Entity " + entityClass.getName() + " must have a field annotated with @Id");
        }
        
        // Собираем все поля с аннотацией Column
        List<Field> fields = new ArrayList<>();
        for (Field field : entityClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Column.class) || field == idField) {
                fields.add(field);
            }
        }
        
        // Создаем и кэшируем информацию
        entityInfo = new EntityInfo(tableName, idField, idColumn, autoIncrement, fields);
        entityInfoCache.put(entityClass, entityInfo);
        
        return entityInfo;
    }
    
    /**
     * Класс для хранения информации о сущности
     */
    private static class EntityInfo {
        private final String tableName;
        private final Field idField;
        private final String idColumn;
        private final boolean autoIncrement;
        private final List<Field> fields;
        
        public EntityInfo(String tableName, Field idField, String idColumn, boolean autoIncrement, List<Field> fields) {
            this.tableName = tableName;
            this.idField = idField;
            this.idColumn = idColumn;
            this.autoIncrement = autoIncrement;
            this.fields = fields;
        }
        
        public String getTableName() {
            return tableName;
        }
        
        public Field getIdField() {
            return idField;
        }
        
        public String getIdColumn() {
            return idColumn;
        }
        
        public boolean isAutoIncrement() {
            return autoIncrement;
        }
        
        public List<Field> getFields() {
            return fields;
        }
    }
} 