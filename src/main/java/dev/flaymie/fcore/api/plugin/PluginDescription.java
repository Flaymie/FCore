package dev.flaymie.fcore.api.plugin;

import java.util.List;

/**
 * Описание плагина FCore, содержит информацию из plugin.yml
 */
public class PluginDescription {
    
    private final String name;
    private final String version;
    private final String main;
    private final String description;
    private final String author;
    private final List<String> depends;
    
    /**
     * Конструктор описания плагина
     * @param name имя плагина
     * @param version версия плагина
     * @param main полное имя главного класса
     * @param description описание плагина
     * @param author автор плагина
     * @param depends зависимости плагина
     */
    public PluginDescription(String name, String version, String main, String description, String author, List<String> depends) {
        this.name = name;
        this.version = version;
        this.main = main;
        this.description = description;
        this.author = author;
        this.depends = depends;
    }
    
    /**
     * Получает имя плагина
     * @return имя плагина
     */
    public String getName() {
        return name;
    }
    
    /**
     * Получает версию плагина
     * @return версия плагина
     */
    public String getVersion() {
        return version;
    }
    
    /**
     * Получает полное имя главного класса
     * @return полное имя главного класса
     */
    public String getMain() {
        return main;
    }
    
    /**
     * Получает описание плагина
     * @return описание плагина
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Получает автора плагина
     * @return автор плагина
     */
    public String getAuthor() {
        return author;
    }
    
    /**
     * Получает список зависимостей плагина
     * @return список зависимостей
     */
    public List<String> getDepends() {
        return depends;
    }
} 