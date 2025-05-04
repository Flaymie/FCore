package dev.flaymie.fcore.core.action;

import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Класс для параллельного выполнения нескольких цепочек действий одновременно
 */
public class ParallelActionSequence extends AbstractAction {
    
    private final List<Action> actions;
    
    /**
     * Создает новую параллельную последовательность действий
     */
    public ParallelActionSequence() {
        this("Parallel Sequence", "Параллельное выполнение действий", true);
    }
    
    /**
     * Создает именованную параллельную последовательность действий
     *
     * @param name название последовательности
     * @param description описание последовательности
     * @param async выполнять ли асинхронно
     */
    public ParallelActionSequence(String name, String description, boolean async) {
        super(name, description, async);
        this.actions = new ArrayList<>();
    }
    
    /**
     * Создает новую параллельную последовательность действий
     *
     * @return новая параллельная последовательность
     */
    public static ParallelActionSequence create() {
        return new ParallelActionSequence();
    }
    
    /**
     * Создает именованную параллельную последовательность действий
     *
     * @param name название последовательности
     * @param description описание последовательности
     * @return новая параллельная последовательность
     */
    public static ParallelActionSequence create(String name, String description) {
        return new ParallelActionSequence(name, description, true);
    }
    
    /**
     * Добавляет действия в параллельную последовательность
     *
     * @param actionsToAdd действия для выполнения параллельно
     * @return эта параллельная последовательность для цепочки вызовов
     */
    public ParallelActionSequence add(Action... actionsToAdd) {
        actions.addAll(Arrays.asList(actionsToAdd));
        return this;
    }
    
    /**
     * Добавляет последовательность действий для параллельного выполнения
     *
     * @param sequence последовательность действий
     * @return эта параллельная последовательность для цепочки вызовов
     */
    public ParallelActionSequence add(ActionSequence sequence) {
        actions.add(sequence);
        return this;
    }
    
    /**
     * Добавляет другую параллельную последовательность действий
     *
     * @param parallelSequence другая параллельная последовательность
     * @return эта параллельная последовательность для цепочки вызовов
     */
    public ParallelActionSequence add(ParallelActionSequence parallelSequence) {
        actions.add(parallelSequence);
        return this;
    }
    
    @Override
    public CompletableFuture<Void> execute(Player player, ActionContext context) {
        // Создаем отдельный контекст для каждого действия, но они разделяют глобальный контекст
        List<CompletableFuture<Void>> futures = actions.stream()
                .map(action -> {
                    ActionContext childContext = context.createChild();
                    return action.execute(player, childContext);
                })
                .collect(Collectors.toList());
        
        // Объединяем все futures в один, который завершится, когда все действия будут выполнены
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }
    
    /**
     * Получает список всех действий в параллельной последовательности
     *
     * @return список действий
     */
    public List<Action> getActions() {
        return new ArrayList<>(actions);
    }
} 