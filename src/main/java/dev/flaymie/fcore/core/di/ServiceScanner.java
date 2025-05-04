package dev.flaymie.fcore.core.di;

import dev.flaymie.fcore.FCore;
import dev.flaymie.fcore.api.annotation.PostConstruct;
import dev.flaymie.fcore.api.annotation.PreDestroy;
import dev.flaymie.fcore.api.annotation.Service;
import dev.flaymie.fcore.api.service.FCoreService;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Сканер для поиска классов с аннотацией @Service
 */
public class ServiceScanner implements FCoreService {

    private final FCore plugin;
    private final Logger logger;
    private final DependencyContainer dependencyContainer;
    private final String basePackage = "dev.flaymie.fcore";
    // Список lazy-сервисов, которые нужно инициализировать при запросе
    private final List<Class<?>> lazyServices = new ArrayList<>();

    public ServiceScanner(FCore plugin, DependencyContainer dependencyContainer) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.dependencyContainer = dependencyContainer;
    }

    @Override
    public void onEnable() {
        try {
            // Сканируем классы в базовом пакете
            scanPackage();
        } catch (Exception e) {
            logger.severe("Ошибка при сканировании сервисов: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        // При отключении очищаем список lazy-сервисов
        lazyServices.clear();
    }

    @Override
    public String getName() {
        return "ServiceScanner";
    }

    /**
     * Сканирует пакет на наличие классов с аннотацией @Service
     */
    private void scanPackage() throws Exception {
        logger.info("Сканирование классов в пакете " + basePackage + "...");
        
        List<Class<?>> classes = findClasses(basePackage);
        List<Class<?>> serviceClasses = new ArrayList<>();
        
        // Ищем классы с аннотацией @Service
        for (Class<?> clazz : classes) {
            if (clazz.isAnnotationPresent(Service.class)) {
                serviceClasses.add(clazz);
            }
        }
        
        logger.info("Найдено " + serviceClasses.size() + " сервисов");
        
        // Сортируем сервисы по приоритету
        serviceClasses.sort(Comparator.comparingInt(clazz -> 
            -clazz.getAnnotation(Service.class).priority()));
        
        // Регистрируем найденные сервисы
        for (Class<?> serviceClass : serviceClasses) {
            try {
                Service annotation = serviceClass.getAnnotation(Service.class);
                
                // Проверяем, является ли сервис lazy-loaded
                if (annotation.lazy()) {
                    registerLazyService(serviceClass);
                } else {
                    registerService(serviceClass);
                }
            } catch (Exception e) {
                logger.severe("Ошибка при регистрации сервиса " + serviceClass.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Регистрирует lazy-сервис
     */
    private void registerLazyService(Class<?> serviceClass) {
        Service annotation = serviceClass.getAnnotation(Service.class);
        String serviceName = annotation.value().isEmpty() ? 
            serviceClass.getSimpleName() : annotation.value();
        
        // Устанавливаем скоуп для сервиса
        dependencyContainer.setScope(serviceClass, annotation.scope());
        
        // Добавляем в список lazy-сервисов
        lazyServices.add(serviceClass);
        
        logger.info("Lazy-сервис " + serviceName + " зарегистрирован (будет создан при первом запросе)");
    }
    
    /**
     * Регистрирует сервис в контейнере зависимостей
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void registerService(Class<?> serviceClass) throws Exception {
        Service annotation = serviceClass.getAnnotation(Service.class);
        String serviceName = annotation.value().isEmpty() ? 
            serviceClass.getSimpleName() : annotation.value();
        
        // Устанавливаем скоуп для сервиса
        dependencyContainer.setScope(serviceClass, annotation.scope());
        
        // Проверяем, реализует ли класс интерфейс FCoreService
        if (FCoreService.class.isAssignableFrom(serviceClass)) {
            // Создаем экземпляр сервиса
            FCoreService service;
            try {
                // Пытаемся создать через конструктор с параметром FCore
                service = (FCoreService) serviceClass.getDeclaredConstructor(FCore.class).newInstance(plugin);
            } catch (NoSuchMethodException e) {
                // Если нет конструктора с FCore, используем конструктор по умолчанию
                service = (FCoreService) serviceClass.getDeclaredConstructor().newInstance();
            }
            
            // Внедряем зависимости
            dependencyContainer.injectDependencies(service);
            
            // Вызываем методы с аннотацией @PostConstruct
            invokePostConstructMethods(service);
            
            // Регистрируем сервис в менеджере сервисов
            plugin.getServiceManager().registerService((Class) serviceClass, service);
            
            // Регистрируем сервис как синглтон в контейнере зависимостей
            dependencyContainer.registerSingleton((Class<Object>) serviceClass, service);
            
            logger.info("Сервис " + serviceName + " зарегистрирован и инициализирован");
        } else {
            // Для обычных сервисов (не FCoreService) создаем экземпляр и регистрируем в DI
            Object instance;
            try {
                // Пытаемся создать через конструктор с параметром FCore
                instance = serviceClass.getDeclaredConstructor(FCore.class).newInstance(plugin);
            } catch (NoSuchMethodException e) {
                // Если нет конструктора с FCore, используем конструктор по умолчанию
                instance = serviceClass.getDeclaredConstructor().newInstance();
            }
            
            // Внедряем зависимости
            dependencyContainer.injectDependencies(instance);
            
            // Вызываем методы с аннотацией @PostConstruct
            invokePostConstructMethods(instance);
            
            // Регистрируем сервис в контейнере зависимостей
            dependencyContainer.registerSingleton((Class<Object>) serviceClass, instance);
            
            logger.info("Сервис " + serviceName + " зарегистрирован");
        }
    }
    
    /**
     * Вызывает методы с аннотацией @PostConstruct
     */
    private void invokePostConstructMethods(Object instance) {
        Class<?> targetClass = instance.getClass();
        
        // Собираем все методы с аннотацией @PostConstruct
        List<Method> methods = new ArrayList<>();
        Class<?> currentClass = targetClass;
        
        // Собираем методы, включая методы из суперклассов
        while (currentClass != null && currentClass != Object.class) {
            for (Method method : currentClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(PostConstruct.class)) {
                    methods.add(method);
                }
            }
            currentClass = currentClass.getSuperclass();
        }
        
        // Сортируем методы по приоритету
        methods.sort(Comparator.comparingInt(m -> 
            -m.getAnnotation(PostConstruct.class).priority()));
        
        // Вызываем методы
        for (Method method : methods) {
            try {
                method.setAccessible(true);
                method.invoke(instance);
                logger.fine("Вызван метод @PostConstruct: " + 
                          targetClass.getName() + "." + method.getName());
            } catch (Exception e) {
                logger.severe("Ошибка при вызове @PostConstruct метода " +
                             targetClass.getName() + "." + method.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Возвращает lazy-сервис, создавая его при необходимости
     */
    public Object getLazyService(Class<?> serviceClass) {
        if (lazyServices.contains(serviceClass)) {
            try {
                return dependencyContainer.getInstance(serviceClass);
            } catch (Exception e) {
                logger.severe("Ошибка при получении lazy-сервиса " + serviceClass.getName() + ": " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        }
        return null;
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