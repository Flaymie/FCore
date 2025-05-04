package dev.flaymie.fcore.commands;

import dev.flaymie.fcore.core.action.Action;
import dev.flaymie.fcore.core.action.ActionManager;
import dev.flaymie.fcore.core.action.ActionSequence;
import dev.flaymie.fcore.core.action.debug.ActionDebugEntry;
import dev.flaymie.fcore.api.annotation.Command;
import dev.flaymie.fcore.api.annotation.Permission;
import dev.flaymie.fcore.api.annotation.Subcommand;
import dev.flaymie.fcore.api.annotation.Inject;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Команда для отладки действий
 */
@Command(name = "actiondebug", description = "Управление отладкой действий", aliases = {"adebug"})
@Permission("fcore.action.debug")
public class ActionDebugCommand {
    
    @Inject
    private ActionManager actionManager;
    
    /**
     * Переключает режим отладки
     */
    @Subcommand(value = "toggle", description = "Включает или выключает режим отладки", aliases = {"t"})
    public void toggleDebug(Player player) {
        boolean enabled = actionManager.toggleDebug(player);
        player.sendMessage(ChatColor.YELLOW + "Режим отладки действий " + 
                (enabled ? ChatColor.GREEN + "включен" : ChatColor.RED + "выключен"));
    }
    
    /**
     * Показывает последние записи лога отладки
     */
    @Subcommand(value = "log", description = "Показывает лог отладки", aliases = {"l"})
    public void showLog(Player player) {
        List<ActionDebugEntry> logs = actionManager.getDebugger().getLastDebugLogs(player, 10);
        
        if (logs.isEmpty()) {
            player.sendMessage(ChatColor.RED + "Лог отладки пуст");
            return;
        }
        
        player.sendMessage(ChatColor.GREEN + "=== Последние записи лога отладки ===");
        
        for (ActionDebugEntry entry : logs) {
            player.sendMessage(ChatColor.GRAY + "" + entry.getMessage());
        }
    }
    
    /**
     * Очищает лог отладки
     */
    @Subcommand(value = "clear", description = "Очищает лог отладки", aliases = {"c"})
    public void clearLog(Player player) {
        actionManager.getDebugger().clearDebugLogs(player);
        player.sendMessage(ChatColor.GREEN + "Лог отладки очищен");
    }
    
    /**
     * Показывает информацию о последовательности действий
     */
    @Subcommand(value = "info", description = "Показывает информацию о последовательности", aliases = {"i"})
    public void showInfo(Player player, String sequenceId) {
        Action action = actionManager.getAction(sequenceId);
        
        if (action == null) {
            player.sendMessage(ChatColor.RED + "Действие с ID " + sequenceId + " не найдено");
            return;
        }
        
        if (action instanceof ActionSequence) {
            actionManager.getDebugger().showSequenceDebugInfo(player, (ActionSequence) action);
        } else {
            player.sendMessage(ChatColor.RED + "Действие не является последовательностью");
        }
    }
    
    /**
     * Показывает помощь по команде
     */
    @Subcommand(value = "help", description = "Показывает справку", aliases = {"h"})
    public void showHelp(Player player) {
        player.sendMessage(ChatColor.GREEN + "=== Команды отладки действий ===");
        player.sendMessage(ChatColor.YELLOW + "/adebug toggle " + ChatColor.GRAY + "- Включить/выключить режим отладки");
        player.sendMessage(ChatColor.YELLOW + "/adebug log " + ChatColor.GRAY + "- Показать лог отладки");
        player.sendMessage(ChatColor.YELLOW + "/adebug clear " + ChatColor.GRAY + "- Очистить лог отладки");
        player.sendMessage(ChatColor.YELLOW + "/adebug info <id> " + ChatColor.GRAY + "- Информация о последовательности");
    }
} 