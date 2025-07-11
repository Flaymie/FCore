# FCore - Ядро для плагинов Minecraft

FCore - это мощный фреймворк для разработки плагинов на Spigot/Paper, предоставляющий продвинутый инструментарий для быстрой и удобной разработки. Ядро включает в себя систему внедрения зависимостей, annotation-based систему команд, билдер для GUI, DSL для прав и многое другое.

## Содержание
- [Установка](#установка)
- [Архитектура](#архитектура)
- [Система зависимостей (DI)](#система-зависимостей-di)
- [Сервисы FCore](#сервисы-fcore)
- [Система команд](#система-команд)
- [Система событий](#система-событий)
- [GUI система](#gui-система)
- [Система прав (Permissions DSL)](#система-прав-permissions-dsl)
- [Система безопасности](#система-безопасности)
- [Система действий и триггеров (Action System)](#система-действий-и-триггеров-action-system)
- [API для плагинов и их загрузка](#api-для-плагинов-и-их-загрузка)
- [Как использовать API](#как-использовать-api)

## Установка

### Требования
- Java 8 или выше
- Сервер Spigot/Paper 1.16.5+
- Maven/Gradle

### Подключение к вашему проекту

#### Maven

```xml
<repositories>
   <repository>
      <id>fcore-repo</id>
      <url>https://raw.githubusercontent.com/Flaymie/FCore/main/repo/</url>
   </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>dev.flaymie</groupId>
        <artifactId>fcore</artifactId>
        <version>1.1</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

### Настройка `plugin.yml`

```yaml
name: YourPlugin
version: 1.0
main: your.package.YourPlugin
api-version: 1.16
depend: [FCore]
```

## Архитектура

Ядро инициализирует набор менеджеров, которые управляют различными аспектами жизненного цикла плагина. Все основные менеджеры доступны через синглтон `FCore.getInstance()`. Ключевой особенностью является автоматическое сканирование и регистрация компонентов (сервисов, команд, слушателей) на основе аннотаций.

- `ServiceManager`: Управляет жизненным циклом сервисов, помеченных аннотацией `@Service`.
- `DependencyContainer`: DI-контейнер для внедрения зависимостей через `@Inject`.
- `CommandManager`: Сканирует и регистрирует классы с аннотацией `@Command`.
- `EventManager`: Управляет событиями и слушателями.
- `GuiManager`: Управляет открытыми GUI.
- `PermissionManager`: Управляет правами и группами.
- `SecurityManager`: Проверяет целостность и подлинность плагинов.

## Система зависимостей (DI)

FCore использует аннотации для управления зависимостями и жизненным циклом объектов.

- **`@Service`**: Помечает класс как сервис. FCore автоматически создаст его экземпляр и зарегистрирует в DI-контейнере.
    - `name` (опционально): Задает кастомное имя для сервиса. По умолчанию используется имя класса.
    - `priority` (опционально): Определяет приоритет инициализации сервиса (чем выше число, тем раньше он будет загружен).
- **`@Inject`** или **`@Autowired`**: Внедряет зависимость в поле класса. FCore найдет нужный сервис в контейнере и подставит его.
- **`@PostConstruct`**: Метод с этой аннотацией будет вызван после создания сервиса и внедрения всех зависимостей. Идеально подходит для инициализации.
- **`@PreDestroy`**: Метод будет вызван перед отключением сервиса. Используется для освобождения ресурсов.

**Пример сервиса:**
```java
@Service(priority = 100) // Этот сервис загрузится одним из первых
public class PlayerDataService {
    
    @Inject
    private DatabaseManager database; // Предположим, что такой сервис существует
    
    @PostConstruct
    public void initialize() {
        System.out.println("PlayerDataService инициализирован!");
        // Логика инициализации, например, создание таблиц в БД
    }

    public PlayerData loadPlayerData(Player player) {
        // Логика загрузки данных
        return null;
    }
    
    @PreDestroy
    public void cleanup() {
        System.out.println("PlayerDataService отключается!");
    }
}
```

## Сервисы FCore

В основе многих менеджеров FCore лежит концепция сервисов.

- **`FCoreService`**: Простой интерфейс, который должны реализовывать все сервисы ядра. Он определяет основные методы жизненного цикла: `onEnable()` и `onDisable()`.
- **`AbstractFCoreService`**: Абстрактный класс, предоставляющий базовую реализацию `FCoreService`. Упрощает создание новых сервисов, так как требует реализации только необходимой логики.

## Система команд

Система команд полностью основана на аннотациях, что делает их создание простым и декларативным.

- **`@Command`**: Помечает класс как обработчик команды.
    - `name`: Основное имя команды.
    - `description` (опционально): Описание, часто используется в сообщениях помощи.
    - `usage` (опционально): Пример использования команды.
    - `aliases` (опционально): Массив альтернативных имен команды.
    - `permission` (опционально): Право, необходимое для выполнения любой подкоманды.

- **`@Subcommand`**: Помечает метод внутри класса как обработчик подкоманды.
    - `value`: Имя подкоманды.
    - `description` (опционально): Описание подкоманды.
    - `usage` (опционально): Пример использования.
    - `aliases` (опционально): Альтернативные имена подкоманды.
    - `permission` (опционально): Право, необходимое для выполнения этой конкретной подкоманды.

- **`@Permission`**: Устанавливает право на класс (команду) или метод (подкоманду).
    - `value`: Строка с названием права (например, `myplugin.kit.use`).
    - `message` (опционально): Сообщение, которое будет отправлено игроку, если у него нет прав.

- **`@Argument`**: Описывает аргумент метода для автоматического парсинга и предоставления в метод.
    - `value`: Имя аргумента (используется в сообщениях об ошибках и автодополнении).
    - `defaultValue` (опционально): Значение по умолчанию. Если указано, аргумент становится необязательным.
    - `required` (опционально, по умолчанию `true`): Указывает, является ли аргумент обязательным. Если `false`, но `defaultValue` не указан, в метод будет передан `null`.

**Пример команды:**
```java
@Command(name = "kit", description = "Управление наборами", aliases = {"kits"})
public class KitCommand {
    
    @Inject
    private KitManager kitManager; // Наш сервис для управления китами

    @Subcommand(value = "give", description = "Выдать набор игроку", usage = "/kit give <игрок> <набор>")
    @Permission(value = "myplugin.kit.give", message = "&cУ вас нет прав на выдачу наборов.")
    public void giveKit(
        Player sender, 
        @Argument("игрок") Player target, 
        @Argument("набор") String kitName
    ) {
        if (kitManager.giveKit(target, kitName)) {
            sender.sendMessage("Набор " + kitName + " выдан игроку " + target.getName());
        } else {
            sender.sendMessage("Не удалось выдать набор.");
        }
    }

    @Subcommand(value = "list", description = "Список доступных наборов")
    public void listKits(
        Player sender,
        @Argument(value = "страница", defaultValue = "1") int page
    ) {
        sender.sendMessage("Доступные наборы на странице " + page + ": " + String.join(", ", kitManager.getAvailableKits()));
    }
}
```
Для регистрации команд необходимо указать FCore пакет для сканирования:
`FCore.getInstance().getCommandManager().registerCommands(this, "com.yourplugin.commands");`

## Система событий

FCore расширяет стандартную систему событий Bukkit, добавляя свои собственные события и сканер слушателей.

- **`FCoreEvent`**: Базовый класс для всех кастомных событий в ядре.
- **`PlayerServiceEvent`**: Событие, связанное с жизненным циклом сервисов для конкретного игрока.
- **`@EventListener`**: Может использоваться для обозначения класса-слушателя, чтобы FCore автоматически его зарегистрировал, если он является `@Service`. Однако стандартная регистрация через `Listener` также полностью поддерживается.
- **`@Priority(EventPriority)`**: Устанавливает приоритет выполнения обработчика. Принимает стандартный Bukkit `EventPriority` (например, `EventPriority.HIGH`).
- **`@IgnoreCancelled`**: Если эта аннотация присутствует, метод-обработчик будет вызван даже в том случае, если событие уже было отменено другим плагином.

**Пример кастомного события и слушателя:**
```java
// 1. Создаем свое событие
public class QuestCompletedEvent extends FCoreEvent {
    private final Player player;
    private final String questId;
    // ... геттеры, конструктор, стандартные методы Event
}

// 2. Создаем слушатель
@Service // Чтобы FCore нашел и зарегистрировал слушатель
public class QuestListener implements Listener {
    
    @Inject
    private EconomyManager economy;
    
    @EventHandler
    @Priority(EventPriority.HIGHEST) // Выполнить почти в самом конце
    @IgnoreCancelled // Выполнить, даже если кто-то отменил событие
    public void onQuestCompleted(QuestCompletedEvent event) {
        Player player = event.getPlayer();
        player.sendMessage("Квест " + event.getQuestId() + " выполнен!");
        economy.addMoney(player, 100);
    }
}

// 3. Вызываем событие
Bukkit.getPluginManager().callEvent(new QuestCompletedEvent(player, "epic_quest"));
```

## GUI система

FCore предоставляет мощный API для создания интерактивных меню (GUI).

- **`GuiFactory`**: Фабрика для создания различных видов GUI.
- **`GuiBuilder`**: Fluent API для конструирования GUI.
- **`PaginatedGui`**: Реализация GUI с поддержкой страниц.
- **`AnimatedGui`**: Реализация GUI с поддержкой анимаций.
- **`ClickHandler`**, **`CloseHandler`**: Интерфейсы для обработки действий в GUI.

**Пример создания простого меню:**
```java
@Service
public class ShopMenu {
    
    @Inject
    private GuiFactory guiFactory;
    
    public void open(Player player) {
        Gui gui = guiFactory.createBuilder("Магазин", 3)
            .item(11, ItemBuilder.of(Material.DIAMOND_SWORD).name("&bМечи").build(), click -> {
                player.sendMessage("Открываем магазин мечей...");
                click.setCancelled(true); // Отменить стандартное действие (взятие предмета)
            })
            .item(15, ItemBuilder.of(Material.BREAD).name("&eЕда").build(), click -> {
                player.sendMessage("Открываем магазин еды...");
                click.setCancelled(true);
            })
            .onClose(close -> {
                System.out.println("Игрок " + close.getPlayer().getName() + " закрыл магазин.");
            })
            .build();
        
        gui.open(player);
    }
}
```

## Система прав (Permissions DSL)

FCore предлагает мощный и гибкий DSL (Domain-Specific Language) для программного определения прав и групп, позволяющий создавать сложную логику без прямого взаимодействия с API плагинов прав.

- **`PermissionManager`**: Главный сервис для работы с правами. Через него происходит регистрация прав, проверка и взаимодействие с холдерами.
- **`PermissionBuilder`**: Fluent API для декларативного создания групп и прав. Это основной инструмент для настройки пермишенов.
- **`PermissionGroup`**: Представляет группу прав. Группы могут наследоваться друг от друга, создавая иерархию.
- **`UserPermissionHolder`**: Контейнер, который хранит права конкретного игрока, включая унаследованные от групп и временные права.
- **`PermissionContext`**: Позволяет задавать контекст для прав. Например, можно выдать право, которое будет работать только в определенном мире, регионе или при выполнении кастомного условия.

**Пример сложной настройки прав:**
```java
@Service
public class MyPermissions {
    @Inject
    private PermissionManager permissionManager;
    
    @PostConstruct
    public void setupPermissions() {
        permissionManager.createBuilder()
            // Группа для обычных игроков
            .group("user")
                .add("myplugin.command.help")
                .add("myplugin.command.spawn")

            // VIP-группа, наследующая права 'user'
            .group("vip")
                .inherit("user")
                .add("myplugin.kit.vip")
                .add("myplugin.command.fly", context -> 
                    // Право на полет работает только в мире 'world_lobby'
                    context.has("world") && context.get("world").equals("world_lobby")
                )

            // Админская группа
            .group("admin")
                .add("*") // Все права

            .buildAndRegister(); // Собираем и регистрируем все группы
    }
    
    // Выдача временного права
    public void giveTempFly(Player player) {
        permissionManager.getUserHolder(player.getUniqueId())
            .addTemporaryPermission("myplugin.temp.fly", 3600); // Право на час
    }
}
```

## Система безопасности

FCore включает в себя продвинутую систему безопасности, предназначенную для защиты сервера от нестабильных или вредоносных плагинов.

- **`SecurityManager`**: Центральный компонент, который управляет всеми проверками. Он отслеживает зависимости плагинов от FCore и кэширует результаты проверок.
- **`PluginErrorHandler`**: Глобальный обработчик ошибок, который перехватывает исключения, возникающие в других плагинах. Он автоматически создает подробные отчеты о сбоях (`crash-reports`) и может отключить плагин, если тот вызывает слишком много ошибок, предотвращая падение всего сервера.
- **`PluginSecurityAnalyzer`**: Инструмент статического анализа, который сканирует `.jar`-файлы плагинов на наличие потенциально опасного кода. Он ищет использование опасных классов и методов, таких как работа с файловой системой, рефлексия, запуск внешних процессов и прямое управление другими плагинами.
- **`SafeOperationExecutor`**: Утилита для выполнения кода в изолированной среде с таймаутами. Это позволяет безопасно вызывать код из сторонних плагинов, не опасаясь, что он вызовет зависание сервера.

## Система действий и триггеров (Action System)

FCore предлагает уникальную и мощную систему для создания скриптовых сценариев, называемых "действиями" (`Action`). Эта система позволяет строить сложные последовательности событий (отправка сообщений, проигрывание звуков, показ эффектов) с помощью удобного fluent API, а затем запускать их вручную или автоматически с помощью триггеров.

### 1. `ActionSequence`: Создание цепочек действий

Основной инструмент для работы с системой — это `ActionSequence`. Он позволяет "цепочкой" вызывать методы для построения сценария.

- `ActionSequence.create(name, description)`: Создает новую последовательность.
- `.then(Action)`: Добавляет любое кастомное действие.
- `.message(String)`: Отправляет сообщение игроку.
- `.sound(Sound, volume, pitch)`: Проигрывает звук.
- `.title(title, subtitle)`: Показывает заголовок.
- `.particle(Particle, ...)`: Создает частицы.
- `.wait(ticks)`: Добавляет задержку.
- `.run(player -> { ... })`: Выполняет произвольный Java-код.
- `.parallel(actions -> ...)`: Выполняет несколько действий одновременно.
- `.condition(player -> ..., then -> ..., else -> ...)`: Добавляет условное ветвление.
- `.repeat(times, sequence -> ...)`: Повторяет блок действий несколько раз.

**Пример создания сложной последовательности:**

```java
ActionManager manager = FCore.getInstance().getActionManager();

// Создаем и регистрируем последовательность
ActionSequence welcomeSequence = ActionSequence.create("player_welcome", "Приветствие игрока")
    .title("&6Добро пожаловать!", "&e" + player.getName())
    .wait(40) // 2 секунды
    .message("&aНадеемся, тебе у нас понравится!")
    .sound(Sound.ENTITY_PLAYER_LEVELUP)
    .parallel(p -> p
        .add(new ParticleAction(Particle.VILLAGER_HAPPY, 50))
        .add(new SoundAction(Sound.ENTITY_EXPERIENCE_ORB_PICKUP))
    )
    .condition(
        player -> player.hasPermission("myplugin.vip"),
        // Ветка для VIP-игроков
        then -> then.message("&bКак VIP-игрок, вы получаете бонус!"),
        // Ветка для обычных
        else_ -> else_.message("&7Хотите стать VIP?")
    );
    
manager.registerAction(welcomeSequence);

// Позже можно выполнить эту последовательность для игрока
manager.executeAction("player_welcome", somePlayer);
```

### 2. Триггеры: Автоматический запуск действий

Триггеры позволяют автоматически запускать последовательности действий при наступлении определенных событий.

- **`JoinTrigger`**: Срабатывает при входе игрока.
- **`CommandTrigger`**: Срабатывает при выполнении игроком определенной команды.
- **`RegionTrigger`**: Срабатывает при входе или выходе игрока из заданной кубоидной области.
- **`IntervalTrigger`**: Срабатывает периодически через заданный интервал времени.

**Пример использования триггера:**
```java
// Получаем ранее созданную последовательность
Action welcomeAction = manager.getAction("player_welcome");

// Создаем триггер, который сработает при первом входе игрока
ActionTrigger joinTrigger = new JoinTrigger(true); // true - только для первого входа

// Активируем триггер, привязав к нему действие
manager.activateTrigger(joinTrigger, welcomeAction);
```

### 3. Отладка действий
Система имеет встроенный отладчик, который можно включить для конкретного игрока, чтобы видеть в чате лог выполнения всех действий и последовательностей.

- Команда `/fcoreactiondebug <player>` включает/выключает режим отладки.

## Система управления данными

FCore предоставляет мощный, интегрированный слой для работы с данными, который включает annotation-based конфигурации, ORM, систему миграций и кэширование. Центральным классом является `DataManager`, доступный через `FCore.getInstance().getDataManager()`.

### 1. Менеджер конфигураций (`ConfigManager`)
Позволяет избавиться от ручного парсинга конфигов. Вы просто создаете класс, описывающий вашу конфигурацию, а FCore сам загрузит данные из `yml` или `json` файла.

- `@ConfigFile("filename.yml")`: Указывает на класс, который является конфигурацией.
- `@ConfigValue("path.to.value")`: Связывает поле класса с ключом в конфиге.
- `@ConfigSection("path")`: Связывает поле, являющееся другим классом, с целой секцией в конфиге.

**Пример `settings.yml` и класса для него:**
```java
// Класс для настроек
@ConfigFile("settings.yml")
public class MySettings {
    @ConfigValue("features.chat.enabled")
    public boolean chatEnabled = true;

    @ConfigValue("features.welcome-message")
    public String welcomeMessage = "Welcome to the server!";
    
    @ConfigSection("database")
    public DatabaseSection database = new DatabaseSection();
    
    public static class DatabaseSection {
        @ConfigValue("host")
        public String host = "localhost";
        
        @ConfigValue("port")
        public int port = 3306;
    }
}

// Загрузка конфигурации
MySettings settings = FCore.getInstance().getDataManager().getConfigManager().load(MySettings.class);
if (settings.chatEnabled) {
    System.out.println("Chat is enabled!");
}
```

### 2. ORM и База данных (`Database`)
FCore включает в себя простой, но функциональный ORM (Object-Relational Mapping), который позволяет работать с базой данных (MySQL или SQLite), как с обычными Java-объектами.

- `@Entity("table_name")`: Помечает класс как сущность, привязанную к таблице.
- `@Id`: Указывает на поле, являющееся первичным ключом. `autoIncrement = true` для автоинкремента.
- `@Column("column_name")`: Привязывает поле к колонке в таблице.

ORM автоматически создает таблицы, если они не существуют.

**Пример сущности `UserData`:**
```java
@Entity("fcore_users")
public class UserData {
    @Id(autoIncrement = true)
    @Column("id")
    private int id;
    
    @Column("uuid")
    private String uuid;
    
    @Column("username")
    private String username;
    
    @Column("balance")
    private double balance;
    
    // Конструкторы, геттеры и сеттеры
}
```

**Пример использования:**
```java
Database db = FCore.getInstance().getDataManager().getDatabase();

// Создание нового пользователя (INSERT)
UserData newUser = new UserData(player.getUniqueId(), player.getName());
db.save(newUser); // После сохранения поле id будет заполнено

// Получение пользователя по ID (SELECT)
UserData foundUser = db.find(UserData.class, newUser.getId());

// Обновление (UPDATE)
foundUser.setBalance(100.0);
db.save(foundUser); // т.к. id уже есть, выполнится UPDATE

// Поиск по условию
List<UserData> richUsers = db.findAll(UserData.class, "balance > ?", 1000);

// Удаление (DELETE)
db.delete(foundUser);
```

### 3. Миграции базы данных (`MigrationManager`)
Система миграций позволяет последовательно и контролируемо изменять схему вашей базы данных (добавлять таблицы, колонки и т.д.) по мере развития плагина.

- `Migration`: Интерфейс для каждой миграции.
- `up()`: Метод для применения миграции.
- `down()`: Метод для отката миграции.

Миграции запускаются автоматически при старте сервера.

**Пример миграции:**
```java
public class AddEmailToUsersMigration extends AbstractMigration {
    public AddEmailToUsersMigration(ConnectionManager cm, Logger log) { super(cm, log); }
    
    @Override public int getVersion() { return 20230102; }
    @Override public String getName() { return "add_email_to_users"; }
    
    @Override
    public boolean up() {
        return executeQuery("ALTER TABLE fcore_users ADD COLUMN email VARCHAR(255)");
    }
    
    @Override
    public boolean down() {
        // Здесь должен быть код для удаления колонки, если БД это поддерживает
        return true; 
    }
}
```

### 4. Кэширование (`CacheManager`)
Встроенный менеджер кэша для снижения нагрузки на базу данных и ускорения доступа к часто используемым данным. Поддерживает автоматическое удаление устаревших записей (TTL). ORM автоматически использует кэш при поиске сущностей по ID.

**Пример использования:**
```java
CacheManager cache = FCore.getInstance().getDataManager().getCacheManager();

// Положить данные в кэш на 10 минут
cache.put("server_stats", "top_donator", "Steve", 600); // 600 секунд

// Получить данные
String topDonator = cache.get("server_stats", "top_donator");

if (topDonator != null) {
    // ...
}
```

## API для плагинов и их загрузка

FCore имеет собственную систему для загрузки и управления плагинами, что позволяет реализовать тесную интеграцию и кастомную логику.

- **`FCorePlugin`**: Абстрактный класс, от которого рекомендуется наследовать ваши плагины для лучшей интеграции с FCore. Он предоставляет удобные методы-геттеры для доступа ко всем менеджерам ядра и упрощает регистрацию компонентов.
- **`PluginLoader`**: Это сердце загрузчика плагинов FCore. Он отвечает за обнаружение плагинов, зависимых от FCore, сканирование их классов на аннотации (`@Service`, `@Command` и т.д.), создание экземпляров и внедрение зависимостей.
- **`PluginDescription`**: Класс, содержащий метаданные плагина (имя, версия, автор), аналогично `PluginDescriptionFile` из Bukkit, но адаптированный для системы FCore.

**Пример основного класса плагина:**
```java
public class MyAwesomePlugin extends FCorePlugin {
    
    @Inject
    private PlayerDataService playerDataService; // Внедряем наш сервис
    
    @Override
    public void onPluginEnable() {
        // Код, который выполнится при включении плагина
        getLogger().info("MyAwesomePlugin включен!");
        
        // Регистрация команд из пакета
        getCommandManager().registerCommands(this, "com.myawesomeplugin.commands");
        
        // Регистрация слушателей из пакета
        getEventManager().registerListeners(this, "com.myawesomeplugin.listeners");
        
        // playerDataService уже внедрен и готов к работе
        playerDataService.initialize();
    }
    
    @Override
    public void onPluginDisable() {
        // Код очистки при выключении
        getLogger().info("MyAwesomePlugin выключен!");
    }
}
```

## Как использовать API

Для взаимодействия с ядром используйте статический метод `FCore.getInstance()`, который предоставляет доступ ко всем основным менеджерам.

**Пример получения менеджера GUI:**
```java
GuiManager guiManager = FCore.getInstance().getGuiManager();
``` 