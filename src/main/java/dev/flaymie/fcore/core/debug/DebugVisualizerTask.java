package dev.flaymie.fcore.core.debug;

import dev.flaymie.fcore.FCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Задача для визуализации отладочной информации
 */
public class DebugVisualizerTask extends BukkitRunnable {
    private final FCore plugin;
    private final DebugManager debugManager;
    
    public DebugVisualizerTask(FCore plugin, DebugManager debugManager) {
        this.plugin = plugin;
        this.debugManager = debugManager;
    }
    
    @Override
    public void run() {
        // Получаем системную информацию
        double tpsValue = 20.0; // Значение по умолчанию
        try {
            // Используем рефлексию для доступа к TPS
            tpsValue = getTps();
        } catch (Exception e) {
            // Игнорируем ошибку, если метод недоступен
        }
        String tpsFormatted = String.format("%.2f", tpsValue);
        
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        long usedMemory = heapUsage.getUsed() / 1024 / 1024;
        long maxMemory = heapUsage.getMax() / 1024 / 1024;
        
        // Формируем строку статуса
        String statusLine = ChatColor.DARK_GRAY + "[" + ChatColor.AQUA + "Debug" + ChatColor.DARK_GRAY + "] " +
                ChatColor.GRAY + "TPS: " + getColorForTPS(tpsValue) + tpsFormatted + 
                ChatColor.GRAY + " | RAM: " + getColorForMemory(usedMemory, maxMemory) + usedMemory + "MB" +
                ChatColor.GRAY + "/" + maxMemory + "MB";
        
        // Отправляем информацию всем игрокам с включенным режимом отладки
        for (UUID uuid : debugManager.getDebugEnabledPlayers()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, 
                        net.md_5.bungee.api.chat.TextComponent.fromLegacyText(statusLine));
            }
        }
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
     * Получить цвет для отображения TPS в зависимости от значения
     * @param tps значение TPS
     * @return цвет для отображения
     */
    private ChatColor getColorForTPS(double tps) {
        if (tps > 18.0) {
            return ChatColor.GREEN;
        } else if (tps > 15.0) {
            return ChatColor.YELLOW;
        } else {
            return ChatColor.RED;
        }
    }
    
    /**
     * Получить цвет для отображения памяти в зависимости от использования
     * @param used использованная память
     * @param max максимальная память
     * @return цвет для отображения
     */
    private ChatColor getColorForMemory(long used, long max) {
        double usagePercent = (double) used / max;
        
        if (usagePercent < 0.5) {
            return ChatColor.GREEN;
        } else if (usagePercent < 0.75) {
            return ChatColor.YELLOW;
        } else {
            return ChatColor.RED;
        }
    }
} 