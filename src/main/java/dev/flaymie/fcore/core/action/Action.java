package dev.flaymie.fcore.core.action;

import org.bukkit.entity.Player;
import java.util.concurrent.CompletableFuture;

/**
 * Базовый интерфейс для всех действий в Action-системе
 */
public interface Action {
    
    /**
     * Выполняет действие для указанного игрока
     * 
     * @param player игрок, для которого выполняется действие
     * @return завершенный CompletableFuture при успешном выполнении действия
     *         или сфейленный CompletableFuture с ошибкой, если действие не удалось выполнить
     */
    CompletableFuture<Void> execute(Player player);
    
    /**
     * Выполняет действие для указанного игрока с заданным контекстом
     *
     * @param player игрок, для которого выполняется действие
     * @param context контекст выполнения
     * @return завершенный CompletableFuture при успешном выполнении действия
     */
    CompletableFuture<Void> execute(Player player, ActionContext context);
    
    /**
     * Получает уникальный идентификатор действия
     *
     * @return ID действия
     */
    String getId();
    
    /**
     * Получает удобочитаемое название действия
     *
     * @return название действия
     */
    String getName();
    
    /**
     * Получает описание действия
     *
     * @return описание действия
     */
    String getDescription();
    
    /**
     * Проверяет, может ли действие быть выполнено асинхронно
     *
     * @return true, если действие может быть выполнено асинхронно, иначе false
     */
    boolean isAsync();
} 