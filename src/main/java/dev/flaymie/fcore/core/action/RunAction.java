package dev.flaymie.fcore.core.action;

import org.bukkit.entity.Player;
import java.util.concurrent.CompletableFuture;

/**
 * Действие, выполняющее произвольный код
 */
public class RunAction extends AbstractAction {
    
    private final ActionRunnable runnable;
    
    /**
     * Создает новое действие
     *
     * @param runnable код для выполнения
     */
    public RunAction(ActionRunnable runnable) {
        super("Run", "Выполняет произвольный код", false);
        this.runnable = runnable;
    }
    
    @Override
    public CompletableFuture<Void> execute(Player player, ActionContext context) {
        try {
            runnable.run(player);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            CompletableFuture<Void> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
} 