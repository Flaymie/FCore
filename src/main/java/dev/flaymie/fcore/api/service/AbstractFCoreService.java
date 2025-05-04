package dev.flaymie.fcore.api.service;

import dev.flaymie.fcore.FCore;
import java.util.logging.Logger;

/**
 * Базовый абстрактный класс для сервисов ядра
 */
public abstract class AbstractFCoreService implements FCoreService {
    
    protected final FCore plugin;
    protected final Logger logger;
    private final String name;
    
    public AbstractFCoreService(FCore plugin, String name) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.name = name;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    protected void log(String message) {
        logger.info("[" + name + "] " + message);
    }
    
    protected void warn(String message) {
        logger.warning("[" + name + "] " + message);
    }
    
    protected void error(String message) {
        logger.severe("[" + name + "] " + message);
    }
} 