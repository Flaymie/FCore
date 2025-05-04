package dev.flaymie.fcore.core.event;

import dev.flaymie.fcore.FCore;
import dev.flaymie.fcore.api.annotation.EventListener;
import dev.flaymie.fcore.api.annotation.Service;
import dev.flaymie.fcore.api.service.FCoreService;
import dev.flaymie.fcore.core.di.DependencyContainer;
import org.bukkit.event.Listener;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

/**
 * Сканер для автоматической регистрации слушателей событий
 */
public class EventListenerScanner implements FCoreService {

    private final FCore plugin;
    private final Logger logger;
    private final EventManager eventManager;
    private final DependencyContainer dependencyContainer;
    private final String basePackage = "dev.flaymie.fcore";
    
    public EventListenerScanner(FCore plugin, EventManager eventManager, DependencyContainer dependencyContainer) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.eventManager = eventManager;
        this.dependencyContainer = dependencyContainer;
    }
    
    @Override
    public void onEnable() {
        try {
            scanListeners();
        } catch (Exception e) {
            logger.severe("Ошибка при сканировании слушателей: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public void onDisable() {
        // Слушатели будут отключены в EventManager
    }
    
    @Override
    public String getName() {
        return "EventListenerScanner";
    }
    
    /**
     * Сканирует классы в базовом пакете и регистрирует слушатели
     */
    private void scanListeners() throws Exception {
        logger.info("Сканирование слушателей в пакете " + basePackage + "...");
        
        List<Class<?>> classes = findClasses(basePackage);
        List<Class<?>> listenerClasses = new ArrayList<>();
        
        // Ищем классы, помеченные аннотацией @EventListener
        for (Class<?> clazz : classes) {
            if (clazz.isAnnotationPresent(EventListener.class) && 
                Listener.class.isAssignableFrom(clazz)) {
                    
                listenerClasses.add(clazz);
            }
        }
        
        logger.info("Найдено " + listenerClasses.size() + " слушателей");
        
        // Сортируем слушатели по приоритету
        listenerClasses.sort(Comparator.comparingInt(clazz -> 
            -clazz.getAnnotation(EventListener.class).priority()));
        
        // Регистрируем найденные слушатели
        for (Class<?> listenerClass : listenerClasses) {
            try {
                EventListener annotation = listenerClass.getAnnotation(EventListener.class);
                
                // Проверяем флаг автоматической регистрации
                if (!annotation.autoRegister()) {
                    logger.info("Слушатель " + listenerClass.getName() + 
                                " помечен для ручной регистрации");
                    continue;
                }
                
                registerListener(listenerClass);
            } catch (Exception e) {
                logger.severe("Ошибка при регистрации слушателя " + 
                              listenerClass.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Регистрирует экземпляр слушателя
     * @param listenerClass класс слушателя
     */
    private void registerListener(Class<?> listenerClass) throws Exception {
        // Проверяем, реализует ли класс интерфейс Listener
        if (!Listener.class.isAssignableFrom(listenerClass)) {
            logger.warning("Класс " + listenerClass.getName() + 
                          " помечен как @EventListener, но не реализует Listener");
            return;
        }
        
        // Проверяем, можно ли создать экземпляр (класс не абстрактный)
        if (java.lang.reflect.Modifier.isAbstract(listenerClass.getModifiers())) {
            logger.warning("Класс " + listenerClass.getName() + 
                          " помечен как @EventListener, но является абстрактным");
            return;
        }
        
        // Создаем экземпляр слушателя
        Listener listener;
        try {
            // Если класс помечен как @Service, получаем его из DI
            if (listenerClass.isAnnotationPresent(Service.class)) {
                listener = (Listener) dependencyContainer.getInstance(listenerClass);
                if (listener == null) {
                    throw new IllegalStateException("Не удалось получить слушатель из DI");
                }
            } else {
                // Пытаемся создать через конструктор с параметром FCore
                try {
                    listener = (Listener) listenerClass.getDeclaredConstructor(FCore.class)
                                                     .newInstance(plugin);
                } catch (NoSuchMethodException e) {
                    // Если нет конструктора с FCore, используем конструктор по умолчанию
                    listener = (Listener) listenerClass.getDeclaredConstructor().newInstance();
                }
                
                // Внедряем зависимости
                dependencyContainer.injectDependencies(listener);
            }
            
            // Регистрируем слушателя
            eventManager.registerListener(listener);
            
        } catch (Exception e) {
            logger.severe("Не удалось создать экземпляр слушателя " + 
                          listenerClass.getName() + ": " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Находит все классы в указанном пакете
     */
    private List<Class<?>> findClasses(String packageName) throws Exception {
        List<Class<?>> classes = new ArrayList<>();
        
        try {
            File jarFile = new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
            
            if (jarFile.isFile()) {
                // Если работаем из JAR-файла
                classes.addAll(findClassesInJar(jarFile, packageName));
            } else {
                // Если работаем из IDE
                String path = packageName.replace('.', '/');
                Enumeration<URL> resources = getClass().getClassLoader().getResources(path);
                
                while (resources.hasMoreElements()) {
                    URL resource = resources.nextElement();
                    File directory = new File(resource.getFile());
                    classes.addAll(findClassesInDirectory(directory, packageName));
                }
            }
        } catch (Exception e) {
            logger.severe("Ошибка при поиске классов: " + e.getMessage());
            throw e;
        }
        
        return classes;
    }
    
    /**
     * Находит классы в JAR-файле
     */
    private List<Class<?>> findClassesInJar(File jarFile, String packageName) throws IOException {
        List<Class<?>> classes = new ArrayList<>();
        String packagePath = packageName.replace('.', '/');
        
        try (JarFile jar = new JarFile(jarFile)) {
            Enumeration<JarEntry> entries = jar.entries();
            
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                
                // Проверяем, относится ли файл к нужному пакету и является ли он .class файлом
                if (name.startsWith(packagePath) && name.endsWith(".class")) {
                    String className = name.replace('/', '.').substring(0, name.length() - 6);
                    
                    try {
                        Class<?> clazz = Class.forName(className);
                        classes.add(clazz);
                    } catch (ClassNotFoundException e) {
                        logger.warning("Не удалось загрузить класс " + className + ": " + e.getMessage());
                    }
                }
            }
        }
        
        return classes;
    }
    
    /**
     * Находит классы в директории
     */
    private List<Class<?>> findClassesInDirectory(File directory, String packageName) {
        List<Class<?>> classes = new ArrayList<>();
        
        if (!directory.exists()) {
            return classes;
        }
        
        File[] files = directory.listFiles();
        if (files == null) {
            return classes;
        }
        
        for (File file : files) {
            if (file.isDirectory()) {
                // Рекурсивно ищем классы в поддиректориях
                classes.addAll(findClassesInDirectory(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                // Загружаем .class файл
                String className = packageName + "." + file.getName().substring(0, file.getName().length() - 6);
                
                try {
                    Class<?> clazz = Class.forName(className);
                    classes.add(clazz);
                } catch (ClassNotFoundException e) {
                    logger.warning("Не удалось загрузить класс " + className + ": " + e.getMessage());
                }
            }
        }
        
        return classes;
    }
} 