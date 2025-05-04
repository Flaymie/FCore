package dev.flaymie.fcore.core.data.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для указания секции в конфигурации
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ConfigSection {
    /**
     * Путь к секции в конфигурации
     */
    String value();
    
    /**
     * Комментарий к секции (для YAML)
     */
    String comment() default "";
} 