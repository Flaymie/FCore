package dev.flaymie.fcore.api.annotation;

import org.bukkit.event.EventPriority;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для указания приоритета метода-обработчика события
 * Используется вместе с @org.bukkit.event.EventHandler
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Priority {
    
    /**
     * Приоритет обработки события
     * @return приоритет из enum EventPriority
     */
    EventPriority value() default EventPriority.NORMAL;
} 