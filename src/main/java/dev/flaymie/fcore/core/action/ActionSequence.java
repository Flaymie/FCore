package dev.flaymie.fcore.core.action;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Последовательность действий, которые могут быть выполнены как цепочка
 */
public class ActionSequence implements Action {
    
    private final String id;
    private final String name;
    private final String description;
    private final List<Action> actions;
    private final boolean async;
    
    /**
     * Создает пустую последовательность действий
     */
    public ActionSequence() {
        this("Sequence", "Последовательность действий", false);
    }
    
    /**
     * Создает именованную последовательность действий
     * 
     * @param name название последовательности
     * @param description описание последовательности
     * @param async должна ли вся последовательность выполняться асинхронно
     */
    public ActionSequence(String name, String description, boolean async) {
        this.id = "sequence-" + System.currentTimeMillis();
        this.name = name;
        this.description = description;
        this.actions = new ArrayList<>();
        this.async = async;
    }
    
    /**
     * Создает новую пустую последовательность действий
     *
     * @return новая последовательность
     */
    public static ActionSequence create() {
        return new ActionSequence();
    }
    
    /**
     * Создает именованную последовательность действий
     *
     * @param name название последовательности
     * @param description описание последовательности
     * @return новая последовательность
     */
    public static ActionSequence create(String name, String description) {
        return new ActionSequence(name, description, false);
    }
    
    /**
     * Создает именованную последовательность действий
     *
     * @param name название последовательности
     * @param description описание последовательности
     * @param async должна ли вся последовательность выполняться асинхронно
     * @return новая последовательность
     */
    public static ActionSequence create(String name, String description, boolean async) {
        return new ActionSequence(name, description, async);
    }
    
    /**
     * Добавляет действие в конец последовательности
     *
     * @param action действие
     * @return эта последовательность для цепочки вызовов
     */
    public ActionSequence then(Action action) {
        actions.add(action);
        return this;
    }
    
    /**
     * Ожидает указанное количество тиков перед продолжением
     *
     * @param ticks количество тиков (20 тиков = 1 секунда)
     * @return эта последовательность для цепочки вызовов
     */
    public ActionSequence wait(int ticks) {
        return then(new WaitAction(ticks));
    }
    
    /**
     * Выполняет произвольный код
     *
     * @param runnable код для выполнения
     * @return эта последовательность для цепочки вызовов
     */
    public ActionSequence run(ActionRunnable runnable) {
        return then(new RunAction(runnable));
    }
    
    /**
     * Отправляет сообщение игроку
     *
     * @param message сообщение
     * @return эта последовательность для цепочки вызовов
     */
    public ActionSequence message(String message) {
        return then(new MessageAction(message));
    }
    
    /**
     * Проигрывает звук игроку
     *
     * @param sound звук
     * @param volume громкость
     * @param pitch высота
     * @return эта последовательность для цепочки вызовов
     */
    public ActionSequence sound(Sound sound, float volume, float pitch) {
        return then(new SoundAction(sound, volume, pitch));
    }
    
    /**
     * Проигрывает звук игроку с громкостью и высотой по умолчанию
     *
     * @param sound звук
     * @return эта последовательность для цепочки вызовов
     */
    public ActionSequence sound(Sound sound) {
        return then(new SoundAction(sound));
    }
    
    /**
     * Отображает частицы в локации игрока
     *
     * @param particle частица
     * @param count количество частиц
     * @param offsetX смещение по X
     * @param offsetY смещение по Y
     * @param offsetZ смещение по Z
     * @param speed скорость частиц
     * @return эта последовательность для цепочки вызовов
     */
    public ActionSequence particle(Particle particle, int count, double offsetX, double offsetY, double offsetZ, double speed) {
        return then(new ParticleAction(particle, count, offsetX, offsetY, offsetZ, speed));
    }
    
    /**
     * Отображает частицы в указанной локации
     *
     * @param particle частица
     * @param location локация
     * @param count количество частиц
     * @param offsetX смещение по X
     * @param offsetY смещение по Y
     * @param offsetZ смещение по Z
     * @param speed скорость частиц
     * @return эта последовательность для цепочки вызовов
     */
    public ActionSequence particle(Particle particle, Location location, int count, double offsetX, double offsetY, double offsetZ, double speed) {
        return then(new ParticleAction(particle, location, count, offsetX, offsetY, offsetZ, speed));
    }
    
    /**
     * Отображает частицы в локации игрока с параметрами по умолчанию
     *
     * @param particle частица
     * @return эта последовательность для цепочки вызовов
     */
    public ActionSequence particle(Particle particle) {
        return then(new ParticleAction(particle));
    }
    
    /**
     * Отображает заголовок и подзаголовок игроку
     *
     * @param title заголовок
     * @param subtitle подзаголовок
     * @param fadeIn время появления в тиках
     * @param stay время отображения в тиках
     * @param fadeOut время исчезновения в тиках
     * @return эта последовательность для цепочки вызовов
     */
    public ActionSequence title(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        return then(new TitleAction(title, subtitle, fadeIn, stay, fadeOut));
    }
    
    /**
     * Отображает заголовок и подзаголовок игроку с временем по умолчанию
     *
     * @param title заголовок
     * @param subtitle подзаголовок
     * @return эта последовательность для цепочки вызовов
     */
    public ActionSequence title(String title, String subtitle) {
        return then(new TitleAction(title, subtitle));
    }
    
    /**
     * Отображает только заголовок игроку
     *
     * @param title заголовок
     * @return эта последовательность для цепочки вызовов
     */
    public ActionSequence title(String title) {
        return then(new TitleAction(title));
    }
    
    /**
     * Добавляет условное ветвление
     *
     * @param condition условие
     * @param thenFunction функция, создающая последовательность для случая, когда условие истинно
     * @param elseFunction функция, создающая последовательность для случая, когда условие ложно
     * @return эта последовательность для цепочки вызовов
     */
    public ActionSequence condition(Predicate<Player> condition, 
                                   Function<ActionSequence, ActionSequence> thenFunction,
                                   Function<ActionSequence, ActionSequence> elseFunction) {
        return then(new ConditionalAction(condition, thenFunction, elseFunction));
    }
    
    /**
     * Добавляет условное ветвление
     *
     * @param condition условие
     * @param thenFunction функция, создающая последовательность для случая, когда условие истинно
     * @return эта последовательность для цепочки вызовов
     */
    public ActionSequence condition(Predicate<Player> condition, 
                                   Function<ActionSequence, ActionSequence> thenFunction) {
        return condition(condition, thenFunction, seq -> seq);
    }
    
    /**
     * Повторяет последовательность действий указанное количество раз
     *
     * @param times количество повторений
     * @param sequenceFunction функция, создающая повторяемую последовательность
     * @return эта последовательность для цепочки вызовов
     */
    public ActionSequence repeat(int times, Function<ActionSequence, ActionSequence> sequenceFunction) {
        return then(new RepeatAction(times, sequenceFunction));
    }
    
    /**
     * Выполняет набор действий параллельно
     *
     * @param actions действия для параллельного выполнения
     * @return эта последовательность для цепочки вызовов
     */
    public ActionSequence parallel(Action... actions) {
        ParallelActionSequence parallelSequence = ParallelActionSequence.create();
        parallelSequence.add(actions);
        return then(parallelSequence);
    }
    
    /**
     * Выполняет набор последовательностей параллельно
     *
     * @param sequencesFunction функция для создания последовательностей
     * @return эта последовательность для цепочки вызовов
     */
    public ActionSequence parallel(Function<ParallelActionSequence, ParallelActionSequence> sequencesFunction) {
        ParallelActionSequence parallelSequence = ParallelActionSequence.create();
        parallelSequence = sequencesFunction.apply(parallelSequence);
        return then(parallelSequence);
    }
    
    /**
     * Запускает выполнение последовательности для игрока
     *
     * @param player игрок
     * @return CompletableFuture, который завершится после выполнения всей последовательности
     */
    public CompletableFuture<Void> start(Player player) {
        return execute(player);
    }
    
    /**
     * Запускает выполнение последовательности для игрока с контекстом
     *
     * @param player игрок
     * @param context контекст выполнения
     * @return CompletableFuture, который завершится после выполнения всей последовательности
     */
    public CompletableFuture<Void> start(Player player, ActionContext context) {
        return execute(player, context);
    }
    
    @Override
    public CompletableFuture<Void> execute(Player player) {
        return execute(player, new ActionContext());
    }
    
    @Override
    public CompletableFuture<Void> execute(Player player, ActionContext context) {
        CompletableFuture<Void> future = CompletableFuture.completedFuture(null);
        
        for (Action action : actions) {
            if (action.isAsync() || async) {
                future = future.thenCompose(v -> action.execute(player, context));
            } else {
                future = future.thenCompose(v -> action.execute(player, context));
            }
        }
        
        return future;
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
     * Получает список действий в последовательности
     *
     * @return список действий
     */
    public List<Action> getActions() {
        return new ArrayList<>(actions);
    }
} 