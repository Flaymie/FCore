package dev.flaymie.fcore.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для классов-команд
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Command {
    
    /**
     * Название команды
     */
    String name();
    
    /**
     * Описание команды
     */
    String description() default "";
    
    /**
     * Синтаксис команды
     */
    String usage() default "";
    
    /**
     * Алиасы команды
     */
    String[] aliases() default {};
    
    /**
     * Минимальное количество аргументов
     */
    int minArgs() default 0;
    
    /**
     * Максимальное количество аргументов
     */
    int maxArgs() default -1;
    
    /**
     * Нужно ли команде быть игроком
     */
    boolean playerOnly() default false;
    
    /**
     * Сообщение, если команду выполняет не игрок
     */
    String playerOnlyMessage() default "Эта команда доступна только для игроков";
    
    /**
     * Приоритет команды (для конфликтующих команд)
     */
    int priority() default 0;
} 