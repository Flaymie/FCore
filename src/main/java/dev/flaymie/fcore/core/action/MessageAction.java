package dev.flaymie.fcore.core.action;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import java.util.concurrent.CompletableFuture;

/**
 * Действие для отправки сообщения игроку
 */
public class MessageAction extends AbstractAction {
    
    private final String message;
    
    /**
     * Создает новое действие отправки сообщения
     *
     * @param message сообщение для отправки
     */
    public MessageAction(String message) {
        super("Message", "Отправляет сообщение игроку", false);
        this.message = message;
    }
    
    @Override
    public CompletableFuture<Void> execute(Player player, ActionContext context) {
        // Отправляем сообщение с цветовым форматированием
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        return CompletableFuture.completedFuture(null);
    }
    
    /**
     * Получает текст сообщения
     *
     * @return текст сообщения
     */
    public String getMessage() {
        return message;
    }
} 