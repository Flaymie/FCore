package dev.flaymie.fcore.core.event;

import dev.flaymie.fcore.FCore;
import dev.flaymie.fcore.api.annotation.EventListener;
import dev.flaymie.fcore.api.annotation.IgnoreCancelled;
import dev.flaymie.fcore.api.annotation.Priority;
import dev.flaymie.fcore.api.event.FCoreEvent;
import dev.flaymie.fcore.api.service.FCoreService;
import dev.flaymie.fcore.core.di.DependencyContainer;
import org.bukkit.Bukkit;
import org.bukkit.event.*;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.RegisteredListener;

import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Logger;

/**
 * Менеджер событий, отвечающий за автоматическую регистрацию и вызов событий
 */
public class EventManager implements FCoreService {
    
    private final FCore plugin;
    private final Logger logger;
    private final DependencyContainer dependencyContainer;
    private final Set<Listener> registeredListeners;
    
    public EventManager(FCore plugin, DependencyContainer dependencyContainer) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.dependencyContainer = dependencyContainer;
        this.registeredListeners = new HashSet<>();
    }
    
    @Override
    public void onEnable() {
        logger.info("Инициализация менеджера событий");
    }
    
    @Override
    public void onDisable() {
        // Отменяем все слушатели
        unregisterAllListeners();
        logger.info("Менеджер событий отключен");
    }
    
    @Override
    public String getName() {
        return "EventManager";
    }
    
    /**
     * Регистрирует слушателя событий
     * @param listener экземпляр слушателя
     */
    public void registerListener(Listener listener) {
        if (registeredListeners.contains(listener)) {
            logger.warning("Слушатель " + listener.getClass().getName() + " уже зарегистрирован");
            return;
        }
        
        // Внедряем зависимости в слушателя
        dependencyContainer.injectDependencies(listener);
        
        // Регистрируем слушателя в Bukkit
        registerCustomHandlers(listener);
        
        // Добавляем в список зарегистрированных слушателей
        registeredListeners.add(listener);
        
        logger.info("Слушатель " + listener.getClass().getName() + " зарегистрирован");
    }
    
    /**
     * Регистрирует слушателя с учетом аннотаций @Priority и @IgnoreCancelled
     * @param listener экземпляр слушателя
     */
    private void registerCustomHandlers(Listener listener) {
        Map<Class<? extends Event>, Set<RegisteredHandler>> handlers = findHandlers(listener);
        
        // Если нет обработчиков, регистрируем как обычный слушатель
        if (handlers.isEmpty()) {
            Bukkit.getPluginManager().registerEvents(listener, plugin);
            return;
        }
        
        // Регистрируем обработчики с учетом приоритетов
        for (Map.Entry<Class<? extends Event>, Set<RegisteredHandler>> entry : handlers.entrySet()) {
            Class<? extends Event> eventClass = entry.getKey();
            Set<RegisteredHandler> eventHandlers = entry.getValue();
            
            for (RegisteredHandler handler : eventHandlers) {
                EventExecutor executor = (l, event) -> {
                    if (!eventClass.isInstance(event)) return;
                    if (handler.ignoreCancelled && event instanceof Cancellable && ((Cancellable) event).isCancelled()) return;
                    
                    try {
                        handler.method.invoke(listener, event);
                    } catch (Exception e) {
                        logger.severe("Ошибка при обработке события " + event.getEventName() + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                };
                
                Bukkit.getPluginManager().registerEvent(
                    eventClass,
                    listener,
                    handler.priority,
                    executor,
                    plugin,
                    handler.ignoreCancelled
                );
                
                logger.fine("Зарегистрирован обработчик " + handler.method.getName() + 
                            " для события " + eventClass.getSimpleName() + 
                            " с приоритетом " + handler.priority);
            }
        }
    }
    
    /**
     * Находит все методы-обработчики событий в классе слушателя
     * @param listener экземпляр слушателя
     * @return карта: класс события -> множество обработчиков
     */
    private Map<Class<? extends Event>, Set<RegisteredHandler>> findHandlers(Listener listener) {
        Map<Class<? extends Event>, Set<RegisteredHandler>> result = new HashMap<>();
        
        // Получаем все методы класса
        Method[] methods = listener.getClass().getDeclaredMethods();
        
        for (Method method : methods) {
            // Проверяем наличие аннотации @EventHandler
            if (!method.isAnnotationPresent(org.bukkit.event.EventHandler.class)) {
                continue;
            }
            
            // Проверяем сигнатуру метода
            if (method.getParameterCount() != 1) {
                logger.warning("Метод " + method.getName() + " в классе " + 
                              listener.getClass().getName() + " помечен как @EventHandler, " +
                              "но имеет неверное количество параметров");
                continue;
            }
            
            // Получаем тип события
            Class<?> eventClass = method.getParameterTypes()[0];
            if (!Event.class.isAssignableFrom(eventClass)) {
                logger.warning("Метод " + method.getName() + " в классе " + 
                              listener.getClass().getName() + " помечен как @EventHandler, " +
                              "но параметр не является подклассом Event");
                continue;
            }
            
            // Проверяем наличие аннотаций для приоритета и игнорирования отмены
            EventPriority priority = EventPriority.NORMAL;
            boolean ignoreCancelled = false;
            
            // Обрабатываем аннотацию Bukkit @EventHandler
            org.bukkit.event.EventHandler eventHandler = method.getAnnotation(org.bukkit.event.EventHandler.class);
            priority = eventHandler.priority();
            ignoreCancelled = eventHandler.ignoreCancelled();
            
            // Наша аннотация @Priority имеет приоритет над @EventHandler
            if (method.isAnnotationPresent(Priority.class)) {
                Priority priorityAnnotation = method.getAnnotation(Priority.class);
                priority = priorityAnnotation.value();
            }
            
            // Наша аннотация @IgnoreCancelled имеет приоритет над @EventHandler
            if (method.isAnnotationPresent(IgnoreCancelled.class)) {
                IgnoreCancelled ignoreCancelledAnnotation = method.getAnnotation(IgnoreCancelled.class);
                ignoreCancelled = ignoreCancelledAnnotation.value();
            }
            
            // Добавляем обработчик в результат
            @SuppressWarnings("unchecked")
            Class<? extends Event> typedEventClass = (Class<? extends Event>) eventClass;
            
            result.computeIfAbsent(typedEventClass, k -> new HashSet<>())
                  .add(new RegisteredHandler(method, priority, ignoreCancelled));
        }
        
        return result;
    }
    
    /**
     * Отменяет регистрацию всех слушателей
     */
    public void unregisterAllListeners() {
        HandlerList.unregisterAll(plugin);
        registeredListeners.clear();
        logger.info("Все слушатели отменены");
    }
    
    /**
     * Отменяет регистрацию указанного слушателя
     * @param listener экземпляр слушателя
     */
    public void unregisterListener(Listener listener) {
        HandlerList.unregisterAll(listener);
        registeredListeners.remove(listener);
        logger.info("Слушатель " + listener.getClass().getName() + " отменен");
    }
    
    /**
     * Вызывает кастомное событие
     * @param event экземпляр события
     * @return true если событие не было отменено
     */
    public boolean callEvent(FCoreEvent event) {
        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }
    
    /**
     * Получает список всех зарегистрированных слушателей
     * @return множество слушателей
     */
    public Set<Listener> getRegisteredListeners() {
        return Collections.unmodifiableSet(registeredListeners);
    }
    
    /**
     * Вспомогательный класс для хранения информации о обработчике события
     */
    private static class RegisteredHandler {
        final Method method;
        final EventPriority priority;
        final boolean ignoreCancelled;
        
        RegisteredHandler(Method method, EventPriority priority, boolean ignoreCancelled) {
            this.method = method;
            this.priority = priority;
            this.ignoreCancelled = ignoreCancelled;
            // Делаем метод доступным для вызова
            this.method.setAccessible(true);
        }
    }
} 