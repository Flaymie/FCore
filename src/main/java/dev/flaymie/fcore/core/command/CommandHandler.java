package dev.flaymie.fcore.core.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Обработчик команд Bukkit
 */
public class CommandHandler implements CommandExecutor, TabCompleter {
    
    private final CommandInfo commandInfo;
    private final CommandManager commandManager;
    
    /**
     * Создает обработчик команд
     * @param commandInfo информация о команде
     * @param commandManager менеджер команд
     */
    public CommandHandler(CommandInfo commandInfo, CommandManager commandManager) {
        this.commandInfo = commandInfo;
        this.commandManager = commandManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Проверяем, нужно ли команде быть игроком
        if (commandInfo.isPlayerOnly() && !(sender instanceof Player)) {
            sender.sendMessage(commandInfo.getPlayerOnlyMessage());
            return true;
        }
        
        // Проверяем права на команду
        if (commandInfo.getPermission() != null && !sender.hasPermission(commandInfo.getPermission())) {
            sender.sendMessage(commandInfo.getPermissionMessage());
            return true;
        }
        
        // Проверяем количество аргументов
        if (args.length < commandInfo.getMinArgs()) {
            sender.sendMessage("§cНедостаточно аргументов. Использование: " + commandInfo.getUsage());
            return true;
        }
        
        if (commandInfo.getMaxArgs() >= 0 && args.length > commandInfo.getMaxArgs()) {
            sender.sendMessage("§cСлишком много аргументов. Использование: " + commandInfo.getUsage());
            return true;
        }
        
        // Ищем подкоманду
        if (args.length > 0) {
            SubcommandInfo subcommandInfo = commandManager.findSubcommand(commandInfo, args[0]);
            if (subcommandInfo != null) {
                // Выполняем подкоманду
                return commandManager.executeSubcommand(sender, commandInfo, subcommandInfo, args);
            }
        }
        
        // Выполняем основную команду
        return commandManager.executeCommand(sender, commandInfo, args);
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // Если нет прав на команду, не предлагаем табкомплит
        if (commandInfo.getPermission() != null && !sender.hasPermission(commandInfo.getPermission())) {
            return Collections.emptyList();
        }
        
        // Если есть подкоманды и вводится первый аргумент
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            
            // Добавляем все подкоманды, доступные пользователю
            for (SubcommandInfo subcommand : commandManager.getSubcommands(commandInfo)) {
                if (subcommand.getPermission() == null || sender.hasPermission(subcommand.getPermission())) {
                    if (subcommand.getName().startsWith(args[0].toLowerCase())) {
                        completions.add(subcommand.getName());
                    }
                    
                    // Добавляем алиасы подкоманд
                    for (String alias2 : subcommand.getAliases()) {
                        if (alias2.startsWith(args[0].toLowerCase())) {
                            completions.add(alias2);
                        }
                    }
                }
            }
            
            return completions;
        }
        
        // Если уже введена подкоманда, то ищем ее и вызываем табкомплит для нее
        else if (args.length > 1) {
            SubcommandInfo subcommandInfo = commandManager.findSubcommand(commandInfo, args[0]);
            if (subcommandInfo != null) {
                return commandManager.getSubcommandTabCompleter(sender, subcommandInfo, args);
            }
        }
        
        return Collections.emptyList();
    }
} 