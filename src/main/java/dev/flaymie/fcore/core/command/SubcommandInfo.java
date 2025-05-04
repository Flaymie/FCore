package dev.flaymie.fcore.core.command;

import dev.flaymie.fcore.api.annotation.Permission;
import dev.flaymie.fcore.api.annotation.Subcommand;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Класс для хранения информации о подкоманде
 */
public class SubcommandInfo {
    
    private final String name;
    private final String description;
    private final String usage;
    private final List<String> aliases;
    private final int minArgs;
    private final int maxArgs;
    private final boolean playerOnly;
    private final String playerOnlyMessage;
    private final String permission;
    private final String permissionMessage;
    private final Method method;
    private final CommandInfo parentCommand;
    
    /**
     * Создает объект с информацией о подкоманде из аннотаций
     * @param method метод подкоманды
     * @param parentCommand родительская команда
     */
    public SubcommandInfo(Method method, CommandInfo parentCommand) {
        Subcommand subcommand = method.getAnnotation(Subcommand.class);
        if (subcommand == null) {
            throw new IllegalArgumentException("Method " + method.getName() + " is not annotated with @Subcommand");
        }
        
        this.method = method;
        this.parentCommand = parentCommand;
        
        this.name = subcommand.value();
        this.description = subcommand.description();
        this.usage = subcommand.usage();
        this.aliases = Arrays.asList(subcommand.aliases());
        this.minArgs = subcommand.minArgs();
        this.maxArgs = subcommand.maxArgs();
        this.playerOnly = subcommand.playerOnly();
        this.playerOnlyMessage = subcommand.playerOnlyMessage();
        
        // Получаем информацию о правах
        Permission permissionAnnotation = method.getAnnotation(Permission.class);
        if (permissionAnnotation != null) {
            this.permission = permissionAnnotation.value();
            this.permissionMessage = permissionAnnotation.message();
        } else {
            this.permission = parentCommand.getPermission();
            this.permissionMessage = parentCommand.getPermissionMessage();
        }
    }
    
    /**
     * Получает название подкоманды
     * @return название подкоманды
     */
    public String getName() {
        return name;
    }
    
    /**
     * Получает описание подкоманды
     * @return описание подкоманды
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Получает синтаксис подкоманды
     * @return синтаксис подкоманды
     */
    public String getUsage() {
        return usage.isEmpty() ? 
            "/" + parentCommand.getName() + " " + name : 
            usage;
    }
    
    /**
     * Получает алиасы подкоманды
     * @return список алиасов
     */
    public List<String> getAliases() {
        return Collections.unmodifiableList(aliases);
    }
    
    /**
     * Получает минимальное количество аргументов
     * @return минимальное количество аргументов
     */
    public int getMinArgs() {
        return minArgs;
    }
    
    /**
     * Получает максимальное количество аргументов
     * @return максимальное количество аргументов или -1, если не ограничено
     */
    public int getMaxArgs() {
        return maxArgs;
    }
    
    /**
     * Проверяет, нужно ли подкоманде быть игроком
     * @return true, если подкоманда только для игроков
     */
    public boolean isPlayerOnly() {
        return playerOnly;
    }
    
    /**
     * Получает сообщение, если подкоманду выполняет не игрок
     * @return сообщение об ошибке
     */
    public String getPlayerOnlyMessage() {
        return playerOnlyMessage;
    }
    
    /**
     * Получает право для выполнения подкоманды
     * @return право или null, если право не требуется
     */
    public String getPermission() {
        return permission;
    }
    
    /**
     * Получает сообщение при отсутствии права
     * @return сообщение об ошибке
     */
    public String getPermissionMessage() {
        return permissionMessage;
    }
    
    /**
     * Получает метод подкоманды
     * @return метод подкоманды
     */
    public Method getMethod() {
        return method;
    }
    
    /**
     * Получает родительскую команду
     * @return родительская команда
     */
    public CommandInfo getParentCommand() {
        return parentCommand;
    }
    
    @Override
    public String toString() {
        return parentCommand.getName() + " " + name + " - " + description;
    }
} 