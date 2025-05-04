package dev.flaymie.fcore.commands;

import dev.flaymie.fcore.api.annotation.Argument;
import dev.flaymie.fcore.api.annotation.Command;
import dev.flaymie.fcore.api.annotation.Permission;
import dev.flaymie.fcore.api.annotation.Subcommand;
import dev.flaymie.fcore.api.permission.PermissionManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import dev.flaymie.fcore.api.annotation.Inject;

/**
 * Команда для управления правами
 */
@Command(name = "permissions", description = "Управление правами", aliases = {"perm", "perms"})
@Permission("fcore.command.permissions")
public class PermissionCommand {
    
    @Inject
    private PermissionManager permissionManager;
    
    /**
     * Выводит список групп
     */
    @Subcommand(value = "groups", description = "Список групп прав")
    @Permission("fcore.command.permissions.groups")
    public void groupsCommand(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== Группы прав ===");
        // TODO: Вывести список групп
        sender.sendMessage(ChatColor.GRAY + "Функционал в разработке...");
    }
    
    /**
     * Добавляет право игроку
     */
    @Subcommand(value = "user add", description = "Добавить право игроку")
    @Permission("fcore.command.permissions.user.add")
    public void userAddPermission(CommandSender sender, 
                                 @Argument("игрок") String playerName, 
                                 @Argument("право") String permission) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Игрок не найден");
            return;
        }
        
        permissionManager.addPermission(target, permission);
        sender.sendMessage(ChatColor.GREEN + "Право " + permission + " добавлено игроку " + target.getName());
    }
    
    /**
     * Удаляет право у игрока
     */
    @Subcommand(value = "user remove", description = "Удалить право у игрока")
    @Permission("fcore.command.permissions.user.remove")
    public void userRemovePermission(CommandSender sender, 
                                    @Argument("игрок") String playerName, 
                                    @Argument("право") String permission) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Игрок не найден");
            return;
        }
        
        permissionManager.removePermission(target, permission);
        sender.sendMessage(ChatColor.GREEN + "Право " + permission + " удалено у игрока " + target.getName());
    }
    
    /**
     * Проверяет право у игрока
     */
    @Subcommand(value = "user check", description = "Проверить право у игрока")
    @Permission("fcore.command.permissions.user.check")
    public void userCheckPermission(CommandSender sender, 
                                   @Argument("игрок") String playerName, 
                                   @Argument("право") String permission) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Игрок не найден");
            return;
        }
        
        boolean has = permissionManager.hasPermission(target, permission);
        sender.sendMessage(ChatColor.YELLOW + "Игрок " + target.getName() + 
                          (has ? ChatColor.GREEN + " имеет " : ChatColor.RED + " не имеет ") + 
                          "право " + permission);
    }
    
    /**
     * Добавляет игрока в группу
     */
    @Subcommand(value = "user group add", description = "Добавить игрока в группу")
    @Permission("fcore.command.permissions.user.group.add")
    public void userAddGroup(CommandSender sender, 
                            @Argument("игрок") String playerName, 
                            @Argument("группа") String groupName) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Игрок не найден");
            return;
        }
        
        if (!permissionManager.groupExists(groupName)) {
            sender.sendMessage(ChatColor.RED + "Группа " + groupName + " не существует");
            return;
        }
        
        permissionManager.addToGroup(target, groupName);
        sender.sendMessage(ChatColor.GREEN + "Игрок " + target.getName() + " добавлен в группу " + groupName);
    }
    
    /**
     * Удаляет игрока из группы
     */
    @Subcommand(value = "user group remove", description = "Удалить игрока из группы")
    @Permission("fcore.command.permissions.user.group.remove")
    public void userRemoveGroup(CommandSender sender, 
                               @Argument("игрок") String playerName, 
                               @Argument("группа") String groupName) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Игрок не найден");
            return;
        }
        
        permissionManager.removeFromGroup(target, groupName);
        sender.sendMessage(ChatColor.GREEN + "Игрок " + target.getName() + " удален из группы " + groupName);
    }
    
    /**
     * Сохраняет права в конфигурацию
     */
    @Subcommand(value = "save", description = "Сохранить права в файл")
    @Permission("fcore.command.permissions.save")
    public void savePermissions(CommandSender sender) {
        permissionManager.savePermissions();
        sender.sendMessage(ChatColor.GREEN + "Права сохранены в файл");
    }
    
    /**
     * Загружает права из конфигурации
     */
    @Subcommand(value = "load", description = "Загрузить права из файла")
    @Permission("fcore.command.permissions.load")
    public void loadPermissions(CommandSender sender) {
        permissionManager.loadPermissions();
        sender.sendMessage(ChatColor.GREEN + "Права загружены из файла");
    }
} 