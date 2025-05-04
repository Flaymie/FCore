package dev.flaymie.fcore.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для подкоманд в классах-командах
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Subcommand {
    
    /**
     * Название подкоманды
     */
    String value();
    
    /**
     * Описание подкоманды
     */
    String description() default "";
    
    /**
     * Синтаксис подкоманды
     */
    String usage() default "";
    
    /**
     * Алиасы подкоманды
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
} 