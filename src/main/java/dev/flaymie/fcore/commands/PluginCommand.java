package dev.flaymie.fcore.commands;

import dev.flaymie.fcore.FCore;
import dev.flaymie.fcore.api.annotation.Command;
import dev.flaymie.fcore.api.annotation.Inject;
import dev.flaymie.fcore.api.annotation.Subcommand;
import dev.flaymie.fcore.api.annotation.Argument;
import dev.flaymie.fcore.api.annotation.Permission;
import dev.flaymie.fcore.api.plugin.FCorePlugin;
import dev.flaymie.fcore.api.plugin.PluginLoader;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * Команда для управления плагинами FCore
 */
@Command(name = "fcoreplugins", description = "Управление плагинами FCore", aliases = {"fcp", "fplugins"})
@Permission("fcore.admin.plugins")
public class PluginCommand {
    
    @Inject
    private FCore plugin;
    
    @Inject
    private PluginLoader pluginLoader;
    
    @Subcommand(value = "", description = "Список плагинов FCore")
    public void onPluginsCommand(CommandSender sender) {
        List<FCorePlugin> plugins = pluginLoader.getPlugins();
        
        if (plugins.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "Плагины FCore не найдены");
            return;
        }
        
        sender.sendMessage(ChatColor.GREEN + "=== Плагины FCore ===");
        for (FCorePlugin fcorePlugin : plugins) {
            String status = fcorePlugin.isEnabled() ? ChatColor.GREEN + "Включен" : ChatColor.RED + "Отключен";
            sender.sendMessage(ChatColor.GOLD + fcorePlugin.getName() + ChatColor.GRAY + " v" + fcorePlugin.getVersion() + " - " + status);
        }
    }
    
    @Subcommand(value = "enable", description = "Включить плагин FCore")
    public void onEnableCommand(CommandSender sender, @Argument("имя") String name) {
        if (!pluginLoader.isPluginLoaded(name)) {
            sender.sendMessage(ChatColor.RED + "Плагин '" + name + "' не найден");
            return;
        }
        
        if (pluginLoader.isPluginEnabled(name)) {
            sender.sendMessage(ChatColor.YELLOW + "Плагин '" + name + "' уже включен");
            return;
        }
        
        boolean success = pluginLoader.enablePlugin(name);
        
        if (success) {
            sender.sendMessage(ChatColor.GREEN + "Плагин '" + name + "' успешно включен");
        } else {
            sender.sendMessage(ChatColor.RED + "Не удалось включить плагин '" + name + "'. Проверьте консоль для подробностей.");
        }
    }
    
    @Subcommand(value = "disable", description = "Отключить плагин FCore")
    public void onDisableCommand(CommandSender sender, @Argument("имя") String name) {
        if (!pluginLoader.isPluginLoaded(name)) {
            sender.sendMessage(ChatColor.RED + "Плагин '" + name + "' не найден");
            return;
        }
        
        if (!pluginLoader.isPluginEnabled(name)) {
            sender.sendMessage(ChatColor.YELLOW + "Плагин '" + name + "' уже отключен");
            return;
        }
        
        boolean success = pluginLoader.disablePlugin(name);
        
        if (success) {
            sender.sendMessage(ChatColor.GREEN + "Плагин '" + name + "' успешно отключен");
        } else {
            sender.sendMessage(ChatColor.RED + "Не удалось отключить плагин '" + name + "'. Проверьте консоль для подробностей.");
        }
    }
    
    @Subcommand(value = "info", description = "Информация о плагине FCore")
    public void onInfoCommand(CommandSender sender, @Argument("имя") String name) {
        if (!pluginLoader.isPluginLoaded(name)) {
            sender.sendMessage(ChatColor.RED + "Плагин '" + name + "' не найден");
            return;
        }
        
        FCorePlugin plugin = pluginLoader.getPlugin(name);
        String status = plugin.isEnabled() ? ChatColor.GREEN + "Включен" : ChatColor.RED + "Отключен";
        
        sender.sendMessage(ChatColor.GREEN + "=== Информация о плагине ===");
        sender.sendMessage(ChatColor.GOLD + "Имя: " + ChatColor.WHITE + plugin.getName());
        sender.sendMessage(ChatColor.GOLD + "Версия: " + ChatColor.WHITE + plugin.getVersion());
        sender.sendMessage(ChatColor.GOLD + "Статус: " + status);
        sender.sendMessage(ChatColor.GOLD + "Класс: " + ChatColor.WHITE + plugin.getClass().getName());
    }
    
    @Subcommand(value = "reload", description = "Перезагрузить плагин FCore")
    public void onReloadCommand(CommandSender sender, @Argument("имя") String name) {
        if (!pluginLoader.isPluginLoaded(name)) {
            sender.sendMessage(ChatColor.RED + "Плагин '" + name + "' не найден");
            return;
        }
        
        boolean wasEnabled = pluginLoader.isPluginEnabled(name);
        
        // Отключаем плагин
        boolean disableSuccess = true;
        if (wasEnabled) {
            disableSuccess = pluginLoader.disablePlugin(name);
        }
        
        if (!disableSuccess) {
            sender.sendMessage(ChatColor.RED + "Не удалось отключить плагин '" + name + "' для перезагрузки");
            return;
        }
        
        // Включаем плагин, если он был включен
        if (wasEnabled) {
            boolean enableSuccess = pluginLoader.enablePlugin(name);
            
            if (enableSuccess) {
                sender.sendMessage(ChatColor.GREEN + "Плагин '" + name + "' успешно перезагружен");
            } else {
                sender.sendMessage(ChatColor.RED + "Не удалось включить плагин '" + name + "' после перезагрузки");
            }
        } else {
            sender.sendMessage(ChatColor.GREEN + "Плагин '" + name + "' успешно перезагружен (остался отключенным)");
        }
    }
} 