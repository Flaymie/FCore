package dev.flaymie.fcore.core.action;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import java.util.concurrent.CompletableFuture;

/**
 * Действие для отображения заголовка и подзаголовка
 */
public class TitleAction extends AbstractAction {
    
    private final String title;
    private final String subtitle;
    private final int fadeIn;
    private final int stay;
    private final int fadeOut;
    
    /**
     * Создает новое действие отображения заголовка
     *
     * @param title заголовок
     * @param subtitle подзаголовок
     * @param fadeIn время появления в тиках
     * @param stay время отображения в тиках
     * @param fadeOut время исчезновения в тиках
     */
    public TitleAction(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        super("Title", "Отображает заголовок и подзаголовок", false);
        this.title = title;
        this.subtitle = subtitle;
        this.fadeIn = fadeIn;
        this.stay = stay;
        this.fadeOut = fadeOut;
    }
    
    /**
     * Создает новое действие отображения заголовка с временем по умолчанию
     *
     * @param title заголовок
     * @param subtitle подзаголовок
     */
    public TitleAction(String title, String subtitle) {
        this(title, subtitle, 10, 70, 20);
    }
    
    /**
     * Создает новое действие отображения только заголовка
     *
     * @param title заголовок
     */
    public TitleAction(String title) {
        this(title, "", 10, 70, 20);
    }
    
    @Override
    public CompletableFuture<Void> execute(Player player, ActionContext context) {
        // Применяем цветовое форматирование
        String formattedTitle = ChatColor.translateAlternateColorCodes('&', title);
        String formattedSubtitle = ChatColor.translateAlternateColorCodes('&', subtitle);
        
        player.sendTitle(formattedTitle, formattedSubtitle, fadeIn, stay, fadeOut);
        return CompletableFuture.completedFuture(null);
    }
    
    /**
     * Получает заголовок
     *
     * @return заголовок
     */
    public String getTitle() {
        return title;
    }
    
    /**
     * Получает подзаголовок
     *
     * @return подзаголовок
     */
    public String getSubtitle() {
        return subtitle;
    }
} 