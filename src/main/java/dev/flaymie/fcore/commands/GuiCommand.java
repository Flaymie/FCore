package dev.flaymie.fcore.commands;

import dev.flaymie.fcore.api.gui.example.GuiExample;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Команда для демонстрации GUI
 */
public class GuiCommand implements CommandExecutor, TabCompleter {
    
    private final List<String> subCommands = Arrays.asList("simple", "paginated", "animated", "combined");
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Эта команда доступна только для игроков!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            player.sendMessage(ChatColor.GREEN + "Использование: /gui <simple|paginated|animated|combined>");
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "simple":
                GuiExample.openSimpleGui(player);
                break;
            case "paginated":
                GuiExample.openPaginatedGui(player);
                break;
            case "animated":
                GuiExample.openAnimatedGui(player);
                break;
            case "combined":
                GuiExample.openCombinedGui(player);
                break;
            default:
                player.sendMessage(ChatColor.RED + "Неизвестная подкоманда! Используйте: /gui <simple|paginated|animated|combined>");
                break;
        }
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String current = args[0].toLowerCase();
            return subCommands.stream()
                    .filter(sub -> sub.startsWith(current))
                    .collect(Collectors.toList());
        }
        
        return new ArrayList<>();
    }
} 