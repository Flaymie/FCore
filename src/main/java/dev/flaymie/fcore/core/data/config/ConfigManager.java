package dev.flaymie.fcore.core.data.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.flaymie.fcore.FCore;
import dev.flaymie.fcore.api.service.FCoreService;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Менеджер конфигураций с поддержкой аннотаций
 */
public class ConfigManager implements FCoreService {
    
    private final FCore plugin;
    private final Logger logger;
    private final Map<String, FileConfiguration> yamlConfigs;
    private final Map<String, Object> jsonConfigs;
    private final Gson gson;
    
    public ConfigManager(FCore plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.yamlConfigs = new HashMap<>();
        this.jsonConfigs = new HashMap<>();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }
    
    @Override
    public void onEnable() {
        logger.info("Загрузка менеджера конфигураций...");
    }
    
    @Override
    public void onDisable() {
        // Сохраняем все конфигурации
        saveAllConfigs();
    }
    
    @Override
    public String getName() {
        return "ConfigManager";
    }
    
    /**
     * Загружает конфигурацию на основе аннотаций класса
     * @param configClass класс с аннотациями
     * @param <T> тип конфигурации
     * @return объект конфигурации
     */
    public <T> T load(Class<T> configClass) {
        try {
            // Проверяем наличие аннотации
            if (!configClass.isAnnotationPresent(ConfigFile.class)) {
                throw new IllegalArgumentException("Class " + configClass.getName() + " must be annotated with @ConfigFile");
            }
            
            // Получаем параметры из аннотации
            ConfigFile annotation = configClass.getAnnotation(ConfigFile.class);
            String fileName = annotation.value();
            ConfigFormat format = annotation.format();
            
            // Создаем экземпляр конфигурации
            Constructor<T> constructor = configClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            T config = constructor.newInstance();
            
            // Загружаем данные в зависимости от формата
            switch (format) {
                case YAML:
                    loadYamlConfig(config, fileName);
                    break;
                case JSON:
                    loadJsonConfig(config, fileName);
                    break;
            }
            
            return config;
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            logger.log(Level.SEVERE, "Ошибка при загрузке конфигурации", e);
            throw new RuntimeException("Не удалось загрузить конфигурацию: " + e.getMessage(), e);
        }
    }
    
    /**
     * Загружает данные из YAML файла в объект
     * @param config объект конфигурации
     * @param fileName имя файла
     */
    private void loadYamlConfig(Object config, String fileName) {
        try {
            FileConfiguration yamlConfig = getYamlConfig(fileName);
            
            // Перебираем все поля класса
            for (Field field : config.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                
                // Проверяем наличие аннотации @ConfigValue
                if (field.isAnnotationPresent(ConfigValue.class)) {
                    ConfigValue valueAnnotation = field.getAnnotation(ConfigValue.class);
                    String path = valueAnnotation.value();
                    
                    // Если значение в конфиге существует, устанавливаем его
                    if (yamlConfig.contains(path)) {
                        Object value = yamlConfig.get(path);
                        field.set(config, convertValue(value, field.getType()));
                    } else {
                        // Если значения нет, устанавливаем значение по умолчанию в конфиг
                        Object defaultValue = field.get(config);
                        if (defaultValue != null) {
                            yamlConfig.set(path, defaultValue);
                        }
                    }
                }
                // Проверяем наличие аннотации @ConfigSection
                else if (field.isAnnotationPresent(ConfigSection.class)) {
                    ConfigSection sectionAnnotation = field.getAnnotation(ConfigSection.class);
                    String path = sectionAnnotation.value();
                    
                    // Создаем новый экземпляр для секции
                    Object sectionObj = field.getType().getDeclaredConstructor().newInstance();
                    field.set(config, sectionObj);
                    
                    // Если секция существует, загружаем значения рекурсивно
                    ConfigurationSection section = yamlConfig.getConfigurationSection(path);
                    if (section == null) {
                        section = yamlConfig.createSection(path);
                    }
                    
                    // Обрабатываем поля вложенного класса
                    for (Field sectionField : field.getType().getDeclaredFields()) {
                        sectionField.setAccessible(true);
                        
                        if (sectionField.isAnnotationPresent(ConfigValue.class)) {
                            ConfigValue valueAnnotation = sectionField.getAnnotation(ConfigValue.class);
                            String subPath = valueAnnotation.value();
                            String fullPath = path + "." + subPath;
                            
                            if (yamlConfig.contains(fullPath)) {
                                Object value = yamlConfig.get(fullPath);
                                sectionField.set(sectionObj, convertValue(value, sectionField.getType()));
                            } else {
                                Object defaultValue = sectionField.get(sectionObj);
                                if (defaultValue != null) {
                                    yamlConfig.set(fullPath, defaultValue);
                                }
                            }
                        }
                    }
                }
            }
            
            // Сохраняем изменения в файл
            yamlConfig.save(new File(plugin.getDataFolder(), fileName));
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ошибка при загрузке YAML конфигурации", e);
            throw new RuntimeException("Не удалось загрузить YAML конфигурацию: " + e.getMessage(), e);
        }
    }
    
    /**
     * Загружает данные из JSON файла в объект
     * @param config объект конфигурации
     * @param fileName имя файла
     */
    private void loadJsonConfig(Object config, String fileName) {
        try {
            File file = new File(plugin.getDataFolder(), fileName);
            
            // Если файл существует, загружаем данные
            if (file.exists()) {
                try (Reader reader = new FileReader(file)) {
                    // Создаем временный объект нужного класса из JSON
                    Object jsonConfig = gson.fromJson(reader, config.getClass());
                    
                    // Копируем значения полей
                    for (Field field : config.getClass().getDeclaredFields()) {
                        field.setAccessible(true);
                        Object value = field.get(jsonConfig);
                        if (value != null) {
                            field.set(config, value);
                        }
                    }
                    
                    // Сохраняем в кэш
                    jsonConfigs.put(fileName, config);
                }
            } else {
                // Если файла нет, создаем его со значениями по умолчанию
                saveJsonConfig(config, fileName);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ошибка при загрузке JSON конфигурации", e);
            throw new RuntimeException("Не удалось загрузить JSON конфигурацию: " + e.getMessage(), e);
        }
    }
    
    /**
     * Сохраняет объект в JSON файл
     * @param config объект конфигурации
     * @param fileName имя файла
     */
    private void saveJsonConfig(Object config, String fileName) {
        try {
            File file = new File(plugin.getDataFolder(), fileName);
            
            // Создаем директорию, если ее нет
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            
            // Сохраняем объект в файл
            try (Writer writer = new FileWriter(file)) {
                gson.toJson(config, writer);
            }
            
            // Кэшируем конфиг
            jsonConfigs.put(fileName, config);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ошибка при сохранении JSON конфигурации", e);
            throw new RuntimeException("Не удалось сохранить JSON конфигурацию: " + e.getMessage(), e);
        }
    }
    
    /**
     * Возвращает YAML конфигурацию из кэша или создает новую
     * @param fileName имя файла
     * @return объект FileConfiguration
     */
    public FileConfiguration getYamlConfig(String fileName) {
        // Проверяем, есть ли конфиг в кэше
        FileConfiguration config = yamlConfigs.get(fileName);
        if (config != null) {
            return config;
        }
        
        // Создаем новый файл, если он не существует
        File configFile = new File(plugin.getDataFolder(), fileName);
        if (!configFile.exists()) {
            // Пытаемся скопировать из ресурсов
            if (plugin.getResource(fileName) != null) {
                plugin.saveResource(fileName, false);
            } else {
                // Создаем пустой файл
                try {
                    configFile.getParentFile().mkdirs();
                    configFile.createNewFile();
                } catch (IOException e) {
                    logger.severe("Не удалось создать файл " + fileName + ": " + e.getMessage());
                }
            }
        }
        
        // Загружаем конфигурацию
        config = YamlConfiguration.loadConfiguration(configFile);
        yamlConfigs.put(fileName, config);
        
        return config;
    }
    
    /**
     * Сохраняет все загруженные конфигурации
     */
    public void saveAllConfigs() {
        // Сохраняем YAML конфиги
        for (Map.Entry<String, FileConfiguration> entry : yamlConfigs.entrySet()) {
            try {
                entry.getValue().save(new File(plugin.getDataFolder(), entry.getKey()));
            } catch (IOException e) {
                logger.severe("Не удалось сохранить конфигурацию " + entry.getKey() + ": " + e.getMessage());
            }
        }
        
        // Сохраняем JSON конфиги
        for (Map.Entry<String, Object> entry : jsonConfigs.entrySet()) {
            saveJsonConfig(entry.getValue(), entry.getKey());
        }
    }
    
    /**
     * Конвертирует значение в нужный тип
     * @param value исходное значение
     * @param targetType целевой тип
     * @return сконвертированное значение
     */
    private Object convertValue(Object value, Class<?> targetType) {
        if (value == null) return null;
        
        // Если типы совпадают, возвращаем как есть
        if (targetType.isInstance(value)) {
            return value;
        }
        
        // Конвертируем примитивные типы
        if (targetType == int.class || targetType == Integer.class) {
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
            return Integer.parseInt(value.toString());
        } else if (targetType == double.class || targetType == Double.class) {
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }
            return Double.parseDouble(value.toString());
        } else if (targetType == float.class || targetType == Float.class) {
            if (value instanceof Number) {
                return ((Number) value).floatValue();
            }
            return Float.parseFloat(value.toString());
        } else if (targetType == long.class || targetType == Long.class) {
            if (value instanceof Number) {
                return ((Number) value).longValue();
            }
            return Long.parseLong(value.toString());
        } else if (targetType == boolean.class || targetType == Boolean.class) {
            if (value instanceof Boolean) {
                return value;
            }
            return Boolean.parseBoolean(value.toString());
        } else if (targetType == String.class) {
            return value.toString();
        }
        
        // Если не удалось сконвертировать, возвращаем null
        return null;
    }
    
    /**
     * Сохраняет конфигурацию в файл
     * @param config объект конфигурации
     */
    public void save(Object config) {
        try {
            // Проверяем наличие аннотации
            Class<?> configClass = config.getClass();
            if (!configClass.isAnnotationPresent(ConfigFile.class)) {
                throw new IllegalArgumentException("Class " + configClass.getName() + " must be annotated with @ConfigFile");
            }
            
            // Получаем параметры из аннотации
            ConfigFile annotation = configClass.getAnnotation(ConfigFile.class);
            String fileName = annotation.value();
            ConfigFormat format = annotation.format();
            
            // Сохраняем в зависимости от формата
            switch (format) {
                case YAML:
                    saveYamlConfig(config, fileName);
                    break;
                case JSON:
                    saveJsonConfig(config, fileName);
                    break;
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ошибка при сохранении конфигурации", e);
            throw new RuntimeException("Не удалось сохранить конфигурацию: " + e.getMessage(), e);
        }
    }
    
    /**
     * Сохраняет объект в YAML файл
     * @param config объект конфигурации
     * @param fileName имя файла
     */
    private void saveYamlConfig(Object config, String fileName) {
        try {
            FileConfiguration yamlConfig = getYamlConfig(fileName);
            
            // Перебираем все поля класса
            for (Field field : config.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                
                // Проверяем наличие аннотации @ConfigValue
                if (field.isAnnotationPresent(ConfigValue.class)) {
                    ConfigValue valueAnnotation = field.getAnnotation(ConfigValue.class);
                    String path = valueAnnotation.value();
                    
                    // Получаем значение поля
                    Object value = field.get(config);
                    if (value != null) {
                        yamlConfig.set(path, value);
                    }
                }
                // Проверяем наличие аннотации @ConfigSection
                else if (field.isAnnotationPresent(ConfigSection.class)) {
                    ConfigSection sectionAnnotation = field.getAnnotation(ConfigSection.class);
                    String path = sectionAnnotation.value();
                    
                    // Получаем объект секции
                    Object sectionObj = field.get(config);
                    if (sectionObj != null) {
                        // Обрабатываем поля вложенного класса
                        for (Field sectionField : field.getType().getDeclaredFields()) {
                            sectionField.setAccessible(true);
                            
                            if (sectionField.isAnnotationPresent(ConfigValue.class)) {
                                ConfigValue valueAnnotation = sectionField.getAnnotation(ConfigValue.class);
                                String subPath = valueAnnotation.value();
                                String fullPath = path + "." + subPath;
                                
                                Object value = sectionField.get(sectionObj);
                                if (value != null) {
                                    yamlConfig.set(fullPath, value);
                                }
                            }
                        }
                    }
                }
            }
            
            // Сохраняем изменения в файл
            yamlConfig.save(new File(plugin.getDataFolder(), fileName));
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ошибка при сохранении YAML конфигурации", e);
            throw new RuntimeException("Не удалось сохранить YAML конфигурацию: " + e.getMessage(), e);
        }
    }
} 