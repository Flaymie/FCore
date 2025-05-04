package dev.flaymie.fcore.api.gui;

import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

/**
 * Интерфейс для анимированного GUI
 */
public interface AnimatedGui extends Gui {
    
    /**
     * Запускает анимацию
     * @return текущий GUI
     */
    AnimatedGui startAnimation();
    
    /**
     * Останавливает анимацию
     * @return текущий GUI
     */
    AnimatedGui stopAnimation();
    
    /**
     * Проверяет, запущена ли анимация
     * @return true, если анимация запущена
     */
    boolean isAnimationRunning();
    
    /**
     * Устанавливает интервал обновления анимации (в тиках)
     * @param interval интервал
     * @return текущий GUI
     */
    AnimatedGui setAnimationInterval(long interval);
    
    /**
     * Получает интервал обновления анимации (в тиках)
     * @return интервал
     */
    long getAnimationInterval();
    
    /**
     * Добавляет кадр анимации
     * @param frameIndex индекс кадра
     * @param slot слот
     * @param item предмет
     * @return текущий GUI
     */
    AnimatedGui addAnimationFrame(int frameIndex, int slot, ItemStack item);
    
    /**
     * Добавляет кадр анимации с обработчиком кликов
     * @param frameIndex индекс кадра
     * @param slot слот
     * @param item предмет
     * @param clickHandler обработчик кликов
     * @return текущий GUI
     */
    AnimatedGui addAnimationFrame(int frameIndex, int slot, ItemStack item, ClickHandler clickHandler);
    
    /**
     * Удаляет кадр анимации
     * @param frameIndex индекс кадра
     * @param slot слот
     * @return текущий GUI
     */
    AnimatedGui removeAnimationFrame(int frameIndex, int slot);
    
    /**
     * Очищает все кадры анимации
     * @return текущий GUI
     */
    AnimatedGui clearAnimationFrames();
    
    /**
     * Устанавливает обработчик для каждого кадра анимации
     * @param frameHandler обработчик кадра
     * @return текущий GUI
     */
    AnimatedGui onFrame(Consumer<Integer> frameHandler);
    
    /**
     * Устанавливает количество кадров в анимации
     * @param frameCount количество кадров
     * @return текущий GUI
     */
    AnimatedGui setFrameCount(int frameCount);
    
    /**
     * Получает количество кадров в анимации
     * @return количество кадров
     */
    int getFrameCount();
    
    /**
     * Получает текущий кадр анимации
     * @return индекс текущего кадра
     */
    int getCurrentFrame();
    
    /**
     * Устанавливает текущий кадр анимации
     * @param frame индекс кадра
     * @return текущий GUI
     */
    AnimatedGui setCurrentFrame(int frame);
    
    /**
     * Устанавливает режим циклической анимации
     * @param loop true, если анимация должна повторяться
     * @return текущий GUI
     */
    AnimatedGui setLoop(boolean loop);
    
    /**
     * Проверяет, является ли анимация циклической
     * @return true, если анимация циклическая
     */
    boolean isLoop();
} 