package dev.flaymie.fcore.api.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Базовый класс для всех кастомных событий FCore
 */
public abstract class FCoreEvent extends Event implements Cancellable {
    
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;
    
    /**
     * Создает синхронное событие
     */
    public FCoreEvent() {
        super();
    }
    
    /**
     * Создает событие с указанием асинхронности
     * @param isAsync true если событие асинхронное
     */
    public FCoreEvent(boolean isAsync) {
        super(isAsync);
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
    
    @Override
    public boolean isCancelled() {
        return cancelled;
    }
    
    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
} 