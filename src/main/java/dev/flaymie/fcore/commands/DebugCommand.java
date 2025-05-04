package dev.flaymie.fcore.commands;

import dev.flaymie.fcore.FCore;
import dev.flaymie.fcore.api.annotation.Command;
import dev.flaymie.fcore.api.annotation.Permission;
import dev.flaymie.fcore.api.annotation.Subcommand;
import dev.flaymie.fcore.core.debug.DebugManager;
import dev.flaymie.fcore.utils.message.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Команда для управления режимом отладки
 */
@Command(name = "debug", description = "Управление режимом отладки")
@Permission("fcore.debug")
public class DebugCommand {
    
    private DebugManager debugManager;
    private FCore plugin;
    
    public DebugCommand(DebugManager debugManager, FCore plugin) {
        this.debugManager = debugManager;
        this.plugin = plugin;
    }
    
    /**
     * Базовая команда, переключает режим отладки
     */
    public void execute(Player player) {
        boolean enabled = debugManager.toggleDebug(player);
        MessageUtils.sendMessage(player, enabled 
                ? "&8[&bDebug&8] &aРежим отладки &2включен"
                : "&8[&bDebug&8] &cРежим отладки &4выключен");
    }
    
    /**
     * Показывает информацию о системе
     */
    @Subcommand("info")
    public void showInfo(Player player) {
        debugManager.showInfo(player);
    }
    
    /**
     * Перезагружает плагин
     */
    @Subcommand("reload")
    @Permission("fcore.debug.reload")
    public void reloadPlugin(Player player, String pluginName) {
        if (pluginName == null || pluginName.isEmpty()) {
            MessageUtils.sendMessage(player, "&8[&bDebug&8] &cУкажите имя плагина!");
            return;
        }
        
        boolean success = debugManager.reloadPlugin(pluginName);
        if (success) {
            MessageUtils.sendMessage(player, "&8[&bDebug&8] &aПлагин &2" + pluginName + " &aуспешно перезагружен");
        } else {
            MessageUtils.sendMessage(player, "&8[&bDebug&8] &cНе удалось перезагрузить плагин &4" + pluginName);
        }
    }
    
    /**
     * Включает/выключает отслеживание файлов
     */
    @Subcommand("watch")
    @Permission("fcore.debug.watch")
    public void toggleFileWatcher(Player player) {
        if (debugManager.isFileWatcherEnabled()) {
            debugManager.stopFileWatcher();
            MessageUtils.sendMessage(player, "&8[&bDebug&8] &cОтслеживание файлов выключено");
        } else {
            File pluginFolder = plugin.getDataFolder();
            boolean success = debugManager.enableFileWatcher(pluginFolder);
            if (success) {
                MessageUtils.sendMessage(player, "&8[&bDebug&8] &aОтслеживание файлов включено для директории: &2" + pluginFolder.getPath());
            } else {
                MessageUtils.sendMessage(player, "&8[&bDebug&8] &cНе удалось включить отслеживание файлов");
            }
        }
    }
    
    /**
     * Проверяет TPS сервера
     */
    @Subcommand("tps")
    public void checkTps(Player player) {
        try {
            double[] tps = getTps();
            String tpsInfo = String.format(
                    "&8[&bDebug&8] &7TPS: &a%.2f&7, &e%.2f&7, &c%.2f",
                    tps[0], tps[1], tps[2]
            );
            MessageUtils.sendMessage(player, tpsInfo);
        } catch (Exception e) {
            MessageUtils.sendMessage(player, "&8[&bDebug&8] &7TPS: &cНедоступно в этой версии сервера");
        }
    }
    
    /**
     * Получить TPS сервера через рефлексию (работает на большинстве версий)
     * @return массив TPS или устанавливает значения по умолчанию
     */
    private double[] getTps() {
        double[] result = {20.0, 20.0, 20.0}; // Значения по умолчанию
        
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
            return (double[]) recentTpsField.get(minecraftServer);
        } catch (Exception e) {
            return result;
        }
    }
    
    /**
     * Проверяет использование памяти
     */
    @Subcommand("memory")
    public void checkMemory(Player player) {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory() / 1024 / 1024;
        long totalMemory = runtime.totalMemory() / 1024 / 1024;
        long freeMemory = runtime.freeMemory() / 1024 / 1024;
        long usedMemory = totalMemory - freeMemory;
        
        MessageUtils.sendMessage(player, "&8[&bDebug&8] &7Память:");
        MessageUtils.sendMessage(player, " &8• &7Максимум: &f" + maxMemory + "MB");
        MessageUtils.sendMessage(player, " &8• &7Выделено: &f" + totalMemory + "MB");
        MessageUtils.sendMessage(player, " &8• &7Использовано: &f" + usedMemory + "MB");
        MessageUtils.sendMessage(player, " &8• &7Свободно: &f" + freeMemory + "MB");
    }
    
    /**
     * Выводит список плагинов
     */
    @Subcommand("plugins")
    public void listPlugins(Player player) {
        Plugin[] plugins = Bukkit.getPluginManager().getPlugins();
        
        MessageUtils.sendMessage(player, "&8[&bDebug&8] &7Плагинов: &f" + plugins.length);
        for (Plugin p : plugins) {
            String status = p.isEnabled() ? "&a✓" : "&c✗";
            MessageUtils.sendMessage(player, " " + status + " &7" + p.getName() + " &8(&7v" + p.getDescription().getVersion() + "&8)");
        }
    }
    
    /**
     * Очистка памяти
     */
    @Subcommand("gc")
    @Permission("fcore.debug.gc")
    public void forceGC(Player player) {
        MessageUtils.sendMessage(player, "&8[&bDebug&8] &7Запуск сборщика мусора...");
        
        long memoryBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        System.gc();
        try {
            Thread.sleep(500); // Даем время GC выполниться
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        long memoryAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long freed = (memoryBefore - memoryAfter) / 1024 / 1024;
        
        MessageUtils.sendMessage(player, "&8[&bDebug&8] &7Освобождено: &f" + freed + "MB");
    }
    
    /**
     * Генерирует отчет о состоянии плагина
     */
    @Subcommand("report")
    @Permission("fcore.debug.report")
    public void generateReport(Player player) {
        MessageUtils.sendMessage(player, "&8[&bDebug&8] &7Создание отчета о состоянии плагина...");
        String reportPath = debugManager.generateReport(player);
        
        if (reportPath == null) {
            MessageUtils.sendMessage(player, "&8[&bDebug&8] &cНе удалось создать отчет. Проверьте консоль для получения информации.");
        }
    }
} 