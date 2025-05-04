package dev.flaymie.fcore.core.security;

import dev.flaymie.fcore.FCore;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Анализатор безопасности плагинов
 * Выполняет проверку плагинов на наличие потенциально опасного кода
 */
public class PluginSecurityAnalyzer {
    
    private final FCore plugin;
    private final Logger logger;
    
    // Шаблоны для поиска потенциально опасного кода
    private final List<Pattern> dangerousPatterns = Arrays.asList(
            Pattern.compile("java\\.lang\\.Runtime\\.getRuntime\\(\\)\\.exec"),
            Pattern.compile("java\\.io\\.File\\(\"/"),
            Pattern.compile("java\\.lang\\.System\\.exit"),
            Pattern.compile("java\\.net\\.URLClassLoader"),
            Pattern.compile("java\\.lang\\.reflect\\.Method\\.invoke"),
            Pattern.compile("org\\.bukkit\\.Bukkit\\.getPluginManager\\(\\)\\.disablePlugin")
    );
    
    // Опасные импорты
    private final List<String> dangerousImports = Arrays.asList(
            "java.lang.reflect.Method",
            "java.lang.reflect.Field",
            "sun.misc.Unsafe",
            "java.nio.channels.FileChannel",
            "java.lang.ProcessBuilder"
    );
    
    /**
     * Конструктор анализатора безопасности
     * @param plugin экземпляр FCore
     */
    public PluginSecurityAnalyzer(FCore plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }
    
    /**
     * Анализирует плагин на наличие потенциально опасного кода
     * @param bukkitPlugin плагин для анализа
     * @return список обнаруженных проблем безопасности
     */
    public List<SecurityIssue> analyzePlugin(Plugin bukkitPlugin) {
        List<SecurityIssue> issues = new ArrayList<>();
        
        try {
            // Получаем jar-файл плагина
            File pluginFile = getPluginFile(bukkitPlugin);
            if (pluginFile == null) {
                issues.add(new SecurityIssue(SecurityIssueType.UNKNOWN, "Не удалось найти файл плагина"));
                return issues;
            }
            
            // Анализируем jar-файл
            analyzeJarFile(pluginFile, issues);
            
            // Анализируем классы плагина
            analyzePluginClasses(bukkitPlugin, issues);
            
        } catch (Exception e) {
            logger.log(Level.WARNING, "Ошибка при анализе плагина " + bukkitPlugin.getName(), e);
            issues.add(new SecurityIssue(SecurityIssueType.EXCEPTION, "Ошибка при анализе: " + e.getMessage()));
        }
        
        return issues;
    }
    
    /**
     * Анализирует jar-файл плагина
     * @param pluginFile файл плагина
     * @param issues список для добавления обнаруженных проблем
     */
    private void analyzeJarFile(File pluginFile, List<SecurityIssue> issues) {
        try (JarFile jarFile = new JarFile(pluginFile)) {
            // Проверяем все классы в jar-файле
            jarFile.stream()
                    .filter(entry -> entry.getName().endsWith(".class"))
                    .forEach(entry -> analyzeClassFile(jarFile, entry, issues));
        } catch (IOException e) {
            logger.log(Level.WARNING, "Ошибка при анализе JAR-файла: " + e.getMessage(), e);
            issues.add(new SecurityIssue(SecurityIssueType.EXCEPTION, "Ошибка при чтении JAR: " + e.getMessage()));
        }
    }
    
    /**
     * Анализирует файл класса в jar
     * @param jarFile файл jar
     * @param entry запись класса
     * @param issues список для добавления обнаруженных проблем
     */
    private void analyzeClassFile(JarFile jarFile, JarEntry entry, List<SecurityIssue> issues) {
        try (InputStream is = jarFile.getInputStream(entry)) {
            byte[] classData = new byte[(int) entry.getSize()];
            is.read(classData);
            
            // Преобразуем в строку для простого анализа
            String classContent = new String(classData, "UTF-8");
            
            // Ищем опасные паттерны
            for (Pattern pattern : dangerousPatterns) {
                if (pattern.matcher(classContent).find()) {
                    issues.add(new SecurityIssue(
                            SecurityIssueType.DANGEROUS_CODE,
                            "Найден потенциально опасный код: " + pattern.pattern() + " в " + entry.getName()
                    ));
                }
            }
            
            // Ищем опасные импорты
            for (String dangerousImport : dangerousImports) {
                if (classContent.contains("import " + dangerousImport)) {
                    issues.add(new SecurityIssue(
                            SecurityIssueType.DANGEROUS_IMPORT,
                            "Найден потенциально опасный импорт: " + dangerousImport + " в " + entry.getName()
                    ));
                }
            }
            
        } catch (IOException e) {
            logger.log(Level.WARNING, "Ошибка при анализе класса " + entry.getName(), e);
        }
    }
    
    /**
     * Анализирует классы загруженного плагина
     * @param bukkitPlugin плагин для анализа
     * @param issues список для добавления обнаруженных проблем
     */
    private void analyzePluginClasses(Plugin bukkitPlugin, List<SecurityIssue> issues) {
        try {
            // Получаем класс плагина
            Class<?> pluginClass = bukkitPlugin.getClass();
            
            // Проверяем наличие опасных методов
            Method[] methods = pluginClass.getDeclaredMethods();
            for (Method method : methods) {
                if (isMethodPotentiallyDangerous(method)) {
                    issues.add(new SecurityIssue(
                            SecurityIssueType.DANGEROUS_METHOD,
                            "Найден потенциально опасный метод: " + method.getName() + " в " + pluginClass.getName()
                    ));
                }
            }
            
        } catch (Exception e) {
            logger.log(Level.WARNING, "Ошибка при анализе классов плагина " + bukkitPlugin.getName(), e);
            issues.add(new SecurityIssue(SecurityIssueType.EXCEPTION, "Ошибка при анализе классов: " + e.getMessage()));
        }
    }
    
    /**
     * Проверяет, является ли метод потенциально опасным
     * @param method метод для проверки
     * @return true, если метод потенциально опасен
     */
    private boolean isMethodPotentiallyDangerous(Method method) {
        // Простая эвристика для выявления подозрительных методов
        String methodName = method.getName().toLowerCase();
        return methodName.contains("exec") || 
               methodName.contains("system") || 
               methodName.contains("runtime") ||
               methodName.contains("process") ||
               methodName.contains("disable") ||
               methodName.contains("reflect");
    }
    
    /**
     * Получает файл плагина
     * @param plugin плагин
     * @return файл плагина или null, если не найден
     */
    private File getPluginFile(Plugin plugin) {
        try {
            File pluginsDir = Bukkit.getPluginManager().getPlugin("FCore").getDataFolder().getParentFile();
            File[] files = pluginsDir.listFiles((dir, name) -> name.endsWith(".jar"));
            
            if (files == null) return null;
            
            for (File file : files) {
                if (file.getName().toLowerCase().startsWith(plugin.getName().toLowerCase())) {
                    return file;
                }
            }
            
            return null;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Ошибка при поиске файла плагина " + plugin.getName(), e);
            return null;
        }
    }
    
    /**
     * Класс для хранения информации о проблеме безопасности
     */
    public static class SecurityIssue {
        private final SecurityIssueType type;
        private final String description;
        
        public SecurityIssue(SecurityIssueType type, String description) {
            this.type = type;
            this.description = description;
        }
        
        public SecurityIssueType getType() {
            return type;
        }
        
        public String getDescription() {
            return description;
        }
        
        @Override
        public String toString() {
            return "[" + type + "] " + description;
        }
    }
    
    /**
     * Типы проблем безопасности
     */
    public enum SecurityIssueType {
        DANGEROUS_CODE,
        DANGEROUS_IMPORT,
        DANGEROUS_METHOD,
        EXCEPTION,
        UNKNOWN
    }
} 