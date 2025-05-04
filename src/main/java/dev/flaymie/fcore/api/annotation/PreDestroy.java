package dev.flaymie.fcore.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для методов, которые должны выполняться
 * перед уничтожением объекта
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PreDestroy {
    /**
     * Приоритет метода, методы с более высоким приоритетом
     * выполняются раньше (если несколько методов с этой аннотацией)
     */
    int priority() default 0;
} 