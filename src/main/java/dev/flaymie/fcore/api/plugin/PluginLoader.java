package dev.flaymie.fcore.api.plugin;

import dev.flaymie.fcore.FCore;
import dev.flaymie.fcore.api.service.FCoreService;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Загрузчик плагинов FCore
 */
public class PluginLoader implements FCoreService {
    
    private final FCore plugin;
    private final Logger logger;
    private final File pluginsDir;
    private final Map<String, FCorePlugin> plugins;
    private final Map<String, PluginDescription> descriptions;
    
    public PluginLoader(FCore plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.pluginsDir = new File(plugin.getDataFolder(), "plugins");
        this.plugins = new HashMap<>();
        this.descriptions = new HashMap<>();
    }
    
    @Override
    public void onEnable() {
        // Создаем директорию для плагинов, если ее нет
        if (!pluginsDir.exists()) {
            pluginsDir.mkdirs();
        }
        
        // Загружаем плагины
        loadPlugins();
        
        // Включаем плагины в порядке зависимостей
        enablePlugins();
        
        logger.info("Загрузчик плагинов инициализирован");
    }
    
    @Override
    public void onDisable() {
        // Отключаем плагины в обратном порядке
        for (FCorePlugin fcorePlugin : new ArrayList<>(plugins.values())) {
            disablePlugin(fcorePlugin.getName());
        }
        
        plugins.clear();
        descriptions.clear();
        
        logger.info("Плагины отключены");
    }
    
    @Override
    public String getName() {
        return "PluginLoader";
    }
    
    /**
     * Загружает все плагины из директории plugins
     */
    private void loadPlugins() {
        File[] files = pluginsDir.listFiles((dir, name) -> name.endsWith(".jar"));
        
        if (files == null || files.length == 0) {
            logger.info("Плагины не найдены");
            return;
        }
        
        logger.info("Найдено " + files.length + " плагинов для загрузки");
        
        // Сначала загружаем описания всех плагинов
        for (File file : files) {
            try {
                PluginDescription description = loadPluginDescription(file);
                if (description != null) {
                    descriptions.put(description.getName(), description);
                    logger.info("Загружено описание плагина: " + description.getName() + " v" + description.getVersion());
                }
            } catch (Exception e) {
                logger.severe("Ошибка при загрузке описания плагина " + file.getName() + ": " + e.getMessage());
            }
        }
        
        // Сортируем плагины по зависимостям
        List<String> sortedPlugins = sortPluginsByDependencies();
        
        // Загружаем плагины в правильном порядке
        for (String pluginName : sortedPlugins) {
            PluginDescription description = descriptions.get(pluginName);
            File file = getPluginFile(pluginName);
            
            if (file != null) {
                try {
                    loadPlugin(file, description);
                } catch (Exception e) {
                    logger.severe("Ошибка при загрузке плагина " + pluginName + ": " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Загружает описание плагина из jar-файла
     * @param file jar-файл плагина
     * @return описание плагина или null, если не удалось загрузить
     */
    private PluginDescription loadPluginDescription(File file) {
        try (JarFile jarFile = new JarFile(file)) {
            JarEntry entry = jarFile.getJarEntry("plugin.yml");
            
            if (entry == null) {
                logger.warning("Файл plugin.yml не найден в " + file.getName());
                return null;
            }
            
            // Читаем plugin.yml
            Properties props = new Properties();
            props.load(jarFile.getInputStream(entry));
            
            // Извлекаем информацию о плагине
            String name = props.getProperty("name");
            String version = props.getProperty("version", "1.0");
            String main = props.getProperty("main");
            String description = props.getProperty("description", "");
            String author = props.getProperty("author", "");
            String dependsStr = props.getProperty("depends", "");
            
            if (name == null || main == null) {
                logger.warning("Не указаны обязательные поля name или main в plugin.yml (" + file.getName() + ")");
                return null;
            }
            
            // Парсим зависимости
            List<String> depends = new ArrayList<>();
            if (!dependsStr.isEmpty()) {
                depends = Arrays.asList(dependsStr.split(","));
            }
            
            return new PluginDescription(name, version, main, description, author, depends);
        } catch (IOException e) {
            logger.severe("Ошибка при чтении plugin.yml из " + file.getName() + ": " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Загружает и инициализирует плагин
     * @param file jar-файл плагина
     * @param description описание плагина
     */
    private void loadPlugin(File file, PluginDescription description) {
        try {
            // Создаем класслоадер для плагина
            URLClassLoader classLoader = new URLClassLoader(
                    new URL[]{file.toURI().toURL()},
                    getClass().getClassLoader()
            );
            
            // Загружаем главный класс плагина
            Class<?> pluginClass = classLoader.loadClass(description.getMain());
            
            // Проверяем, что класс расширяет FCorePlugin
            if (!FCorePlugin.class.isAssignableFrom(pluginClass)) {
                logger.severe("Главный класс плагина " + description.getName() + " не расширяет FCorePlugin");
                return;
            }
            
            // Создаем экземпляр плагина
            Constructor<?> constructor = pluginClass.getConstructor(FCore.class, String.class, String.class);
            FCorePlugin fcorePlugin = (FCorePlugin) constructor.newInstance(plugin, description.getName(), description.getVersion());
            
            // Загружаем плагин
            fcorePlugin.onLoad();
            
            // Добавляем плагин в список
            plugins.put(description.getName(), fcorePlugin);
            
            logger.info("Плагин " + description.getName() + " v" + description.getVersion() + " загружен");
        } catch (Exception e) {
            logger.severe("Ошибка при загрузке плагина " + description.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Сортирует плагины по зависимостям, чтобы зависимости загружались раньше
     * @return отсортированный список имен плагинов
     */
    private List<String> sortPluginsByDependencies() {
        // Строим граф зависимостей
        Map<String, List<String>> dependencyGraph = new HashMap<>();
        
        for (PluginDescription description : descriptions.values()) {
            dependencyGraph.put(description.getName(), description.getDepends());
        }
        
        // Выполняем топологическую сортировку
        List<String> sorted = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Set<String> visiting = new HashSet<>();
        
        for (String plugin : dependencyGraph.keySet()) {
            if (!visited.contains(plugin)) {
                topologicalSort(plugin, dependencyGraph, visited, visiting, sorted);
            }
        }
        
        // Возвращаем отсортированный список в обратном порядке
        Collections.reverse(sorted);
        return sorted;
    }
    
    /**
     * Рекурсивная функция для топологической сортировки графа зависимостей
     */
    private void topologicalSort(String plugin, Map<String, List<String>> graph, Set<String> visited,
                                 Set<String> visiting, List<String> sorted) {
        visiting.add(plugin);
        
        for (String dependency : graph.getOrDefault(plugin, Collections.emptyList())) {
            // Проверяем, существует ли зависимость
            if (!graph.containsKey(dependency)) {
                logger.warning("Зависимость " + dependency + " для плагина " + plugin + " не найдена");
                continue;
            }
            
            // Проверяем циклическую зависимость
            if (visiting.contains(dependency)) {
                logger.severe("Обнаружена циклическая зависимость между плагинами: " + plugin + " и " + dependency);
                continue;
            }
            
            if (!visited.contains(dependency)) {
                topologicalSort(dependency, graph, visited, visiting, sorted);
            }
        }
        
        visiting.remove(plugin);
        visited.add(plugin);
        sorted.add(plugin);
    }
    
    /**
     * Находит jar-файл плагина по имени
     * @param pluginName имя плагина
     * @return jar-файл плагина или null, если не найден
     */
    private File getPluginFile(String pluginName) {
        File[] files = pluginsDir.listFiles((dir, name) -> name.endsWith(".jar"));
        
        if (files == null) {
            return null;
        }
        
        for (File file : files) {
            try (JarFile jarFile = new JarFile(file)) {
                JarEntry entry = jarFile.getJarEntry("plugin.yml");
                
                if (entry == null) {
                    continue;
                }
                
                Properties props = new Properties();
                props.load(jarFile.getInputStream(entry));
                
                String name = props.getProperty("name");
                if (pluginName.equals(name)) {
                    return file;
                }
            } catch (IOException ignored) {
            }
        }
        
        return null;
    }
    
    /**
     * Включает все загруженные плагины
     */
    private void enablePlugins() {
        for (FCorePlugin plugin : plugins.values()) {
            enablePlugin(plugin.getName());
        }
    }
    
    /**
     * Включает плагин по имени
     * @param pluginName имя плагина
     * @return true, если плагин успешно включен
     */
    public boolean enablePlugin(String pluginName) {
        FCorePlugin fcorePlugin = plugins.get(pluginName);
        
        if (fcorePlugin == null) {
            logger.warning("Плагин " + pluginName + " не найден");
            return false;
        }
        
        if (fcorePlugin.isEnabled()) {
            return true;
        }
        
        // Включаем зависимости
        PluginDescription description = descriptions.get(pluginName);
        if (description != null) {
            for (String dependency : description.getDepends()) {
                if (!enablePlugin(dependency)) {
                    logger.severe("Не удалось включить зависимость " + dependency + " для плагина " + pluginName);
                    return false;
                }
            }
        }
        
        try {
            fcorePlugin.enable();
            return true;
        } catch (Exception e) {
            logger.severe("Ошибка при включении плагина " + pluginName + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Отключает плагин по имени
     * @param pluginName имя плагина
     * @return true, если плагин успешно отключен
     */
    public boolean disablePlugin(String pluginName) {
        FCorePlugin fcorePlugin = plugins.get(pluginName);
        
        if (fcorePlugin == null) {
            return false;
        }
        
        if (!fcorePlugin.isEnabled()) {
            return true;
        }
        
        try {
            fcorePlugin.disable();
            return true;
        } catch (Exception e) {
            logger.severe("Ошибка при отключении плагина " + pluginName + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Получает плагин по имени
     * @param pluginName имя плагина
     * @return экземпляр плагина или null, если не найден
     */
    public FCorePlugin getPlugin(String pluginName) {
        return plugins.get(pluginName);
    }
    
    /**
     * Получает список всех загруженных плагинов
     * @return список плагинов
     */
    public List<FCorePlugin> getPlugins() {
        return new ArrayList<>(plugins.values());
    }
    
    /**
     * Проверяет, загружен ли плагин
     * @param pluginName имя плагина
     * @return true, если плагин загружен
     */
    public boolean isPluginLoaded(String pluginName) {
        return plugins.containsKey(pluginName);
    }
    
    /**
     * Проверяет, включен ли плагин
     * @param pluginName имя плагина
     * @return true, если плагин включен
     */
    public boolean isPluginEnabled(String pluginName) {
        FCorePlugin fcorePlugin = plugins.get(pluginName);
        return fcorePlugin != null && fcorePlugin.isEnabled();
    }
    
    /**
     * Получает список включенных плагинов
     * @return список включенных плагинов
     */
    public List<FCorePlugin> getEnabledPlugins() {
        return plugins.values().stream()
                .filter(FCorePlugin::isEnabled)
                .collect(Collectors.toList());
    }
} 