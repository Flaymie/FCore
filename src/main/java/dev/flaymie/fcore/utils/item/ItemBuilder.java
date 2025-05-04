package dev.flaymie.fcore.utils.item;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Билдер для создания предметов в удобном виде
 * Позволяет быстро настроить предмет с помощью цепочки методов
 */
public class ItemBuilder {
    private ItemStack itemStack;
    private ItemMeta itemMeta;

    private ItemBuilder(ItemStack itemStack) {
        this.itemStack = itemStack;
        this.itemMeta = itemStack.getItemMeta();
    }

    /**
     * Создать новый билдер на основе материала
     * @param material материал предмета
     * @return новый билдер
     */
    public static ItemBuilder of(Material material) {
        return new ItemBuilder(new ItemStack(material));
    }

    /**
     * Создать новый билдер на основе предмета
     * @param itemStack готовый предмет
     * @return новый билдер
     */
    public static ItemBuilder of(ItemStack itemStack) {
        return new ItemBuilder(itemStack.clone());
    }

    /**
     * Создать новый билдер на основе материала с указанным количеством
     * @param material материал предмета
     * @param amount количество
     * @return новый билдер
     */
    public static ItemBuilder of(Material material, int amount) {
        return new ItemBuilder(new ItemStack(material, amount));
    }

    /**
     * Задать имя предмета
     * @param name новое имя предмета (поддерживает цветовые коды &)
     * @return тот же билдер
     */
    public ItemBuilder name(String name) {
        if (itemMeta != null) {
            itemMeta.setDisplayName(name.replace("&", "§"));
        }
        return this;
    }

    /**
     * Задать описание предмета
     * @param lore строки описания (поддерживают цветовые коды &)
     * @return тот же билдер
     */
    public ItemBuilder lore(String... lore) {
        if (itemMeta != null) {
            List<String> coloredLore = new ArrayList<>();
            for (String line : lore) {
                coloredLore.add(line.replace("&", "§"));
            }
            itemMeta.setLore(coloredLore);
        }
        return this;
    }

    /**
     * Задать описание предмета
     * @param lore список строк описания (поддерживают цветовые коды &)
     * @return тот же билдер
     */
    public ItemBuilder lore(List<String> lore) {
        if (itemMeta != null) {
            List<String> coloredLore = new ArrayList<>();
            for (String line : lore) {
                coloredLore.add(line.replace("&", "§"));
            }
            itemMeta.setLore(coloredLore);
        }
        return this;
    }

    /**
     * Добавить строку к описанию предмета
     * @param line строка для добавления (поддерживает цветовые коды &)
     * @return тот же билдер
     */
    public ItemBuilder addLoreLine(String line) {
        if (itemMeta != null) {
            List<String> lore = itemMeta.getLore();
            if (lore == null) {
                lore = new ArrayList<>();
            }
            lore.add(line.replace("&", "§"));
            itemMeta.setLore(lore);
        }
        return this;
    }

    /**
     * Добавить несколько строк к описанию предмета
     * @param lines строки для добавления (поддерживают цветовые коды &)
     * @return тот же билдер
     */
    public ItemBuilder addLoreLines(String... lines) {
        if (itemMeta != null) {
            List<String> lore = itemMeta.getLore();
            if (lore == null) {
                lore = new ArrayList<>();
            }
            for (String line : lines) {
                lore.add(line.replace("&", "§"));
            }
            itemMeta.setLore(lore);
        }
        return this;
    }

    /**
     * Добавить зачарование на предмет
     * @param enchantment тип зачарования
     * @param level уровень зачарования
     * @return тот же билдер
     */
    public ItemBuilder enchant(Enchantment enchantment, int level) {
        itemStack.addUnsafeEnchantment(enchantment, level);
        return this;
    }

    /**
     * Добавить несколько зачарований на предмет
     * @param enchantments карта зачарований и их уровней
     * @return тот же билдер
     */
    public ItemBuilder enchant(Map<Enchantment, Integer> enchantments) {
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            itemStack.addUnsafeEnchantment(entry.getKey(), entry.getValue());
        }
        return this;
    }

    /**
     * Добавить флаг предмету
     * @param flag флаг предмета
     * @return тот же билдер
     */
    public ItemBuilder flag(ItemFlag flag) {
        if (itemMeta != null) {
            itemMeta.addItemFlags(flag);
        }
        return this;
    }

    /**
     * Добавить несколько флагов предмету
     * @param flags флаги предмета
     * @return тот же билдер
     */
    public ItemBuilder flags(ItemFlag... flags) {
        if (itemMeta != null) {
            itemMeta.addItemFlags(flags);
        }
        return this;
    }

    /**
     * Скрыть все атрибуты предмета
     * @return тот же билдер
     */
    public ItemBuilder hideAll() {
        return flags(ItemFlag.values());
    }

    /**
     * Сделать предмет небьющимся
     * @return тот же билдер
     */
    public ItemBuilder unbreakable() {
        if (itemMeta != null) {
            itemMeta.setUnbreakable(true);
        }
        return this;
    }

    /**
     * Задать модель предмета
     * @param modelData номер модели
     * @return тот же билдер
     */
    public ItemBuilder modelData(int modelData) {
        if (itemMeta != null) {
            itemMeta.setCustomModelData(modelData);
        }
        return this;
    }

    /**
     * Задать количество предметов в стаке
     * @param amount количество предметов
     * @return тот же билдер
     */
    public ItemBuilder amount(int amount) {
        itemStack.setAmount(amount);
        return this;
    }

    /**
     * Применить функцию к мета-данным предмета
     * @param metaConsumer функция для изменения мета-данных
     * @return тот же билдер
     */
    public ItemBuilder meta(Consumer<ItemMeta> metaConsumer) {
        if (itemMeta != null) {
            metaConsumer.accept(itemMeta);
        }
        return this;
    }

    /**
     * Собрать готовый предмет
     * @return готовый предмет
     */
    public ItemStack build() {
        if (itemMeta != null) {
            itemStack.setItemMeta(itemMeta);
        }
        return itemStack;
    }
} 