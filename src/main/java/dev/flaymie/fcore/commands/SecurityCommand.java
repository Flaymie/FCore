package dev.flaymie.fcore.commands;

import dev.flaymie.fcore.FCore;
import dev.flaymie.fcore.api.annotation.Command;
import dev.flaymie.fcore.api.annotation.Permission;
import dev.flaymie.fcore.api.annotation.Subcommand;
import dev.flaymie.fcore.core.command.FCoreCommand;
import dev.flaymie.fcore.core.security.PluginSecurityAnalyzer;
import dev.flaymie.fcore.core.security.SecurityManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.List;

/**
 * Команда для управления безопасностью FCore
 */
@Command(name = "fcore.security", description = "Управление безопасностью FCore", aliases = {"fsec", "security"})
@Permission("fcore.admin.security")
public class SecurityCommand extends FCoreCommand {
    
    private final FCore plugin;
    private final SecurityManager securityManager;
    
    public SecurityCommand(FCore plugin) {
        super(plugin);
        this.plugin = plugin;
        this.securityManager = plugin.getSecurityManager();
    }
    
    /**
     * Отправляет справку по команде
     * @param sender отправитель
     */
    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GREEN + "=== Команды управления безопасностью FCore ===");
        sender.sendMessage(ChatColor.YELLOW + "/fcore security status " + ChatColor.WHITE + "- Статус системы безопасности");
        sender.sendMessage(ChatColor.YELLOW + "/fcore security scan <плагин> " + ChatColor.WHITE + "- Проверить плагин на уязвимости");
        sender.sendMessage(ChatColor.YELLOW + "/fcore security crashreports " + ChatColor.WHITE + "- Просмотр отчетов о сбоях");
        sender.sendMessage(ChatColor.YELLOW + "/fcore security viewreport <имя> " + ChatColor.WHITE + "- Просмотр отчета о сбое");
    }
    
    @Subcommand(value = "status", description = "Статус системы безопасности")
    @Permission("fcore.admin.security.status")
    public void statusCommand(CommandSender sender, String[] args) {
        sender.sendMessage(ChatColor.GREEN + "=== Статус системы безопасности FCore ===");
        sender.sendMessage(ChatColor.YELLOW + "Проверка лицензий: " + 
                (plugin.getConfig().getBoolean("security.license-check") ? ChatColor.GREEN + "Включена" : ChatColor.RED + "Отключена"));
        sender.sendMessage(ChatColor.YELLOW + "Проверка подписей: " + 
                (plugin.getConfig().getBoolean("security.verify-signatures") ? ChatColor.GREEN + "Включена" : ChatColor.RED + "Отключена"));
        sender.sendMessage(ChatColor.YELLOW + "Изоляция опасных операций: " + 
                (plugin.getConfig().getBoolean("security.isolate-unsafe") ? ChatColor.GREEN + "Включена" : ChatColor.RED + "Отключена"));
        sender.sendMessage(ChatColor.YELLOW + "Анализ плагинов: " + 
                (plugin.getConfig().getBoolean("security.analyze-plugins") ? ChatColor.GREEN + "Включен" : ChatColor.RED + "Отключен"));
        
        // Папка с отчетами о сбоях
        File crashReportsDir = new File(plugin.getDataFolder(), "crash-reports");
        int crashReportsCount = crashReportsDir.exists() ? crashReportsDir.listFiles(f -> f.getName().endsWith(".log")).length : 0;
        sender.sendMessage(ChatColor.YELLOW + "Отчеты о сбоях: " + ChatColor.AQUA + crashReportsCount);
    }
    
    @Subcommand(value = "scan", description = "Проверить плагин на уязвимости")
    @Permission("fcore.admin.security.scan")
    public void scanCommand(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Использование: /fcore security scan <имя_плагина>");
            return;
        }
        
        String pluginName = args[0];
        Plugin targetPlugin = Bukkit.getPluginManager().getPlugin(pluginName);
        
        if (targetPlugin == null) {
            sender.sendMessage(ChatColor.RED + "Плагин " + pluginName + " не найден!");
            return;
        }
        
        sender.sendMessage(ChatColor.YELLOW + "Сканирование плагина " + targetPlugin.getName() + " на уязвимости...");
        
        // Запускаем сканирование асинхронно
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            List<PluginSecurityAnalyzer.SecurityIssue> issues = securityManager.getSecurityAnalyzer().analyzePlugin(targetPlugin);
            
            // Отправляем результаты в основном потоке
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (issues.isEmpty()) {
                    sender.sendMessage(ChatColor.GREEN + "Уязвимости в плагине " + targetPlugin.getName() + " не обнаружены.");
                } else {
                    sender.sendMessage(ChatColor.RED + "В плагине " + targetPlugin.getName() + " обнаружены следующие проблемы:");
                    for (PluginSecurityAnalyzer.SecurityIssue issue : issues) {
                        sender.sendMessage(ChatColor.YELLOW + "- " + issue.toString());
                    }
                }
            });
        });
    }
    
    @Subcommand(value = "crashreports", description = "Просмотр отчетов о сбоях")
    @Permission("fcore.admin.security.crashreports")
    public void crashReportsCommand(CommandSender sender, String[] args) {
        File crashReportsDir = new File(plugin.getDataFolder(), "crash-reports");
        
        if (!crashReportsDir.exists() || crashReportsDir.listFiles() == null || crashReportsDir.listFiles().length == 0) {
            sender.sendMessage(ChatColor.YELLOW + "Отчеты о сбоях отсутствуют.");
            return;
        }
        
        File[] reports = crashReportsDir.listFiles(f -> f.getName().endsWith(".log"));
        
        if (reports.length == 0) {
            sender.sendMessage(ChatColor.YELLOW + "Отчеты о сбоях отсутствуют.");
            return;
        }
        
        sender.sendMessage(ChatColor.GREEN + "=== Отчеты о сбоях ===");
        for (int i = 0; i < Math.min(10, reports.length); i++) {
            File report = reports[i];
            String fileName = report.getName();
            String date = fileName.substring(6, 25); // extract date from "crash-yyyy-MM-dd_HH-mm-ss-plugin.log"
            String pluginName = fileName.substring(26, fileName.length() - 4); // extract plugin name
            
            sender.sendMessage(ChatColor.YELLOW + String.valueOf(i+1) + ". " + ChatColor.AQUA + date + ChatColor.YELLOW + 
                    " - Плагин: " + ChatColor.RED + pluginName);
        }
        
        if (reports.length > 10) {
            sender.sendMessage(ChatColor.YELLOW + "... и еще " + (reports.length - 10) + " отчетов.");
        }
        
        sender.sendMessage(ChatColor.YELLOW + "Просмотр отчета: /fcore security viewreport <имя_файла>");
    }
    
    @Subcommand(value = "viewreport", description = "Просмотр отчета о сбое")
    @Permission("fcore.admin.security.crashreports")
    public void viewReportCommand(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Использование: /fcore security viewreport <имя_файла>");
            return;
        }
        
        String fileName = args[0];
        if (!fileName.endsWith(".log")) {
            fileName += ".log";
        }
        
        File reportFile = new File(new File(plugin.getDataFolder(), "crash-reports"), fileName);
        
        if (!reportFile.exists()) {
            sender.sendMessage(ChatColor.RED + "Отчет не найден!");
            return;
        }
        
        // Если отправитель - игрок, передаем ему файл через плагин
        if (sender instanceof Player) {
            sender.sendMessage(ChatColor.YELLOW + "Отчет открыт. Используйте /fcore security crashreports для просмотра списка отчетов.");
            
            // Здесь можно реализовать отправку отчета игроку через чат или GUI
            // В данном примере просто выводим основную информацию
            sender.sendMessage(ChatColor.GREEN + "=== " + fileName + " ===");
            sender.sendMessage(ChatColor.YELLOW + "Размер: " + ChatColor.AQUA + (reportFile.length() / 1024) + " КБ");
            sender.sendMessage(ChatColor.YELLOW + "Для полного просмотра используйте FTP или консоль сервера.");
        } else {
            // Если отправитель - консоль, выводим сообщение
            sender.sendMessage(ChatColor.YELLOW + "Отчет находится в " + reportFile.getAbsolutePath());
        }
    }
} 