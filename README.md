# FCore - Ядро для плагинов Minecraft Spigot 1.16.5

![Version](https://img.shields.io/badge/version-1.0-blue)
![API](https://img.shields.io/badge/API-Spigot%201.16.5-yellow)
![Java](https://img.shields.io/badge/Java-1.8%2B-orange)

## Содержание
- [Введение](#введение)
- [Установка](#установка)
- [Архитектура](#архитектура)
- [Система зависимостей (DI)](#система-зависимостей-di)
- [Система команд](#система-команд)
- [Система событий](#система-событий)
- [GUI система](#gui-система)
- [Управление данными](#управление-данными)
- [Action-система](#action-система)
- [Система прав](#система-прав)
- [Утилиты](#утилиты)
- [Интеграции](#интеграции)
- [Режим разработчика](#режим-разработчика)
- [Security API](#security-api)
- [Примеры использования](#примеры-использования)

## Введение

FCore - это мощное ядро для разработки плагинов Minecraft, объединяющее защиту и удобство разработки. Без этого ядра ни один плагин не сможет работать, что обеспечивает надежную защиту от кражи контента. Богатый набор инструментов упрощает и ускоряет процесс разработки плагинов.

### Ключевые возможности

- **Система зависимостей (DI)** - автоматическое внедрение сервисов через аннотации
- **Система команд** - простая регистрация команд через аннотации
- **Система событий** - удобная работа с событиями Bukkit и кастомными событиями
- **GUI система** - создание меню через удобный билдер
- **Управление данными** - работа с конфигурациями и базами данных
- **Action-система** - визуальный движок для создания пользовательских сценариев
- **Система прав** - гибкая настройка прав через DSL
- **Утилиты** - множество вспомогательных классов для работы с игровыми объектами
- **Интеграции** - готовые интеграции с популярными плагинами
- **Режим разработчика** - инструменты для отладки плагинов
- **Security API** - защита плагинов от взлома и кражи кода

## Установка

### Требования
- Java 8 или выше
- Сервер Spigot/Paper 1.16.5
- Maven/Gradle

### Установка ядра на сервер

1. Скачайте последнюю версию FCore с сайта или GitHub
2. Поместите файл `FCore-1.0.jar` в директорию `plugins` вашего сервера
3. Перезапустите сервер

### Подключение к вашему проекту

#### Maven

```xml
<repositories>
    <repository>
        <id>flaymie-repo</id>
        <url>https://repo.flaymie.dev/maven</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>dev.flaymie</groupId>
        <artifactId>fcore</artifactId>
        <version>1.0</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

#### Gradle

```groovy
repositories {
    maven { url 'https://repo.flaymie.dev/maven' }
}

dependencies {
    compileOnly 'dev.flaymie:fcore:1.0'
}
```

### Настройка проекта

В вашем файле `plugin.yml` добавьте зависимость от FCore:

```yaml
name: YourPlugin
version: 1.0
main: your.package.YourPlugin
api-version: 1.16
depend: [FCore]
```

## Архитектура

FCore организован в следующие модули:

### Слои архитектуры

1. **Core Layer** - базовая функциональность, сканеры классов, загрузчики
   - `dev.flaymie.fcore.core.di` - система зависимостей
   - `dev.flaymie.fcore.core.command` - система команд
   - `dev.flaymie.fcore.core.event` - система событий
   - `dev.flaymie.fcore.core.config` - система конфигураций
   - `dev.flaymie.fcore.core.data` - работа с данными
   - `dev.flaymie.fcore.core.task` - планировщик задач
   - `dev.flaymie.fcore.core.action` - система действий
   - `dev.flaymie.fcore.core.security` - безопасность

2. **API Layer** - публичный API для разработчиков плагинов
   - `dev.flaymie.fcore.api.annotation` - аннотации
   - `dev.flaymie.fcore.api.event` - API событий
   - `dev.flaymie.fcore.api.command` - API команд
   - `dev.flaymie.fcore.api.gui` - API GUI
   - `dev.flaymie.fcore.api.service` - сервисные интерфейсы

3. **Utility Layer** - вспомогательные классы
   - `dev.flaymie.fcore.utils.item` - работа с предметами
   - `dev.flaymie.fcore.utils.text` - форматирование текста
   - `dev.flaymie.fcore.utils.location` - работа с локациями
   - `dev.flaymie.fcore.utils.math` - математические утилиты

4. **Integration Layer** - интеграции со сторонними плагинами
   - `dev.flaymie.fcore.integration.vault` - интеграция с Vault
   - `dev.flaymie.fcore.integration.papi` - интеграция с PlaceholderAPI

### Жизненный цикл плагина

1. **Начальная загрузка** - регистрация FCore в Bukkit
2. **Сканирование классов** - поиск аннотированных классов
3. **Инициализация DI** - создание контейнера зависимостей
4. **Регистрация сервисов** - регистрация и инициализация сервисов
5. **Регистрация команд и слушателей** - автоматическая регистрация
6. **Загрузка конфигураций** - загрузка настроек из файлов
7. **Инициализация интеграций** - подключение к сторонним плагинам
8. **Плагин готов к работе** - запуск пользовательского кода

## Система зависимостей (DI)

Система зависимостей (Dependency Injection) в FCore обеспечивает автоматическое внедрение сервисов и компонентов в ваш код, избавляя от необходимости вручную создавать и управлять зависимостями.

### Основные возможности

- **Автоматическое внедрение** через аннотации
- **Контейнер зависимостей**, управляющий жизненным циклом объектов
- **Скоуп-менеджмент**: Singleton, Prototype, Session и др.
- **Автоинициализация** сервисов при старте плагина
- **Поддержка жизненного цикла** через аннотации @PostConstruct и @PreDestroy
- **Lazy-loading** для тяжелых компонентов

### Аннотации

- **@Service** - помечает класс как сервис, который будет автоматически зарегистрирован в контейнере
- **@Inject** - внедряет зависимость в поле или конструктор
- **@Autowired** - аналог @Inject, для разработчиков, привыкших к Spring
- **@PostConstruct** - метод, который будет вызван после создания объекта и внедрения всех зависимостей
- **@PreDestroy** - метод, который будет вызван перед уничтожением объекта
- **@Lazy** - указывает, что сервис должен быть создан при первом обращении, а не при старте

### Скоупы

- **SINGLETON** (по умолчанию) - создается один экземпляр на весь жизненный цикл плагина
- **PROTOTYPE** - создается новый экземпляр при каждом запросе
- **SESSION** - один экземпляр для каждой сессии (например, для каждого игрока)
- **REQUEST** - один экземпляр для каждого запроса

### Примеры использования

#### Создание сервиса

```java
@Service
public class PlayerDataService {
    
    @Inject
    private DatabaseManager database;
    
    @Inject
    private ConfigManager config;
    
    private Map<UUID, PlayerData> cache = new HashMap<>();
    
    @PostConstruct
    public void init() {
        // Код инициализации, выполняется после внедрения всех зависимостей
        System.out.println("PlayerDataService инициализирован");
    }
    
    public PlayerData getPlayerData(Player player) {
        return cache.computeIfAbsent(player.getUniqueId(), id -> 
            database.load(PlayerData.class, id));
    }
    
    @PreDestroy
    public void shutdown() {
        // Код очистки ресурсов перед выключением
        cache.clear();
    }
}
```

#### Внедрение через конструктор

```java
@Service
public class EconomyService {
    
    private final DatabaseManager database;
    private final ConfigManager config;
    
    @Inject
    public EconomyService(DatabaseManager database, ConfigManager config) {
        this.database = database;
        this.config = config;
    }
    
    // Методы сервиса
}
```

#### Использование скоупов

```java
@Service
@Scope(InjectionScope.SESSION)
public class PlayerSession {
    
    private final UUID playerId;
    private long lastAction;
    
    @Inject
    public PlayerSession(SessionContext context) {
        this.playerId = context.getPlayerId();
        this.lastAction = System.currentTimeMillis();
    }
    
    // Методы сессии
}
```

#### Получение сервиса из контейнера

```java
// В вашем плагине
public class YourPlugin extends JavaPlugin {
    
    @Override
    public void onEnable() {
        // Получение DI-контейнера FCore
        DependencyContainer container = FCore.getInstance().getDependencyContainer();
        
        // Получение сервиса из контейнера
        PlayerDataService playerDataService = container.getInstance(PlayerDataService.class);
        
        // Прямое внедрение в существующий объект
        container.injectDependencies(this);
    }
}
```

## Система команд

Система команд FCore позволяет легко создавать и регистрировать команды с помощью аннотаций, автоматически генерировать табдополнение и проверять права.

### Основные возможности

- **Аннотации для команд**: @Command, @Subcommand
- **Проверка прав**: автоматическая проверка через аннотацию @Permission
- **Автодополнение**: генерация подсказок для команд и аргументов
- **Валидация аргументов**: автоматическая конвертация и проверка типов
- **Алиасы команд**: поддержка множественных алиасов
- **Кулдауны**: ограничение частоты использования команд
- **Документация**: автоматическая генерация справки

### Аннотации

- **@Command** - помечает класс как команду
- **@Subcommand** - помечает метод как подкоманду
- **@Permission** - указывает права, необходимые для выполнения команды
- **@Cooldown** - устанавливает задержку между использованиями команды
- **@Argument** - задает имя и тип аргумента для документации и валидации

### Примеры использования

#### Простая команда

```java
@Command(name = "heal", description = "Лечит игрока", aliases = {"h"})
@Permission("yourplugin.heal")
public class HealCommand {
    
    @Inject
    private PlayerManager playerManager;
    
    @Subcommand(value = "self", aliases = {"me"})
    public void healSelf(Player player) {
        playerManager.heal(player);
        player.sendMessage("Ты исцелен!");
    }
    
    @Subcommand("other")
    @Permission("yourplugin.heal.other")
    public void healOther(Player player, @Argument("цель") Player target) {
        playerManager.heal(target);
        player.sendMessage("Ты исцелил игрока " + target.getName());
    }
}
```

#### Команда с автодополнением

```java
@Command(name = "teleport", description = "Телепортация", aliases = {"tp"})
@Permission("yourplugin.teleport")
public class TeleportCommand {
    
    @Inject
    private LocationManager locationManager;
    
    @Subcommand("to")
    public void teleportTo(Player player, @Argument("игрок") Player target) {
        player.teleport(target.getLocation());
        player.sendMessage("Телепортация к " + target.getName());
    }
    
    @Subcommand("location")
    public void teleportLocation(Player player, @Argument("локация") String locationName) {
        Location location = locationManager.getLocation(locationName);
        if (location != null) {
            player.teleport(location);
            player.sendMessage("Телепортация в " + locationName);
        } else {
            player.sendMessage("Локация не найдена");
        }
    }
    
    // Автодополнение для аргумента locationName
    @TabComplete("location")
    public List<String> completeLocationNames(Player player, String[] args) {
        return locationManager.getLocationNames().stream()
                .filter(name -> name.startsWith(args[0]))
                .collect(Collectors.toList());
    }
}
```

#### Команда с кулдауном

```java
@Command(name = "kit", description = "Выдает набор предметов")
@Permission("yourplugin.kit")
public class KitCommand {
    
    @Inject
    private KitManager kitManager;
    
    @Subcommand("starter")
    @Cooldown(value = 86400, message = "Ты сможешь получить этот набор через {time}")
    public void giveStarterKit(Player player) {
        kitManager.giveKit(player, "starter");
        player.sendMessage("Ты получил стартовый набор!");
    }
    
    @Subcommand("vip")
    @Permission("yourplugin.kit.vip")
    @Cooldown(value = 3600, message = "Ты сможешь получить VIP набор через {time}")
    public void giveVipKit(Player player) {
        kitManager.giveKit(player, "vip");
        player.sendMessage("Ты получил VIP набор!");
    }
}
```

#### Регистрация команд

Команды регистрируются автоматически при запуске плагина. Вам нужно только указать пакеты для сканирования:

```java
public class YourPlugin extends JavaPlugin {
    
    @Override
    public void onEnable() {
        FCore.getInstance().getCommandManager().registerCommands(this, "your.plugin.commands");
    }
}
```

## Система событий

Система событий FCore упрощает работу с событиями Bukkit и позволяет создавать собственные события для вашего плагина.

### Основные возможности

- **АвтоРегистрация обработчиков**: автоматическая регистрация слушателей событий
- **Кастомные события**: фреймворк для создания собственных событий
- **Приоритизация**: гибкая система приоритетов
- **Фильтрация событий**: возможность фильтровать события по различным критериям
- **Асинхронная обработка**: API для асинхронной обработки событий

### Аннотации

- **@EventHandler** - стандартная аннотация Bukkit для обработчиков событий
- **@Priority** - задает приоритет обработчика (перекрывает priority в @EventHandler)
- **@IgnoreCancelled** - указывает, должен ли обработчик игнорировать отмененные события
- **@AsyncEvent** - помечает событие как асинхронное

### Примеры использования

#### Стандартный слушатель событий

```java
@Service
public class PlayerEventListener implements Listener {
    
    @Inject
    private PlayerManager playerManager;
    
    @EventHandler
    @Priority(EventPriority.HIGH)
    @IgnoreCancelled(true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        playerManager.loadPlayerData(player);
        
        // Изменение сообщения о входе
        event.setJoinMessage(ChatColor.GREEN + "Игрок " + player.getName() + " присоединился к игре!");
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        playerManager.savePlayerData(player);
    }
}
```

#### Создание кастомного события

```java
// Определение события
public class PlayerDataLoadEvent extends Event {
    
    private static final HandlerList HANDLERS = new HandlerList();
    
    private final Player player;
    private final PlayerData data;
    private boolean success;
    
    public PlayerDataLoadEvent(Player player, PlayerData data, boolean success) {
        this.player = player;
        this.data = data;
        this.success = success;
    }
    
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
    
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
    
    // Геттеры и сеттеры
    public Player getPlayer() {
        return player;
    }
    
    public PlayerData getData() {
        return data;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
}

// Вызов события
PlayerData data = loadDataFromDb(player);
PlayerDataLoadEvent event = new PlayerDataLoadEvent(player, data, data != null);
Bukkit.getPluginManager().callEvent(event);
if (event.isSuccess()) {
    // Обработка успешной загрузки
}
```

#### Асинхронная обработка событий

```java
@Service
public class AsyncEventProcessor {
    
    @Inject
    private TaskScheduler scheduler;
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Асинхронная загрузка данных игрока
        scheduler.runAsync(() -> {
            // Загрузка данных из БД
            PlayerData data = loadDataFromDatabase(player.getUniqueId());
            
            // Возврат в основной поток
            scheduler.runSync(() -> {
                // Обновление данных игрока
                applyPlayerData(player, data);
            });
        });
    }
}
```

#### Регистрация слушателей

Слушатели событий регистрируются автоматически при запуске плагина. Вам нужно только указать пакеты для сканирования:

```java
public class YourPlugin extends JavaPlugin {
    
    @Override
    public void onEnable() {
        FCore.getInstance().getEventManager().registerListeners(this, "your.plugin.listeners");
    }
}
```

## GUI система

Система GUI в FCore предоставляет удобный способ создания меню и интерфейсов для игроков, с поддержкой интерактивных элементов и пагинации.

### Основные возможности

- **Билдеры для меню** - Fluent API для создания GUI
- **Шаблоны интерфейсов** - готовые шаблоны для типовых меню
- **Интерактивные элементы** - кнопки, переключатели, слайдеры
- **Пагинация** - многостраничные интерфейсы, скроллинг
- **Анимации** - поддержка анимированных GUI
- **Сохранение состояния** - персистентность между перезагрузками
- **Обработка событий** - удобные хэндлеры нажатий

### Типы меню

- **Обычное меню** - базовое меню с кнопками
- **Пагинированное меню** - меню с поддержкой страниц
- **Прокручиваемое меню** - меню с возможностью скроллинга
- **Динамическое меню** - меню с обновляющимися элементами

### Примеры использования

#### Создание простого меню

```java
@Service
public class ShopService {
    
    @Inject
    private GuiFactory guiFactory;
    
    @Inject
    private ItemService itemService;
    
    public void openShopMenu(Player player) {
        Gui shopMenu = guiFactory.create("Магазин", 3) // 3 ряда
            .item(11, ItemBuilder.of(Material.DIAMOND_SWORD)
                .name("&bМечи")
                .lore("&7Нажми чтобы открыть")
                .build(),
                click -> openSwordsShop(player))
            .item(13, ItemBuilder.of(Material.DIAMOND_CHESTPLATE)
                .name("&aБроня")
                .lore("&7Нажми чтобы открыть")
                .build(),
                click -> openArmorShop(player))
            .item(15, ItemBuilder.of(Material.POTION)
                .name("&dЗелья")
                .lore("&7Нажми чтобы открыть")
                .build(),
                click -> openPotionShop(player))
            .border(ItemBuilder.of(Material.BLACK_STAINED_GLASS_PANE)
                .name(" ")
                .build())
            .build();
        
        shopMenu.open(player);
    }
    
    private void openSwordsShop(Player player) {
        // Реализация магазина мечей
    }
    
    private void openArmorShop(Player player) {
        // Реализация магазина брони
    }
    
    private void openPotionShop(Player player) {
        // Реализация магазина зелий
    }
}
```

#### Создание пагинированного меню

```java
public void openItemsList(Player player, List<ItemStack> items) {
    PaginatedGui itemsMenu = guiFactory.createPaginated("Предметы", 6); // 6 рядов
    
    // Добавляем элементы на страницы
    int slot = 0;
    for (ItemStack item : items) {
        itemsMenu.item(slot++, item, click -> {
            player.getInventory().addItem(item.clone());
            player.sendMessage("Вы получили предмет!");
        });
    }
    
    // Добавляем кнопки навигации
    itemsMenu.previousPage(45, ItemBuilder.of(Material.ARROW)
        .name("&aПредыдущая страница")
        .build());
    
    itemsMenu.nextPage(53, ItemBuilder.of(Material.ARROW)
        .name("&aСледующая страница")
        .build());
    
    // Добавляем кнопку закрытия
    itemsMenu.item(49, ItemBuilder.of(Material.BARRIER)
        .name("&cЗакрыть")
        .build(),
        click -> player.closeInventory());
    
    itemsMenu.open(player);
}
```

#### Создание динамического меню

```java
public void openServerStatsMenu(Player player) {
    DynamicGui statsMenu = guiFactory.createDynamic("Статистика сервера", 3);
    
    // Обновление каждую секунду
    statsMenu.setUpdateInterval(20);
    
    // Динамические элементы
    statsMenu.dynamicItem(11, () -> {
        int online = Bukkit.getOnlinePlayers().size();
        return ItemBuilder.of(Material.PLAYER_HEAD)
            .name("&aИгроки онлайн")
            .lore("&7Сейчас на сервере: &f" + online)
            .build();
    });
    
    statsMenu.dynamicItem(13, () -> {
        double tps = getServerTPS();
        return ItemBuilder.of(Material.CLOCK)
            .name("&aПроизводительность")
            .lore(
                "&7TPS: &f" + String.format("%.2f", tps),
                "&7Память: &f" + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576 + "MB"
            )
            .build();
    });
    
    statsMenu.dynamicItem(15, () -> {
        return ItemBuilder.of(Material.SUNFLOWER)
            .name("&aЭкономика")
            .lore("&7Баланс: &f" + getEconomyBalance(player))
            .build();
    });
    
    statsMenu.open(player);
}
```

#### Создание кастомного шаблона

```java
public class ConfirmationMenu {
    
    private final GuiFactory guiFactory;
    
    @Inject
    public ConfirmationMenu(GuiFactory guiFactory) {
        this.guiFactory = guiFactory;
    }
    
    public void open(Player player, String title, Runnable onConfirm, Runnable onCancel) {
        Gui menu = guiFactory.create(title, 3)
            .item(11, ItemBuilder.of(Material.LIME_WOOL)
                .name("&aПодтвердить")
                .lore("&7Нажмите, чтобы подтвердить")
                .build(),
                click -> {
                    player.closeInventory();
                    onConfirm.run();
                })
            .item(15, ItemBuilder.of(Material.RED_WOOL)
                .name("&cОтменить")
                .lore("&7Нажмите, чтобы отменить")
                .build(),
                click -> {
                    player.closeInventory();
                    onCancel.run();
                })
            .border(ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE)
                .name(" ")
                .build())
            .build();
        
        menu.open(player);
    }
}
```

## Управление данными

Система управления данными в FCore обеспечивает удобную работу с конфигурациями, базами данных и игровыми данными.

### Основные возможности

- **ORM для MySQL/SQLite** - упрощенная работа с базами данных
- **Кэширование** - многоуровневая система кэша
- **YAML/JSON конфиги** - автоматическая сериализация/десериализация
- **Миграции** - система миграций для обновления структуры БД
- **Валидация данных** - встроенная валидация при сохранении
- **Автоконфиг** - автоматическая загрузка конфигов через аннотации

### Аннотации для конфигураций

- **@ConfigFile** - указывает файл конфигурации для класса
- **@ConfigValue** - связывает поле с значением из конфигурации
- **@ConfigSection** - связывает поле с секцией конфигурации
- **@Validate** - добавляет валидацию значения

### Аннотации для БД

- **@Entity** - помечает класс как сущность БД
- **@Id** - указывает первичный ключ
- **@Column** - связывает поле с колонкой в таблице
- **@Transient** - исключает поле из сохранения
- **@Index** - создает индекс для колонки
- **@ForeignKey** - создает внешний ключ

### Примеры использования

#### Работа с конфигурациями

```java
@ConfigFile("config.yml")
public class MainConfig {
    
    @ConfigValue("server.name")
    private String serverName = "Default Server";
    
    @ConfigValue("server.max-players")
    private int maxPlayers = 100;
    
    @ConfigValue("server.motd")
    private String motd = "Welcome to the server!";
    
    @ConfigSection("economy")
    private EconomyConfig economyConfig;
    
    @ConfigValue("debug-mode")
    private boolean debugMode = false;
    
    // Внутренний класс для секции
    public static class EconomyConfig {
        @ConfigValue("starting-balance")
        private double startingBalance = 100.0;
        
        @ConfigValue("currency-symbol")
        private String currencySymbol = "$";
        
        @ConfigValue("maximum-balance")
        private double maxBalance = 1000000.0;
        
        // Геттеры и сеттеры
    }
    
    // Геттеры и сеттеры
    public String getServerName() {
        return serverName;
    }
    
    public int getMaxPlayers() {
        return maxPlayers;
    }
    
    public String getMotd() {
        return motd;
    }
    
    public EconomyConfig getEconomyConfig() {
        return economyConfig;
    }
    
    public boolean isDebugMode() {
        return debugMode;
    }
}

// Использование
@Inject
private ConfigManager configManager;

public void onEnable() {
    MainConfig config = configManager.load(MainConfig.class);
    getLogger().info("Сервер: " + config.getServerName());
}
```

#### Работа с базой данных

```java
@Entity("players")
public class PlayerData {
    
    @Id
    private UUID playerId;
    
    @Column("name")
    private String name;
    
    @Column("balance")
    @Validate(min = "0", max = "1000000")
    private double balance;
    
    @Column("last_login")
    private long lastLogin;
    
    @Column("rank")
    private String rank = "default";
    
    @Transient
    private transient Player bukkitPlayer;
    
    // Конструкторы, геттеры и сеттеры
    public PlayerData() {}
    
    public PlayerData(UUID playerId, String name) {
        this.playerId = playerId;
        this.name = name;
        this.lastLogin = System.currentTimeMillis();
    }
    
    // Геттеры и сеттеры
}

// Сервис для работы с данными игроков
@Service
public class PlayerDataService {
    
    @Inject
    private Database database;
    
    @Inject
    private Cache<UUID, PlayerData> playerCache;
    
    public PlayerData getPlayerData(UUID playerId) {
        return playerCache.computeIfAbsent(playerId, id -> 
            database.find(PlayerData.class, id));
    }
    
    public PlayerData getPlayerData(Player player) {
        return getPlayerData(player.getUniqueId());
    }
    
    public void savePlayerData(PlayerData data) {
        database.save(data);
        playerCache.put(data.getPlayerId(), data);
    }
    
    public List<PlayerData> getTopPlayers(int limit) {
        return database.createQuery(PlayerData.class)
            .orderBy("balance DESC")
            .limit(limit)
            .list();
    }
    
    public void createPlayerData(Player player) {
        PlayerData data = new PlayerData(player.getUniqueId(), player.getName());
        savePlayerData(data);
    }
}
```

#### Миграции базы данных

```java
@Migration(version = 1)
public class InitialMigration implements DatabaseMigration {
    
    @Override
    public void up(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Создание таблицы игроков
            stmt.execute(
                "CREATE TABLE players (" +
                "   player_id VARCHAR(36) PRIMARY KEY," +
                "   name VARCHAR(16) NOT NULL," +
                "   balance DOUBLE NOT NULL DEFAULT 0," +
                "   last_login BIGINT NOT NULL," +
                "   rank VARCHAR(16) NOT NULL DEFAULT 'default'" +
                ")"
            );
            
            // Создание индекса
            stmt.execute("CREATE INDEX idx_player_name ON players(name)");
        }
    }
    
    @Override
    public void down(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS players");
        }
    }
}

@Migration(version = 2)
public class AddPermissionsTable implements DatabaseMigration {
    
    @Override
    public void up(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(
                "CREATE TABLE permissions (" +
                "   id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "   player_id VARCHAR(36) NOT NULL," +
                "   permission VARCHAR(255) NOT NULL," +
                "   FOREIGN KEY (player_id) REFERENCES players(player_id)" +
                ")"
            );
        }
    }
    
    @Override
    public void down(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS permissions");
        }
    }
}
```

## Action-система

Action-система (Визуальный движок действий) в FCore позволяет создавать сложные последовательности действий без написания кода, используя простой и понятный API.

### Основные возможности

- **Fluent API** - цепочки действий для создания сложных сценариев
- **Скриптовый движок** - исполнение сценариев без перекомпиляции
- **Шаблоны действий** - библиотека готовых блоков
- **Редактор сценариев** - GUI для создания цепочек действий
- **Триггеры** - система активации сценариев по событиям
- **Параллельное выполнение** - запуск нескольких цепочек одновременно

### Типы действий

- **Сообщения и звуки** - отправка сообщений, звуков, заголовков
- **Эффекты** - визуальные эффекты, партиклы, анимации
- **Управление сущностями** - создание, удаление, модификация
- **Управление игроками** - телепортация, выдача предметов, изменение атрибутов
- **Таймеры и задержки** - выполнение действий через интервалы
- **Условия и ветвления** - выполнение действий при определенных условиях
- **Циклы** - повторение действий
- **Метаданные** - сохранение и чтение временных данных

### Примеры использования

#### Простая последовательность действий

```java
@Service
public class PlayerWelcomer {
    
    @Inject
    private ActionFactory actionFactory;
    
    public void welcomePlayer(Player player) {
        actionFactory.createSequence()
            .wait(20)                                       // Ждать 1 секунду (20 тиков)
            .message("&aДобро пожаловать на сервер!")       // Отправить сообщение
            .sound(Sound.ENTITY_PLAYER_LEVELUP, 1, 1)       // Проиграть звук
            .title("&bСервер", "&7Добро пожаловать", 20)    // Показать заголовок
            .run(() -> giveStarterKit(player))              // Выполнить произвольный код
            .start(player);                                 // Запустить для игрока
    }
    
    private void giveStarterKit(Player player) {
        // Выдача стартового набора
    }
}
```

#### Сложный сценарий с условиями

```java
public void processRewards(Player player) {
    actionFactory.createSequence()
        .message("&7Проверка наград...")
        .condition(player::hasPermission, "vip", then -> then
            .message("&6VIP &aбонус активирован!")
            .give(Material.DIAMOND, 5)
            .effect(player.getLocation(), Effect.FIREWORKS_SPARK),
    otherwise -> otherwise
        .message("&7Купите &6VIP &7для бонусов")
        .actionBar("&eПодробнее на сайте"))
        .repeat(5, seq -> seq
            .particle(Particle.FLAME, player.getLocation())
            .wait(10))
        .start(player);
}
```

#### Использование триггеров

```java
// Регистрация триггера для команды
actionFactory.createTrigger()
    .onCommand("kit daily")
    .withPermission("kit.daily")
    .withCooldown(86400, "Вы сможете получить этот набор через {time}")
    .action(player -> {
        actionFactory.createSequence()
            .message("&aВы получили ежедневный набор!")
            .give(Material.IRON_SWORD, 1)
            .give(Material.BREAD, 16)
            .give(Material.IRON_INGOT, 5)
            .start(player);
    })
    .register();

// Регистрация триггера для региона
actionFactory.createTrigger()
    .onRegionEnter("spawn")
    .action(player -> {
        actionFactory.createSequence()
            .title("&bЗащищенная зона", "&7PvP отключен", 20)
            .start(player);
    })
    .register();
```

#### Создание квеста

```java
public void startQuest(Player player) {
    actionFactory.createSequence()
        .metadata("quest_step", 1)                           // Сохраняем прогресс квеста
        .message("&6Начало квеста: &aПоиски артефакта")
        .message("&7Найдите древний артефакт в подземелье")
        .title("&6Новый квест", "&aПоиски артефакта", 40)
        .waypoint("dungeon_entrance", "Вход в подземелье")   // Добавляем точку на карте
        
        // Условие для продолжения квеста
        .waitFor(player -> player.getLocation().distance(getDungeonLocation()) < 10)
        .metadata("quest_step", 2)
        .message("&7Вы нашли вход в подземелье!")
        .message("&7Найдите артефакт внутри")
        
        .condition(p -> hasArtifact(p), then -> then         // Проверяем, нашел ли игрок артефакт
            .message("&aВы нашли артефакт!")
            .metadata("quest_step", 3)
            .give(Material.DIAMOND, 10)
            .experience(500)
            .finish(),
        otherwise -> otherwise
            .message("&cВы не нашли артефакт")
            .wait(100)
            .message("&7Попробуйте поискать лучше...")
            .restart())                                       // Рестарт этого блока
        
        .start(player);
}
```

## Система прав

Система прав в FCore предоставляет гибкий и удобный способ управления разрешениями в вашем плагине через Fluent API (Permissions DSL).

### Основные возможности

- **Fluent API для прав** - удобный декларативный стиль настройки
- **Группы прав** - создание и управление группами доступа
- **Наследование** - гибкая система наследования прав
- **Временные права** - права с ограничением по времени
- **Контекстные права** - права, зависящие от условий
- **Интеграция с плагинами** - работа с популярными плагинами прав

### Примеры использования

#### Основное API прав

```java
@Service
public class PermissionSetup {
    
    @Inject
    private PermissionManager permissionManager;
    
    @PostConstruct
    public void setupPermissions() {
        permissionManager.setup()
            // Админская группа
            .group("admin")
                .add("yourplugin.admin.*")
                .addChildren("yourplugin.command.*")
                .permission("yourplugin.debug", true)
            
            // VIP группа
            .group("vip")
                .add("yourplugin.vip.*")
                .permission("yourplugin.command.fly", true)
                .permission("yourplugin.command.heal", true)
                
            // Игрок (по умолчанию)  
            .group("default")
                .permission("yourplugin.command.help", true)
                .permission("yourplugin.command.spawn", true)
            
            .register();
    }
}
```

#### Контекстные права

```java
// Создаем контекст для проверки прав
PermissionContext context = PermissionContext.create()
    .world(player.getWorld())                       // Мир
    .region("spawn")                                // Регион
    .time(server.getTime())                         // Время на сервере
    .gameMode(player.getGameMode())                 // Режим игры
    .condition("in_combat", isPlayerInCombat(player)) // Кастомное условие
    .build();

// Проверяем право с контекстом
boolean canUse = permissionManager.check(player, "yourplugin.command.pvp", context);

// Добавляем временное право
permissionManager.addTemporary(player, "yourplugin.fly", 600); // На 10 минут
```

#### Интеграция с плагинами прав

```java
// Использование с LuckPerms
permissionManager.setPermissionPlugin(new LuckPermsAdapter());

// Добавление прав через Permissions DSL
permissionManager.setup()
    .group("vip")
        .permission("luckperms.user.promote", true)
        .permission("yourplugin.kits.vip", true, context -> 
            context.getRegion().equals("spawn")) // Только в регионе spawn
    .register();
```

#### Проверка прав в команде

```java
@Command(name = "admin", description = "Админ-команды")
@Permission("yourplugin.admin")
public class AdminCommand {
    
    @Inject
    private PermissionManager permissionManager;
    
    @Subcommand("reload")
    public void reload(Player player) {
        // Дополнительная проверка с контекстом
        if (!permissionManager.check(player, "yourplugin.admin.reload", 
                PermissionContext.create().gameMode(GameMode.CREATIVE).build())) {
            player.sendMessage("Эту команду можно использовать только в креативе!");
            return;
        }
        
        // Выполнение команды
        player.sendMessage("Плагин перезагружен!");
    }
}
```

## Утилиты

FCore включает множество утилитных классов для упрощения разработки плагинов.

### Доступные утилиты

#### ItemBuilder - создание предметов

```java
ItemStack sword = ItemBuilder.of(Material.DIAMOND_SWORD)
    .name("&c&lОгненный меч")
    .lore(
        "&7Урон: &c+10",
        "&7Огонь: &c+5",
        "",
        "&eПКМ&7: Выпустить огненный шар"
    )
    .enchant(Enchantment.DAMAGE_ALL, 5)
    .enchant(Enchantment.FIRE_ASPECT, 2)
    .flag(ItemFlag.HIDE_ATTRIBUTES)
    .durability(10)
    .unbreakable(true)
    .data("fire_sword", "true")
    .build();
```

#### TextUtils - форматирование текста

```java
// Цветной текст
String coloredText = TextUtils.colorize("&aПривет, &b{player}!", "player", player.getName());

// Градиент
String gradient = TextUtils.gradient("Супер текст", TextColor.fromRGB(255, 0, 0), TextColor.fromRGB(0, 0, 255));

// Центрирование
String centered = TextUtils.center("Заголовок");

// Замена плейсхолдеров
String replaced = TextUtils.replace("Баланс: {balance}", Map.of("balance", economy.getBalance(player) + "$"));

// Progress bar
String bar = TextUtils.progressBar(player.getHealth(), player.getMaxHealth(), 20, '|', ChatColor.GREEN, ChatColor.RED);
```

#### LocationUtils - работа с локациями

```java
// Сериализация локации
String serialized = LocationUtils.serialize(player.getLocation());

// Десериализация
Location location = LocationUtils.deserialize(serialized);

// Создание кубоида между двумя точками
Cuboid cuboid = LocationUtils.createCuboid(loc1, loc2);

// Проверка, находится ли локация в кубоиде
boolean inside = cuboid.contains(player.getLocation());

// Случайная локация в радиусе
Location random = LocationUtils.getRandomLocationInRadius(center, 10);

// Безопасная телепортация
LocationUtils.teleportSafely(player, destination);
```

#### MathUtils - математические утилиты

```java
// Получение случайного числа в диапазоне
int random = MathUtils.random(1, 100);

// Шанс (вероятность)
boolean success = MathUtils.chance(25); // 25% шанс

// Округление до N знаков
double rounded = MathUtils.round(3.14159, 2); // 3.14

// Проверка, находится ли число в диапазоне
boolean inRange = MathUtils.isInRange(value, min, max);

// Перевод градусов в радианы
double radians = MathUtils.degreesToRadians(90);
```

#### EffectUtils - эффекты и партиклы

```java
// Создание эффекта частиц в форме круга
EffectUtils.createCircle(player.getLocation(), 5, Particle.FLAME);

// Создание эффекта в форме линии между двумя точками
EffectUtils.createLine(start, end, Particle.REDSTONE, 0.5);

// Спиральный эффект
EffectUtils.createSpiral(player.getLocation(), 3, 10, Particle.PORTAL);

// Случайные фейерверки
EffectUtils.spawnRandomFireworks(player.getLocation(), 3);
```

#### ConfigUtils - работа с файлами конфигурации

```java
// Загрузка конфигурации
YamlConfiguration config = ConfigUtils.loadConfig(plugin, "settings.yml");

// Сохранение конфигурации
ConfigUtils.saveConfig(plugin, config, "settings.yml");

// Безопасное получение значения с проверкой типа
int value = ConfigUtils.getOrDefault(config, "path.to.value", 10);

// Проверка наличия всех требуемых параметров
List<String> missing = ConfigUtils.validateRequiredPaths(config, "database.host", "database.port");
```

#### AsyncUtils - асинхронная обработка

```java
// Выполнение задачи асинхронно
AsyncUtils.runAsync(plugin, () -> {
    // Долгая операция (например, запрос к БД)
    PlayerData data = loadPlayerDataFromDatabase(player.getUniqueId());
    
    // Возвращаемся в основной поток
    AsyncUtils.runSync(plugin, () -> {
        // Обновляем игрока с полученными данными
        applyPlayerData(player, data);
    });
});

// Выполнение с задержкой
AsyncUtils.runLater(plugin, () -> {
    player.sendMessage("Сообщение с задержкой");
}, 60); // 3 секунды (60 тиков)
```

## Интеграции

FCore предоставляет готовые интеграции с популярными плагинами для сервера Minecraft.

### Поддерживаемые плагины

- **Vault** - экономика, пермишены, чат
- **PlaceholderAPI** - плейсхолдеры для текста
- **WorldGuard** - регионы и защита
- **LuckPerms** - система прав
- **ProtocolLib** - низкоуровневая работа с пакетами
- **Citizens** - NPC

### Примеры использования

#### Интеграция с Vault (экономика)

```java
@Service
public class EconomyService {
    
    @Inject
    private VaultIntegration vaultIntegration;
    
    public double getBalance(Player player) {
        if (!vaultIntegration.isEconomyEnabled()) {
            return 0.0;
        }
        
        return vaultIntegration.getEconomy().getBalance(player);
    }
    
    public boolean withdraw(Player player, double amount) {
        if (!vaultIntegration.isEconomyEnabled()) {
            return false;
        }
        
        Economy economy = vaultIntegration.getEconomy();
        if (economy.has(player, amount)) {
            economy.withdrawPlayer(player, amount);
            return true;
        }
        
        return false;
    }
    
    public void deposit(Player player, double amount) {
        if (vaultIntegration.isEconomyEnabled()) {
            vaultIntegration.getEconomy().depositPlayer(player, amount);
        }
    }
}
```

#### Интеграция с PlaceholderAPI

```java
@Service
public class ServerPlaceholders extends PlaceholderExpansion {
    
    @Inject
    private PlayerDataService playerDataService;
    
    @Override
    public String getIdentifier() {
        return "yourplugin";
    }
    
    @Override
    public String getAuthor() {
        return "YourName";
    }
    
    @Override
    public String getVersion() {
        return "1.0";
    }
    
    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) {
            return "";
        }
        
        if (identifier.equals("rank")) {
            PlayerData data = playerDataService.getPlayerData(player);
            return data.getRank();
        }
        
        if (identifier.equals("playtime")) {
            PlayerData data = playerDataService.getPlayerData(player);
            long minutes = data.getPlaytime() / 60;
            return minutes + " мин";
        }
        
        return null;
    }
}
```

#### Интеграция с WorldGuard

```java
@Service
public class RegionService {
    
    @Inject
    private WorldGuardIntegration worldGuard;
    
    public boolean isInRegion(Player player, String regionName) {
        if (!worldGuard.isEnabled()) {
            return false;
        }
        
        return worldGuard.isPlayerInRegion(player, regionName);
    }
    
    public Set<String> getRegionsAt(Location location) {
        if (!worldGuard.isEnabled()) {
            return Collections.emptySet();
        }
        
        return worldGuard.getRegionsAt(location);
    }
    
    public boolean canBuild(Player player, Location location) {
        if (!worldGuard.isEnabled()) {
            return true;
        }
        
        return worldGuard.canBuild(player, location);
    }
}
```

## Режим разработчика

Режим разработчика в FCore предоставляет инструменты для отладки и мониторинга плагинов.

### Основные возможности

- **Дебаг-команды** - встроенные команды для отладки
- **Расширенное логирование** - подробные логи для диагностики
- **Визуализация данных** - отображение отладочной информации в игре
- **Мониторинг производительности** - отслеживание нагрузки от плагинов
- **Инспектор объектов** - просмотр состояния игровых объектов

### Примеры использования

#### Включение режима отладки

```java
@Command(name = "debug", description = "Управление режимом отладки")
@Permission("yourplugin.debug")
public class DebugCommand {
    
    @Inject
    private DebugManager debugManager;
    
    @Subcommand("toggle")
    public void toggleDebug(Player player) {
        boolean enabled = debugManager.toggleDebug(player);
        player.sendMessage("Режим отладки: " + (enabled ? "включен" : "выключен"));
    }
    
    @Subcommand("info")
    public void showInfo(Player player) {
        debugManager.showDebugInfo(player);
    }
    
    @Subcommand("reload")
    public void reloadPlugin(Player player) {
        player.sendMessage("Перезагрузка плагина...");
        debugManager.reload();
        player.sendMessage("Плагин перезагружен!");
    }
}
```

#### Вывод отладочной информации

```java
@Service
public class EntityTracker {
    
    @Inject
    private DebugManager debugManager;
    
    @Inject
    private TaskScheduler scheduler;
    
    @PostConstruct
    public void init() {
        // Запускаем задачу отслеживания сущностей
        scheduler.runRepeating(() -> trackEntities(), 100, 100);
    }
    
    private void trackEntities() {
        for (World world : Bukkit.getWorlds()) {
            int entities = world.getEntities().size();
            int chunks = world.getLoadedChunks().length;
            
            debugManager.log("Мир " + world.getName() + ": " + entities + " сущностей, " + chunks + " чанков");
            
            // Вывод информации для игроков в режиме отладки
            for (Player player : debugManager.getDebuggingPlayers()) {
                if (player.getWorld().equals(world)) {
                    debugManager.sendActionBar(player, 
                        "Сущности: " + entities + " | Чанки: " + chunks);
                }
            }
        }
    }
}
```

#### Мониторинг производительности

```java
@Service
public class PerformanceMonitor {
    
    @Inject
    private DebugManager debugManager;
    
    private final Map<String, Long> timings = new HashMap<>();
    
    public void startTiming(String key) {
        timings.put(key, System.nanoTime());
    }
    
    public long stopTiming(String key) {
        if (!timings.containsKey(key)) {
            return -1;
        }
        
        long start = timings.remove(key);
        long time = System.nanoTime() - start;
        
        // Логируем, если время выполнения слишком большое
        if (time > 5_000_000) { // 5 мс
            debugManager.log("Предупреждение: операция " + key + " заняла " + (time / 1_000_000.0) + " мс");
        }
        
        return time;
    }
    
    public void displayStats(Player player) {
        // Сбор статистики сервера
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1048576;
        
        List<String> stats = new ArrayList<>();
        stats.add("§aСтатистика сервера:");
        stats.add("§7Использовано памяти: §f" + usedMemory + " MB");
        stats.add("§7TPS: §f" + getTPS());
        stats.add("§7Игроков онлайн: §f" + Bukkit.getOnlinePlayers().size());
        stats.add("§7Загружено чанков: §f" + getLoadedChunksCount());
        stats.add("§7Сущностей: §f" + getEntitiesCount());
        
        for (String line : stats) {
            player.sendMessage(line);
        }
    }
    
    private double getTPS() {
        // Реализация получения TPS
        return 20.0;
    }
    
    private int getLoadedChunksCount() {
        // Реализация подсчета загруженных чанков
        return 0;
    }
    
    private int getEntitiesCount() {
        // Реализация подсчета сущностей
        return 0;
    }
}
```

#### Инспектор объектов

```java
@Command(name = "inspect", description = "Инспектор объектов")
@Permission("yourplugin.debug.inspect")
public class InspectCommand {
    
    @Inject
    private DebugManager debugManager;
    
    @Subcommand("entity")
    public void inspectEntity(Player player) {
        debugManager.startEntityInspection(player);
        player.sendMessage("Нажмите ПКМ на сущность для проверки");
    }
    
    @Subcommand("block")
    public void inspectBlock(Player player) {
        debugManager.startBlockInspection(player);
        player.sendMessage("Нажмите ПКМ на блок для проверки");
    }
    
    @EventHandler
    public void onInteract(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        
        if (debugManager.isInspecting(player, InspectType.ENTITY)) {
            Entity entity = event.getRightClicked();
            
            List<String> info = new ArrayList<>();
            info.add("§aИнформация о сущности:");
            info.add("§7Тип: §f" + entity.getType());
            info.add("§7UUID: §f" + entity.getUniqueId());
            info.add("§7Координаты: §f" + entity.getLocation().toVector());
            
            if (entity instanceof LivingEntity) {
                LivingEntity living = (LivingEntity) entity;
                info.add("§7Здоровье: §f" + living.getHealth() + "/" + living.getMaxHealth());
                info.add("§7AI: §f" + (living.hasAI() ? "Да" : "Нет"));
            }
            
            for (String line : info) {
                player.sendMessage(line);
            }
            
            event.setCancelled(true);
        }
    }
}
```

## Security API

Система безопасности FCore обеспечивает защиту плагинов от кражи кода и несанкционированного использования.

### Основные возможности

- **Лицензирование** - проверка лицензионных ключей
- **Обфускация** - защита кода от декомпиляции
- **Проверка целостности** - защита от модификации плагинов
- **Цифровая подпись** - проверка подлинности плагинов
- **Анти-дамп** - защита от извлечения кода из памяти
- **Шифрование конфигов** - защита конфиденциальных данных

### Примеры использования

#### Проверка лицензии

```java
@Service
public class LicenseService {
    
    @Inject
    private SecurityManager securityManager;
    
    @PostConstruct
    public void init() {
        // Проверяем лицензию при запуске
        if (!securityManager.verifyLicense()) {
            Bukkit.getPluginManager().disablePlugin(FCorePlugin.getInstance());
            return;
        }
        
        // Запускаем периодическую проверку
        securityManager.startLicenseChecker();
    }
    
    public boolean isLicensed() {
        return securityManager.isLicensed();
    }
    
    public boolean activateLicense(String key) {
        return securityManager.activateLicense(key);
    }
    
    public LicenseInfo getLicenseInfo() {
        return securityManager.getLicenseInfo();
    }
}
```

#### Защита конфигураций

```java
@Service
public class ConfigProtectionService {
    
    @Inject
    private SecurityManager securityManager;
    
    @Inject
    private ConfigManager configManager;
    
    public void saveSecureConfig(String filename, Map<String, Object> data) {
        // Шифруем конфиденциальные данные
        String encrypted = securityManager.encrypt(configManager.serialize(data));
        
        // Сохраняем зашифрованные данные
        try (FileWriter writer = new FileWriter(new File(FCorePlugin.getInstance().getDataFolder(), filename))) {
            writer.write(encrypted);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public Map<String, Object> loadSecureConfig(String filename) {
        try {
            String content = Files.readString(new File(FCorePlugin.getInstance().getDataFolder(), filename).toPath());
            String decrypted = securityManager.decrypt(content);
            return configManager.deserialize(decrypted);
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }
}
```

#### Проверка целостности плагинов

```java
@Service
public class PluginIntegrityService {
    
    @Inject
    private SecurityManager securityManager;
    
    public void verifyPlugins() {
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            // Пропускаем несовместимые плагины
            if (!securityManager.isProtectable(plugin)) {
                continue;
            }
            
            // Проверяем подпись плагина
            if (!securityManager.verifySignature(plugin)) {
                Bukkit.getLogger().warning("Плагин " + plugin.getName() + " имеет недействительную подпись!");
                Bukkit.getPluginManager().disablePlugin(plugin);
                continue;
            }
            
            // Проверяем целостность плагина
            if (!securityManager.verifyIntegrity(plugin)) {
                Bukkit.getLogger().warning("Плагин " + plugin.getName() + " был модифицирован!");
                Bukkit.getPluginManager().disablePlugin(plugin);
            }
        }
    }
    
    public boolean signPlugin(Plugin plugin, String privateKey) {
        return securityManager.signPlugin(plugin, privateKey);
    }
}
```

#### Защита от клонирования сервера

```java
@Service
public class AntiCloneService {
    
    @Inject
    private SecurityManager securityManager;
    
    @PostConstruct
    public void init() {
        // Генерируем уникальный идентификатор сервера при первом запуске
        if (!securityManager.hasServerIdentifier()) {
            securityManager.generateServerIdentifier();
        }
        
        // Проверяем, не клонирован ли сервер
        if (securityManager.isServerCloned()) {
            Bukkit.getLogger().severe("Обнаружена попытка запуска клонированного сервера!");
            Bukkit.shutdown();
        }
    }
    
    public void bindLicenseToServer() {
        securityManager.bindLicenseToServerHardware();
    }
    
    public boolean validateServerBinding() {
        return securityManager.validateServerBinding();
    }
}
```

## Примеры использования

### Создание плагина на основе FCore

Полное руководство по созданию плагина с использованием всех возможностей FCore.

#### Структура проекта

```
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── example/
│       │           └── myplugin/
│       │               ├── MyPlugin.java
│       │               ├── commands/
│       │               │   ├── AdminCommand.java
│       │               │   └── ShopCommand.java
│       │               ├── listeners/
│       │               │   ├── PlayerListener.java
│       │               │   └── WorldListener.java
│       │               ├── gui/
│       │               │   ├── MainMenu.java
│       │               │   └── ShopMenu.java
│       │               ├── data/
│       │               │   ├── PlayerData.java
│       │               │   └── ShopItem.java
│       │               └── services/
│       │                   ├── EconomyService.java
│       │                   └── PlayerDataService.java
│       └── resources/
│           ├── plugin.yml
│           ├── config.yml
│           └── messages.yml
└── pom.xml
```

#### Основной класс плагина

```java
package com.example.myplugin;

import dev.flaymie.fcore.FCore;
import dev.flaymie.fcore.api.annotation.Inject;
import org.bukkit.plugin.java.JavaPlugin;

public class MyPlugin extends JavaPlugin {
    
    @Inject
    private PlayerDataService playerDataService;
    
    @Inject
    private EconomyService economyService;
    
    @Override
    public void onEnable() {
        // Получаем экземпляр FCore
        FCore core = FCore.getInstance();
        
        // Внедряем зависимости в основной класс плагина
        core.getDependencyContainer().injectDependencies(this);
        
        // Регистрируем команды
        core.getCommandManager().registerCommands(this, "com.example.myplugin.commands");
        
        // Регистрируем слушатели событий
        core.getEventManager().registerListeners(this, "com.example.myplugin.listeners");
        
        // Настраиваем базы данных
        playerDataService.init();
        
        getLogger().info("MyPlugin успешно загружен!");
    }
    
    @Override
    public void onDisable() {
        // Сохраняем данные при выключении
        playerDataService.saveAll();
        
        getLogger().info("MyPlugin успешно выгружен!");
    }
}
```

#### Сервис для работы с данными игроков

```java
package com.example.myplugin.services;

import dev.flaymie.fcore.api.annotation.Inject;
import dev.flaymie.fcore.api.annotation.PostConstruct;
import dev.flaymie.fcore.api.annotation.Service;
import dev.flaymie.fcore.api.database.Database;
import dev.flaymie.fcore.api.database.Query;
import com.example.myplugin.data.PlayerData;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class PlayerDataService {
    
    @Inject
    private Database database;
    
    private Map<UUID, PlayerData> cache = new HashMap<>();
    
    @PostConstruct
    public void init() {
        // Создаем или обновляем таблицу в БД
        database.migrate(PlayerData.class);
    }
    
    public PlayerData getPlayerData(Player player) {
        return cache.computeIfAbsent(player.getUniqueId(), uuid -> {
            // Ищем данные в БД
            PlayerData data = database.find(PlayerData.class, uuid);
            
            // Если данных нет, создаем новые
            if (data == null) {
                data = new PlayerData(uuid, player.getName());
            }
            
            return data;
        });
    }
    
    public void savePlayerData(Player player) {
        PlayerData data = cache.get(player.getUniqueId());
        if (data != null) {
            database.save(data);
        }
    }
    
    public void saveAll() {
        for (PlayerData data : cache.values()) {
            database.save(data);
        }
    }
    
    public void removeFromCache(UUID playerId) {
        cache.remove(playerId);
    }
    
    public PlayerData getTopPlayer() {
        return database.createQuery(PlayerData.class)
            .orderBy("balance DESC")
            .first();
    }
}
```

#### Команда с подкомандами

```java
package com.example.myplugin.commands;

import dev.flaymie.fcore.api.annotation.Command;
import dev.flaymie.fcore.api.annotation.Inject;
import dev.flaymie.fcore.api.annotation.Permission;
import dev.flaymie.fcore.api.annotation.Subcommand;
import com.example.myplugin.gui.MainMenu;
import com.example.myplugin.services.PlayerDataService;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@Command(name = "myplugin", description = "Основная команда плагина", aliases = {"mp"})
public class MainCommand {
    
    @Inject
    private PlayerDataService playerDataService;
    
    @Inject
    private MainMenu mainMenu;
    
    @Subcommand(value = "menu", aliases = {"gui", "m"})
    public void openMenu(Player player) {
        mainMenu.open(player);
    }
    
    @Subcommand("profile")
    public void showProfile(Player player) {
        player.sendMessage(ChatColor.GREEN + "Ваш профиль:");
        player.sendMessage(ChatColor.GRAY + "Имя: " + ChatColor.WHITE + player.getName());
        player.sendMessage(ChatColor.GRAY + "Баланс: " + ChatColor.GOLD + 
            playerDataService.getPlayerData(player).getBalance());
    }
    
    @Subcommand("reload")
    @Permission("myplugin.admin.reload")
    public void reload(Player player) {
        // Перезагрузка плагина
        player.sendMessage(ChatColor.GREEN + "Плагин перезагружен!");
    }
}
```

#### Создание меню

```java
package com.example.myplugin.gui;

import dev.flaymie.fcore.api.annotation.Inject;
import dev.flaymie.fcore.api.annotation.Service;
import dev.flaymie.fcore.api.gui.Gui;
import dev.flaymie.fcore.api.gui.GuiFactory;
import dev.flaymie.fcore.utils.item.ItemBuilder;
import com.example.myplugin.services.PlayerDataService;
import org.bukkit.Material;
import org.bukkit.entity.Player;

@Service
public class MainMenu {
    
    @Inject
    private GuiFactory guiFactory;
    
    @Inject
    private PlayerDataService playerDataService;
    
    @Inject
    private ShopMenu shopMenu;
    
    public void open(Player player) {
        // Создаем меню
        Gui menu = guiFactory.create("Главное меню", 3)
            .item(11, ItemBuilder.of(Material.EMERALD)
                .name("&aМагазин")
                .lore("&7Нажмите, чтобы открыть магазин")
                .build(),
                click -> shopMenu.open(player))
            .item(13, ItemBuilder.of(Material.PLAYER_HEAD)
                .owner(player.getName())
                .name("&bПрофиль")
                .lore(
                    "&7Имя: &f" + player.getName(),
                    "&7Баланс: &f" + playerDataService.getPlayerData(player).getBalance(),
                    "",
                    "&eНажмите, чтобы просмотреть"
                )
                .build(),
                click -> player.performCommand("myplugin profile"))
            .item(15, ItemBuilder.of(Material.NETHER_STAR)
                .name("&6Настройки")
                .lore("&7Нажмите, чтобы открыть настройки")
                .build(),
                click -> {
                    // Открыть меню настроек
                })
            .border(ItemBuilder.of(Material.BLACK_STAINED_GLASS_PANE)
                .name(" ")
                .build())
            .build();
        
        // Открываем меню игроку
        menu.open(player);
    }
}
```

#### Слушатель событий

```java
package com.example.myplugin.listeners;

import dev.flaymie.fcore.api.annotation.Inject;
import dev.flaymie.fcore.api.annotation.Service;
import com.example.myplugin.services.PlayerDataService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@Service
public class PlayerListener implements Listener {
    
    @Inject
    private PlayerDataService playerDataService;
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Загружаем данные игрока
        playerDataService.getPlayerData(player);
        
        // Изменяем сообщение о входе
        event.setJoinMessage("§a+ §7" + player.getName());
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Сохраняем данные и удаляем из кэша
        playerDataService.savePlayerData(player);
        playerDataService.removeFromCache(player.getUniqueId());
        
        // Изменяем сообщение о выходе
        event.setQuitMessage("§c- §7" + player.getName());
    }
}
```

#### Использование Action-системы для квестов

```java
package com.example.myplugin.quests;

import dev.flaymie.fcore.api.annotation.Inject;
import dev.flaymie.fcore.api.annotation.Service;
import dev.flaymie.fcore.api.action.ActionFactory;
import com.example.myplugin.services.PlayerDataService;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

@Service
public class QuestService {
    
    @Inject
    private ActionFactory actionFactory;
    
    @Inject
    private PlayerDataService playerDataService;
    
    public void startMiningQuest(Player player) {
        // Создаем последовательность действий для квеста
        actionFactory.createSequence()
            .metadata("quest_type", "mining")
            .metadata("quest_step", 1)
            .message("&6Новый квест: &aДобыча руды")
            .message("&7Добудьте 5 железной руды")
            .title("&6Новый квест", "&aДобыча руды", 40)
            .sound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1)
            
            // Ждем, пока игрок добудет руду
            .waitFor(p -> hasMinedOre(p, Material.IRON_ORE, 5))
            
            // Выдаем награду
            .message("&aКвест выполнен!")
            .message("&7Вы получили &a100 монет &7и &aзелье силы")
            .run(() -> {
                // Обновляем данные игрока
                playerDataService.getPlayerData(player).addBalance(100);
                
                // Выдаем награду
                player.getInventory().addItem(ItemBuilder.of(Material.POTION)
                    .name("&aЗелье силы")
                    .lore("&7Награда за квест")
                    .build());
            })
            .sound(Sound.ENTITY_PLAYER_LEVELUP, 1, 1)
            .experience(50)
            
            // Запускаем последовательность для игрока
            .start(player);
    }
    
    private boolean hasMinedOre(Player player, Material material, int amount) {
        // Проверка, добыл ли игрок нужное количество руды
        // Реализация зависит от способа отслеживания
        return false;
    }
}
```

## Заключение

FCore представляет собой мощный и гибкий фреймворк для разработки плагинов Minecraft, объединяющий удобство разработки и защиту от кражи кода. Благодаря модульной архитектуре и богатому набору инструментов, FCore значительно ускоряет процесс создания качественных плагинов.

### Преимущества использования FCore:

1. **Ускорение разработки** - готовые компоненты и утилиты для типичных задач
2. **Улучшение качества** - проверенные и оптимизированные решения
3. **Упрощение поддержки** - единый стандарт и архитектура
4. **Защита интеллектуальной собственности** - система безопасности плагинов
5. **Расширяемость** - модульная структура позволяет добавлять новые компоненты

### Планы развития:

1. **Версия 1.1** - поддержка новых версий Minecraft
2. **Версия 1.2** - расширенный API для разработчиков плагинов
3. **Версия 1.3** - улучшенная система безопасности
4. **Версия 1.4** - визуальный редактор для Action-системы
5. **Версия 2.0** - комплексная среда разработки плагинов

### Как внести вклад

1. Форкните репозиторий на GitHub
2. Внесите свои изменения
3. Отправьте Pull Request с описанием изменений

### Лицензия

FCore лицензирован под [Proprietary License](LICENSE). Все права защищены. 