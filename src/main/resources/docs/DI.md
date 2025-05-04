# Документация по Системе Внедрения Зависимостей (DI) FCore

## Обзор
Система внедрения зависимостей (Dependency Injection, DI) в FCore предоставляет надежный механизм управления зависимостями между компонентами вашего плагина. Она основана на принципах инверсии управления (IoC) и позволяет создавать слабосвязанные, тестируемые и масштабируемые приложения.

## Основные компоненты

### 1. Аннотации

#### @Service
Отмечает класс, который следует зарегистрировать как сервис в контейнере зависимостей.

```java
@Service
public class MyService implements FCoreService {
    // Реализация сервиса
}
```

Параметры:
- `value()` - имя сервиса (если не указано, используется имя класса)
- `priority()` - приоритет загрузки (сервисы с более высоким приоритетом загружаются раньше)
- `scope()` - область видимости сервиса (SINGLETON, PROTOTYPE, SESSION, REQUEST)
- `lazy()` - если true, сервис будет создан только при первом запросе

#### @Inject
Отмечает поле, конструктор или метод для внедрения зависимости.

```java
@Service
public class PlayerManager {
    @Inject
    private Database database;
    
    // Остальной код...
}
```

Параметры:
- `scope()` - область видимости внедряемой зависимости (по умолчанию SINGLETON)

#### @Autowired
Альтернативная аннотация для внедрения зависимостей, синоним @Inject.

#### @PostConstruct
Методы с этой аннотацией вызываются после создания объекта и внедрения всех зависимостей.

```java
@Service
public class ConfigManager {
    @Inject
    private FCore plugin;
    
    private Map<String, Config> configs;
    
    @PostConstruct
    public void initialize() {
        configs = new HashMap<>();
        loadDefaultConfigs();
    }
    
    // Остальной код...
}
```

Параметры:
- `priority()` - приоритет метода (методы с более высоким приоритетом выполняются раньше)

#### @PreDestroy
Методы с этой аннотацией вызываются перед уничтожением объекта.

```java
@Service
public class ConnectionPool {
    private List<Connection> connections;
    
    @PreDestroy
    public void closeConnections() {
        for (Connection conn : connections) {
            try {
                conn.close();
            } catch (Exception e) {
                // Обработка исключений
            }
        }
    }
    
    // Остальной код...
}
```

Параметры:
- `priority()` - приоритет метода (методы с более высоким приоритетом выполняются раньше)

### 2. Области видимости (Scopes)

#### SINGLETON (по умолчанию)
Один экземпляр объекта на весь плагин. Создается при старте плагина и существует до его отключения.

```java
@Service(scope = InjectionScope.SINGLETON)
public class ServerStats {
    // Реализация...
}
```

#### PROTOTYPE
Новый экземпляр создается при каждом запросе сервиса.

```java
@Service(scope = InjectionScope.PROTOTYPE)
public class Request {
    // Каждый раз создается новый экземпляр
}
```

#### SESSION
Один экземпляр на сессию. Полезно для объектов, привязанных к сессии игрока.

```java
@Service(scope = InjectionScope.SESSION)
public class PlayerSession {
    // Один экземпляр на сессию игрока
}
```

#### REQUEST
Экземпляры создаются для обработки одного запроса и затем уничтожаются.

```java
@Service(scope = InjectionScope.REQUEST)
public class CommandContext {
    // Создается для каждого вызова команды
}
```

## Примеры использования

### Базовое внедрение зависимостей

```java
// Определение сервиса
@Service
public class EconomyService implements FCoreService {
    @Override
    public void onEnable() {
        // Инициализация сервиса
    }
    
    @Override
    public void onDisable() {
        // Очистка ресурсов
    }
    
    @Override
    public String getName() {
        return "EconomyService";
    }
    
    public double getBalance(UUID playerId) {
        // Реализация метода
        return 0;
    }
}

// Использование сервиса
@Service
public class ShopCommand {
    @Inject
    private EconomyService economyService;
    
    public void processTransaction(Player player, double amount) {
        double balance = economyService.getBalance(player.getUniqueId());
        // Остальная логика...
    }
}
```

### Внедрение с разными областями видимости

```java
@Service(scope = InjectionScope.SINGLETON)
public class ConfigManager {
    // Содержимое...
}

@Service
public class PlayerManager {
    @Inject(scope = InjectionScope.SESSION)
    private PlayerData playerData;
    
    @Inject
    private ConfigManager configManager; // По умолчанию синглтон
    
    // Методы...
}
```

### Использование жизненного цикла

```java
@Service
public class DatabaseManager {
    private Connection connection;
    
    @Inject
    private FCore plugin;
    
    @PostConstruct
    public void connect() {
        // Инициализация соединения с БД
        connection = DriverManager.getConnection("jdbc:mysql://localhost/db", "user", "pass");
        plugin.getLogger().info("БД подключена");
    }
    
    @PreDestroy
    public void disconnect() {
        // Закрытие соединения при отключении плагина
        if (connection != null) {
            try {
                connection.close();
                plugin.getLogger().info("Соединение с БД закрыто");
            } catch (SQLException e) {
                plugin.getLogger().severe("Ошибка при закрытии соединения: " + e.getMessage());
            }
        }
    }
}
```

### Ленивая загрузка (Lazy Loading)

```java
@Service(lazy = true)
public class ExpensiveService {
    // Этот сервис будет создан только при первом запросе
    
    public ExpensiveService() {
        // Ресурсоемкая инициализация
    }
    
    // Методы...
}
```

## Расширенное использование

### Программное получение сервисов

```java
public class Main {
    @Inject
    private DependencyContainer container;
    
    public void someMethod() {
        // Получение сервиса программно
        MyService service = container.getInstance(MyService.class);
        service.doSomething();
    }
}
```

### Биндинг интерфейса к реализации

```java
// В вашем главном классе плагина
@Override
public void onEnable() {
    DependencyContainer container = new DependencyContainer(this);
    
    // Связываем интерфейс с конкретной реализацией
    container.bind(Storage.class, MySQLStorage.class);
    
    // Теперь когда @Inject Storage storage; будет создан экземпляр MySQLStorage
}
```

### Использование фабрик

```java
@Service
public class EntityFactory implements DependencyContainer.InstanceFactory<Entity> {
    @Inject
    private World world;
    
    @Override
    public Entity createInstance() {
        return world.spawn(new Location(world, 0, 64, 0), Zombie.class);
    }
}

// Регистрация фабрики
container.registerFactory(Entity.class, entityFactory);
```

## Лучшие практики

1. **Используйте интерфейсы**: Внедряйте зависимости через интерфейсы, а не конкретные реализации.

```java
// Плохо
@Inject
private MySQLDatabase database;

// Хорошо
@Inject
private Database database;
```

2. **Разделяйте обязанности**: Каждый сервис должен выполнять только одну функцию.

3. **Избегайте циклических зависимостей**: Если A зависит от B, а B зависит от A, это создаст проблемы.

4. **Правильно выбирайте scope**: Используйте области видимости в соответствии с жизненным циклом объекта.

5. **Документируйте ваши сервисы**: Добавляйте JavaDoc к вашим сервисам, объясняя их назначение и API.

6. **Используйте @PostConstruct и @PreDestroy**: Правильно инициализируйте и освобождайте ресурсы.

## Устранение неполадок

### Циклические зависимости
Если вы столкнулись с ошибкой циклической зависимости:

```
Обнаружена циклическая зависимость: ServiceA -> ServiceB -> ServiceA
```

Решения:
- Используйте проверку null и отложенную инициализацию
- Внедряйте DependencyContainer и получайте зависимости программно
- Реорганизуйте ваши сервисы, чтобы избежать цикла

### Не удалось создать экземпляр
Если вы видите ошибку:

```
Не удалось создать экземпляр типа MyService
```

Возможные причины:
- Отсутствует конструктор без параметров
- Исключение в конструкторе
- Недоступный конструктор (приватный)

### Не удалось внедрить зависимость
Если вы видите предупреждение:

```
Не удалось внедрить зависимость Database в PlayerManager.database
```

Возможные причины:
- Зависимость не зарегистрирована как сервис
- Проблемы с доступом к полю (приватное без сеттера)
- Неправильный тип зависимости

## Заключение
Система внедрения зависимостей FCore предоставляет мощный инструмент для управления зависимостями в вашем плагине. Используйте эту документацию как руководство для эффективной реализации и использования сервисов с применением принципов DI. 