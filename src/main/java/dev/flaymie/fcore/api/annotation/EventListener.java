package dev.flaymie.fcore.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для пометки классов как слушателей событий
 * Классы с этой аннотацией автоматически регистрируются в EventManager
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EventListener {
    
    /**
     * Приоритет слушателя
     * Слушатели с более высоким приоритетом регистрируются раньше
     */
    int priority() default 0;
    
    /**
     * Флаг автоматической регистрации слушателя
     * Если true, слушатель будет зарегистрирован при старте плагина
     * Если false, необходимо регистрировать слушатель вручную
     */
    boolean autoRegister() default true;
} 