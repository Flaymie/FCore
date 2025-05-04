package dev.flaymie.fcore.api.gui.impl;

import dev.flaymie.fcore.api.gui.ClickHandler;
import dev.flaymie.fcore.api.gui.PaginatedGui;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class PaginatedGuiImpl extends GuiImpl implements PaginatedGui {
    
    private final Map<Integer, Map<Integer, ItemStack>> pageItems;
    private final Map<Integer, Map<Integer, ClickHandler>> pageHandlers;
    
    private List<Integer> pageSlots;
    private int currentPage;
    private int pageCount;
    
    private ItemStack nextPageItem;
    private ItemStack previousPageItem;
    
    private int nextPageSlot;
    private int previousPageSlot;
    
    public PaginatedGuiImpl(String title, int rows) {
        super(title, rows);
        this.pageItems = new HashMap<>();
        this.pageHandlers = new HashMap<>();
        this.pageSlots = new ArrayList<>();
        this.currentPage = 0;
        this.pageCount = 1;
        
        // Устанавливаем слоты для страницы по умолчанию (центральная область GUI)
        setPageSlots(1, 1, rows - 2, 7);
        
        // Устанавливаем слоты для кнопок навигации по умолчанию
        this.nextPageSlot = rows * 9 - 3;
        this.previousPageSlot = rows * 9 - 7;
    }
    
    @Override
    public PaginatedGui addItem(ItemStack item) {
        return addItem(item, null);
    }
    
    @Override
    public PaginatedGui addItem(ItemStack item, ClickHandler clickHandler) {
        if (item == null) return this;
        
        // Найдем первый свободный слот на любой странице
        for (int page = 0; page < pageCount; page++) {
            for (int slot : pageSlots) {
                if (getItem(page, slot) == null) {
                    setItem(page, slot, item, clickHandler);
                    return this;
                }
            }
        }
        
        // Если не нашли свободных слотов, создаем новую страницу
        int newPage = pageCount;
        pageCount++;
        setItem(newPage, pageSlots.get(0), item, clickHandler);
        
        return this;
    }
    
    @Override
    public PaginatedGui addItem(int page, ItemStack item) {
        return addItem(page, item, null);
    }
    
    @Override
    public PaginatedGui addItem(int page, ItemStack item, ClickHandler clickHandler) {
        if (item == null) return this;
        
        // Проверяем, что страница существует или создаем новую
        if (page >= pageCount) {
            pageCount = page + 1;
        }
        
        // Находим первый свободный слот на указанной странице
        for (int slot : pageSlots) {
            if (getItem(page, slot) == null) {
                setItem(page, slot, item, clickHandler);
                return this;
            }
        }
        
        return this;
    }
    
    @Override
    public PaginatedGui addPageItem(ItemStack item) {
        return addItem(currentPage, item);
    }
    
    @Override
    public PaginatedGui addPageItem(ItemStack item, ClickHandler clickHandler) {
        return addItem(currentPage, item, clickHandler);
    }
    
    @Override
    public PaginatedGui addPageItems(Collection<ItemStack> items) {
        if (items == null || items.isEmpty()) return this;
        
        for (ItemStack item : items) {
            addItem(item);
        }
        
        return this;
    }
    
    @Override
    public PaginatedGui setItem(int page, int slot, ItemStack item) {
        if (page < 0 || slot < 0 || slot >= getSize()) return this;
        
        // Расширяем количество страниц, если нужно
        if (page >= pageCount) {
            pageCount = page + 1;
        }
        
        // Сохраняем предмет для указанной страницы и слота
        if (item == null) {
            if (pageItems.containsKey(page)) {
                pageItems.get(page).remove(slot);
            }
        } else {
            pageItems.computeIfAbsent(page, k -> new HashMap<>()).put(slot, item);
        }
        
        // Если это текущая страница, обновляем отображение
        if (page == currentPage) {
            super.setItem(slot, item);
        }
        
        return this;
    }
    
    @Override
    public PaginatedGui setItem(int page, int slot, ItemStack item, ClickHandler clickHandler) {
        setItem(page, slot, item);
        setClickHandler(page, slot, clickHandler);
        return this;
    }
    
    @Override
    public ItemStack getItem(int page, int slot) {
        if (page < 0 || page >= pageCount || slot < 0 || slot >= getSize()) {
            return null;
        }
        
        Map<Integer, ItemStack> items = pageItems.get(page);
        return items != null ? items.get(slot) : null;
    }
    
    @Override
    public PaginatedGui setClickHandler(int page, int slot, ClickHandler clickHandler) {
        if (page < 0 || slot < 0 || slot >= getSize()) return this;
        
        // Расширяем количество страниц, если нужно
        if (page >= pageCount) {
            pageCount = page + 1;
        }
        
        // Сохраняем обработчик для указанной страницы и слота
        if (clickHandler == null) {
            if (pageHandlers.containsKey(page)) {
                pageHandlers.get(page).remove(slot);
            }
        } else {
            pageHandlers.computeIfAbsent(page, k -> new HashMap<>()).put(slot, clickHandler);
        }
        
        // Если это текущая страница, обновляем обработчик
        if (page == currentPage) {
            super.setClickHandler(slot, clickHandler);
        }
        
        return this;
    }
    
    @Override
    public boolean hasClickHandler(int page, int slot) {
        if (page < 0 || page >= pageCount || slot < 0 || slot >= getSize()) {
            return false;
        }
        
        Map<Integer, ClickHandler> handlers = pageHandlers.get(page);
        return handlers != null && handlers.containsKey(slot);
    }
    
    @Override
    public PaginatedGui nextPage() {
        return setPage(currentPage + 1);
    }
    
    @Override
    public PaginatedGui previousPage() {
        return setPage(currentPage - 1);
    }
    
    @Override
    public PaginatedGui setPage(int page) {
        if (page < 0 || page >= pageCount || page == currentPage) return this;
        
        currentPage = page;
        return updatePage();
    }
    
    @Override
    public int getCurrentPage() {
        return currentPage;
    }
    
    @Override
    public int getPageCount() {
        return pageCount;
    }
    
    @Override
    public PaginatedGui setNextPageItem(ItemStack item) {
        this.nextPageItem = item;
        return updatePage();
    }
    
    @Override
    public PaginatedGui setPreviousPageItem(ItemStack item) {
        this.previousPageItem = item;
        return updatePage();
    }
    
    @Override
    public PaginatedGui setNextPageSlot(int slot) {
        if (slot < 0 || slot >= getSize()) return this;
        
        this.nextPageSlot = slot;
        return updatePage();
    }
    
    @Override
    public PaginatedGui setPreviousPageSlot(int slot) {
        if (slot < 0 || slot >= getSize()) return this;
        
        this.previousPageSlot = slot;
        return updatePage();
    }
    
    @Override
    public PaginatedGui setNextPageButton(ItemStack item, int slot) {
        this.nextPageItem = item;
        this.nextPageSlot = slot;
        return updatePage();
    }
    
    @Override
    public PaginatedGui setPreviousPageButton(ItemStack item, int slot) {
        this.previousPageItem = item;
        this.previousPageSlot = slot;
        return updatePage();
    }
    
    @Override
    public List<Integer> getPageSlots() {
        return new ArrayList<>(pageSlots);
    }
    
    @Override
    public PaginatedGui setPageSlots(List<Integer> slots) {
        if (slots == null || slots.isEmpty()) return this;
        
        this.pageSlots = new ArrayList<>(slots);
        return this;
    }
    
    @Override
    public PaginatedGui setPageSlots(int startSlot, int endSlot) {
        if (startSlot < 0 || endSlot >= getSize() || startSlot > endSlot) return this;
        
        List<Integer> slots = new ArrayList<>();
        for (int i = startSlot; i <= endSlot; i++) {
            slots.add(i);
        }
        
        return setPageSlots(slots);
    }
    
    @Override
    public PaginatedGui setPageSlots(int startRow, int startColumn, int endRow, int endColumn) {
        if (startRow < 0 || endRow >= rows || startColumn < 0 || endColumn >= 9 || 
            startRow > endRow || startColumn > endColumn) {
            return this;
        }
        
        List<Integer> slots = new ArrayList<>();
        for (int row = startRow; row <= endRow; row++) {
            for (int col = startColumn; col <= endColumn; col++) {
                slots.add(row * 9 + col);
            }
        }
        
        return setPageSlots(slots);
    }
    
    @Override
    public PaginatedGui updatePage() {
        // Очищаем все слоты
        for (int i = 0; i < getSize(); i++) {
            super.setItem(i, null);
            super.setClickHandler(i, null);
        }
        
        // Заполняем слоты текущей страницы
        Map<Integer, ItemStack> items = pageItems.getOrDefault(currentPage, Collections.emptyMap());
        Map<Integer, ClickHandler> handlers = pageHandlers.getOrDefault(currentPage, Collections.emptyMap());
        
        for (Map.Entry<Integer, ItemStack> entry : items.entrySet()) {
            int slot = entry.getKey();
            ItemStack item = entry.getValue();
            super.setItem(slot, item);
            
            ClickHandler handler = handlers.get(slot);
            if (handler != null) {
                super.setClickHandler(slot, handler);
            }
        }
        
        // Добавляем кнопки навигации, если необходимо
        if (currentPage > 0 && previousPageItem != null) {
            super.setItem(previousPageSlot, previousPageItem, event -> previousPage().update());
        }
        
        if (currentPage < pageCount - 1 && nextPageItem != null) {
            super.setItem(nextPageSlot, nextPageItem, event -> nextPage().update());
        }
        
        super.update();
        return this;
    }
    
    @Override
    public PaginatedGui clearAllItems() {
        pageItems.clear();
        pageHandlers.clear();
        pageCount = 1;
        currentPage = 0;
        
        return updatePage();
    }
    
    @Override
    public PaginatedGui clearPageItems() {
        return clearPage(currentPage);
    }
    
    @Override
    public PaginatedGui clearPage(int page) {
        if (page < 0 || page >= pageCount) return this;
        
        pageItems.remove(page);
        pageHandlers.remove(page);
        
        // Если очистили текущую страницу, обновляем отображение
        if (page == currentPage) {
            updatePage();
        }
        
        return this;
    }
    
    @Override
    public List<ItemStack> getCurrentPageItems() {
        Map<Integer, ItemStack> items = pageItems.getOrDefault(currentPage, Collections.emptyMap());
        return new ArrayList<>(items.values());
    }
    
    @Override
    public void open(Player player, int page) {
        if (player == null) return;
        
        currentPage = Math.max(0, Math.min(page, pageCount - 1));
        updatePage();
        open(player);
    }
    
    @Override
    public void open(Player player) {
        super.open(player);
        updatePage();
    }
} 