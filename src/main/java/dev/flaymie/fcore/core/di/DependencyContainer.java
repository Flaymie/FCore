package dev.flaymie.fcore.core.di;

import dev.flaymie.fcore.FCore;
import dev.flaymie.fcore.api.annotation.*;
import dev.flaymie.fcore.api.service.FCoreService;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Контейнер зависимостей, управляющий созданием и внедрением сервисов
 */
public class DependencyContainer implements FCoreService {
    
    private final FCore plugin;
    private final Logger logger;
    // Карта синглтонов: класс -> экземпляр
    private final Map<Class<?>, Object> singletons;
    // Карта прототипов: класс -> фабрика
    private final Map<Class<?>, InstanceFactory<?>> factories;
    // Карта биндингов: интерфейс -> реализация
    private final Map<Class<?>, Class<?>> bindings;
    // Карта скоупов: класс -> скоуп
    private final Map<Class<?>, InjectionScope> scopes;
    // Карта сессионных объектов: ключ сессии + класс -> экземпляр
    private final Map<String, Map<Class<?>, Object>> sessionInstances;
    // Стек создаваемых экземпляров для отслеживания циклических зависимостей
    private final ThreadLocal<Set<Class<?>>> creationStack;
    
    public DependencyContainer(FCore plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.singletons = new ConcurrentHashMap<>();
        this.factories = new ConcurrentHashMap<>();
        this.bindings = new ConcurrentHashMap<>();
        this.scopes = new ConcurrentHashMap<>();
        this.sessionInstances = new ConcurrentHashMap<>();
        this.creationStack = ThreadLocal.withInitial(HashSet::new);
    }
    
    @Override
    public void onEnable() {
        logger.info("Контейнер зависимостей инициализирован");
    }
    
    @Override
    public void onDisable() {
        // Вызываем методы с аннотацией @PreDestroy для всех синглтонов
        for (Object instance : singletons.values()) {
            invokePreDestroyMethods(instance);
        }
        
        // Очищаем все карты
        singletons.clear();
        factories.clear();
        bindings.clear();
        scopes.clear();
        sessionInstances.clear();
        
        logger.info("Контейнер зависимостей отключен");
    }
    
    @Override
    public String getName() {
        return "DependencyContainer";
    }
    
    /**
     * Регистрирует реализацию для интерфейса
     * @param interfaceClass интерфейс
     * @param implementationClass реализация
     * @param <T> тип интерфейса
     */
    public <T> void bind(Class<T> interfaceClass, Class<? extends T> implementationClass) {
        bindings.put(interfaceClass, implementationClass);
    }
    
    /**
     * Регистрирует синглтон 
     * @param type тип сервиса
     * @param instance экземпляр сервиса
     * @param <T> тип сервиса
     */
    public <T> void registerSingleton(Class<T> type, T instance) {
        singletons.put(type, instance);
        scopes.put(type, InjectionScope.SINGLETON);
    }
    
    /**
     * Регистрирует фабрику для создания экземпляров класса
     * @param type тип создаваемого объекта
     * @param factory фабрика
     * @param <T> тип создаваемого объекта
     */
    public <T> void registerFactory(Class<T> type, InstanceFactory<T> factory) {
        factories.put(type, factory);
    }
    
    /**
     * Устанавливает скоуп для класса
     * @param type класс
     * @param scope скоуп
     */
    public void setScope(Class<?> type, InjectionScope scope) {
        scopes.put(type, scope);
    }
    
    /**
     * Получает экземпляр в соответствии с указанным скоупом
     * @param type тип сервиса
     * @param <T> тип сервиса
     * @return экземпляр сервиса
     */
    @SuppressWarnings("unchecked")
    public <T> T getInstance(Class<T> type) {
        return getInstance(type, null);
    }
    
    /**
     * Получает экземпляр в соответствии с указанным скоупом и сессией
     * @param type тип сервиса
     * @param sessionKey ключ сессии (для сессионных объектов)
     * @param <T> тип сервиса
     * @return экземпляр сервиса
     */
    @SuppressWarnings("unchecked")
    public <T> T getInstance(Class<T> type, String sessionKey) {
        // Проверяем циклические зависимости
        if (creationStack.get().contains(type)) {
            StringBuilder path = new StringBuilder();
            for (Class<?> cls : creationStack.get()) {
                path.append(cls.getSimpleName()).append(" -> ");
            }
            path.append(type.getSimpleName());
            
            throw new IllegalStateException("Обнаружена циклическая зависимость: " + path);
        }
        
        // Если тип - интерфейс, ищем его реализацию
        if (type.isInterface() && bindings.containsKey(type)) {
            return getInstance((Class<T>) bindings.get(type), sessionKey);
        }
        
        // Определяем скоуп
        InjectionScope scope = scopes.getOrDefault(type, InjectionScope.SINGLETON);
        
        // Возвращаем экземпляр в соответствии со скоупом
        switch (scope) {
            case SINGLETON:
                return getSingletonInstance(type);
            case PROTOTYPE:
                return createNewInstance(type);
            case SESSION:
                if (sessionKey == null) {
                    throw new IllegalArgumentException("Ключ сессии не может быть null для скоупа SESSION");
                }
                return getSessionInstance(type, sessionKey);
            case REQUEST:
                return createNewInstance(type);
            default:
                throw new IllegalStateException("Неизвестный скоуп: " + scope);
        }
    }
    
    /**
     * Получает или создает синглтон
     */
    @SuppressWarnings("unchecked")
    private <T> T getSingletonInstance(Class<T> type) {
        if (singletons.containsKey(type)) {
            return (T) singletons.get(type);
        }
        
        // Создаем новый синглтон
        creationStack.get().add(type);
        try {
            T instance = createInstance(type);
            singletons.put(type, instance);
            return instance;
        } finally {
            creationStack.get().remove(type);
        }
    }
    
    /**
     * Получает или создает сессионный экземпляр
     */
    @SuppressWarnings("unchecked")
    private <T> T getSessionInstance(Class<T> type, String sessionKey) {
        Map<Class<?>, Object> sessionMap = sessionInstances.computeIfAbsent(sessionKey, k -> new ConcurrentHashMap<>());
        
        if (sessionMap.containsKey(type)) {
            return (T) sessionMap.get(type);
        }
        
        // Создаем новый экземпляр для сессии
        creationStack.get().add(type);
        try {
            T instance = createInstance(type);
            sessionMap.put(type, instance);
            return instance;
        } finally {
            creationStack.get().remove(type);
        }
    }
    
    /**
     * Создает новый экземпляр (для прототипов и запросов)
     */
    private <T> T createNewInstance(Class<T> type) {
        creationStack.get().add(type);
        try {
            return createInstance(type);
        } finally {
            creationStack.get().remove(type);
        }
    }
    
    /**
     * Создает экземпляр, используя фабрику или конструктор
     */
    @SuppressWarnings("unchecked")
    private <T> T createInstance(Class<T> type) {
        // Используем фабрику, если она есть
        if (factories.containsKey(type)) {
            return (T) factories.get(type).createInstance();
        }
        
        try {
            // Пытаемся создать через конструктор с аннотацией @Inject
            // Пока это заглушка, нужно будет реализовать полную поддержку аннотируемых конструкторов
            T instance = type.getDeclaredConstructor().newInstance();
            
            // Внедряем зависимости
            injectDependencies(instance);
            
            // Вызываем методы с аннотацией @PostConstruct
            invokePostConstructMethods(instance);
            
            return instance;
        } catch (Exception e) {
            logger.severe("Не удалось создать экземпляр типа " + type.getName() + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Ошибка создания экземпляра: " + e.getMessage(), e);
        }
    }
    
    /**
     * Внедряет зависимости в объект
     * @param target объект для внедрения зависимостей
     */
    public void injectDependencies(Object target) {
        Class<?> targetClass = target.getClass();
        
        // Обрабатываем все поля класса, включая приватные
        for (Field field : getAllFields(targetClass)) {
            // Проверяем наличие аннотаций @Inject или @Autowired
            if (field.isAnnotationPresent(Inject.class) || field.isAnnotationPresent(Autowired.class)) {
                field.setAccessible(true);
                
                try {
                    // Получаем тип поля
                    Class<?> fieldType = field.getType();
                    
                    // Получаем скоуп
                    InjectionScope scope = InjectionScope.SINGLETON;
                    if (field.isAnnotationPresent(Inject.class)) {
                        scope = field.getAnnotation(Inject.class).scope();
                    }
                    
                    // Получаем или создаем экземпляр нужного типа с учетом скоупа
                    Object dependency;
                    if (scope == InjectionScope.SESSION) {
                        // В этом примере для SESSION используем имя класса как ключ сессии
                        // В реальном приложении это может быть sessionId
                        dependency = getInstance(fieldType, targetClass.getName());
                    } else {
                        dependency = getInstance(fieldType);
                    }
                    
                    if (dependency != null) {
                        // Устанавливаем значение поля
                        field.set(target, dependency);
                    } else {
                        logger.warning("Не удалось внедрить зависимость " + fieldType.getName() + 
                                      " в " + targetClass.getName() + "." + field.getName());
                    }
                } catch (Exception e) {
                    logger.severe("Ошибка при внедрении зависимости в " + 
                                 targetClass.getName() + "." + field.getName() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Вызывает методы с аннотацией @PostConstruct
     */
    private void invokePostConstructMethods(Object instance) {
        Class<?> targetClass = instance.getClass();
        
        // Собираем все методы с аннотацией @PostConstruct
        List<Method> methods = Arrays.stream(targetClass.getDeclaredMethods())
            .filter(m -> m.isAnnotationPresent(PostConstruct.class))
            .sorted(Comparator.comparingInt(m -> -m.getAnnotation(PostConstruct.class).priority()))
            .collect(Collectors.toList());
        
        // Вызываем методы
        for (Method method : methods) {
            try {
                method.setAccessible(true);
                method.invoke(instance);
            } catch (Exception e) {
                logger.severe("Ошибка при вызове @PostConstruct метода " +
                             targetClass.getName() + "." + method.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Вызывает методы с аннотацией @PreDestroy
     */
    private void invokePreDestroyMethods(Object instance) {
        Class<?> targetClass = instance.getClass();
        
        // Собираем все методы с аннотацией @PreDestroy
        List<Method> methods = Arrays.stream(targetClass.getDeclaredMethods())
            .filter(m -> m.isAnnotationPresent(PreDestroy.class))
            .sorted(Comparator.comparingInt(m -> -m.getAnnotation(PreDestroy.class).priority()))
            .collect(Collectors.toList());
        
        // Вызываем методы
        for (Method method : methods) {
            try {
                method.setAccessible(true);
                method.invoke(instance);
            } catch (Exception e) {
                logger.severe("Ошибка при вызове @PreDestroy метода " +
                             targetClass.getName() + "." + method.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Получает все поля класса, включая поля из суперклассов
     */
    private List<Field> getAllFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        
        // Получаем поля текущего класса
        fields.addAll(Arrays.asList(type.getDeclaredFields()));
        
        // Рекурсивно получаем поля суперклассов
        Class<?> superClass = type.getSuperclass();
        if (superClass != null && superClass != Object.class) {
            fields.addAll(getAllFields(superClass));
        }
        
        return fields;
    }
    
    /**
     * Завершает сессию, освобождая все объекты с SESSION скоупом
     */
    public void closeSession(String sessionKey) {
        Map<Class<?>, Object> sessionMap = sessionInstances.remove(sessionKey);
        if (sessionMap != null) {
            // Вызываем методы @PreDestroy для всех объектов сессии
            for (Object instance : sessionMap.values()) {
                invokePreDestroyMethods(instance);
            }
        }
    }
    
    /**
     * Очищает все кэшированные объекты скоупа REQUEST
     * Этот метод нужно вызывать в конце каждого запроса
     */
    public void clearRequestScoped() {
        // REQUEST скоуп не кэшируется, поэтому здесь ничего не делаем
        // Этот метод нужен только для совместимости с другими контейнерами DI
    }
    
    /**
     * Интерфейс для фабрик, создающих экземпляры объектов
     */
    public interface InstanceFactory<T> {
        T createInstance();
    }
} 