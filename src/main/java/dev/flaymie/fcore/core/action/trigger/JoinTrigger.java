package dev.flaymie.fcore.core.action.trigger;

import dev.flaymie.fcore.FCore;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import java.util.function.Predicate;
import org.bukkit.entity.Player;

/**
 * Триггер, срабатывающий при входе игрока на сервер
 */
public class JoinTrigger extends AbstractActionTrigger implements Listener {
    
    private final boolean firstJoinOnly;
    
    /**
     * Создает новый триггер входа игрока
     */
    public JoinTrigger() {
        this(false);
    }
    
    /**
     * Создает новый триггер входа игрока
     *
     * @param firstJoinOnly срабатывать только при первом входе
     */
    public JoinTrigger(boolean firstJoinOnly) {
        super("join");
        this.firstJoinOnly = firstJoinOnly;
    }
    
    /**
     * Создает новый триггер входа игрока с фильтром
     *
     * @param playerFilter фильтр игроков
     * @param firstJoinOnly срабатывать только при первом входе
     */
    public JoinTrigger(Predicate<Player> playerFilter, boolean firstJoinOnly) {
        super("join", playerFilter);
        this.firstJoinOnly = firstJoinOnly;
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!active) return;
        
        Player player = event.getPlayer();
        
        // Если условие первого входа
        if (firstJoinOnly && player.hasPlayedBefore()) {
            return;
        }
        
        // Выполняем действие с небольшой задержкой, чтобы дать игроку полностью войти
        Bukkit.getScheduler().runTaskLater(FCore.getInstance(), () -> executeAction(player), 5L);
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
     * Проверяет, настроен ли триггер на срабатывание только при первом входе
     *
     * @return true, если триггер срабатывает только при первом входе, иначе false
     */
    public boolean isFirstJoinOnly() {
        return firstJoinOnly;
    }
} 