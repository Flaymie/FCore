package dev.flaymie.fcore.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для аргументов в методах команд
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Argument {
    
    /**
     * Название аргумента (для вывода в сообщениях)
     */
    String value();
    
    /**
     * Описание аргумента (для помощи)
     */
    String description() default "";
    
    /**
     * Значение по умолчанию (если аргумент не указан)
     */
    String defaultValue() default "";
    
    /**
     * Нужен ли этот аргумент обязательно
     */
    boolean required() default true;
    
    /**
     * Индекс аргумента в команде (если -1, то определяется автоматически)
     */
    int index() default -1;
} 