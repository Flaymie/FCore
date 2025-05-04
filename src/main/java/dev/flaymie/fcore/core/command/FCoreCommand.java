package dev.flaymie.fcore.core.command;

import dev.flaymie.fcore.FCore;
import dev.flaymie.fcore.api.annotation.Argument;
import dev.flaymie.fcore.api.annotation.Command;
import dev.flaymie.fcore.api.annotation.Permission;
import dev.flaymie.fcore.api.annotation.Subcommand;
import dev.flaymie.fcore.core.config.FCoreConfig;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Основная команда управления ядром
 */
@Command(name = "fcore", description = "Основная команда ядра FCore", aliases = {"fc"})
@Permission("fcore.command.fcore")
public class FCoreCommand {
    
    private final FCore plugin;
    
    public FCoreCommand(FCore plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Подкоманда для получения информации о ядре
     */
    @Subcommand(value = "info", description = "Информация о ядре")
    public void infoCommand(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== FCore ===");
        sender.sendMessage(ChatColor.YELLOW + "Версия: " + ChatColor.WHITE + plugin.getDescription().getVersion());
        sender.sendMessage(ChatColor.YELLOW + "Разработчик: " + ChatColor.WHITE + "Flaymie");
        sender.sendMessage(ChatColor.YELLOW + "Локализация: " + ChatColor.WHITE + plugin.getCoreConfig().getLocale());
        sender.sendMessage(ChatColor.YELLOW + "Режим отладки: " + ChatColor.WHITE + 
                          (plugin.getCoreConfig().isDebugMode() ? "включен" : "выключен"));
        sender.sendMessage(ChatColor.YELLOW + "Автоскан сервисов: " + ChatColor.WHITE + 
                          (plugin.getCoreConfig().isAutoScanServices() ? "включен" : "выключен"));
    }
    
    /**
     * Подкоманда для перезагрузки ядра
     */
    @Subcommand(value = "reload", description = "Перезагружает конфигурацию ядра")
    @Permission("fcore.command.reload")
    public void reloadCommand(CommandSender sender) {
        plugin.getCoreConfig().loadConfig();
        sender.sendMessage(ChatColor.GREEN + "Конфигурация ядра перезагружена!");
    }
    
    /**
     * Подкоманда для переключения режима отладки
     */
    @Subcommand(value = "debug", description = "Включает/выключает режим отладки")
    @Permission("fcore.command.debug")
    public void debugCommand(CommandSender sender, @Argument(value = "состояние", description = "true/false") boolean state) {
        plugin.getCoreConfig().setDebugMode(state);
        sender.sendMessage(ChatColor.GREEN + "Режим отладки " + 
                          (state ? "включен" : "выключен"));
    }
    
    /**
     * Подкоманда для установки локализации
     */
    @Subcommand(value = "locale", description = "Устанавливает локализацию")
    @Permission("fcore.command.locale")
    public void localeCommand(CommandSender sender, @Argument(value = "локализация", description = "Код локали (ru_RU, en_US)") String locale) {
        plugin.getCoreConfig().setLocale(locale);
        sender.sendMessage(ChatColor.GREEN + "Локализация установлена: " + locale);
    }
    
    /**
     * Подкоманда для проверки состояния
     */
    @Subcommand(value = "status", description = "Показывает статус сервисов ядра")
    @Permission("fcore.command.status")
    public void statusCommand(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== Статус сервисов ===");
        sender.sendMessage(ChatColor.YELLOW + "Всего сервисов: " + 
                          ChatColor.WHITE + plugin.getServiceManager().getServicesCount());
        
        // Пока это заглушка, позже можно будет отображать статус каждого сервиса
    }
    
    /**
     * Подкоманда для приветствия игрока
     */
    @Subcommand(value = "hello", description = "Приветствует игрока", playerOnly = true)
    public void helloCommand(Player player) {
        player.sendMessage(ChatColor.GREEN + "Привет, " + player.getName() + "!");
        player.sendMessage(ChatColor.YELLOW + "Добро пожаловать в мир FCore!");
    }
} 