package dev.flaymie.fcore;

import dev.flaymie.fcore.api.permission.PermissionManager;
import dev.flaymie.fcore.api.plugin.PluginLoader;
import dev.flaymie.fcore.api.service.FCoreService;
import dev.flaymie.fcore.commands.DebugCommand;
import dev.flaymie.fcore.commands.PermissionCommand;
import dev.flaymie.fcore.commands.PluginCommand;
import dev.flaymie.fcore.commands.SecurityCommand;
import dev.flaymie.fcore.core.ServiceManager;
import dev.flaymie.fcore.core.action.ActionManager;
import dev.flaymie.fcore.core.action.ActionManagerService;
import dev.flaymie.fcore.core.command.CommandManager;
import dev.flaymie.fcore.core.command.FCoreCommand;
import dev.flaymie.fcore.core.config.FCoreConfig;
import dev.flaymie.fcore.core.data.DataManager;
import dev.flaymie.fcore.core.debug.DebugManager;
import dev.flaymie.fcore.core.di.DependencyContainer;
import dev.flaymie.fcore.core.di.ServiceScanner;
import dev.flaymie.fcore.core.event.EventManager;
import dev.flaymie.fcore.core.event.EventListenerScanner;
import dev.flaymie.fcore.core.permission.PermissionManagerImpl;
import dev.flaymie.fcore.integration.IntegrationManager;
import dev.flaymie.fcore.core.security.SecurityManager;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.logging.Logger;

/**
 * Основной класс ядра FCore
 */
public final class FCore extends JavaPlugin {
    private static FCore instance;
    private Logger logger;
    private ServiceManager serviceManager;
    private DependencyContainer dependencyContainer;
    private ServiceScanner serviceScanner;
    private FCoreConfig coreConfig;
    private CommandManager commandManager;
    private EventManager eventManager;
    private EventListenerScanner eventListenerScanner;
    private DataManager dataManager;
    private ActionManager actionManager;
    private PermissionManager permissionManager;
    private DebugManager debugManager;
    private IntegrationManager integrationManager;
    private PluginLoader pluginLoader;
    private SecurityManager securityManager;

    @Override
    public void onEnable() {
        instance = this;
        logger = getLogger();
        
        // Выводим информацию о запуске
        logger.info("█▀▀ █▀▀ █▀█ █▀█ █▀▀");
        logger.info("█▀  █   █ █ █▀▄ █▀▀");
        logger.info("▀   ▀▀▀ ▀▀▀ ▀ ▀ ▀▀▀");
        logger.info("Версия: " + getDescription().getVersion());
        logger.info("Разработчик: Flaymie");
        
        // Инициализация компонентов
        initCore();
        
        // Регистрируем команды
        registerCommands();
        
        logger.info("Ядро успешно загружено!");
    }

    @Override
    public void onDisable() {
        logger.info("Отключение ядра...");
        
        // Отключаем все сервисы
        if (serviceManager != null) {
            serviceManager.disableAllServices();
        }
        
        logger.info("Ядро успешно отключено!");
        instance = null;
    }
    
    /**
     * Инициализация основных компонентов ядра
     */
    private void initCore() {
        // Создаем конфигурацию
        coreConfig = new FCoreConfig(this);
        
        // Создаем контейнер зависимостей
        dependencyContainer = new DependencyContainer(this);
        
        // Создаем менеджер сервисов
        serviceManager = new ServiceManager(this);
        
        // Создаем менеджер команд
        commandManager = new CommandManager(this, dependencyContainer);
        
        // Создаем менеджер событий
        eventManager = new EventManager(this, dependencyContainer);
        
        // Создаем менеджер действий
        actionManager = new ActionManager(this);
        
        // Создаем менеджер прав
        permissionManager = new PermissionManagerImpl(this);
        
        // Создаем менеджер отладки
        debugManager = new DebugManager(this);
        
        // Создаем менеджер интеграций
        integrationManager = new IntegrationManager(this);
        
        // Создаем загрузчик плагинов
        pluginLoader = new PluginLoader(this);
        
        // Создаем менеджер безопасности
        securityManager = new SecurityManager(this);
        
        // Регистрируем базовые сервисы
        registerServices();
        
        // Создаем и регистрируем менеджер данных
        dataManager = new DataManager(this);
        serviceManager.registerService(DataManager.class, dataManager);
        dependencyContainer.registerSingleton(DataManager.class, dataManager);
        
        // Создаем и регистрируем сканер сервисов
        serviceScanner = new ServiceScanner(this, dependencyContainer);
        serviceManager.registerService(ServiceScanner.class, serviceScanner);
        
        // Создаем и регистрируем сканер слушателей событий
        eventListenerScanner = new EventListenerScanner(this, eventManager, dependencyContainer);
        serviceManager.registerService(EventListenerScanner.class, eventListenerScanner);
        
        // Запускаем все сервисы
        serviceManager.enableAllServices();
    }
    
    /**
     * Регистрация базовых сервисов
     */
    private void registerServices() {
        // Регистрируем конфигурацию
        serviceManager.registerService(FCoreConfig.class, coreConfig);
        
        // Регистрируем контейнер зависимостей как сервис
        serviceManager.registerService(DependencyContainer.class, dependencyContainer);
        
        // Регистрируем менеджер команд
        serviceManager.registerService(CommandManager.class, commandManager);
        
        // Регистрируем менеджер событий
        serviceManager.registerService(EventManager.class, eventManager);
        
        // Создаем и регистрируем сервис менеджера действий
        ActionManagerService actionManagerService = new ActionManagerService(this, actionManager);
        serviceManager.registerService(ActionManagerService.class, actionManagerService);
        
        // Регистрируем менеджер прав
        serviceManager.registerService(PermissionManager.class, permissionManager);
        
        // Регистрируем менеджер отладки
        serviceManager.registerService((Class<FCoreService>)(Class<?>)FCoreService.class, debugManager);
        
        // Регистрируем менеджер интеграций
        serviceManager.registerService(IntegrationManager.class, integrationManager);
        
        // Регистрируем загрузчик плагинов
        serviceManager.registerService(PluginLoader.class, pluginLoader);
        
        // Регистрируем менеджер безопасности
        serviceManager.registerService(SecurityManager.class, securityManager);
        
        // Регистрируем сам плагин как синглтон
        dependencyContainer.registerSingleton(FCore.class, this);
        
        // Регистрируем менеджер сервисов
        dependencyContainer.registerSingleton(ServiceManager.class, serviceManager);
        
        // Регистрируем конфигурацию в DI
        dependencyContainer.registerSingleton(FCoreConfig.class, coreConfig);
        
        // Регистрируем менеджер команд в DI
        dependencyContainer.registerSingleton(CommandManager.class, commandManager);
        
        // Регистрируем менеджер событий в DI
        dependencyContainer.registerSingleton(EventManager.class, eventManager);
        
        // Регистрируем менеджер действий в DI
        dependencyContainer.registerSingleton(ActionManager.class, actionManager);
        
        // Регистрируем менеджер прав в DI
        dependencyContainer.registerSingleton(PermissionManager.class, permissionManager);
        
        // Регистрируем менеджер отладки в DI
        dependencyContainer.registerSingleton(DebugManager.class, debugManager);
        
        // Регистрируем менеджер интеграций в DI
        dependencyContainer.registerSingleton(IntegrationManager.class, integrationManager);
        
        // Регистрируем загрузчик плагинов в DI
        dependencyContainer.registerSingleton(PluginLoader.class, pluginLoader);
        
        // Регистрируем менеджер безопасности в DI
        dependencyContainer.registerSingleton(SecurityManager.class, securityManager);
    }
    
    /**
     * Регистрация команд
     */
    private void registerCommands() {
        // Регистрируем основную команду ядра
        commandManager.registerCommand(FCoreCommand.class);
        
        // Регистрируем команду отладки действий
        commandManager.registerCommand(dev.flaymie.fcore.commands.ActionDebugCommand.class);
        
        // Регистрируем команду управления правами
        commandManager.registerCommand(PermissionCommand.class);
        
        // Регистрируем команду отладки
        commandManager.registerCommand(DebugCommand.class);
        
        // Регистрируем команду управления плагинами FCore
        commandManager.registerCommand(PluginCommand.class);
        
        // Регистрируем команду управления безопасностью
        commandManager.registerCommand(SecurityCommand.class);
    }
    
    /**
     * Получение экземпляра плагина
     * @return экземпляр FCore
     */
    public static FCore getInstance() {
        return instance;
    }
    
    /**
     * Получение менеджера сервисов
     * @return менеджер сервисов
     */
    public ServiceManager getServiceManager() {
        return serviceManager;
    }
    
    /**
     * Получение контейнера зависимостей
     * @return контейнер зависимостей
     */
    public DependencyContainer getDependencyContainer() {
        return dependencyContainer;
    }
    
    /**
     * Получение конфигурации ядра
     * @return конфигурация ядра
     */
    public FCoreConfig getCoreConfig() {
        return coreConfig;
    }
    
    /**
     * Получение менеджера команд
     * @return менеджер команд
     */
    public CommandManager getCommandManager() {
        return commandManager;
    }
    
    /**
     * Получение менеджера событий
     * @return менеджер событий
     */
    public EventManager getEventManager() {
        return eventManager;
    }
    
    /**
     * Получение менеджера данных
     * @return менеджер данных
     */
    public DataManager getDataManager() {
        return dataManager;
    }
    
    /**
     * Получение менеджера действий
     * @return менеджер действий
     */
    public ActionManager getActionManager() {
        return actionManager;
    }
    
    /**
     * Получение менеджера прав
     * @return менеджер прав
     */
    public PermissionManager getPermissionManager() {
        return permissionManager;
    }
    
    /**
     * Получение менеджера отладки
     * @return менеджер отладки
     */
    public DebugManager getDebugManager() {
        return debugManager;
    }
    
    /**
     * Получение менеджера интеграций
     * @return менеджер интеграций
     */
    public IntegrationManager getIntegrationManager() {
        return integrationManager;
    }
    
    /**
     * Получение загрузчика плагинов
     * @return загрузчик плагинов
     */
    public PluginLoader getFCorePluginLoader() {
        return pluginLoader;
    }
    
    /**
     * Получает менеджер безопасности
     * @return менеджер безопасности
     */
    public SecurityManager getSecurityManager() {
        return securityManager;
    }
}
