package dev.flaymie.fcore.core.debug;

import dev.flaymie.fcore.FCore;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

/**
 * Генератор отчетов о состоянии плагина
 */
public class PluginStatusReport {
    private final FCore plugin;
    
    public PluginStatusReport(FCore plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Генерирует отчет о состоянии плагина и сохраняет его в файл
     * @return путь к файлу отчета или null, если произошла ошибка
     */
    public String generateReport() {
        try {
            File debugDir = new File(plugin.getDataFolder(), "debug");
            if (!debugDir.exists() && !debugDir.mkdirs()) {
                plugin.getLogger().warning("Не удалось создать директорию для отчетов");
                return null;
            }
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
            String fileName = "report_" + dateFormat.format(new Date()) + ".txt";
            File reportFile = new File(debugDir, fileName);
            
            try (PrintWriter writer = new PrintWriter(new FileWriter(reportFile))) {
                writeReportHeader(writer);
                writeSystemInfo(writer);
                writePluginInfo(writer);
                writeMemoryInfo(writer);
                writeThreadInfo(writer);
                writePluginsList(writer);
                
                plugin.getLogger().info("Отчет сгенерирован и сохранен в " + reportFile.getAbsolutePath());
                return reportFile.getAbsolutePath();
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка при создании отчета", e);
            return null;
        }
    }
    
    /**
     * Записывает заголовок отчета
     * @param writer поток для записи
     */
    private void writeReportHeader(PrintWriter writer) {
        writer.println("==============================================");
        writer.println("            ОТЧЕТ О СОСТОЯНИИ FCORE           ");
        writer.println("==============================================");
        writer.println("Дата создания: " + new Date());
        writer.println("Версия плагина: " + plugin.getDescription().getVersion());
        writer.println("==============================================\n");
    }
    
    /**
     * Записывает информацию о системе
     * @param writer поток для записи
     */
    private void writeSystemInfo(PrintWriter writer) {
        writer.println("СИСТЕМНАЯ ИНФОРМАЦИЯ:");
        writer.println("---------------------------------------------");
        writer.println("Версия сервера: " + Bukkit.getVersion());
        writer.println("Bukkit API: " + Bukkit.getBukkitVersion());
        writer.println("Java: " + System.getProperty("java.version"));
        writer.println("OS: " + System.getProperty("os.name") + " " + 
                       System.getProperty("os.version") + " " + 
                       System.getProperty("os.arch"));
        writer.println("Доступные процессоры: " + Runtime.getRuntime().availableProcessors());
        
        // TPS (возможно потребуется адаптация в зависимости от версии сервера)
        try {
            double[] tps = getTps();
            writer.println("TPS (1m, 5m, 15m): " + formatTPS(tps));
        } catch (Exception e) {
            writer.println("TPS: Недоступно");
        }
        
        writer.println("Всего игроков: " + Bukkit.getOnlinePlayers().size() + "/" + Bukkit.getMaxPlayers());
        writer.println("Загружено миров: " + Bukkit.getWorlds().size());
        writer.println("---------------------------------------------\n");
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
     * Записывает информацию о плагине
     * @param writer поток для записи
     */
    private void writePluginInfo(PrintWriter writer) {
        writer.println("ИНФОРМАЦИЯ О ПЛАГИНЕ:");
        writer.println("---------------------------------------------");
        writer.println("Имя: " + plugin.getDescription().getName());
        writer.println("Версия: " + plugin.getDescription().getVersion());
        writer.println("Авторы: " + String.join(", ", plugin.getDescription().getAuthors()));
        writer.println("Описание: " + plugin.getDescription().getDescription());
        writer.println("Зависимости: " + String.join(", ", plugin.getDescription().getDepend()));
        writer.println("Мягкие зависимости: " + String.join(", ", plugin.getDescription().getSoftDepend()));
        writer.println("Время работы: " + formatUptime());
        writer.println("Директория плагина: " + plugin.getDataFolder().getAbsolutePath());
        writer.println("---------------------------------------------\n");
    }
    
    /**
     * Записывает информацию о памяти
     * @param writer поток для записи
     */
    private void writeMemoryInfo(PrintWriter writer) {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        
        writer.println("ИНФОРМАЦИЯ О ПАМЯТИ:");
        writer.println("---------------------------------------------");
        writer.println("Heap используется: " + (memoryBean.getHeapMemoryUsage().getUsed() / 1024 / 1024) + " MB");
        writer.println("Heap выделено: " + (memoryBean.getHeapMemoryUsage().getCommitted() / 1024 / 1024) + " MB");
        writer.println("Heap максимум: " + (memoryBean.getHeapMemoryUsage().getMax() / 1024 / 1024) + " MB");
        
        writer.println("Non-Heap используется: " + (memoryBean.getNonHeapMemoryUsage().getUsed() / 1024 / 1024) + " MB");
        writer.println("Non-Heap выделено: " + (memoryBean.getNonHeapMemoryUsage().getCommitted() / 1024 / 1024) + " MB");
        
        ClassLoadingMXBean classBean = ManagementFactory.getClassLoadingMXBean();
        writer.println("Загружено классов: " + classBean.getLoadedClassCount());
        writer.println("Всего загружено классов: " + classBean.getTotalLoadedClassCount());
        writer.println("Выгружено классов: " + classBean.getUnloadedClassCount());
        writer.println("---------------------------------------------\n");
    }
    
    /**
     * Записывает информацию о потоках
     * @param writer поток для записи
     */
    private void writeThreadInfo(PrintWriter writer) {
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        
        writer.println("ИНФОРМАЦИЯ О ПОТОКАХ:");
        writer.println("---------------------------------------------");
        writer.println("Активные потоки: " + threadBean.getThreadCount());
        writer.println("Пиковое количество потоков: " + threadBean.getPeakThreadCount());
        writer.println("Всего запущено потоков: " + threadBean.getTotalStartedThreadCount());
        writer.println("Потоки в состоянии daemon: " + threadBean.getDaemonThreadCount());
        
        // Список активных потоков
        writer.println("\nСписок активных потоков:");
        for (long threadId : threadBean.getAllThreadIds()) {
            writer.println(" - " + threadBean.getThreadInfo(threadId).getThreadName() + 
                           " (состояние: " + threadBean.getThreadInfo(threadId).getThreadState() + ")");
        }
        writer.println("---------------------------------------------\n");
    }
    
    /**
     * Записывает список плагинов
     * @param writer поток для записи
     */
    private void writePluginsList(PrintWriter writer) {
        Plugin[] plugins = Bukkit.getPluginManager().getPlugins();
        
        writer.println("СПИСОК ПЛАГИНОВ (" + plugins.length + "):");
        writer.println("---------------------------------------------");
        for (Plugin p : plugins) {
            writer.println(p.getName() + " v" + p.getDescription().getVersion() + 
                           " - " + (p.isEnabled() ? "Включен" : "Выключен"));
        }
        writer.println("---------------------------------------------\n");
    }
    
    /**
     * Форматирует значения TPS
     * @param tps массив значений TPS
     * @return отформатированная строка
     */
    private String formatTPS(double[] tps) {
        StringBuilder sb = new StringBuilder();
        for (double t : tps) {
            sb.append(String.format("%.2f, ", t));
        }
        // Удаляем последнюю запятую и пробел
        if (sb.length() > 2) {
            sb.setLength(sb.length() - 2);
        }
        return sb.toString();
    }
    
    /**
     * Форматирует время работы сервера
     * @return отформатированная строка
     */
    private String formatUptime() {
        long uptime = ManagementFactory.getRuntimeMXBean().getUptime() / 1000; // в секундах
        long days = uptime / (24 * 3600);
        uptime %= (24 * 3600);
        long hours = uptime / 3600;
        uptime %= 3600;
        long minutes = uptime / 60;
        long seconds = uptime % 60;
        
        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append(" д ");
        if (hours > 0) sb.append(hours).append(" ч ");
        if (minutes > 0) sb.append(minutes).append(" м ");
        sb.append(seconds).append(" с");
        
        return sb.toString();
    }
} 