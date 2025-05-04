package dev.flaymie.fcore.core.debug;

import dev.flaymie.fcore.FCore;
import dev.flaymie.fcore.api.service.FCoreService;
import dev.flaymie.fcore.utils.message.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Менеджер режима отладки
 */
public class DebugManager implements FCoreService {
    private final FCore plugin;
    private final Set<UUID> debugEnabledPlayers;
    private final Map<String, Long> performanceMetrics;
    private boolean fileWatcherEnabled;
    private FileWatcher fileWatcher;
    private DebugVisualizerTask visualizerTask;
    
    public DebugManager(FCore plugin) {
        this.plugin = plugin;
        this.debugEnabledPlayers = Collections.newSetFromMap(new ConcurrentHashMap<>());
        this.performanceMetrics = new ConcurrentHashMap<>();
        this.fileWatcherEnabled = false;
    }
    
    @Override
    public void onEnable() {
        // Запускаем визуализатор для показа дебаг-информации
        this.visualizerTask = new DebugVisualizerTask(plugin, this);
        this.visualizerTask.runTaskTimer(plugin, 20L, 20L);
        
        plugin.getLogger().info("Менеджер отладки запущен");
    }
    
    @Override
    public void onDisable() {
        // Останавливаем визуализатор
        if (this.visualizerTask != null) {
            this.visualizerTask.cancel();
            this.visualizerTask = null;
        }
        
        // Останавливаем отслеживание файлов, если оно было включено
        stopFileWatcher();
        
        // Очищаем данные
        this.debugEnabledPlayers.clear();
        this.performanceMetrics.clear();
        
        plugin.getLogger().info("Менеджер отладки отключен");
    }
    
    @Override
    public String getName() {
        return "DebugManager";
    }
    
    /**
     * Включить/выключить режим отладки для игрока
     * @param player игрок
     * @return текущее состояние режима отладки (true - включен)
     */
    public boolean toggleDebug(Player player) {
        UUID uuid = player.getUniqueId();
        if (debugEnabledPlayers.contains(uuid)) {
            debugEnabledPlayers.remove(uuid);
            return false;
        } else {
            debugEnabledPlayers.add(uuid);
            return true;
        }
    }
    
    /**
     * Проверяет, включен ли режим отладки у игрока
     * @param player игрок
     * @return true, если режим отладки включен
     */
    public boolean isDebugEnabled(Player player) {
        return debugEnabledPlayers.contains(player.getUniqueId());
    }
    
    /**
     * Отправить отладочное сообщение игрокам с включенным режимом отладки
     * @param message сообщение
     */
    public void sendDebugMessage(String message) {
        String debugPrefix = ChatColor.DARK_GRAY + "[" + ChatColor.AQUA + "DEBUG" + ChatColor.DARK_GRAY + "] ";
        String formattedMessage = debugPrefix + ChatColor.GRAY + message;
        
        for (UUID uuid : debugEnabledPlayers) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                player.sendMessage(formattedMessage);
            }
        }
    }
    
    /**
     * Логировать отладочную информацию в консоль
     * @param message сообщение
     */
    public void logDebug(String message) {
        plugin.getLogger().log(Level.INFO, "[DEBUG] " + message);
    }
    
    /**
     * Начать отслеживание производительности
     * @param key идентификатор операции
     */
    public void startPerformanceTracking(String key) {
        performanceMetrics.put(key, System.currentTimeMillis());
    }
    
    /**
     * Завершить отслеживание производительности и получить результат
     * @param key идентификатор операции
     * @return время выполнения в мс или -1, если ключ не найден
     */
    public long endPerformanceTracking(String key) {
        Long startTime = performanceMetrics.remove(key);
        if (startTime == null) {
            return -1;
        }
        
        long elapsedTime = System.currentTimeMillis() - startTime;
        sendDebugMessage("Производительность [" + key + "]: " + elapsedTime + "мс");
        return elapsedTime;
    }
    
    /**
     * Показать информацию о системе игроку
     * @param player игрок
     */
    public void showInfo(Player player) {
        MessageUtils.sendMessage(player, "&8[&bFCore Debug&8] &7Информация о системе:");
        
        // Информация о сервере
        MessageUtils.sendMessage(player, " &8• &7Версия сервера: &f" + Bukkit.getVersion());
        MessageUtils.sendMessage(player, " &8• &7Bukkit API: &f" + Bukkit.getBukkitVersion());
        
        // Информация о Java
        MessageUtils.sendMessage(player, " &8• &7Java: &f" + System.getProperty("java.version"));
        
        // Информация о памяти
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        long usedMemory = heapUsage.getUsed() / 1024 / 1024;
        long maxMemory = heapUsage.getMax() / 1024 / 1024;
        MessageUtils.sendMessage(player, " &8• &7Память: &f" + usedMemory + "MB&7/&f" + maxMemory + "MB");
        
        // Информация о плагинах
        Plugin[] plugins = Bukkit.getPluginManager().getPlugins();
        MessageUtils.sendMessage(player, " &8• &7Плагинов: &f" + plugins.length);
        
        // Информация о TPS (тики в секунду)
        try {
            double tpsValue = getTps();
            String tpsFormatted = String.format("%.2f", tpsValue);
            MessageUtils.sendMessage(player, " &8• &7TPS: &f" + tpsFormatted);
        } catch (Exception e) {
            // Игнорируем ошибку, если метод недоступен
            MessageUtils.sendMessage(player, " &8• &7TPS: &fНедоступно");
        }
        
        // Информация о мирах
        MessageUtils.sendMessage(player, " &8• &7Миров: &f" + Bukkit.getWorlds().size());
        
        // Информация о игроках
        MessageUtils.sendMessage(player, " &8• &7Игроков: &f" + Bukkit.getOnlinePlayers().size());
    }
    
    /**
     * Получить TPS сервера через рефлексию (работает на большинстве версий)
     * @return текущий TPS или 20.0 по умолчанию
     */
    private double getTps() {
        try {
            // Получаем класс CraftServer
            Class<?> craftServerClass = Bukkit.getServer().getClass();
            
            // Получаем метод getServer который возвращает MinecraftServer
            Method getServerMethod = craftServerClass.getMethod("getServer");
            getServerMethod.setAccessible(true);
            Object minecraftServer = getServerMethod.invoke(Bukkit.getServer());
            
            // Получаем поле RecentTps из MinecraftServer
            Field recentTpsField = minecraftServer.getClass().getField("recentTps");
            recentTpsField.setAccessible(true);
            double[] recentTps = (double[]) recentTpsField.get(minecraftServer);
            
            // Возвращаем первое значение (за последнюю минуту)
            return recentTps[0];
        } catch (Exception e) {
            return 20.0;
        }
    }
    
    /**
     * Перезагрузить плагин
     * @param pluginName имя плагина
     * @return true, если перезагрузка успешна
     */
    public boolean reloadPlugin(String pluginName) {
        Plugin targetPlugin = Bukkit.getPluginManager().getPlugin(pluginName);
        if (targetPlugin == null) {
            return false;
        }
        
        try {
            Bukkit.getPluginManager().disablePlugin(targetPlugin);
            Bukkit.getPluginManager().enablePlugin(targetPlugin);
            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка при перезагрузке плагина " + pluginName, e);
            return false;
        }
    }
    
    /**
     * Включить отслеживание изменений файлов для автоперезагрузки
     * @param pluginFolder папка плагина
     * @return true, если отслеживание включено успешно
     */
    public boolean enableFileWatcher(File pluginFolder) {
        if (fileWatcherEnabled) {
            return true;
        }
        
        try {
            fileWatcher = new FileWatcher(plugin, pluginFolder);
            fileWatcher.startWatching();
            fileWatcherEnabled = true;
            plugin.getLogger().info("Включено отслеживание изменений файлов для автоперезагрузки");
            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Не удалось включить отслеживание файлов", e);
            return false;
        }
    }
    
    /**
     * Выключить отслеживание изменений файлов
     */
    public void stopFileWatcher() {
        if (fileWatcher != null) {
            fileWatcher.stopWatching();
            fileWatcher = null;
            fileWatcherEnabled = false;
            plugin.getLogger().info("Отслеживание изменений файлов отключено");
        }
    }
    
    /**
     * Проверяет, включено ли отслеживание изменений файлов
     * @return true, если отслеживание включено
     */
    public boolean isFileWatcherEnabled() {
        return fileWatcherEnabled;
    }
    
    /**
     * Получает список игроков с включенным режимом отладки
     * @return набор UUID игроков
     */
    public Set<UUID> getDebugEnabledPlayers() {
        return Collections.unmodifiableSet(debugEnabledPlayers);
    }
    
    /**
     * Генерирует отчет о состоянии плагина
     * @param player игрок, запросивший отчет (может быть null для консоли)
     * @return путь к файлу отчета или null, если произошла ошибка
     */
    public String generateReport(Player player) {
        PluginStatusReport report = new PluginStatusReport(plugin);
        String reportPath = report.generateReport();
        
        if (reportPath != null && player != null) {
            MessageUtils.sendMessage(player, "&8[&bDebug&8] &aОтчет создан: &f" + reportPath);
        }
        
        return reportPath;
    }
} 