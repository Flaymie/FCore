package dev.flaymie.fcore.integration;

import dev.flaymie.fcore.FCore;
import dev.flaymie.fcore.api.service.FCoreService;
import dev.flaymie.fcore.integration.placeholderapi.PlaceholderAPIManager;
import dev.flaymie.fcore.integration.vault.VaultManager;
import dev.flaymie.fcore.integration.worldguard.WorldGuardManager;

import java.util.logging.Logger;

/**
 * Менеджер интеграций FCore с другими плагинами
 */
public class IntegrationManager implements FCoreService {
    
    private final FCore plugin;
    private final Logger logger;
    
    private PlaceholderAPIManager placeholderAPIManager;
    private VaultManager vaultManager;
    private WorldGuardManager worldGuardManager;
    
    public IntegrationManager(FCore plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }
    
    @Override
    public void onEnable() {
        logger.info("Инициализация интеграций...");
        
        // Инициализируем менеджер PlaceholderAPI
        placeholderAPIManager = new PlaceholderAPIManager(plugin);
        plugin.getServiceManager().registerService(PlaceholderAPIManager.class, placeholderAPIManager);
        
        // Инициализируем менеджер Vault
        vaultManager = new VaultManager(plugin);
        plugin.getServiceManager().registerService(VaultManager.class, vaultManager);
        
        // Инициализируем менеджер WorldGuard
        worldGuardManager = new WorldGuardManager(plugin);
        plugin.getServiceManager().registerService(WorldGuardManager.class, worldGuardManager);
        
        // Включаем все сервисы интеграций
        placeholderAPIManager.onEnable();
        vaultManager.onEnable();
        worldGuardManager.onEnable();
        
        logger.info("Интеграции успешно инициализированы");
    }
    
    @Override
    public void onDisable() {
        logger.info("Отключение интеграций...");
        
        // Отключаем все сервисы интеграций
        if (placeholderAPIManager != null) placeholderAPIManager.onDisable();
        if (vaultManager != null) vaultManager.onDisable();
        if (worldGuardManager != null) worldGuardManager.onDisable();
        
        logger.info("Интеграции успешно отключены");
    }
    
    @Override
    public String getName() {
        return "IntegrationManager";
    }
    
    /**
     * Получает менеджер интеграции с PlaceholderAPI
     * @return менеджер интеграции с PlaceholderAPI
     */
    public PlaceholderAPIManager getPlaceholderAPIManager() {
        return placeholderAPIManager;
    }
    
    /**
     * Получает менеджер интеграции с Vault
     * @return менеджер интеграции с Vault
     */
    public VaultManager getVaultManager() {
        return vaultManager;
    }
    
    /**
     * Получает менеджер интеграции с WorldGuard
     * @return менеджер интеграции с WorldGuard
     */
    public WorldGuardManager getWorldGuardManager() {
        return worldGuardManager;
    }
    
    /**
     * Проверяет, доступна ли интеграция с PlaceholderAPI
     * @return true, если интеграция доступна и включена
     */
    public boolean isPlaceholderAPIAvailable() {
        return placeholderAPIManager != null && placeholderAPIManager.isEnabled();
    }
    
    /**
     * Проверяет, доступна ли интеграция с Vault
     * @return true, если интеграция доступна и включена
     */
    public boolean isVaultAvailable() {
        return vaultManager != null && vaultManager.isEconomyEnabled();
    }
    
    /**
     * Проверяет, доступна ли интеграция с WorldGuard
     * @return true, если интеграция доступна и включена
     */
    public boolean isWorldGuardAvailable() {
        return worldGuardManager != null && worldGuardManager.isEnabled();
    }
} 