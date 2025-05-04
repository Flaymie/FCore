package dev.flaymie.fcore.core.security;

import dev.flaymie.fcore.FCore;
import org.bukkit.Bukkit;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Утилита для безопасного выполнения потенциально опасных операций
 * с защитой от зависаний и других проблем
 */
public class SafeOperationExecutor {
    
    private final FCore plugin;
    private final Logger logger;
    
    // Максимальное время выполнения операции по умолчанию
    private static final long DEFAULT_TIMEOUT_MS = 5000; // 5 секунд
    
    /**
     * Конструктор исполнителя безопасных операций
     * @param plugin экземпляр FCore
     */
    public SafeOperationExecutor(FCore plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }
    
    /**
     * Выполняет операцию с защитой от исключений
     * @param operation операция для выполнения
     * @param <T> тип результата операции
     * @return результат операции или null в случае ошибки
     */
    public <T> T executeSafely(Callable<T> operation) {
        return executeSafely(operation, null, DEFAULT_TIMEOUT_MS);
    }
    
    /**
     * Выполняет операцию с защитой от исключений
     * @param operation операция для выполнения
     * @param defaultValue значение по умолчанию в случае ошибки
     * @param <T> тип результата операции
     * @return результат операции или defaultValue в случае ошибки
     */
    public <T> T executeSafely(Callable<T> operation, T defaultValue) {
        return executeSafely(operation, defaultValue, DEFAULT_TIMEOUT_MS);
    }
    
    /**
     * Выполняет операцию с защитой от исключений и тайм-аутом
     * @param operation операция для выполнения
     * @param defaultValue значение по умолчанию в случае ошибки
     * @param timeoutMs максимальное время выполнения в миллисекундах
     * @param <T> тип результата операции
     * @return результат операции или defaultValue в случае ошибки
     */
    public <T> T executeSafely(Callable<T> operation, T defaultValue, long timeoutMs) {
        try {
            // Выполняем операцию асинхронно с тайм-аутом
            return executeWithTimeout(operation, timeoutMs);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Ошибка при выполнении безопасной операции: " + e.getMessage(), e);
            return defaultValue;
        }
    }
    
    /**
     * Выполняет операцию с тайм-аутом
     * @param operation операция для выполнения
     * @param timeoutMs максимальное время выполнения в миллисекундах
     * @param <T> тип результата операции
     * @return результат операции
     * @throws Exception если операция завершилась с ошибкой или вышло время
     */
    private <T> T executeWithTimeout(Callable<T> operation, long timeoutMs) throws Exception {
        // Создаем задачу для выполнения операции
        ResultHolder<T> resultHolder = new ResultHolder<>();
        Exception[] exceptionHolder = new Exception[1];
        boolean[] completed = new boolean[1];
        
        // Запускаем задачу асинхронно
        Thread workerThread = new Thread(() -> {
            try {
                T result = operation.call();
                resultHolder.setResult(result);
                completed[0] = true;
            } catch (Exception e) {
                exceptionHolder[0] = e;
                completed[0] = true;
            }
        });
        
        workerThread.setDaemon(true);
        workerThread.start();
        
        // Ждем завершения операции с тайм-аутом
        long startTime = System.currentTimeMillis();
        while (!completed[0]) {
            if (System.currentTimeMillis() - startTime > timeoutMs) {
                workerThread.interrupt();
                throw new TimeoutException("Превышено время выполнения операции (" + timeoutMs + " мс)");
            }
            
            // Даем другим потокам возможность выполняться
            Thread.sleep(10);
        }
        
        // Проверяем, завершилась ли операция с ошибкой
        if (exceptionHolder[0] != null) {
            throw exceptionHolder[0];
        }
        
        return resultHolder.getResult();
    }
    
    /**
     * Выполняет Runnable операцию безопасно
     * @param operation операция для выполнения
     * @param timeoutMs максимальное время выполнения в миллисекундах
     * @return true, если операция выполнена успешно
     */
    public boolean executeSafely(Runnable operation, long timeoutMs) {
        return executeSafely(() -> {
            operation.run();
            return true;
        }, false, timeoutMs);
    }
    
    /**
     * Выполняет операцию в основном потоке сервера безопасно
     * @param operation операция для выполнения
     * @param <T> тип результата операции
     * @return результат операции или null в случае ошибки
     */
    public <T> T executeSafelySync(Callable<T> operation) {
        if (Bukkit.isPrimaryThread()) {
            return executeSafely(operation);
        } else {
            // Запускаем операцию в основном потоке
            ResultHolder<T> resultHolder = new ResultHolder<>();
            Exception[] exceptionHolder = new Exception[1];
            boolean[] completed = new boolean[1];
            
            Bukkit.getScheduler().runTask(plugin, () -> {
                try {
                    T result = executeSafely(operation);
                    resultHolder.setResult(result);
                } catch (Exception e) {
                    exceptionHolder[0] = e;
                } finally {
                    completed[0] = true;
                }
            });
            
            // Ждем завершения операции
            while (!completed[0]) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
            
            return resultHolder.getResult();
        }
    }
    
    /**
     * Вспомогательный класс для хранения результата операции
     * @param <T> тип результата
     */
    private static class ResultHolder<T> {
        private T result;
        
        public void setResult(T result) {
            this.result = result;
        }
        
        public T getResult() {
            return result;
        }
    }
} 