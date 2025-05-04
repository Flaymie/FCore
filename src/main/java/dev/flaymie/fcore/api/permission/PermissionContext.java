package dev.flaymie.fcore.api.permission;

import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Контекст проверки прав
 * Используется для передачи дополнительных условий при проверке прав
 */
public class PermissionContext {
    
    private final Map<String, Object> context;
    private final Player player;
    private final World world;
    private final long time;
    
    private PermissionContext(Player player, World world, long time, Map<String, Object> context) {
        this.player = player;
        this.world = world;
        this.time = time;
        this.context = context;
    }
    
    /**
     * Получает значение из контекста
     * @param key ключ
     * @return значение или null
     */
    public Object get(String key) {
        return context.get(key);
    }
    
    /**
     * Проверяет наличие ключа в контексте
     * @param key ключ
     * @return true, если ключ существует
     */
    public boolean has(String key) {
        return context.containsKey(key);
    }
    
    /**
     * Получает игрока из контекста
     * @return игрок
     */
    public Player getPlayer() {
        return player;
    }
    
    /**
     * Получает мир из контекста
     * @return мир
     */
    public World getWorld() {
        return world;
    }
    
    /**
     * Получает время из контекста
     * @return время
     */
    public long getTime() {
        return time;
    }
    
    /**
     * Создает builder для контекста
     * @return builder
     */
    public static Builder create() {
        return new Builder();
    }
    
    /**
     * Создает простой контекст для игрока
     * @param player игрок
     * @return контекст
     */
    public static PermissionContext forPlayer(Player player) {
        return create()
                .player(player)
                .world(player.getWorld())
                .time(player.getWorld().getTime())
                .build();
    }
    
    /**
     * Builder для создания контекстов
     */
    public static class Builder {
        private final Map<String, Object> context = new HashMap<>();
        private Player player;
        private World world;
        private long time;
        
        /**
         * Добавляет значение в контекст
         * @param key ключ
         * @param value значение
         * @return builder
         */
        public Builder add(String key, Object value) {
            context.put(key, value);
            return this;
        }
        
        /**
         * Добавляет игрока в контекст
         * @param player игрок
         * @return builder
         */
        public Builder player(Player player) {
            this.player = player;
            return this;
        }
        
        /**
         * Добавляет мир в контекст
         * @param world мир
         * @return builder
         */
        public Builder world(World world) {
            this.world = world;
            return this;
        }
        
        /**
         * Добавляет время в контекст
         * @param time время
         * @return builder
         */
        public Builder time(long time) {
            this.time = time;
            return this;
        }
        
        /**
         * Строит контекст
         * @return контекст
         */
        public PermissionContext build() {
            return new PermissionContext(player, world, time, context);
        }
    }
} 