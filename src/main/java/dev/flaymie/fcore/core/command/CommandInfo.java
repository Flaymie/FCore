package dev.flaymie.fcore.core.command;

import dev.flaymie.fcore.api.annotation.Command;
import dev.flaymie.fcore.api.annotation.Permission;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Класс для хранения информации о команде
 */
public class CommandInfo {
    
    private final String name;
    private final String description;
    private final String usage;
    private final List<String> aliases;
    private final int minArgs;
    private final int maxArgs;
    private final boolean playerOnly;
    private final String playerOnlyMessage;
    private final int priority;
    private final String permission;
    private final String permissionMessage;
    private final Class<?> commandClass;
    private final Object commandInstance;
    
    /**
     * Создает объект с информацией о команде из аннотаций
     * @param commandClass класс команды
     * @param commandInstance экземпляр команды
     */
    public CommandInfo(Class<?> commandClass, Object commandInstance) {
        Command command = commandClass.getAnnotation(Command.class);
        if (command == null) {
            throw new IllegalArgumentException("Class " + commandClass.getName() + " is not annotated with @Command");
        }
        
        this.commandClass = commandClass;
        this.commandInstance = commandInstance;
        
        this.name = command.name();
        this.description = command.description();
        this.usage = command.usage();
        this.aliases = Arrays.asList(command.aliases());
        this.minArgs = command.minArgs();
        this.maxArgs = command.maxArgs();
        this.playerOnly = command.playerOnly();
        this.playerOnlyMessage = command.playerOnlyMessage();
        this.priority = command.priority();
        
        // Получаем информацию о правах
        Permission permissionAnnotation = commandClass.getAnnotation(Permission.class);
        if (permissionAnnotation != null) {
            this.permission = permissionAnnotation.value();
            this.permissionMessage = permissionAnnotation.message();
        } else {
            this.permission = null;
            this.permissionMessage = null;
        }
    }
    
    /**
     * Получает название команды
     * @return название команды
     */
    public String getName() {
        return name;
    }
    
    /**
     * Получает описание команды
     * @return описание команды
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Получает синтаксис команды
     * @return синтаксис команды
     */
    public String getUsage() {
        return usage.isEmpty() ? "/" + name : usage;
    }
    
    /**
     * Получает алиасы команды
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
     * Проверяет, нужно ли команде быть игроком
     * @return true, если команда только для игроков
     */
    public boolean isPlayerOnly() {
        return playerOnly;
    }
    
    /**
     * Получает сообщение, если команду выполняет не игрок
     * @return сообщение об ошибке
     */
    public String getPlayerOnlyMessage() {
        return playerOnlyMessage;
    }
    
    /**
     * Получает приоритет команды
     * @return приоритет команды
     */
    public int getPriority() {
        return priority;
    }
    
    /**
     * Получает право для выполнения команды
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
     * Получает класс команды
     * @return класс команды
     */
    public Class<?> getCommandClass() {
        return commandClass;
    }
    
    /**
     * Получает экземпляр команды
     * @return экземпляр команды
     */
    public Object getCommandInstance() {
        return commandInstance;
    }
    
    @Override
    public String toString() {
        return name + " - " + description;
    }
} 