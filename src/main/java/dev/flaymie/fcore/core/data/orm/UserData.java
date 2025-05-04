package dev.flaymie.fcore.core.data.orm;

import java.util.Date;
import java.util.UUID;

/**
 * Пример сущности для базы данных с использованием ORM
 */
@Entity("fcore_users")
public class UserData {
    
    @Id(autoIncrement = true)
    @Column("id")
    private int id;
    
    @Column("uuid")
    private String uuid;
    
    @Column("username")
    private String username;
    
    @Column("last_login")
    private Date lastLogin;
    
    @Column("balance")
    private double balance;
    
    @Column("status")
    private String status;
    
    @Column("created_at")
    private Date createdAt;
    
    // Конструктор по умолчанию (необходим для ORM)
    public UserData() {
    }
    
    // Конструктор для создания нового пользователя
    public UserData(UUID uuid, String username) {
        this.uuid = uuid.toString();
        this.username = username;
        this.lastLogin = new Date();
        this.balance = 0.0;
        this.status = "active";
        this.createdAt = new Date();
    }
    
    // Геттеры и сеттеры
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getUuid() {
        return uuid;
    }
    
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
    
    public UUID getUuidAsUUID() {
        return UUID.fromString(uuid);
    }
    
    public void setUuidAsUUID(UUID uuid) {
        this.uuid = uuid.toString();
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public Date getLastLogin() {
        return lastLogin;
    }
    
    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }
    
    public double getBalance() {
        return balance;
    }
    
    public void setBalance(double balance) {
        this.balance = balance;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public String toString() {
        return "UserData{" +
                "id=" + id +
                ", uuid='" + uuid + '\'' +
                ", username='" + username + '\'' +
                ", lastLogin=" + lastLogin +
                ", balance=" + balance +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
} 