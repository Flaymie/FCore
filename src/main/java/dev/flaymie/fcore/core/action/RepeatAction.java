package dev.flaymie.fcore.core.action;

import org.bukkit.entity.Player;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Действие, повторяющее последовательность действий указанное количество раз
 */
public class RepeatAction extends AbstractAction {
    
    private final int times;
    private final Function<ActionSequence, ActionSequence> sequenceFunction;
    
    /**
     * Создает новое действие повторения
     *
     * @param times количество повторений
     * @param sequenceFunction функция, создающая повторяемую последовательность
     */
    public RepeatAction(int times, Function<ActionSequence, ActionSequence> sequenceFunction) {
        super("Repeat", "Повторяет последовательность действий " + times + " раз", false);
        this.times = times;
        this.sequenceFunction = sequenceFunction;
    }
    
    @Override
    public CompletableFuture<Void> execute(Player player, ActionContext context) {
        CompletableFuture<Void> future = CompletableFuture.completedFuture(null);
        
        // Создаем последовательность, которую будем повторять
        ActionSequence sequence = sequenceFunction.apply(ActionSequence.create());
        
        // Повторяем ее указанное количество раз
        for (int i = 0; i < times; i++) {
            final int iteration = i;
            
            // Добавляем номер итерации в контекст
            context.set("repeat.iteration", iteration);
            context.set("repeat.total", times);
            
            future = future.thenCompose(v -> sequence.execute(player, context));
        }
        
        return future;
    }
    
    /**
     * Получает количество повторений
     *
     * @return количество повторений
     */
    public int getTimes() {
        return times;
    }
} 