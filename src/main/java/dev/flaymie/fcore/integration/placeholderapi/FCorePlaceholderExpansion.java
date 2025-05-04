package dev.flaymie.fcore.integration.placeholderapi;

import dev.flaymie.fcore.FCore;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Класс для регистрации плейсхолдеров FCore в PlaceholderAPI
 */
public class FCorePlaceholderExpansion extends PlaceholderExpansion {
    
    private final FCore plugin;
    private final Map<String, Function<Player, String>> placeholders;
    
    public FCorePlaceholderExpansion(FCore plugin) {
        this.plugin = plugin;
        this.placeholders = new HashMap<>();
        
        // Регистрируем базовые плейсхолдеры
        registerDefaultPlaceholders();
    }
    
    /**
     * Регистрация плейсхолдера
     * @param key ключ плейсхолдера (без %fcore_%)
     * @param function функция, возвращающая значение плейсхолдера
     */
    public void registerPlaceholder(String key, Function<Player, String> function) {
        placeholders.put(key.toLowerCase(), function);
    }
    
    /**
     * Регистрация встроенных плейсхолдеров
     */
    private void registerDefaultPlaceholders() {
        // Версия ядра
        registerPlaceholder("version", player -> plugin.getDescription().getVersion());
        
        // Количество онлайн игроков
        registerPlaceholder("online", player -> String.valueOf(Bukkit.getOnlinePlayers().size()));
        
        // Максимальное количество игроков
        registerPlaceholder("max_players", player -> String.valueOf(Bukkit.getMaxPlayers()));
        
        // Имя сервера из конфига
        registerPlaceholder("server_name", player -> plugin.getCoreConfig().getServerName());
        
        // Имя мира игрока
        registerPlaceholder("world", player -> player != null ? player.getWorld().getName() : "unknown");
    }
    
    @Override
    public String getIdentifier() {
        return "fcore";
    }
    
    @Override
    public String getAuthor() {
        return plugin.getDescription().getAuthors().get(0);
    }
    
    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }
    
    @Override
    public boolean persist() {
        return true;
    }
    
    @Override
    public String onRequest(OfflinePlayer player, String identifier) {
        identifier = identifier.toLowerCase();
        
        if (player == null) {
            // Обрабатываем плейсхолдеры, не требующие наличия игрока
            if (placeholders.containsKey(identifier)) {
                return placeholders.get(identifier).apply(null);
            }
            return "";
        }
        
        if (!player.isOnline()) {
            return "";
        }
        
        Player onlinePlayer = player.getPlayer();
        
        if (placeholders.containsKey(identifier)) {
            return placeholders.get(identifier).apply(onlinePlayer);
        }
        
        return null; // PlaceholderAPI продолжит искать плейсхолдер в других расширениях
    }
} 