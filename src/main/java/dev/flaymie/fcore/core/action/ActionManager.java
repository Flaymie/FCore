package dev.flaymie.fcore.core.action;

import dev.flaymie.fcore.FCore;
import dev.flaymie.fcore.core.action.debug.ActionDebugger;
import dev.flaymie.fcore.core.action.trigger.ActionTrigger;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * Менеджер действий и триггеров
 */
public class ActionManager {
    
    private final FCore plugin;
    private final Map<String, Action> registeredActions;
    private final Map<String, ActionTrigger> activeTriggers;
    private final Map<UUID, Map<String, ActionContext>> playerContexts;
    private final ActionDebugger debugger;
    
    /**
     * Создает новый менеджер действий
     *
     * @param plugin экземпляр плагина
     */
    public ActionManager(FCore plugin) {
        this.plugin = plugin;
        this.registeredActions = new HashMap<>();
        this.activeTriggers = new HashMap<>();
        this.playerContexts = new ConcurrentHashMap<>();
        this.debugger = new ActionDebugger(plugin);
    }
    
    /**
     * Регистрирует действие
     *
     * @param action действие
     */
    public void registerAction(Action action) {
        registeredActions.put(action.getId(), action);
    }
    
    /**
     * Удаляет регистрацию действия
     *
     * @param actionId ID действия
     */
    public void unregisterAction(String actionId) {
        registeredActions.remove(actionId);
    }
    
    /**
     * Получает зарегистрированное действие по ID
     *
     * @param actionId ID действия
     * @return действие или null, если не найдено
     */
    public Action getAction(String actionId) {
        return registeredActions.get(actionId);
    }
    
    /**
     * Проверяет, зарегистрировано ли действие с указанным ID
     *
     * @param actionId ID действия
     * @return true, если действие зарегистрировано, иначе false
     */
    public boolean hasAction(String actionId) {
        return registeredActions.containsKey(actionId);
    }
    
    /**
     * Выполняет действие для игрока
     *
     * @param actionId ID действия
     * @param player игрок
     * @return CompletableFuture, который завершится после выполнения действия
     */
    public CompletableFuture<Void> executeAction(String actionId, Player player) {
        Action action = getAction(actionId);
        if (action == null) {
            CompletableFuture<Void> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalArgumentException("Действие не найдено: " + actionId));
            return future;
        }
        
        return executeAction(action, player);
    }
    
    /**
     * Выполняет действие для игрока
     *
     * @param action действие
     * @param player игрок
     * @return CompletableFuture, который завершится после выполнения действия
     */
    public CompletableFuture<Void> executeAction(Action action, Player player) {
        ActionContext context = getContextForPlayer(player, action.getId());
        return executeAction(action, player, context);
    }
    
    /**
     * Выполняет действие для игрока с заданным контекстом
     *
     * @param action действие
     * @param player игрок
     * @param context контекст
     * @return CompletableFuture, который завершится после выполнения действия
     */
    public CompletableFuture<Void> executeAction(Action action, Player player, ActionContext context) {
        try {
            // Логируем начало выполнения действия
            debugger.logActionStart(player, action);
            
            return action.execute(player, context)
                .thenAccept(v -> {
                    // Логируем успешное завершение
                    debugger.logActionEnd(player, action, true);
                })
                .exceptionally(ex -> {
                    // Логируем ошибку
                    debugger.logActionError(player, action, ex);
                    plugin.getLogger().log(Level.WARNING, "Ошибка при выполнении действия " + action.getId(), ex);
                    return null;
                });
        } catch (Exception e) {
            // Логируем ошибку при запуске
            debugger.logActionError(player, action, e);
            plugin.getLogger().log(Level.WARNING, "Ошибка при запуске действия " + action.getId(), e);
            CompletableFuture<Void> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
    
    /**
     * Регистрирует и активирует триггер с привязанным действием
     *
     * @param trigger триггер
     * @param action действие
     */
    public void activateTrigger(ActionTrigger trigger, Action action) {
        activeTriggers.put(trigger.getId(), trigger);
        trigger.activate(action);
    }
    
    /**
     * Регистрирует и активирует триггер с привязанным действием
     *
     * @param trigger триггер
     * @param actionId ID действия
     */
    public void activateTrigger(ActionTrigger trigger, String actionId) {
        Action action = getAction(actionId);
        if (action == null) {
            throw new IllegalArgumentException("Действие не найдено: " + actionId);
        }
        
        activateTrigger(trigger, action);
    }
    
    /**
     * Деактивирует триггер
     *
     * @param triggerId ID триггера
     */
    public void deactivateTrigger(String triggerId) {
        ActionTrigger trigger = activeTriggers.remove(triggerId);
        if (trigger != null) {
            trigger.deactivate();
        }
    }
    
    /**
     * Получает активный триггер по ID
     *
     * @param triggerId ID триггера
     * @return триггер или null, если не найден
     */
    public ActionTrigger getTrigger(String triggerId) {
        return activeTriggers.get(triggerId);
    }
    
    /**
     * Проверяет, активен ли триггер с указанным ID
     *
     * @param triggerId ID триггера
     * @return true, если триггер активен, иначе false
     */
    public boolean hasTrigger(String triggerId) {
        return activeTriggers.containsKey(triggerId);
    }
    
    /**
     * Деактивирует все триггеры
     */
    public void deactivateAllTriggers() {
        activeTriggers.values().forEach(ActionTrigger::deactivate);
        activeTriggers.clear();
    }
    
    /**
     * Получает контекст для игрока и действия
     *
     * @param player игрок
     * @param actionId ID действия
     * @return контекст
     */
    public ActionContext getContextForPlayer(Player player, String actionId) {
        UUID playerId = player.getUniqueId();
        Map<String, ActionContext> contexts = playerContexts.computeIfAbsent(playerId, k -> new HashMap<>());
        return contexts.computeIfAbsent(actionId, k -> new ActionContext());
    }
    
    /**
     * Очищает контекст для игрока и действия
     *
     * @param player игрок
     * @param actionId ID действия
     */
    public void clearContextForPlayer(Player player, String actionId) {
        Map<String, ActionContext> contexts = playerContexts.get(player.getUniqueId());
        if (contexts != null) {
            contexts.remove(actionId);
        }
    }
    
    /**
     * Очищает все контексты для игрока
     *
     * @param player игрок
     */
    public void clearAllContextsForPlayer(Player player) {
        playerContexts.remove(player.getUniqueId());
    }
    
    /**
     * Создает и возвращает последовательность действий
     *
     * @param name название последовательности
     * @param description описание последовательности
     * @return новая последовательность действий
     */
    public ActionSequence createSequence(String name, String description) {
        ActionSequence sequence = new ActionSequence(name, description, false);
        registerAction(sequence);
        return sequence;
    }
    
    /**
     * Создает и возвращает параллельную последовательность действий
     *
     * @param name название последовательности
     * @param description описание последовательности
     * @return новая параллельная последовательность действий
     */
    public ParallelActionSequence createParallelSequence(String name, String description) {
        ParallelActionSequence sequence = new ParallelActionSequence(name, description, true);
        registerAction(sequence);
        return sequence;
    }
    
    /**
     * Регистрирует последовательность действий
     *
     * @param sequence последовательность
     */
    public void registerSequence(ActionSequence sequence) {
        registerAction(sequence);
    }
    
    /**
     * Получает отладчик действий
     *
     * @return отладчик действий
     */
    public ActionDebugger getDebugger() {
        return debugger;
    }
    
    /**
     * Включает или отключает режим отладки для игрока
     *
     * @param player игрок
     * @param enabled состояние
     * @return предыдущее состояние
     */
    public boolean setDebugEnabled(Player player, boolean enabled) {
        return debugger.setDebugEnabled(player, enabled);
    }
    
    /**
     * Переключает режим отладки для игрока
     *
     * @param player игрок
     * @return новое состояние
     */
    public boolean toggleDebug(Player player) {
        return debugger.toggleDebug(player);
    }
    
    /**
     * Показывает отладочную информацию о последовательности
     *
     * @param player игрок
     * @param sequenceId ID последовательности
     */
    public void showSequenceDebugInfo(Player player, String sequenceId) {
        Action action = getAction(sequenceId);
        if (action instanceof ActionSequence) {
            debugger.showSequenceDebugInfo(player, (ActionSequence) action);
        } else {
            player.sendMessage("Действие не является последовательностью");
        }
    }
} 