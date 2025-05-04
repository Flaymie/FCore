package dev.flaymie.fcore.core.action;

import dev.flaymie.fcore.FCore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.concurrent.CompletableFuture;

/**
 * Действие, ожидающее указанное количество тиков
 */
public class WaitAction extends AbstractAction {
    
    private final int ticks;
    
    /**
     * Создает новое действие ожидания
     *
     * @param ticks количество тиков (20 тиков = 1 секунда)
     */
    public WaitAction(int ticks) {
        super("Wait", "Ожидает " + ticks + " тиков (" + (ticks / 20.0) + " сек)", true);
        this.ticks = ticks;
    }
    
    @Override
    public CompletableFuture<Void> execute(Player player, ActionContext context) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        Bukkit.getScheduler().runTaskLater(FCore.getInstance(), () -> {
            future.complete(null);
        }, ticks);
        
        return future;
    }
    
    /**
     * Получает количество тиков ожидания
     *
     * @return количество тиков
     */
    public int getTicks() {
        return ticks;
    }
} 