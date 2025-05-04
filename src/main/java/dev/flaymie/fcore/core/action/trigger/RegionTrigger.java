package dev.flaymie.fcore.core.action.trigger;

import dev.flaymie.fcore.FCore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Триггер, срабатывающий при входе/выходе из региона
 */
public class RegionTrigger extends AbstractActionTrigger implements Listener {
    
    private final Location min;
    private final Location max;
    private final boolean executeOnEntry;
    private final boolean executeOnExit;
    private final Set<UUID> playersInRegion;
    
    /**
     * Создает новый триггер региона
     *
     * @param min минимальная точка региона
     * @param max максимальная точка региона
     * @param executeOnEntry выполнять действие при входе
     * @param executeOnExit выполнять действие при выходе
     */
    public RegionTrigger(Location min, Location max, boolean executeOnEntry, boolean executeOnExit) {
        super("region");
        
        // Убеждаемся, что world одинаковый
        if (!min.getWorld().equals(max.getWorld())) {
            throw new IllegalArgumentException("Точки должны быть в одном мире");
        }
        
        this.min = min;
        this.max = max;
        this.executeOnEntry = executeOnEntry;
        this.executeOnExit = executeOnExit;
        this.playersInRegion = new HashSet<>();
    }
    
    /**
     * Создает новый триггер региона с фильтром
     *
     * @param min минимальная точка региона
     * @param max максимальная точка региона
     * @param executeOnEntry выполнять действие при входе
     * @param executeOnExit выполнять действие при выходе
     * @param playerFilter фильтр игроков
     */
    public RegionTrigger(Location min, Location max, boolean executeOnEntry, boolean executeOnExit, Predicate<Player> playerFilter) {
        super("region", playerFilter);
        
        // Убеждаемся, что world одинаковый
        if (!min.getWorld().equals(max.getWorld())) {
            throw new IllegalArgumentException("Точки должны быть в одном мире");
        }
        
        this.min = min;
        this.max = max;
        this.executeOnEntry = executeOnEntry;
        this.executeOnExit = executeOnExit;
        this.playersInRegion = new HashSet<>();
    }
    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!active) return;
        
        // Оптимизация: проверяем только если игрок изменил блок
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockY() == event.getTo().getBlockY() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }
        
        Player player = event.getPlayer();
        if (!matchesPlayer(player)) {
            return;
        }
        
        boolean wasInRegion = playersInRegion.contains(player.getUniqueId());
        boolean isInRegion = isInRegion(event.getTo());
        
        // Игрок вошел в регион
        if (!wasInRegion && isInRegion) {
            playersInRegion.add(player.getUniqueId());
            if (executeOnEntry) {
                executeAction(player);
            }
        }
        // Игрок вышел из региона
        else if (wasInRegion && !isInRegion) {
            playersInRegion.remove(player.getUniqueId());
            if (executeOnExit) {
                executeAction(player);
            }
        }
    }
    
    /**
     * Проверяет, находится ли локация в регионе
     *
     * @param location локация для проверки
     * @return true, если локация в регионе, иначе false
     */
    public boolean isInRegion(Location location) {
        if (!location.getWorld().equals(min.getWorld())) {
            return false;
        }
        
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        
        return x >= min.getX() && x <= max.getX() &&
               y >= min.getY() && y <= max.getY() &&
               z >= min.getZ() && z <= max.getZ();
    }
    
    @Override
    protected void onActivate() {
        Bukkit.getPluginManager().registerEvents(this, FCore.getInstance());
        
        // Проверяем всех онлайн игроков
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (matchesPlayer(player) && isInRegion(player.getLocation())) {
                playersInRegion.add(player.getUniqueId());
            }
        }
    }
    
    @Override
    protected void onDeactivate() {
        HandlerList.unregisterAll(this);
        playersInRegion.clear();
    }
    
    /**
     * Получает минимальную точку региона
     *
     * @return минимальная точка
     */
    public Location getMin() {
        return min.clone();
    }
    
    /**
     * Получает максимальную точку региона
     *
     * @return максимальная точка
     */
    public Location getMax() {
        return max.clone();
    }
    
    /**
     * Проверяет, выполняется ли действие при входе в регион
     *
     * @return true, если действие выполняется при входе, иначе false
     */
    public boolean isExecuteOnEntry() {
        return executeOnEntry;
    }
    
    /**
     * Проверяет, выполняется ли действие при выходе из региона
     *
     * @return true, если действие выполняется при выходе, иначе false
     */
    public boolean isExecuteOnExit() {
        return executeOnExit;
    }
} 