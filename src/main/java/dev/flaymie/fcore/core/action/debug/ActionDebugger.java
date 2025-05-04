package dev.flaymie.fcore.core.action.debug;

import dev.flaymie.fcore.FCore;
import dev.flaymie.fcore.core.action.Action;
import dev.flaymie.fcore.core.action.ActionSequence;
import dev.flaymie.fcore.core.action.ParallelActionSequence;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Класс для отладки действий и цепочек
 */
public class ActionDebugger {
    
    private final FCore plugin;
    private final Map<UUID, Boolean> debugEnabled;
    private final Map<UUID, List<ActionDebugEntry>> debugLogs;
    private final Map<String, Long> executionTimes;
    
    private boolean globalDebugEnabled;
    private int debugLogMaxSize = 100;
    
    /**
     * Создает новый отладчик действий
     *
     * @param plugin экземпляр плагина
     */
    public ActionDebugger(FCore plugin) {
        this.plugin = plugin;
        this.debugEnabled = new ConcurrentHashMap<>();
        this.debugLogs = new ConcurrentHashMap<>();
        this.executionTimes = new ConcurrentHashMap<>();
        this.globalDebugEnabled = false;
    }
    
    /**
     * Включает или отключает отладку для конкретного игрока
     *
     * @param player игрок
     * @param enabled включена ли отладка
     * @return предыдущее состояние
     */
    public boolean setDebugEnabled(Player player, boolean enabled) {
        boolean prev = isDebugEnabled(player);
        debugEnabled.put(player.getUniqueId(), enabled);
        
        if (enabled) {
            player.sendMessage(ChatColor.GREEN + "Режим отладки действий включен");
        } else {
            player.sendMessage(ChatColor.RED + "Режим отладки действий выключен");
        }
        
        return prev;
    }
    
    /**
     * Переключает режим отладки для игрока
     *
     * @param player игрок
     * @return новое состояние
     */
    public boolean toggleDebug(Player player) {
        boolean newState = !isDebugEnabled(player);
        return setDebugEnabled(player, newState);
    }
    
    /**
     * Включает или отключает глобальную отладку
     *
     * @param enabled включена ли отладка
     * @return предыдущее состояние
     */
    public boolean setGlobalDebugEnabled(boolean enabled) {
        boolean prev = globalDebugEnabled;
        globalDebugEnabled = enabled;
        return prev;
    }
    
    /**
     * Проверяет, включена ли отладка для игрока
     *
     * @param player игрок
     * @return включена ли отладка
     */
    public boolean isDebugEnabled(Player player) {
        return globalDebugEnabled || debugEnabled.getOrDefault(player.getUniqueId(), false);
    }
    
    /**
     * Устанавливает максимальный размер лога отладки
     *
     * @param size размер лога
     */
    public void setDebugLogMaxSize(int size) {
        this.debugLogMaxSize = size;
    }
    
    /**
     * Логирует начало выполнения действия
     *
     * @param player игрок
     * @param action действие
     */
    public void logActionStart(Player player, Action action) {
        if (!isDebugEnabled(player)) {
            return;
        }
        
        long startTime = System.currentTimeMillis();
        executionTimes.put(action.getId(), startTime);
        
        String actionType = getActionTypeName(action);
        String message = ChatColor.YELLOW + "Начало выполнения действия: " + 
                ChatColor.AQUA + action.getName() + 
                ChatColor.YELLOW + " (" + actionType + ")";
        
        logDebugMessage(player, message);
        
        // Сообщаем игроку, если отладка включена
        if (isDebugEnabled(player)) {
            player.sendMessage(message);
        }
    }
    
    /**
     * Логирует завершение выполнения действия
     *
     * @param player игрок
     * @param action действие
     * @param success успешно ли выполнено
     */
    public void logActionEnd(Player player, Action action, boolean success) {
        if (!isDebugEnabled(player)) {
            return;
        }
        
        long endTime = System.currentTimeMillis();
        long startTime = executionTimes.getOrDefault(action.getId(), endTime);
        long duration = endTime - startTime;
        
        String actionType = getActionTypeName(action);
        String status = success ? ChatColor.GREEN + "успешно" : ChatColor.RED + "с ошибкой";
        String message = ChatColor.YELLOW + "Завершение действия: " + 
                ChatColor.AQUA + action.getName() + 
                ChatColor.YELLOW + " (" + actionType + ") " + 
                status + ChatColor.YELLOW + " за " + 
                ChatColor.WHITE + duration + "мс";
        
        logDebugMessage(player, message);
        
        // Сообщаем игроку, если отладка включена
        if (isDebugEnabled(player)) {
            player.sendMessage(message);
        }
    }
    
    /**
     * Логирует ошибку при выполнении действия
     *
     * @param player игрок
     * @param action действие
     * @param error ошибка
     */
    public void logActionError(Player player, Action action, Throwable error) {
        if (!isDebugEnabled(player)) {
            return;
        }
        
        String actionType = getActionTypeName(action);
        String message = ChatColor.RED + "Ошибка в действии: " + 
                ChatColor.AQUA + action.getName() + 
                ChatColor.RED + " (" + actionType + "): " + 
                ChatColor.WHITE + error.getMessage();
        
        logDebugMessage(player, message);
        
        // Сообщаем игроку и логируем ошибку
        if (isDebugEnabled(player)) {
            player.sendMessage(message);
        }
        
        plugin.getLogger().log(Level.WARNING, "Ошибка при выполнении действия " + action.getId(), error);
    }
    
    /**
     * Добавляет запись в лог отладки
     *
     * @param player игрок
     * @param message сообщение
     */
    private void logDebugMessage(Player player, String message) {
        UUID playerId = player.getUniqueId();
        
        if (!debugLogs.containsKey(playerId)) {
            debugLogs.put(playerId, new ArrayList<>());
        }
        
        List<ActionDebugEntry> logs = debugLogs.get(playerId);
        logs.add(new ActionDebugEntry(System.currentTimeMillis(), message));
        
        // Ограничиваем размер лога
        if (logs.size() > debugLogMaxSize) {
            logs.remove(0);
        }
    }
    
    /**
     * Получает последние записи лога отладки для игрока
     *
     * @param player игрок
     * @param count количество записей
     * @return список записей
     */
    public List<ActionDebugEntry> getLastDebugLogs(Player player, int count) {
        List<ActionDebugEntry> logs = debugLogs.getOrDefault(player.getUniqueId(), new ArrayList<>());
        int fromIndex = Math.max(0, logs.size() - count);
        return new ArrayList<>(logs.subList(fromIndex, logs.size()));
    }
    
    /**
     * Очищает лог отладки для игрока
     *
     * @param player игрок
     */
    public void clearDebugLogs(Player player) {
        debugLogs.remove(player.getUniqueId());
    }
    
    /**
     * Получает имя типа действия
     *
     * @param action действие
     * @return имя типа
     */
    private String getActionTypeName(Action action) {
        if (action instanceof ActionSequence) {
            return "Последовательность";
        } else if (action instanceof ParallelActionSequence) {
            return "Параллельное выполнение";
        } else {
            return action.getClass().getSimpleName().replace("Action", "");
        }
    }
    
    /**
     * Показывает отладочную информацию о последовательности действий
     *
     * @param player игрок
     * @param sequence последовательность
     */
    public void showSequenceDebugInfo(Player player, ActionSequence sequence) {
        if (!isDebugEnabled(player)) {
            return;
        }
        
        player.sendMessage(ChatColor.GREEN + "=== Отладка последовательности: " + 
                ChatColor.YELLOW + sequence.getName() + ChatColor.GREEN + " ===");
        
        List<Action> actions = sequence.getActions();
        for (int i = 0; i < actions.size(); i++) {
            Action action = actions.get(i);
            String type = getActionTypeName(action);
            
            player.sendMessage(ChatColor.YELLOW + String.valueOf(i + 1) + ". " + 
                    ChatColor.AQUA + action.getName() + 
                    ChatColor.GRAY + " (" + type + ")");
        }
        
        player.sendMessage(ChatColor.GREEN + "=== Всего действий: " + 
                ChatColor.YELLOW + actions.size() + ChatColor.GREEN + " ===");
    }
} 