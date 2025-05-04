package dev.flaymie.fcore.core.debug;

import dev.flaymie.fcore.FCore;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * Отслеживает изменения в файлах для автоматической перезагрузки плагина
 */
public class FileWatcher {
    private final FCore plugin;
    private final File pluginFolder;
    private final Map<String, Long> lastModifiedTimes;
    private WatchService watchService;
    private ScheduledExecutorService executor;
    private boolean running;
    
    public FileWatcher(FCore plugin, File pluginFolder) {
        this.plugin = plugin;
        this.pluginFolder = pluginFolder;
        this.lastModifiedTimes = new HashMap<>();
        this.running = false;
    }
    
    /**
     * Начать отслеживание файлов
     */
    public void startWatching() {
        if (running) {
            return;
        }
        
        try {
            // Создаем сервис для отслеживания файлов
            watchService = FileSystems.getDefault().newWatchService();
            
            // Регистрируем директорию плагина для отслеживания
            Path dir = pluginFolder.toPath();
            dir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, 
                    StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
            
            // Записываем начальное время изменения файлов
            scanInitialFiles(pluginFolder);
            
            // Запускаем поток для проверки изменений
            executor = Executors.newSingleThreadScheduledExecutor();
            executor.scheduleAtFixedRate(this::checkForChanges, 0, 2, TimeUnit.SECONDS);
            
            running = true;
            plugin.getLogger().info("Начато отслеживание файлов в директории: " + pluginFolder.getAbsolutePath());
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка при запуске отслеживания файлов", e);
        }
    }
    
    /**
     * Остановить отслеживание файлов
     */
    public void stopWatching() {
        if (!running) {
            return;
        }
        
        try {
            if (executor != null) {
                executor.shutdown();
                executor = null;
            }
            
            if (watchService != null) {
                watchService.close();
                watchService = null;
            }
            
            running = false;
            plugin.getLogger().info("Отслеживание файлов остановлено");
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка при остановке отслеживания файлов", e);
        }
    }
    
    /**
     * Сканирует все файлы в директории и записывает их время изменения
     * @param directory директория для сканирования
     */
    private void scanInitialFiles(File directory) {
        if (!directory.isDirectory()) {
            return;
        }
        
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }
        
        for (File file : files) {
            if (file.isDirectory()) {
                scanInitialFiles(file);
            } else if (isWatchableFile(file)) {
                lastModifiedTimes.put(file.getAbsolutePath(), file.lastModified());
            }
        }
    }
    
    /**
     * Проверяет, является ли файл отслеживаемым
     * @param file файл для проверки
     * @return true, если файл отслеживается
     */
    private boolean isWatchableFile(File file) {
        String name = file.getName().toLowerCase();
        // Отслеживаем только yaml, json и jar файлы
        return name.endsWith(".yml") || name.endsWith(".yaml") || 
               name.endsWith(".json") || name.endsWith(".class");
    }
    
    /**
     * Проверяет изменения в отслеживаемых файлах
     */
    private void checkForChanges() {
        try {
            boolean hasChanges = false;
            
            // Проверяем события в watchService
            WatchKey key = watchService.poll();
            if (key != null) {
                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    
                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        continue;
                    }
                    
                    // Получаем имя измененного файла
                    @SuppressWarnings("unchecked")
                    WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;
                    Path filename = pathEvent.context();
                    
                    // Формируем полный путь
                    Path fullPath = pluginFolder.toPath().resolve(filename);
                    File file = fullPath.toFile();
                    
                    if (!isWatchableFile(file)) {
                        continue;
                    }
                    
                    // Проверяем, изменился ли файл
                    String path = file.getAbsolutePath();
                    long lastModified = file.lastModified();
                    Long oldLastModified = lastModifiedTimes.get(path);
                    
                    if (oldLastModified == null || lastModified > oldLastModified) {
                        lastModifiedTimes.put(path, lastModified);
                        plugin.getLogger().info("Обнаружено изменение файла: " + file.getName());
                        hasChanges = true;
                    }
                }
                
                // Сбрасываем ключ для дальнейшего использования
                key.reset();
            }
            
            // Проверяем все известные файлы на изменения
            for (Map.Entry<String, Long> entry : new HashMap<>(lastModifiedTimes).entrySet()) {
                File file = new File(entry.getKey());
                if (file.exists() && file.lastModified() > entry.getValue()) {
                    lastModifiedTimes.put(entry.getKey(), file.lastModified());
                    plugin.getLogger().info("Обнаружено изменение файла: " + file.getName());
                    hasChanges = true;
                }
            }
            
            // Если есть изменения, перезагружаем плагин
            if (hasChanges) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    plugin.getLogger().info("Перезагрузка плагина из-за изменений файлов...");
                    Bukkit.getPluginManager().disablePlugin(plugin);
                    Bukkit.getPluginManager().enablePlugin(plugin);
                });
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка при проверке изменений файлов", e);
        }
    }
    
    /**
     * Проверяет, запущено ли отслеживание
     * @return true, если отслеживание запущено
     */
    public boolean isRunning() {
        return running;
    }
} 