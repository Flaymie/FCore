package dev.flaymie.fcore.core.data.orm;

import dev.flaymie.fcore.core.data.config.ConfigFile;
import dev.flaymie.fcore.core.data.config.ConfigValue;

/**
 * Конфигурация базы данных
 */
@ConfigFile("database.yml")
public class DatabaseConfig {
    
    @ConfigValue("mysql.enabled")
    private boolean mysqlEnabled = false;
    
    @ConfigValue("mysql.host")
    private String host = "localhost";
    
    @ConfigValue("mysql.port")
    private int port = 3306;
    
    @ConfigValue("mysql.database")
    private String database = "minecraft";
    
    @ConfigValue("mysql.username")
    private String username = "root";
    
    @ConfigValue("mysql.password")
    private String password = "";
    
    @ConfigValue("mysql.pool-size")
    private int poolSize = 10;
    
    @ConfigValue("mysql.connection-timeout")
    private int connectionTimeout = 30000;
    
    @ConfigValue("mysql.use-ssl")
    private boolean useSSL = false;
    
    @ConfigValue("sqlite.enabled")
    private boolean sqliteEnabled = true;
    
    @ConfigValue("sqlite.file")
    private String sqliteFile = "database.db";
    
    @ConfigValue("general.show-sql")
    private boolean showSql = false;
    
    // Геттеры и сеттеры
    
    public boolean isMysqlEnabled() {
        return mysqlEnabled;
    }
    
    public void setMysqlEnabled(boolean mysqlEnabled) {
        this.mysqlEnabled = mysqlEnabled;
    }
    
    public String getHost() {
        return host;
    }
    
    public void setHost(String host) {
        this.host = host;
    }
    
    public int getPort() {
        return port;
    }
    
    public void setPort(int port) {
        this.port = port;
    }
    
    public String getDatabase() {
        return database;
    }
    
    public void setDatabase(String database) {
        this.database = database;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public int getPoolSize() {
        return poolSize;
    }
    
    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }
    
    public int getConnectionTimeout() {
        return connectionTimeout;
    }
    
    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }
    
    public boolean isUseSSL() {
        return useSSL;
    }
    
    public void setUseSSL(boolean useSSL) {
        this.useSSL = useSSL;
    }
    
    public boolean isSqliteEnabled() {
        return sqliteEnabled;
    }
    
    public void setSqliteEnabled(boolean sqliteEnabled) {
        this.sqliteEnabled = sqliteEnabled;
    }
    
    public String getSqliteFile() {
        return sqliteFile;
    }
    
    public void setSqliteFile(String sqliteFile) {
        this.sqliteFile = sqliteFile;
    }
    
    public boolean isShowSql() {
        return showSql;
    }
    
    public void setShowSql(boolean showSql) {
        this.showSql = showSql;
    }
} 