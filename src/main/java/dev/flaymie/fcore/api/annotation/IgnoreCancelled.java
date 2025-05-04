package dev.flaymie.fcore.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для игнорирования отмененных событий
 * Используется вместе с @org.bukkit.event.EventHandler
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface IgnoreCancelled {
    
    /**
     * Если true, метод-обработчик не будет вызван для отмененных событий
     * @return флаг игнорирования отмененных событий
     */
    boolean value() default true;
} 