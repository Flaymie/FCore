package dev.flaymie.fcore.core.action;

import org.bukkit.entity.Player;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Действие для условного ветвления
 */
public class ConditionalAction extends AbstractAction {
    
    private final Predicate<Player> condition;
    private final Function<ActionSequence, ActionSequence> thenFunction;
    private final Function<ActionSequence, ActionSequence> elseFunction;
    
    /**
     * Создает новое условное действие
     *
     * @param condition условие
     * @param thenFunction функция, создающая последовательность для случая, когда условие истинно
     * @param elseFunction функция, создающая последовательность для случая, когда условие ложно
     */
    public ConditionalAction(Predicate<Player> condition,
                            Function<ActionSequence, ActionSequence> thenFunction,
                            Function<ActionSequence, ActionSequence> elseFunction) {
        super("Condition", "Выполняет одну из двух последовательностей действий в зависимости от условия", false);
        this.condition = condition;
        this.thenFunction = thenFunction;
        this.elseFunction = elseFunction;
    }
    
    @Override
    public CompletableFuture<Void> execute(Player player, ActionContext context) {
        ActionSequence sequence;
        
        if (condition.test(player)) {
            // Создаем последовательность для true-ветки
            sequence = thenFunction.apply(ActionSequence.create());
        } else {
            // Создаем последовательность для false-ветки
            sequence = elseFunction.apply(ActionSequence.create());
        }
        
        return sequence.execute(player, context);
    }
} 