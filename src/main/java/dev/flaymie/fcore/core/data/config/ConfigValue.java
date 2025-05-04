package dev.flaymie.fcore.core.data.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для указания поля в конфигурации
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ConfigValue {
    /**
     * Путь к значению в конфигурации
     */
    String value();
    
    /**
     * Комментарий к значению (для YAML)
     */
    String comment() default "";
} 