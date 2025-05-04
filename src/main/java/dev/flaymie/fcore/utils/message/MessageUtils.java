package dev.flaymie.fcore.utils.message;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Утилиты для работы с сообщениями
 */
public class MessageUtils {
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Pattern GRADIENT_PATTERN = Pattern.compile("\\{#([A-Fa-f0-9]{6})>(#[A-Fa-f0-9]{6})}(.*?)\\{/>}");
    
    /**
     * Преобразует цветовые коды и HEX-коды в сообщении
     * @param message сообщение для форматирования
     * @return форматированное сообщение
     */
    public static String colorize(String message) {
        if (message == null) return null;
        
        // Обработка HEX-цветов формата &#RRGGBB
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer sb = new StringBuffer();
        
        while (matcher.find()) {
            String hex = matcher.group(1);
            ChatColor hexColor = ChatColor.of("#" + hex);
            matcher.appendReplacement(sb, hexColor.toString());
        }
        matcher.appendTail(sb);
        
        // Обработка стандартных цветовых кодов
        return ChatColor.translateAlternateColorCodes('&', sb.toString());
    }
    
    /**
     * Применяет градиент к тексту
     * @param text текст
     * @param fromHex начальный HEX-цвет
     * @param toHex конечный HEX-цвет
     * @return текст с градиентом
     */
    public static String gradient(String text, String fromHex, String toHex) {
        if (text == null || text.isEmpty()) return text;
        
        java.awt.Color fromColor = java.awt.Color.decode(fromHex);
        java.awt.Color toColor = java.awt.Color.decode(toHex);
        
        int rDiff = toColor.getRed() - fromColor.getRed();
        int gDiff = toColor.getGreen() - fromColor.getGreen();
        int bDiff = toColor.getBlue() - fromColor.getBlue();
        
        int length = text.length();
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < length; i++) {
            // Пропускаем символы форматирования
            if (text.charAt(i) == '&' && i + 1 < length) {
                result.append("&").append(text.charAt(i+1));
                i++;
                continue;
            }
            
            float ratio = (float) i / (length - 1);
            int r = (int) (fromColor.getRed() + rDiff * ratio);
            int g = (int) (fromColor.getGreen() + gDiff * ratio);
            int b = (int) (fromColor.getBlue() + bDiff * ratio);
            
            String hex = String.format("#%02X%02X%02X", r, g, b);
            result.append(ChatColor.of(hex)).append(text.charAt(i));
        }
        
        return result.toString();
    }
    
    /**
     * Обрабатывает градиенты в тексте формата {#RRGGBB>#RRGGBB}текст{/>}
     * @param text текст с градиентами
     * @return обработанный текст
     */
    public static String processGradients(String text) {
        if (text == null) return null;
        
        Matcher matcher = GRADIENT_PATTERN.matcher(text);
        StringBuffer sb = new StringBuffer();
        
        while (matcher.find()) {
            String fromHex = "#" + matcher.group(1);
            String toHex = matcher.group(2);
            String content = matcher.group(3);
            
            String processed = gradient(content, fromHex, toHex);
            matcher.appendReplacement(sb, processed);
        }
        matcher.appendTail(sb);
        
        return sb.toString();
    }
    
    /**
     * Отправляет сообщение игроку или в консоль с поддержкой цветов
     * @param sender получатель сообщения
     * @param message сообщение
     */
    public static void send(CommandSender sender, String message) {
        if (sender == null || message == null) return;
        
        sender.sendMessage(colorize(message));
    }
    
    /**
     * Отправляет сообщение игроку или в консоль с поддержкой цветов и подстановкой плейсхолдеров
     * @param sender получатель сообщения
     * @param message сообщение
     * @param replacements пары ключ-значение для замены
     */
    public static void send(CommandSender sender, String message, Object... replacements) {
        if (sender == null || message == null) return;
        
        String processedMessage = message;
        for (int i = 0; i < replacements.length - 1; i += 2) {
            processedMessage = processedMessage.replace(
                    String.valueOf(replacements[i]),
                    String.valueOf(replacements[i + 1])
            );
        }
        
        send(sender, processedMessage);
    }
    
    /**
     * Отправляет заголовок игроку
     * @param player игрок
     * @param title заголовок
     * @param subtitle подзаголовок
     * @param fadeIn время появления (тики)
     * @param stay время отображения (тики)
     * @param fadeOut время исчезновения (тики)
     */
    public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        if (player == null) return;
        
        player.sendTitle(
                title != null ? colorize(title) : null,
                subtitle != null ? colorize(subtitle) : null,
                fadeIn, stay, fadeOut
        );
    }
    
    /**
     * Отправляет сообщение в ActionBar игроку
     * @param player игрок
     * @param message сообщение
     */
    public static void sendActionBar(Player player, String message) {
        if (player == null || message == null) return;
        
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(colorize(message)));
    }
    
    /**
     * Создает кликабельный компонент сообщения
     * @param text текст
     * @param command команда (начинается с /)
     * @param hover текст при наведении
     * @return компонент сообщения
     */
    public static TextComponent createClickableCommand(String text, String command, String hover) {
        TextComponent component = new TextComponent(colorize(text));
        
        if (command != null && !command.isEmpty()) {
            component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        }
        
        if (hover != null && !hover.isEmpty()) {
            component.setHoverEvent(new HoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder(colorize(hover)).create()
            ));
        }
        
        return component;
    }
    
    /**
     * Создает кликабельную ссылку
     * @param text текст
     * @param url ссылка
     * @param hover текст при наведении
     * @return компонент сообщения
     */
    public static TextComponent createClickableLink(String text, String url, String hover) {
        TextComponent component = new TextComponent(colorize(text));
        
        if (url != null && !url.isEmpty()) {
            component.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
        }
        
        if (hover != null && !hover.isEmpty()) {
            component.setHoverEvent(new HoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder(colorize(hover)).create()
            ));
        }
        
        return component;
    }
    
    /**
     * Отправляет сообщение всем игрокам
     * @param message сообщение
     */
    public static void broadcast(String message) {
        if (message == null) return;
        
        Bukkit.broadcastMessage(colorize(message));
    }
    
    /**
     * Центрирует текст в чате
     * @param message сообщение для центрирования
     * @return центрированное сообщение
     */
    public static String center(String message) {
        if (message == null || message.isEmpty()) return message;
        
        // Примерная ширина чата в символах
        final int CHAT_WIDTH = 154;
        
        int messageWidth = 0;
        boolean isBold = false;
        
        // Подсчитываем ширину сообщения с учетом форматирования
        for (int i = 0; i < message.length(); i++) {
            char c = message.charAt(i);
            
            if (c == '§' || c == '&') {
                if (i + 1 < message.length()) {
                    char format = message.charAt(i + 1);
                    if (format == 'l') {
                        isBold = true;
                    } else if (format == 'r') {
                        isBold = false;
                    }
                    i++; // Пропускаем следующий символ
                    continue;
                }
            }
            
            // Ширина символа (приблизительно)
            messageWidth += isBold ? 6 : 4;
        }
        
        int spaces = (CHAT_WIDTH - messageWidth) / 8; // 4 - примерная ширина пробела
        
        // Добавляем пробелы в начало сообщения
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < spaces; i++) {
            result.append(" ");
        }
        result.append(message);
        
        return result.toString();
    }
    
    /**
     * Отправляет сообщение игроку с поддержкой цветов
     * @param player игрок
     * @param message сообщение
     */
    public static void sendMessage(Player player, String message) {
        if (player == null || message == null) return;
        
        player.sendMessage(colorize(message));
    }
    
    /**
     * Отправляет сообщение игроку с поддержкой цветов и подстановкой плейсхолдеров
     * @param player игрок
     * @param message сообщение
     * @param replacements пары ключ-значение для замены
     */
    public static void sendMessage(Player player, String message, Object... replacements) {
        if (player == null || message == null) return;
        
        String processedMessage = message;
        for (int i = 0; i < replacements.length - 1; i += 2) {
            processedMessage = processedMessage.replace(
                    String.valueOf(replacements[i]),
                    String.valueOf(replacements[i + 1])
            );
        }
        
        sendMessage(player, processedMessage);
    }
} 