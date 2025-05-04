package dev.flaymie.fcore.api.gui.example;

import dev.flaymie.fcore.api.gui.AnimatedGui;
import dev.flaymie.fcore.api.gui.Gui;
import dev.flaymie.fcore.api.gui.GuiFactory;
import dev.flaymie.fcore.api.gui.GuiManager;
import dev.flaymie.fcore.api.gui.PaginatedGui;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Пример использования GUI компонентов
 */
public class GuiExample {
    
    /**
     * Создает простой GUI
     * @param player игрок
     */
    public static void openSimpleGui(Player player) {
        // Создаем простой GUI с 3 строками
        Gui gui = GuiFactory.createGui(ChatColor.GOLD + "Простой GUI", 3);
        
        // Добавляем предметы
        gui.setItem(4, createItem(Material.DIAMOND,
                ChatColor.AQUA + "Алмаз",
                ChatColor.GRAY + "Кликните, чтобы получить алмазы"), 
                event -> player.getInventory().addItem(new ItemStack(Material.DIAMOND, 64)));
        
        // Добавляем предмет с использованием координат (строка, колонка)
        gui.setItem(1, 1, createItem(Material.GOLD_INGOT,
                ChatColor.YELLOW + "Золото",
                ChatColor.GRAY + "Кликните, чтобы получить золото"), 
                event -> player.getInventory().addItem(new ItemStack(Material.GOLD_INGOT, 64)));
        
        // Заполняем пустые слоты стеклом
        gui.fillEmptySlots(createItem(Material.BLACK_STAINED_GLASS_PANE, " "));
        
        // Открываем GUI через менеджер
        GuiManager.getInstance().openGui(player, gui);
    }
    
    /**
     * Создает пагинированный GUI
     * @param player игрок
     */
    public static void openPaginatedGui(Player player) {
        // Создаем пагинированный GUI с 6 строками
        PaginatedGui gui = GuiFactory.createPaginatedGui(ChatColor.GREEN + "Пагинированный GUI", 6);
        
        // Создаем кнопки для навигации
        ItemStack nextButton = createItem(Material.ARROW,
                ChatColor.GREEN + "Следующая страница");
        
        ItemStack prevButton = createItem(Material.ARROW,
                ChatColor.RED + "Предыдущая страница");
        
        // Устанавливаем кнопки навигации
        gui.setNextPageButton(nextButton, 53);
        gui.setPreviousPageButton(prevButton, 45);
        
        // Добавляем много предметов для пагинации
        for (int i = 0; i < 100; i++) {
            Material material = Material.values()[i % Material.values().length];
            if (!material.isItem()) continue;
            
            gui.addItem(createItem(material,
                    ChatColor.YELLOW + material.name(),
                    ChatColor.GRAY + "Страница: " + (gui.getCurrentPage() + 1)), 
                    event -> player.getInventory().addItem(new ItemStack(material, 1)));
        }
        
        // Открываем GUI через менеджер
        GuiManager.getInstance().openGui(player, gui);
    }
    
    /**
     * Создает анимированный GUI
     * @param player игрок
     */
    public static void openAnimatedGui(Player player) {
        // Создаем анимированный GUI с 3 строками и интервалом 10 тиков (0.5 секунды)
        AnimatedGui gui = GuiFactory.createAnimatedGui(ChatColor.LIGHT_PURPLE + "Анимированный GUI", 3, 10);
        
        // Создаем анимацию с 10 кадрами
        gui.setFrameCount(10);
        
        // Добавляем анимацию для центрального слота
        for (int frame = 0; frame < 10; frame++) {
            Material material = Material.values()[frame % Material.values().length];
            if (!material.isItem()) continue;
            
            gui.addAnimationFrame(frame, 13, createItem(material,
                    ChatColor.GOLD + "Кадр: " + (frame + 1)));
        }
        
        // Добавляем обработчик для каждого кадра
        gui.onFrame(frame -> {
            // Можно делать что-то при каждом кадре
            // Например, воспроизводить звук
            if (frame % 2 == 0) {
                player.playSound(player.getLocation(), "block.note_block.harp", 1.0f, 1.0f);
            }
        });
        
        // Устанавливаем циклическую анимацию
        gui.setLoop(true);
        
        // Открываем GUI через менеджер
        GuiManager.getInstance().openGui(player, gui);
    }
    
    /**
     * Создает комбинированный GUI (пагинация + анимация)
     * @param player игрок
     */
    public static void openCombinedGui(Player player) {
        // Создаем простой GUI с 6 строками
        Gui gui = GuiFactory.createGui(ChatColor.AQUA + "Комбинированный GUI", 6);
        
        // Добавляем кнопку для открытия простого GUI
        gui.setItem(10, createItem(Material.CHEST,
                ChatColor.YELLOW + "Простой GUI",
                ChatColor.GRAY + "Кликните, чтобы открыть простой GUI"), 
                event -> openSimpleGui(player));
        
        // Добавляем кнопку для открытия пагинированного GUI
        gui.setItem(12, createItem(Material.BOOK,
                ChatColor.GREEN + "Пагинированный GUI",
                ChatColor.GRAY + "Кликните, чтобы открыть пагинированный GUI"), 
                event -> openPaginatedGui(player));
        
        // Добавляем кнопку для открытия анимированного GUI
        gui.setItem(14, createItem(Material.ENDER_EYE,
                ChatColor.LIGHT_PURPLE + "Анимированный GUI",
                ChatColor.GRAY + "Кликните, чтобы открыть анимированный GUI"), 
                event -> openAnimatedGui(player));
        
        // Добавляем кнопку закрытия
        gui.setItem(16, createItem(Material.BARRIER,
                ChatColor.RED + "Закрыть"), 
                event -> player.closeInventory());
        
        // Заполняем пустые слоты стеклом
        gui.fillEmptySlots(createItem(Material.GRAY_STAINED_GLASS_PANE, " "));
        
        // Открываем GUI через менеджер
        GuiManager.getInstance().openGui(player, gui);
    }
    
    /**
     * Создает предмет с названием и описанием
     * @param material материал
     * @param name название
     * @param lore описание (строки)
     * @return созданный предмет
     */
    private static ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(name);
            
            if (lore.length > 0) {
                List<String> loreList = new ArrayList<>();
                Collections.addAll(loreList, lore);
                meta.setLore(loreList);
            }
            
            item.setItemMeta(meta);
        }
        
        return item;
    }
} 