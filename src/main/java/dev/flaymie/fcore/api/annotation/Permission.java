package dev.flaymie.fcore.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для определения прав доступа к командам
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Permission {
    
    /**
     * Название права
     */
    String value();
    
    /**
     * Сообщение при отсутствии права
     */
    String message() default "У вас нет прав на выполнение этой команды";
} 