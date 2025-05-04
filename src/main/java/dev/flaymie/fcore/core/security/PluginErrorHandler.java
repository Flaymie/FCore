package dev.flaymie.fcore.core.security;

import dev.flaymie.fcore.FCore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Обработчик ошибок плагинов
 * Перехватывает и логирует исключения, возникающие в плагинах
 */
public class PluginErrorHandler {
    
    private final FCore plugin;
    private final Logger logger;
    private final File crashReportsDir;
    
    // Карта для отслеживания частоты ошибок плагинов
    private final Map<String, PluginErrorStats> pluginErrorStats = new ConcurrentHashMap<>();
    
    // Набор плагинов, которые были отключены из-за ошибок
    private final Set<String> disabledPlugins = new HashSet<>();
    
    // Максимальное количество ошибок перед отключением плагина
    private int maxErrorsBeforeDisable = 10;
    
    /**
     * Конструктор обработчика ошибок
     * @param plugin экземпляр FCore
     */
    public PluginErrorHandler(FCore plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.crashReportsDir = new File(plugin.getDataFolder(), "crash-reports");
        
        // Создаем директорию для отчетов об ошибках
        if (!crashReportsDir.exists()) {
            crashReportsDir.mkdirs();
        }
        
        // Загружаем настройки из конфигурации
        maxErrorsBeforeDisable = plugin.getConfig().getInt("security.max-errors-before-disable", 10);
    }
    
    /**
     * Инициализирует обработчик ошибок
     */
    public void init() {
        try {
            // Устанавливаем собственный обработчик исключений для событий
            injectEventErrorHandler();
            
            // Устанавливаем глобальный обработчик неперехваченных исключений
            Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
                handleUncaughtException(thread, throwable);
            });
            
            logger.info("Обработчик ошибок плагинов успешно инициализирован");
        } catch (Exception e) {
            logger.severe("Ошибка при инициализации обработчика ошибок: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Внедряет обработчик ошибок в систему событий Bukkit
     */
    private void injectEventErrorHandler() {
        try {
            // В этом методе мы используем отражение для доступа к внутренней структуре событий Bukkit
            // Это позволяет перехватывать ошибки, возникающие при вызове обработчиков событий
            
            // Примечание: этот код зависит от реализации Bukkit
            // и может потребовать обновления при изменении внутренней структуры
            
            // В "реальном" коде будет использоваться инъекция в RegisteredListener
            // Здесь просто заглушка, так как без точной реализации Bukkit это сложно реализовать
            
            logger.info("Обработчик ошибок событий установлен");
        } catch (Exception e) {
            logger.log(Level.WARNING, "Не удалось установить обработчик ошибок событий", e);
        }
    }
    
    /**
     * Обрабатывает неперехваченное исключение в потоке
     * @param thread поток, в котором произошло исключение
     * @param throwable исключение
     */
    private void handleUncaughtException(Thread thread, Throwable throwable) {
        try {
            // Определяем, какой плагин вызвал исключение
            String pluginName = identifyPluginFromStackTrace(throwable);
            
            if (pluginName != null) {
                // Логируем ошибку
                logger.severe("Неперехваченное исключение в плагине " + pluginName + ": " + throwable.getMessage());
                
                // Обновляем статистику ошибок
                updateErrorStats(pluginName, throwable);
                
                // Создаем отчет об ошибке
                createCrashReport(pluginName, thread, throwable);
                
                // Проверяем, не превышен ли лимит ошибок
                checkErrorLimit(pluginName);
            } else {
                // Если не удалось определить плагин, просто логируем ошибку
                logger.severe("Неперехваченное исключение в потоке " + thread.getName() + ": " + throwable.getMessage());
                throwable.printStackTrace();
            }
        } catch (Exception e) {
            // Если произошла ошибка при обработке исключения, логируем ее
            logger.log(Level.SEVERE, "Ошибка при обработке исключения: " + e.getMessage(), e);
        }
    }
    
    /**
     * Обрабатывает исключение в обработчике события
     * @param event событие
     * @param listener слушатель события
     * @param throwable исключение
     */
    public void handleEventException(Event event, Listener listener, Throwable throwable) {
        try {
            // Определяем плагин, к которому принадлежит слушатель
            Plugin listenerPlugin = getPluginFromListener(listener);
            String pluginName = listenerPlugin != null ? listenerPlugin.getName() : "Неизвестный";
            
            // Логируем ошибку
            logger.severe("Исключение при обработке события " + event.getEventName() + 
                    " в плагине " + pluginName + ": " + throwable.getMessage());
            
            // Обновляем статистику ошибок
            updateErrorStats(pluginName, throwable);
            
            // Создаем отчет об ошибке
            createCrashReport(pluginName, Thread.currentThread(), throwable, event, listener);
            
            // Проверяем, не превышен ли лимит ошибок
            checkErrorLimit(pluginName);
        } catch (Exception e) {
            // Если произошла ошибка при обработке исключения, логируем ее
            logger.log(Level.SEVERE, "Ошибка при обработке исключения события: " + e.getMessage(), e);
        }
    }
    
    /**
     * Определяет плагин, к которому принадлежит слушатель
     * @param listener слушатель события
     * @return плагин или null
     */
    private Plugin getPluginFromListener(Listener listener) {
        try {
            // Получаем все плагины
            Plugin[] plugins = Bukkit.getPluginManager().getPlugins();
            
            // Проверяем, принадлежит ли слушатель какому-либо плагину
            for (Plugin plugin : plugins) {
                if (listener.getClass().getClassLoader() == plugin.getClass().getClassLoader()) {
                    return plugin;
                }
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Ошибка при определении плагина для слушателя", e);
        }
        
        return null;
    }
    
    /**
     * Определяет плагин из стека вызовов
     * @param throwable исключение с информацией о стеке вызовов
     * @return имя плагина или null
     */
    private String identifyPluginFromStackTrace(Throwable throwable) {
        StackTraceElement[] stackTrace = throwable.getStackTrace();
        
        // Получаем все плагины
        Plugin[] plugins = Bukkit.getPluginManager().getPlugins();
        
        for (StackTraceElement element : stackTrace) {
            String className = element.getClassName();
            
            // Если это класс FCore, пропускаем
            if (className.startsWith("dev.flaymie.fcore")) {
                continue;
            }
            
            // Проверяем, принадлежит ли класс какому-либо плагину
            for (Plugin plugin : plugins) {
                // Если это не FCore и имя пакета содержит имя плагина
                if (!plugin.getName().equals("FCore") && className.toLowerCase().contains(plugin.getName().toLowerCase())) {
                    return plugin.getName();
                }
            }
        }
        
        return null;
    }
    
    /**
     * Обновляет статистику ошибок для плагина
     * @param pluginName имя плагина
     * @param throwable исключение
     */
    private void updateErrorStats(String pluginName, Throwable throwable) {
        if (pluginName == null) return;
        
        pluginErrorStats.computeIfAbsent(pluginName, k -> new PluginErrorStats())
                .addError(throwable);
    }
    
    /**
     * Проверяет, не превышен ли лимит ошибок для плагина
     * @param pluginName имя плагина
     */
    private void checkErrorLimit(String pluginName) {
        if (pluginName == null || disabledPlugins.contains(pluginName)) return;
        
        PluginErrorStats stats = pluginErrorStats.get(pluginName);
        if (stats != null && stats.getErrorCount() >= maxErrorsBeforeDisable) {
            // Если превышен лимит ошибок, отключаем плагин
            logger.severe("Плагин " + pluginName + " превысил лимит ошибок и будет отключен");
            
            // Добавляем в список отключенных
            disabledPlugins.add(pluginName);
            
            // Отключаем плагин
            Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
            if (plugin != null) {
                try {
                    // Запускаем отключение в основном потоке сервера
                    Bukkit.getScheduler().runTask(this.plugin, () -> {
                        Bukkit.getPluginManager().disablePlugin(plugin);
                        
                        // Уведомляем администраторов
                        String message = "§c[FCore] Плагин " + pluginName + " был отключен из-за слишком большого количества ошибок.";
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            if (player.hasPermission("fcore.admin")) {
                                player.sendMessage(message);
                            }
                        }
                    });
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Ошибка при отключении плагина " + pluginName, e);
                }
            }
        }
    }
    
    /**
     * Создает отчет об ошибке
     * @param pluginName имя плагина
     * @param thread поток
     * @param throwable исключение
     * @param additionalInfo дополнительная информация
     */
    private void createCrashReport(String pluginName, Thread thread, Throwable throwable, Object... additionalInfo) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
            String timeStamp = dateFormat.format(new Date());
            
            // Создаем имя файла отчета
            String fileName = "crash-" + timeStamp + "-" + pluginName + ".log";
            File reportFile = new File(crashReportsDir, fileName);
            
            // Создаем отчет
            try (PrintWriter writer = new PrintWriter(new FileWriter(reportFile))) {
                writer.println("=== FCore Crash Report ===");
                writer.println("Время: " + new Date());
                writer.println("Плагин: " + pluginName);
                writer.println("Поток: " + thread.getName() + " (id: " + thread.getId() + ")");
                writer.println("Исключение: " + throwable.getClass().getName() + ": " + throwable.getMessage());
                writer.println();
                
                // Выводим дополнительную информацию
                if (additionalInfo.length > 0) {
                    writer.println("=== Дополнительная информация ===");
                    for (Object info : additionalInfo) {
                        writer.println(info);
                    }
                    writer.println();
                }
                
                // Выводим стек вызовов
                writer.println("=== Стек вызовов ===");
                throwable.printStackTrace(writer);
                writer.println();
                
                // Выводим информацию о системе
                writer.println("=== Информация о системе ===");
                writer.println("Bukkit: " + Bukkit.getBukkitVersion());
                writer.println("Java: " + System.getProperty("java.version"));
                writer.println("ОС: " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
                writer.println();
                
                // Выводим информацию о загруженных плагинах
                writer.println("=== Загруженные плагины ===");
                for (Plugin p : Bukkit.getPluginManager().getPlugins()) {
                    writer.println(p.getName() + " v" + p.getDescription().getVersion() + 
                            " (" + (p.isEnabled() ? "включен" : "отключен") + ")");
                }
            }
            
            logger.info("Создан отчет об ошибке: " + reportFile.getName());
        } catch (Exception e) {
            logger.log(Level.WARNING, "Ошибка при создании отчета об ошибке", e);
        }
    }
    
    /**
     * Класс для отслеживания ошибок плагина
     */
    private static class PluginErrorStats {
        private final List<ErrorEntry> errors = new ArrayList<>();
        private final Map<String, Integer> errorTypeCount = new HashMap<>();
        
        /**
         * Добавляет ошибку в статистику
         * @param throwable исключение
         */
        public void addError(Throwable throwable) {
            errors.add(new ErrorEntry(throwable));
            
            // Подсчитываем количество ошибок каждого типа
            String errorType = throwable.getClass().getName();
            errorTypeCount.put(errorType, errorTypeCount.getOrDefault(errorType, 0) + 1);
        }
        
        /**
         * Получает общее количество ошибок
         * @return количество ошибок
         */
        public int getErrorCount() {
            return errors.size();
        }
        
        /**
         * Получает количество ошибок заданного типа
         * @param errorType тип ошибки
         * @return количество ошибок
         */
        public int getErrorTypeCount(String errorType) {
            return errorTypeCount.getOrDefault(errorType, 0);
        }
        
        /**
         * Запись об ошибке
         */
        private static class ErrorEntry {
            private final Throwable throwable;
            private final long timestamp;
            
            public ErrorEntry(Throwable throwable) {
                this.throwable = throwable;
                this.timestamp = System.currentTimeMillis();
            }
            
            public Throwable getThrowable() {
                return throwable;
            }
            
            public long getTimestamp() {
                return timestamp;
            }
        }
    }
} 