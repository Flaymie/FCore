package dev.flaymie.fcore.api.annotation;

import dev.flaymie.fcore.core.di.InjectionScope;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для инъекции зависимостей
 * Помечает поле для автоматического внедрения сервиса
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.METHOD})
public @interface Inject {
    /**
     * Скоуп внедряемой зависимости
     * По умолчанию SINGLETON
     */
    InjectionScope scope() default InjectionScope.SINGLETON;
} 