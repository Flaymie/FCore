package dev.flaymie.fcore.api.gui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.List;

/**
 * Интерфейс для пагинированного GUI
 */
public interface PaginatedGui extends Gui {
    
    /**
     * Добавляет предмет на следующую доступную страницу
     * @param item предмет
     * @return текущий GUI
     */
    PaginatedGui addItem(ItemStack item);
    
    /**
     * Добавляет предмет на следующую доступную страницу с обработчиком кликов
     * @param item предмет
     * @param clickHandler обработчик кликов
     * @return текущий GUI
     */
    PaginatedGui addItem(ItemStack item, ClickHandler clickHandler);
    
    /**
     * Добавляет предмет на указанную страницу
     * @param page страница
     * @param item предмет
     * @return текущий GUI
     */
    PaginatedGui addItem(int page, ItemStack item);
    
    /**
     * Добавляет предмет на указанную страницу с обработчиком кликов
     * @param page страница
     * @param item предмет
     * @param clickHandler обработчик кликов
     * @return текущий GUI
     */
    PaginatedGui addItem(int page, ItemStack item, ClickHandler clickHandler);
    
    /**
     * Добавляет предмет на текущую страницу
     * @param item предмет
     * @return текущий GUI
     */
    PaginatedGui addPageItem(ItemStack item);
    
    /**
     * Добавляет предмет на текущую страницу с обработчиком кликов
     * @param item предмет
     * @param clickHandler обработчик кликов
     * @return текущий GUI
     */
    PaginatedGui addPageItem(ItemStack item, ClickHandler clickHandler);
    
    /**
     * Добавляет коллекцию предметов на страницы
     * @param items коллекция предметов
     * @return текущий GUI
     */
    PaginatedGui addPageItems(Collection<ItemStack> items);
    
    /**
     * Устанавливает предмет на указанную страницу и слот
     * @param page страница
     * @param slot слот
     * @param item предмет
     * @return текущий GUI
     */
    PaginatedGui setItem(int page, int slot, ItemStack item);
    
    /**
     * Устанавливает предмет на указанную страницу и слот с обработчиком кликов
     * @param page страница
     * @param slot слот
     * @param item предмет
     * @param clickHandler обработчик кликов
     * @return текущий GUI
     */
    PaginatedGui setItem(int page, int slot, ItemStack item, ClickHandler clickHandler);
    
    /**
     * Получает предмет с указанной страницы и слота
     * @param page страница
     * @param slot слот
     * @return предмет
     */
    ItemStack getItem(int page, int slot);
    
    /**
     * Устанавливает обработчик кликов на указанной странице и слоте
     * @param page страница
     * @param slot слот
     * @param clickHandler обработчик кликов
     * @return текущий GUI
     */
    PaginatedGui setClickHandler(int page, int slot, ClickHandler clickHandler);
    
    /**
     * Проверяет, установлен ли обработчик кликов на указанной странице и слоте
     * @param page страница
     * @param slot слот
     * @return true, если обработчик установлен
     */
    boolean hasClickHandler(int page, int slot);
    
    /**
     * Переключает на следующую страницу
     * @return текущий GUI
     */
    PaginatedGui nextPage();
    
    /**
     * Переключает на предыдущую страницу
     * @return текущий GUI
     */
    PaginatedGui previousPage();
    
    /**
     * Переключает на указанную страницу
     * @param page страница
     * @return текущий GUI
     */
    PaginatedGui setPage(int page);
    
    /**
     * Получает текущую страницу
     * @return текущая страница
     */
    int getCurrentPage();
    
    /**
     * Получает количество страниц
     * @return количество страниц
     */
    int getPageCount();
    
    /**
     * Устанавливает предмет для кнопки "Следующая страница"
     * @param item предмет
     * @return текущий GUI
     */
    PaginatedGui setNextPageItem(ItemStack item);
    
    /**
     * Устанавливает предмет для кнопки "Предыдущая страница"
     * @param item предмет
     * @return текущий GUI
     */
    PaginatedGui setPreviousPageItem(ItemStack item);
    
    /**
     * Устанавливает слот для кнопки "Следующая страница"
     * @param slot слот
     * @return текущий GUI
     */
    PaginatedGui setNextPageSlot(int slot);
    
    /**
     * Устанавливает слот для кнопки "Предыдущая страница"
     * @param slot слот
     * @return текущий GUI
     */
    PaginatedGui setPreviousPageSlot(int slot);
    
    /**
     * Устанавливает кнопку "Следующая страница"
     * @param item предмет
     * @param slot слот
     * @return текущий GUI
     */
    PaginatedGui setNextPageButton(ItemStack item, int slot);
    
    /**
     * Устанавливает кнопку "Предыдущая страница"
     * @param item предмет
     * @param slot слот
     * @return текущий GUI
     */
    PaginatedGui setPreviousPageButton(ItemStack item, int slot);
    
    /**
     * Получает слоты, которые используются для контента страницы
     * @return список слотов
     */
    List<Integer> getPageSlots();
    
    /**
     * Устанавливает слоты, которые будут использоваться для контента страницы
     * @param slots список слотов
     * @return текущий GUI
     */
    PaginatedGui setPageSlots(List<Integer> slots);
    
    /**
     * Устанавливает слоты по диапазону, которые будут использоваться для контента страницы
     * @param startSlot начальный слот (включительно)
     * @param endSlot конечный слот (включительно)
     * @return текущий GUI
     */
    PaginatedGui setPageSlots(int startSlot, int endSlot);
    
    /**
     * Устанавливает слоты по координатам, которые будут использоваться для контента страницы
     * @param startRow начальная строка (включительно)
     * @param startColumn начальная колонка (включительно)
     * @param endRow конечная строка (включительно)
     * @param endColumn конечная колонка (включительно)
     * @return текущий GUI
     */
    PaginatedGui setPageSlots(int startRow, int startColumn, int endRow, int endColumn);
    
    /**
     * Обновляет текущую страницу
     * @return текущий GUI
     */
    PaginatedGui updatePage();
    
    /**
     * Очищает все предметы на всех страницах
     * @return текущий GUI
     */
    PaginatedGui clearAllItems();
    
    /**
     * Очищает все предметы на текущей странице
     * @return текущий GUI
     */
    PaginatedGui clearPageItems();
    
    /**
     * Очищает все предметы на указанной странице
     * @param page страница
     * @return текущий GUI
     */
    PaginatedGui clearPage(int page);
    
    /**
     * Получает список предметов на текущей странице
     * @return список предметов
     */
    List<ItemStack> getCurrentPageItems();
    
    /**
     * Открывает инвентарь для игрока на указанной странице
     * @param player игрок
     * @param page страница
     */
    void open(Player player, int page);
    
    @Override
    default Gui update() {
        return updatePage();
    }
} 