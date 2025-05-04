package dev.flaymie.fcore.utils.async;

import dev.flaymie.fcore.FCore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Утилиты для работы с асинхронностью
 */
public class AsyncUtils {
    private static final FCore plugin = FCore.getInstance();
    private static final Executor ASYNC_EXECUTOR = Executors.newCachedThreadPool();
    private static final ScheduledExecutorService SCHEDULED_EXECUTOR = Executors.newScheduledThreadPool(1);
    
    /**
     * Выполняет действие асинхронно
     * @param runnable действие для выполнения
     * @return задача Bukkit
     */
    public static BukkitTask runAsync(Runnable runnable) {
        if (runnable == null || plugin == null) return null;
        
        return new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(plugin);
    }
    
    /**
     * Выполняет действие в основном потоке
     * @param runnable действие для выполнения
     * @return задача Bukkit
     */
    public static BukkitTask runSync(Runnable runnable) {
        if (runnable == null || plugin == null) return null;
        
        return new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.runTask(plugin);
    }
    
    /**
     * Выполняет действие в основном потоке с задержкой
     * @param runnable действие для выполнения
     * @param delayTicks задержка в тиках
     * @return задача Bukkit
     */
    public static BukkitTask runLater(Runnable runnable, long delayTicks) {
        if (runnable == null || plugin == null) return null;
        
        return new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.runTaskLater(plugin, delayTicks);
    }
    
    /**
     * Выполняет действие асинхронно с задержкой
     * @param runnable действие для выполнения
     * @param delayTicks задержка в тиках
     * @return задача Bukkit
     */
    public static BukkitTask runAsyncLater(Runnable runnable, long delayTicks) {
        if (runnable == null || plugin == null) return null;
        
        return new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.runTaskLaterAsynchronously(plugin, delayTicks);
    }
    
    /**
     * Выполняет действие периодически в основном потоке
     * @param runnable действие для выполнения
     * @param delayTicks начальная задержка в тиках
     * @param periodTicks период повторения в тиках
     * @return задача Bukkit
     */
    public static BukkitTask runTimer(Runnable runnable, long delayTicks, long periodTicks) {
        if (runnable == null || plugin == null) return null;
        
        return new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.runTaskTimer(plugin, delayTicks, periodTicks);
    }
    
    /**
     * Выполняет действие периодически асинхронно
     * @param runnable действие для выполнения
     * @param delayTicks начальная задержка в тиках
     * @param periodTicks период повторения в тиках
     * @return задача Bukkit
     */
    public static BukkitTask runAsyncTimer(Runnable runnable, long delayTicks, long periodTicks) {
        if (runnable == null || plugin == null) return null;
        
        return new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.runTaskTimerAsynchronously(plugin, delayTicks, periodTicks);
    }
    
    /**
     * Создает и выполняет CompletableFuture асинхронно
     * @param supplier функция поставки данных
     * @param <T> тип данных
     * @return CompletableFuture
     */
    public static <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier) {
        if (supplier == null) return CompletableFuture.completedFuture(null);
        
        return CompletableFuture.supplyAsync(supplier, ASYNC_EXECUTOR);
    }
    
    /**
     * Создает CompletableFuture, который будет выполнен в основном потоке
     * @param supplier функция поставки данных
     * @param <T> тип данных
     * @return CompletableFuture
     */
    public static <T> CompletableFuture<T> supplySync(Supplier<T> supplier) {
        if (supplier == null || plugin == null) return CompletableFuture.completedFuture(null);
        
        CompletableFuture<T> future = new CompletableFuture<>();
        
        runSync(() -> {
            try {
                future.complete(supplier.get());
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        
        return future;
    }
    
    /**
     * Ожидает указанное количество тиков перед выполнением действия
     * @param ticks количество тиков
     * @return CompletableFuture
     */
    public static CompletableFuture<Void> delay(long ticks) {
        if (plugin == null) return CompletableFuture.completedFuture(null);
        
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        runLater(() -> future.complete(null), ticks);
        
        return future;
    }
    
    /**
     * Выполняет действие в основном потоке
     * @param runnable действие для выполнения
     * @return CompletableFuture
     */
    public static CompletableFuture<Void> callSync(Runnable runnable) {
        if (runnable == null || plugin == null) return CompletableFuture.completedFuture(null);
        
        // Если мы уже в основном потоке, выполняем сразу
        if (Bukkit.isPrimaryThread()) {
            try {
                runnable.run();
                return CompletableFuture.completedFuture(null);
            } catch (Exception e) {
                CompletableFuture<Void> future = new CompletableFuture<>();
                future.completeExceptionally(e);
                return future;
            }
        }
        
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        runSync(() -> {
            try {
                runnable.run();
                future.complete(null);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        
        return future;
    }
    
    /**
     * Выполняет асинхронную задачу с таймаутом
     * @param supplier функция поставки данных
     * @param timeout время ожидания
     * @param unit единица измерения времени
     * @param defaultValue значение по умолчанию в случае таймаута
     * @param <T> тип данных
     * @return результат или значение по умолчанию
     */
    public static <T> CompletableFuture<T> withTimeout(Supplier<T> supplier, long timeout, TimeUnit unit, T defaultValue) {
        if (supplier == null) return CompletableFuture.completedFuture(defaultValue);
        
        CompletableFuture<T> future = supplyAsync(supplier);
        CompletableFuture<T> timeoutFuture = new CompletableFuture<>();
        
        // Создаем задачу таймаута
        SCHEDULED_EXECUTOR.schedule(() -> {
            if (!future.isDone()) {
                timeoutFuture.complete(defaultValue);
            }
        }, timeout, unit);
        
        // Соединяем два фьючерса
        future.thenAccept(timeoutFuture::complete);
        future.exceptionally(ex -> {
            timeoutFuture.complete(defaultValue);
            return null;
        });
        
        return timeoutFuture;
    }
    
    /**
     * Выполняет несколько задач параллельно
     * @param tasks список задач
     * @return CompletableFuture, который завершится после завершения всех задач
     */
    public static CompletableFuture<Void> allOf(List<Runnable> tasks) {
        if (tasks == null || tasks.isEmpty()) return CompletableFuture.completedFuture(null);
        
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        
        for (Runnable task : tasks) {
            futures.add(CompletableFuture.runAsync(task, ASYNC_EXECUTOR));
        }
        
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }
    
    /**
     * Выполняет несколько задач параллельно и ждет завершения любой из них
     * @param tasks список задач, возвращающих результаты
     * @param <T> тип данных
     * @return CompletableFuture, который завершится после завершения любой из задач
     */
    public static <T> CompletableFuture<T> anyOf(List<Supplier<T>> tasks) {
        if (tasks == null || tasks.isEmpty()) return CompletableFuture.completedFuture(null);
        
        List<CompletableFuture<T>> futures = new ArrayList<>();
        
        for (Supplier<T> task : tasks) {
            futures.add(supplyAsync(task));
        }
        
        @SuppressWarnings("unchecked")
        CompletableFuture<T> result = (CompletableFuture<T>) CompletableFuture.anyOf(futures.toArray(new CompletableFuture[0]));
        
        return result;
    }
    
    /**
     * Проверяет, выполняется ли код в основном потоке
     * @return true, если код выполняется в основном потоке
     */
    public static boolean isMainThread() {
        return Bukkit.isPrimaryThread();
    }
    
    /**
     * Запускает задачу для игрока. Если игрок оффлайн, выполнение откладывается до его входа в игру.
     * @param playerName имя игрока
     * @param action действие для выполнения
     * @param maxWaitTicks максимальное время ожидания в тиках
     * @return CompletableFuture
     */
    public static CompletableFuture<Void> withPlayer(String playerName, Consumer<Player> action, int maxWaitTicks) {
        if (playerName == null || action == null || plugin == null) return CompletableFuture.completedFuture(null);
        
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        Player player = Bukkit.getPlayerExact(playerName);
        
        if (player != null && player.isOnline()) {
            try {
                callSync(() -> action.accept(player)).thenAccept(future::complete);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
            return future;
        }
        
        final int[] remainingTicks = {maxWaitTicks};
        
        BukkitTask task = runTimer(() -> {
            if (remainingTicks[0] <= 0) {
                future.complete(null);
                return;
            }
            
            Player p = Bukkit.getPlayerExact(playerName);
            if (p != null && p.isOnline()) {
                try {
                    callSync(() -> action.accept(p)).thenAccept(future::complete);
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
            }
            
            remainingTicks[0] -= 20; // Проверяем каждую секунду
        }, 0, 20);
        
        future.thenRun(task::cancel);
        
        return future;
    }
    
    /**
     * Создает сервис-поток, который выполняет задачу асинхронно и непрерывно
     * @param runnable задача для выполнения
     * @param initialDelayTicks начальная задержка в тиках
     * @param periodTicks период выполнения в тиках
     * @return ServiceThread для управления потоком
     */
    public static ServiceThread createServiceThread(Runnable runnable, long initialDelayTicks, long periodTicks) {
        return new ServiceThread(runnable, initialDelayTicks, periodTicks);
    }
    
    /**
     * Класс для управления сервисным потоком
     */
    public static class ServiceThread {
        private final Runnable task;
        private final long initialDelay;
        private final long period;
        private BukkitTask bukkitTask;
        private boolean running;
        
        public ServiceThread(Runnable task, long initialDelay, long period) {
            this.task = task;
            this.initialDelay = initialDelay;
            this.period = period;
            this.running = false;
        }
        
        /**
         * Запускает сервисный поток
         */
        public void start() {
            if (running) return;
            
            bukkitTask = runAsyncTimer(task, initialDelay, period);
            running = true;
        }
        
        /**
         * Останавливает сервисный поток
         */
        public void stop() {
            if (!running) return;
            
            if (bukkitTask != null) {
                bukkitTask.cancel();
                bukkitTask = null;
            }
            
            running = false;
        }
        
        /**
         * Проверяет, запущен ли поток
         * @return true, если поток запущен
         */
        public boolean isRunning() {
            return running;
        }
        
        /**
         * Перезапускает сервисный поток
         */
        public void restart() {
            stop();
            start();
        }
    }
} 