package dev.flaymie.fcore.core.action.trigger;

import dev.flaymie.fcore.core.action.Action;
import org.bukkit.entity.Player;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Базовая реализация триггера действий
 */
public abstract class AbstractActionTrigger implements ActionTrigger {
    
    protected final String id;
    protected final String type;
    protected Action action;
    protected boolean active;
    protected Predicate<Player> playerFilter;
    
    /**
     * Создает новый триггер
     *
     * @param type тип триггера
     */
    public AbstractActionTrigger(String type) {
        this(type, null);
    }
    
    /**
     * Создает новый триггер с фильтром игроков
     *
     * @param type тип триггера
     * @param playerFilter фильтр игроков
     */
    public AbstractActionTrigger(String type, Predicate<Player> playerFilter) {
        this.id = type + "-" + UUID.randomUUID().toString().substring(0, 8);
        this.type = type;
        this.active = false;
        this.playerFilter = playerFilter;
    }
    
    @Override
    public void activate(Action action) {
        this.action = action;
        this.active = true;
        onActivate();
    }
    
    @Override
    public boolean isActive() {
        return active;
    }
    
    @Override
    public void deactivate() {
        this.active = false;
        onDeactivate();
    }
    
    @Override
    public String getType() {
        return type;
    }
    
    @Override
    public boolean matchesPlayer(Player player) {
        return playerFilter == null || playerFilter.test(player);
    }
    
    /**
     * Устанавливает фильтр игроков
     *
     * @param playerFilter фильтр игроков
     */
    public void setPlayerFilter(Predicate<Player> playerFilter) {
        this.playerFilter = playerFilter;
    }
    
    /**
     * Получает ID триггера
     *
     * @return ID триггера
     */
    public String getId() {
        return id;
    }
    
    /**
     * Вызывается при активации триггера
     * Может быть переопределен в подклассах для выполнения дополнительных действий
     */
    protected void onActivate() {
        // По умолчанию ничего не делает
    }
    
    /**
     * Вызывается при деактивации триггера
     * Может быть переопределен в подклассах для выполнения дополнительных действий
     */
    protected void onDeactivate() {
        // По умолчанию ничего не делает
    }
    
    /**
     * Выполняет привязанное действие для указанного игрока
     *
     * @param player игрок
     */
    protected void executeAction(Player player) {
        if (action != null && active && matchesPlayer(player)) {
            action.execute(player);
        }
    }
} 