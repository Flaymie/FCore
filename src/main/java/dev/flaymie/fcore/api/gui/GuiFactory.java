package dev.flaymie.fcore.api.gui;

import dev.flaymie.fcore.api.gui.impl.AnimatedGuiImpl;
import dev.flaymie.fcore.api.gui.impl.GuiImpl;
import dev.flaymie.fcore.api.gui.impl.PaginatedGuiImpl;
import org.bukkit.inventory.ItemStack;

/**
 * Фабрика для создания различных типов GUI
 */
public class GuiFactory {
    
    private GuiFactory() {
        // Приватный конструктор для предотвращения создания экземпляров
    }
    
    /**
     * Создает простой GUI
     * @param title название
     * @param rows количество строк (1-6)
     * @return новый GUI
     */
    public static Gui createGui(String title, int rows) {
        return new GuiImpl(title, rows);
    }
    
    /**
     * Создает пагинированный GUI
     * @param title название
     * @param rows количество строк (1-6)
     * @return новый пагинированный GUI
     */
    public static PaginatedGui createPaginatedGui(String title, int rows) {
        return new PaginatedGuiImpl(title, rows);
    }
    
    /**
     * Создает пагинированный GUI с кнопками навигации
     * @param title название
     * @param rows количество строк (1-6)
     * @param nextPageItem предмет для кнопки "Следующая страница"
     * @param previousPageItem предмет для кнопки "Предыдущая страница"
     * @return новый пагинированный GUI с кнопками навигации
     */
    public static PaginatedGui createPaginatedGui(String title, int rows, ItemStack nextPageItem, ItemStack previousPageItem) {
        PaginatedGui gui = new PaginatedGuiImpl(title, rows);
        gui.setNextPageItem(nextPageItem);
        gui.setPreviousPageItem(previousPageItem);
        return gui;
    }
    
    /**
     * Создает пагинированный GUI с полной настройкой навигации
     * @param title название
     * @param rows количество строк (1-6)
     * @param nextPageItem предмет для кнопки "Следующая страница"
     * @param previousPageItem предмет для кнопки "Предыдущая страница"
     * @param nextPageSlot слот для кнопки "Следующая страница"
     * @param previousPageSlot слот для кнопки "Предыдущая страница"
     * @return новый пагинированный GUI с полной настройкой навигации
     */
    public static PaginatedGui createPaginatedGui(String title, int rows, 
                                                ItemStack nextPageItem, ItemStack previousPageItem, 
                                                int nextPageSlot, int previousPageSlot) {
        PaginatedGui gui = new PaginatedGuiImpl(title, rows);
        gui.setNextPageButton(nextPageItem, nextPageSlot);
        gui.setPreviousPageButton(previousPageItem, previousPageSlot);
        return gui;
    }
    
    /**
     * Создает анимированный GUI
     * @param title название
     * @param rows количество строк (1-6)
     * @return новый анимированный GUI
     */
    public static AnimatedGui createAnimatedGui(String title, int rows) {
        return new AnimatedGuiImpl(title, rows);
    }
    
    /**
     * Создает анимированный GUI с заданным интервалом обновления
     * @param title название
     * @param rows количество строк (1-6)
     * @param interval интервал обновления в тиках
     * @return новый анимированный GUI с заданным интервалом
     */
    public static AnimatedGui createAnimatedGui(String title, int rows, long interval) {
        AnimatedGui gui = new AnimatedGuiImpl(title, rows);
        gui.setAnimationInterval(interval);
        return gui;
    }
    
    /**
     * Создает анимированный GUI с заданным интервалом и количеством кадров
     * @param title название
     * @param rows количество строк (1-6)
     * @param interval интервал обновления в тиках
     * @param frameCount количество кадров
     * @return новый анимированный GUI с заданными параметрами
     */
    public static AnimatedGui createAnimatedGui(String title, int rows, long interval, int frameCount) {
        AnimatedGui gui = new AnimatedGuiImpl(title, rows);
        gui.setAnimationInterval(interval);
        gui.setFrameCount(frameCount);
        return gui;
    }
} 