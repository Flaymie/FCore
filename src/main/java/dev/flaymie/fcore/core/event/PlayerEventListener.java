package dev.flaymie.fcore.core.event;

import dev.flaymie.fcore.api.annotation.EventListener;
import dev.flaymie.fcore.api.annotation.IgnoreCancelled;
import dev.flaymie.fcore.api.annotation.Priority;
import dev.flaymie.fcore.api.event.AbstractEventListener;
import dev.flaymie.fcore.api.event.PlayerServiceEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.logging.Logger;

/**
 * Пример слушателя событий, связанных с игроками
 */
@EventListener(priority = 10)
public class PlayerEventListener extends AbstractEventListener {
    
    private Logger logger;
    
    @Override
    public boolean isInitialized() {
        if (!super.isInitialized()) {
            return false;
        }
        
        if (logger == null) {
            logger = plugin.getLogger();
        }
        
        return true;
    }
    
    @EventHandler
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        // Пример обработки асинхронного события входа игрока
        logger.fine("Игрок " + event.getName() + " пытается войти с IP " + event.getAddress().getHostAddress());
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    @IgnoreCancelled
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Пример обработки события входа игрока
        Player player = event.getPlayer();
        logger.info("Игрок " + player.getName() + " вошел в игру!");
        
        // Вызов кастомного события
        PlayerServiceEvent serviceEvent = new PlayerServiceEvent(
            player,
            PlayerServiceEvent.ServiceAction.CREATED,
            "PlayerJoinService"
        );
        
        callEvent(serviceEvent);
        
        if (serviceEvent.isCancelled()) {
            logger.warning("Событие PlayerServiceEvent было отменено");
        }
    }
    
    @EventHandler
    @Priority(EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Пример обработки события выхода игрока
        Player player = event.getPlayer();
        logger.info("Игрок " + player.getName() + " вышел из игры!");
        
        // Вызов кастомного события
        PlayerServiceEvent serviceEvent = new PlayerServiceEvent(
            player,
            PlayerServiceEvent.ServiceAction.DESTROYED,
            "PlayerQuitService"
        );
        
        callEvent(serviceEvent);
    }
    
    /**
     * Пример обработчика кастомного события
     */
    @EventHandler
    public void onPlayerService(PlayerServiceEvent event) {
        // Пример обработки кастомного события
        logger.info("Получено событие сервиса игрока: " + event.toString());
        
        // Изменение данных события для последующих обработчиков
        if (event.getAction() == PlayerServiceEvent.ServiceAction.CREATED) {
            event.setData("Созданный сервис для " + event.getPlayer().getName());
        }
    }
} 