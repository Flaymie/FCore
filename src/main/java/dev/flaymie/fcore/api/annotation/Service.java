package dev.flaymie.fcore.api.annotation;

import dev.flaymie.fcore.core.di.InjectionScope;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для пометки класса как сервиса
 * Сервисы автоматически регистрируются в контейнере зависимостей
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Service {
    
    /**
     * Имя сервиса
     * Если не указано, используется имя класса
     */
    String value() default "";
    
    /**
     * Приоритет загрузки сервиса
     * Сервисы с более высоким приоритетом загружаются раньше
     */
    int priority() default 0;
    
    /**
     * Скоуп сервиса
     * По умолчанию SINGLETON
     */
    InjectionScope scope() default InjectionScope.SINGLETON;
    
    /**
     * Флаг lazy-loading
     * Если true, сервис будет создан только при первом запросе
     * Если false, сервис будет создан при старте приложения
     */
    boolean lazy() default false;
} 