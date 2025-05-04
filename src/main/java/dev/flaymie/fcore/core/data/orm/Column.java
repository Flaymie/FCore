package dev.flaymie.fcore.core.data.orm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для указания колонки в таблице
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {
    /**
     * Имя колонки в таблице
     */
    String value();
    
    /**
     * Тип данных колонки
     */
    String type() default "";
    
    /**
     * Размер поля (для VARCHAR, CHAR и т.д.)
     */
    int length() default 0;
    
    /**
     * Указывает, что колонка не может быть NULL
     */
    boolean notNull() default false;
} 