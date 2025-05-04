package dev.flaymie.fcore.integration.worldguard;

import dev.flaymie.fcore.FCore;
import dev.flaymie.fcore.api.service.FCoreService;
import org.bukkit.Bukkit;

import java.util.logging.Logger;

/**
 * Менеджер интеграции с WorldGuard
 */
public class WorldGuardManager implements FCoreService {
    
    private final FCore plugin;
    private final Logger logger;
    private RegionManager regionManager;
    private boolean enabled = false;
    
    public WorldGuardManager(FCore plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }
    
    @Override
    public void onEnable() {
        if (!isWorldGuardInstalled()) {
            logger.info("WorldGuard не обнаружен, интеграция отключена");
            return;
        }
        
        // Создаем менеджер регионов
        regionManager = new RegionManager(plugin);
        
        if (regionManager.isWorldGuardAvailable()) {
            enabled = true;
            logger.info("Интеграция с WorldGuard включена");
        } else {
            logger.warning("Не удалось инициализировать WorldGuard API");
        }
    }
    
    @Override
    public void onDisable() {
        if (enabled) {
            logger.info("Интеграция с WorldGuard отключена");
            enabled = false;
            regionManager = null;
        }
    }
    
    @Override
    public String getName() {
        return "WorldGuardManager";
    }
    
    /**
     * Проверяет, установлен ли WorldGuard
     * @return true, если WorldGuard установлен
     */
    public boolean isWorldGuardInstalled() {
        return Bukkit.getPluginManager().getPlugin("WorldGuard") != null;
    }
    
    /**
     * Проверяет, включена ли интеграция с WorldGuard
     * @return true, если интеграция включена
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Получает менеджер регионов
     * @return менеджер регионов или null, если интеграция отключена
     */
    public RegionManager getRegionManager() {
        return enabled ? regionManager : null;
    }
} 