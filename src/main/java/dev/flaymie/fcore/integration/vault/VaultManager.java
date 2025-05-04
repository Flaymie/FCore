package dev.flaymie.fcore.integration.vault;

import dev.flaymie.fcore.FCore;
import dev.flaymie.fcore.api.service.FCoreService;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;

import java.util.logging.Logger;

/**
 * Менеджер интеграции с Vault
 */
public class VaultManager implements FCoreService {
    
    private final FCore plugin;
    private final Logger logger;
    private boolean economyEnabled = false;
    private FCoreEconomy economy;
    
    public VaultManager(FCore plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }
    
    @Override
    public void onEnable() {
        if (!isVaultInstalled()) {
            logger.info("Vault не обнаружен, интеграция отключена");
            return;
        }
        
        // Регистрируем провайдер экономики
        setupEconomy();
    }
    
    @Override
    public void onDisable() {
        if (economyEnabled && economy != null) {
            economy.disable();
            economyEnabled = false;
            logger.info("Интеграция с Vault отключена");
        }
    }
    
    @Override
    public String getName() {
        return "VaultManager";
    }
    
    /**
     * Проверяет, установлен ли Vault
     * @return true, если Vault установлен
     */
    public boolean isVaultInstalled() {
        return Bukkit.getPluginManager().getPlugin("Vault") != null;
    }
    
    /**
     * Настраивает интеграцию с экономикой Vault
     * @return true, если интеграция успешно настроена
     */
    private boolean setupEconomy() {
        if (!isVaultInstalled()) {
            return false;
        }
        
        // Создаем и регистрируем провайдер экономики
        economy = new FCoreEconomy(plugin);
        
        // Регистрируем сервис экономики в Bukkit
        Bukkit.getServicesManager().register(Economy.class, economy, plugin, ServicePriority.High);
        
        // Включаем экономику
        economy.enable();
        economyEnabled = true;
        
        logger.info("Экономика FCore успешно зарегистрирована в Vault");
        return true;
    }
    
    /**
     * Возвращает провайдер экономики FCore
     * @return экономика FCore или null, если интеграция отключена
     */
    public FCoreEconomy getEconomy() {
        return economyEnabled ? economy : null;
    }
    
    /**
     * Проверяет, включена ли интеграция с экономикой Vault
     * @return true, если интеграция включена
     */
    public boolean isEconomyEnabled() {
        return economyEnabled;
    }
    
    /**
     * Получает провайдер прав из Vault
     * @return провайдер прав или null, если не найден
     */
    public Permission getPermissionProvider() {
        if (!isVaultInstalled()) {
            return null;
        }
        
        RegisteredServiceProvider<Permission> rsp = Bukkit.getServicesManager().getRegistration(Permission.class);
        return rsp != null ? rsp.getProvider() : null;
    }
} 