package dev.flaymie.fcore.core.security;

import dev.flaymie.fcore.FCore;
import dev.flaymie.fcore.api.plugin.FCorePlugin;
import dev.flaymie.fcore.api.plugin.PluginDescription;
import dev.flaymie.fcore.api.service.FCoreService;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Менеджер безопасности FCore
 * Отвечает за проверку зависимостей, версий и целостности плагинов
 */
public class SecurityManager implements FCoreService {

    private final FCore plugin;
    private final Logger logger;
    
    // Информация о проверке зависимостей плагинов от FCore
    private final Map<String, DependencyInfo> pluginDependencies;
    
    // Кеш проверенных плагинов
    private final Map<String, PluginVerificationResult> verificationCache;
    
    // Обработчик ошибок плагинов
    private PluginErrorHandler errorHandler;
    
    // Анализатор безопасности плагинов
    private PluginSecurityAnalyzer securityAnalyzer;
    
    // Утилита для безопасного выполнения операций
    private SafeOperationExecutor safeExecutor;
    
    // Настройки безопасности
    private boolean licenseCheckEnabled;
    private long checkIntervalMillis;
    private boolean verifyPluginSignatures;
    private boolean isolateUnsafeOperations;
    
    /**
     * Конструктор менеджера безопасности
     * @param plugin экземпляр FCore
     */
    public SecurityManager(FCore plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.pluginDependencies = new HashMap<>();
        this.verificationCache = new HashMap<>();
    }
    
    @Override
    public void onEnable() {
        loadConfiguration();
        
        // Создаем вспомогательные компоненты
        errorHandler = new PluginErrorHandler(plugin);
        securityAnalyzer = new PluginSecurityAnalyzer(plugin);
        safeExecutor = new SafeOperationExecutor(plugin);
        
        // Инициализируем обработчик ошибок
        errorHandler.init();
        
        // Регистрируем шедулер для периодических проверок
        if (licenseCheckEnabled) {
            Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::verifyAllPlugins, 20L, checkIntervalMillis / 50);
        }
        
        // Проверяем все плагины при первом запуске
        verifyAllPlugins();
        
        // Сканируем все плагины Bukkit для проверки зависимостей
        scanBukkitPlugins();
        
        logger.info("Менеджер безопасности успешно инициализирован");
    }
    
    @Override
    public void onDisable() {
        // Очищаем кеш при отключении
        pluginDependencies.clear();
        verificationCache.clear();
    }
    
    @Override
    public String getName() {
        return "SecurityManager";
    }
    
    /**
     * Загружает настройки безопасности из конфигурации
     */
    private void loadConfiguration() {
        FileConfiguration config = plugin.getConfig();
        
        licenseCheckEnabled = config.getBoolean("security.license-check", true);
        int checkInterval = config.getInt("security.check-interval", 60);
        checkIntervalMillis = TimeUnit.MINUTES.toMillis(checkInterval);
        verifyPluginSignatures = config.getBoolean("security.verify-signatures", false);
        isolateUnsafeOperations = config.getBoolean("security.isolate-unsafe", true);
    }
    
    /**
     * Проверяет все плагины на сервере
     */
    private void verifyAllPlugins() {
        // Проверяем FCorePlugins
        for (FCorePlugin plugin : plugin.getFCorePluginLoader().getPlugins()) {
            verifyFCorePlugin(plugin);
        }
        
        // Также проверяем обычные плагины Bukkit
        if (isolateUnsafeOperations) {
            for (Plugin bukkitPlugin : Bukkit.getPluginManager().getPlugins()) {
                if (!(bukkitPlugin instanceof FCore)) {
                    checkPluginForVulnerabilities(bukkitPlugin);
                }
            }
        }
    }
    
    /**
     * Проверяет отдельный плагин FCore
     * @param fcorePlugin плагин для проверки
     * @return результат проверки
     */
    public PluginVerificationResult verifyFCorePlugin(FCorePlugin fcorePlugin) {
        String pluginName = fcorePlugin.getName();
        
        // Проверяем кеш сначала
        if (verificationCache.containsKey(pluginName)) {
            PluginVerificationResult result = verificationCache.get(pluginName);
            // Проверяем, не устарела ли проверка
            if (System.currentTimeMillis() - result.getTimestamp() < checkIntervalMillis) {
                return result;
            }
        }
        
        PluginVerificationResult result = new PluginVerificationResult(pluginName);
        result.setTimestamp(System.currentTimeMillis());
        
        // Проверяем версию ядра
        String coreVersion = plugin.getDescription().getVersion();
        result.setFcoreVersion(coreVersion);
        
        // Устанавливаем статус по умолчанию
        result.setStatus(PluginVerificationStatus.VERIFIED);
        
        // Сохраняем результат в кеш
        verificationCache.put(pluginName, result);
        
        return result;
    }
    
    /**
     * Проверяет отдельный плагин Bukkit
     * @param bukkitPlugin плагин для проверки
     * @return результат проверки
     */
    public PluginVerificationResult verifyPlugin(Plugin bukkitPlugin) {
        String pluginName = bukkitPlugin.getName();

        // Проверяем кеш сначала
        if (verificationCache.containsKey(pluginName)) {
            PluginVerificationResult cachedResult = verificationCache.get(pluginName);
            if (System.currentTimeMillis() - cachedResult.getTimestamp() < checkIntervalMillis) {
                return cachedResult;
            }
        }

        PluginVerificationResult result = new PluginVerificationResult(pluginName);
        result.setTimestamp(System.currentTimeMillis());

        // Проверяем, что плагин зависит от FCore
        if (!bukkitPlugin.getDescription().getDepend().contains("FCore")) {
            result.setStatus(PluginVerificationStatus.BLOCKED);
            result.addIssue("Плагин не зависит от FCore в plugin.yml");
            verificationCache.put(pluginName, result);
            return result;
        }

        // В будущем здесь может быть более сложная логика проверки лицензии,
        // хеша, подписи и т.д.
        // Пока что, если зависимость есть, считаем, что все в порядке.
        result.setStatus(PluginVerificationStatus.VERIFIED);
        
        // Сохраняем результат в кеш
        verificationCache.put(pluginName, result);
        
        return result;
    }

    /**
     * Проверяет плагин на уязвимости
     * @param bukkitPlugin плагин для проверки
     */
    private void checkPluginForVulnerabilities(Plugin bukkitPlugin) {
        if (plugin.getConfig().getBoolean("security.analyze-plugins", true)) {
            // Выполняем анализ в отдельном потоке
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    List<PluginSecurityAnalyzer.SecurityIssue> issues = securityAnalyzer.analyzePlugin(bukkitPlugin);
                    
                    // Если найдены проблемы, логируем их
                    if (!issues.isEmpty()) {
                        logger.warning("В плагине " + bukkitPlugin.getName() + " найдены проблемы безопасности:");
                        for (PluginSecurityAnalyzer.SecurityIssue issue : issues) {
                            logger.warning("  " + issue);
                        }
                    }
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Ошибка при анализе плагина " + bukkitPlugin.getName(), e);
                }
            });
        }
    }
    
    /**
     * Сканирует все плагины на сервере для проверки зависимостей от FCore
     */
    private void scanBukkitPlugins() {
        File pluginsDir = Bukkit.getPluginManager().getPlugin("FCore").getDataFolder().getParentFile();
        File[] files = pluginsDir.listFiles((dir, name) -> name.endsWith(".jar"));
        
        if (files == null) return;
        
        for (File file : files) {
            try (ZipInputStream zis = new ZipInputStream(new FileInputStream(file))) {
                ZipEntry entry;
                boolean hasFCoreDependency = false;
                
                while ((entry = zis.getNextEntry()) != null) {
                    if (entry.getName().equals("plugin.yml")) {
                        // Примитивная проверка на наличие строки "FCore" в plugin.yml
                        byte[] buffer = new byte[1024];
                        StringBuilder sb = new StringBuilder();
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            sb.append(new String(buffer, 0, len));
                        }
                        
                        String content = sb.toString();
                        if (content.contains("FCore")) {
                            hasFCoreDependency = true;
                            
                            // Получаем имя плагина из plugin.yml
                            String pluginName = file.getName().replace(".jar", "");
                            
                            // Сохраняем информацию о зависимости
                            DependencyInfo info = new DependencyInfo(pluginName, file.getName());
                            info.setHasFCoreDependency(true);
                            
                            // Вычисляем хеш плагина для проверки целостности
                            String fileHash = calculateFileHash(file);
                            info.setFileHash(fileHash);
                            
                            pluginDependencies.put(pluginName, info);
                            
                            logger.info("Плагин " + pluginName + " зависит от FCore");
                        }
                    }
                }
                
                if (!hasFCoreDependency) {
                    DependencyInfo info = new DependencyInfo(file.getName().replace(".jar", ""), file.getName());
                    info.setHasFCoreDependency(false);
                    pluginDependencies.put(info.getPluginName(), info);
                }
            } catch (IOException e) {
                logger.log(Level.WARNING, "Ошибка при сканировании плагина " + file.getName(), e);
            }
        }
    }
    
    /**
     * Вычисляет хеш файла для проверки целостности
     * @param file файл для проверки
     * @return хеш в виде строки
     */
    private String calculateFileHash(File file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int read;
            
            try (FileInputStream fis = new FileInputStream(file)) {
                while ((read = fis.read(buffer)) > 0) {
                    digest.update(buffer, 0, read);
                }
            }
            
            byte[] hash = digest.digest();
            
            // Преобразуем байты в строку hex
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            
            return sb.toString();
        } catch (NoSuchAlgorithmException | IOException e) {
            logger.log(Level.WARNING, "Ошибка при вычислении хеша файла " + file.getName(), e);
            return "";
        }
    }
    
    /**
     * Получает информацию о зависимости плагина от FCore
     * @param pluginName имя плагина
     * @return информация о зависимости или null
     */
    public DependencyInfo getPluginDependencyInfo(String pluginName) {
        return pluginDependencies.get(pluginName);
    }
    
    /**
     * Проверяет версию плагина на совместимость с FCore
     * @param description описание плагина
     * @return true если версии совместимы
     */
    public boolean checkPluginVersion(PluginDescription description) {
        // Просто проверяем, что плагин зависит от FCore
        // В будущем можно добавить более сложную логику проверки версий
        return description.getDepends().contains("FCore");
    }
    
    /**
     * Проверяет, безопасен ли плагин для загрузки
     * @param pluginName имя плагина
     * @return true если плагин безопасен
     */
    public boolean isPluginSafe(String pluginName) {
        PluginVerificationResult result = verificationCache.get(pluginName);
        if (result == null) {
            return false;
        }
        
        return result.getStatus() == PluginVerificationStatus.VERIFIED;
    }
    
    /**
     * Проверяет, включена ли изоляция небезопасных операций
     * @return true если изоляция включена
     */
    public boolean isIsolateUnsafeOperations() {
        return isolateUnsafeOperations;
    }
    
    /**
     * Получает обработчик ошибок плагинов
     * @return обработчик ошибок
     */
    public PluginErrorHandler getErrorHandler() {
        return errorHandler;
    }
    
    /**
     * Получает анализатор безопасности плагинов
     * @return анализатор безопасности
     */
    public PluginSecurityAnalyzer getSecurityAnalyzer() {
        return securityAnalyzer;
    }
    
    /**
     * Получает утилиту для безопасного выполнения операций
     * @return утилита для безопасного выполнения
     */
    public SafeOperationExecutor getSafeExecutor() {
        return safeExecutor;
    }
} 