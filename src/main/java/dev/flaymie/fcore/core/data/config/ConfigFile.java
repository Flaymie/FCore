package dev.flaymie.fcore.core.data.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для указания файла конфигурации
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ConfigFile {
    /**
     * Путь к файлу конфигурации относительно папки плагина
     */
    String value();
    
    /**
     * Формат файла конфигурации
     */
    ConfigFormat format() default ConfigFormat.YAML;
} 