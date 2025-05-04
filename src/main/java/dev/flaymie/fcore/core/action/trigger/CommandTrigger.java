package dev.flaymie.fcore.core.action.trigger;

import dev.flaymie.fcore.FCore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.function.Predicate;

/**
 * Триггер, срабатывающий при вводе определенной команды
 */
public class CommandTrigger extends AbstractActionTrigger implements Listener {
    
    private final String command;
    private final boolean cancelCommand;
    
    /**
     * Создает новый триггер команды
     *
     * @param command команда (без слеша в начале)
     * @param cancelCommand отменять ли выполнение команды после срабатывания
     */
    public CommandTrigger(String command, boolean cancelCommand) {
        super("command");
        this.command = command.toLowerCase().startsWith("/") ? command.toLowerCase() : "/" + command.toLowerCase();
        this.cancelCommand = cancelCommand;
    }
    
    /**
     * Создает новый триггер команды с фильтром
     *
     * @param command команда (без слеша в начале)
     * @param cancelCommand отменять ли выполнение команды после срабатывания
     * @param playerFilter фильтр игроков
     */
    public CommandTrigger(String command, boolean cancelCommand, Predicate<Player> playerFilter) {
        super("command", playerFilter);
        this.command = command.toLowerCase().startsWith("/") ? command.toLowerCase() : "/" + command.toLowerCase();
        this.cancelCommand = cancelCommand;
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if (!active) return;
        
        String message = event.getMessage().toLowerCase();
        Player player = event.getPlayer();
        
        // Проверяем, соответствует ли команда
        if (message.equals(command) || message.startsWith(command + " ")) {
            if (matchesPlayer(player)) {
                executeAction(player);
                
                // Отменяем команду, если нужно
                if (cancelCommand) {
                    event.setCancelled(true);
                }
            }
        }
    }
    
    @Override
    protected void onActivate() {
        Bukkit.getPluginManager().registerEvents(this, FCore.getInstance());
    }
    
    @Override
    protected void onDeactivate() {
        HandlerList.unregisterAll(this);
    }
    
    /**
     * Получает команду
     *
     * @return команда
     */
    public String getCommand() {
        return command;
    }
    
    /**
     * Проверяет, нужно ли отменять команду после срабатывания триггера
     *
     * @return true, если команда должна быть отменена, иначе false
     */
    public boolean isCancelCommand() {
        return cancelCommand;
    }
} 