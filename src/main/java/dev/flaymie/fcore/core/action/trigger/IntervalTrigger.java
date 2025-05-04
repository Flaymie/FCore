package dev.flaymie.fcore.core.action.trigger;

import dev.flaymie.fcore.FCore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.function.Predicate;

/**
 * Триггер, срабатывающий через определенные интервалы времени
 */
public class IntervalTrigger extends AbstractActionTrigger {
    
    private final long intervalTicks;
    private int taskId = -1;
    
    /**
     * Создает новый триггер интервала
     *
     * @param intervalTicks интервал в тиках (20 тиков = 1 секунда)
     */
    public IntervalTrigger(long intervalTicks) {
        super("interval");
        this.intervalTicks = intervalTicks;
    }
    
    /**
     * Создает новый триггер интервала с фильтром игроков
     *
     * @param intervalTicks интервал в тиках (20 тиков = 1 секунда)
     * @param playerFilter фильтр игроков
     */
    public IntervalTrigger(long intervalTicks, Predicate<Player> playerFilter) {
        super("interval", playerFilter);
        this.intervalTicks = intervalTicks;
    }
    
    @Override
    protected void onActivate() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
        
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(FCore.getInstance(), () -> {
            // Выполняем действие для всех подходящих игроков
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (matchesPlayer(player)) {
                    executeAction(player);
                }
            }
        }, intervalTicks, intervalTicks);
    }
    
    @Override
    protected void onDeactivate() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }
    
    /**
     * Получает интервал в тиках
     *
     * @return интервал в тиках
     */
    public long getIntervalTicks() {
        return intervalTicks;
    }
} 