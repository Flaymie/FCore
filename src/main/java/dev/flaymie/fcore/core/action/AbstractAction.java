package dev.flaymie.fcore.core.action;

import org.bukkit.entity.Player;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Базовая реализация интерфейса Action
 */
public abstract class AbstractAction implements Action {
    
    private final String id;
    private final String name;
    private final String description;
    private final boolean async;
    
    /**
     * Создает новое действие
     *
     * @param name название действия
     * @param description описание действия 
     * @param async может ли действие выполняться асинхронно
     */
    public AbstractAction(String name, String description, boolean async) {
        this.id = generateId();
        this.name = name;
        this.description = description;
        this.async = async;
    }
    
    /**
     * Создает новое действие с заданным ID
     *
     * @param id ID действия
     * @param name название действия
     * @param description описание действия
     * @param async может ли действие выполняться асинхронно
     */
    public AbstractAction(String id, String name, String description, boolean async) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.async = async;
    }
    
    @Override
    public CompletableFuture<Void> execute(Player player) {
        return execute(player, new ActionContext());
    }
    
    @Override
    public String getId() {
        return id;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public String getDescription() {
        return description;
    }
    
    @Override
    public boolean isAsync() {
        return async;
    }
    
    /**
     * Генерирует уникальный ID для действия
     *
     * @return уникальный ID
     */
    protected String generateId() {
        return getClass().getSimpleName() + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
} 