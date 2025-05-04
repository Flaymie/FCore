# Документация по Системе Событий FCore

## Обзор
Система событий FCore расширяет стандартный механизм событий Bukkit, добавляя удобные возможности:
- Автоматическая регистрация слушателей с помощью аннотаций
- Поддержка внедрения зависимостей в слушатели
- Расширенное управление приоритетами обработчиков
- Упрощенное создание и вызов кастомных событий

## Основные компоненты

### 1. Аннотации

#### @EventListener
Отмечает класс как слушатель событий для автоматической регистрации.

```java
@EventListener(priority = 10)
public class MyListener implements Listener {
    // Методы-обработчики
}
```

Параметры:
- `priority()` - приоритет слушателя (слушатели с более высоким приоритетом регистрируются раньше)
- `autoRegister()` - если true, слушатель будет зарегистрирован автоматически при старте плагина

#### @Priority
Указывает приоритет метода-обработчика события. Используется вместе с `@EventHandler`.

```java
@EventHandler
@Priority(EventPriority.HIGH)
public void onPlayerJoin(PlayerJoinEvent event) {
    // Обработка события
}
```

Параметры:
- `value()` - приоритет из перечисления EventPriority (LOWEST, LOW, NORMAL, HIGH, HIGHEST, MONITOR)

#### @IgnoreCancelled
Указывает, должен ли метод-обработчик игнорировать отмененные события.

```java
@EventHandler
@IgnoreCancelled(true)
public void onBlockBreak(BlockBreakEvent event) {
    // Этот метод не будет вызван для отмененных событий
}
```

Параметры:
- `value()` - если true, метод не будет вызван для отмененных событий

### 2. Базовые классы

#### FCoreEvent
Базовый класс для всех кастомных событий в FCore.

```java
public class MyCustomEvent extends FCoreEvent {
    // Поля и методы события
}
```

Особенности:
- Наследует от стандартного Bukkit Event
- Реализует интерфейс Cancellable для поддержки отмены события
- Поддерживает асинхронные события

#### AbstractEventListener
Абстрактный класс для слушателей событий, предоставляющий базовую функциональность.

```java
@EventListener
public class MyListener extends AbstractEventListener {
    // Реализация обработчиков
}
```

Особенности:
- Внедрение экземпляра плагина через DI
- Методы для вызова кастомных событий
- Проверка инициализации слушателя

## Примеры использования

### Создание слушателя событий

```java
@EventListener(priority = 5)
public class PlayerDataListener extends AbstractEventListener {
    
    @Inject
    private PlayerDataService dataService;
    
    @EventHandler
    @Priority(EventPriority.HIGH)
    @IgnoreCancelled
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        dataService.loadPlayerData(player);
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        dataService.savePlayerData(player);
    }
}
```

### Создание кастомного события

```java
public class PlayerDataLoadEvent extends FCoreEvent {
    
    private final Player player;
    private final Map<String, Object> data;
    
    public PlayerDataLoadEvent(Player player, Map<String, Object> data) {
        this.player = player;
        this.data = data;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public Map<String, Object> getData() {
        return data;
    }
}
```

### Вызов кастомного события

```java
@Service
public class PlayerDataService {
    
    @Inject
    private EventManager eventManager;
    
    public void loadPlayerData(Player player) {
        // Загрузка данных
        Map<String, Object> data = loadDataFromDatabase(player);
        
        // Создание и вызов события
        PlayerDataLoadEvent event = new PlayerDataLoadEvent(player, data);
        boolean notCancelled = eventManager.callEvent(event);
        
        if (notCancelled) {
            // Продолжаем обработку данных
        } else {
            // Событие было отменено, пропускаем обработку
        }
    }
}
```

### Обработка кастомного события

```java
@EventListener
public class StatsListener implements Listener {
    
    @EventHandler
    public void onPlayerDataLoad(PlayerDataLoadEvent event) {
        Player player = event.getPlayer();
        Map<String, Object> data = event.getData();
        
        // Обработка загруженных данных
        updatePlayerStatistics(player, data);
        
        // При необходимости, можно отменить событие
        if (containsInvalidData(data)) {
            event.setCancelled(true);
        }
    }
}
```

### Работа с асинхронными событиями

```java
// Создание асинхронного события
public class AsyncDataProcessEvent extends FCoreEvent {
    
    private final UUID playerId;
    private final String dataType;
    
    public AsyncDataProcessEvent(UUID playerId, String dataType) {
        super(true); // Указываем, что событие асинхронное
        this.playerId = playerId;
        this.dataType = dataType;
    }
    
    // Геттеры...
}

// Обработка асинхронного события
@EventHandler
public void onAsyncDataProcess(AsyncDataProcessEvent event) {
    // ВАЖНО: не обращаться к Bukkit API из асинхронных событий
    // если Bukkit API не является потокобезопасным
    
    // Безопасно делать:
    processDataAsync(event.getPlayerId(), event.getDataType());
    
    // Небезопасно делать:
    // Player player = Bukkit.getPlayer(event.getPlayerId()); // Не делать в асинхронных обработчиках!
}
```

## Программная регистрация слушателей

Если вам нужно зарегистрировать слушателя программно (не через аннотацию):

```java
@Service
public class MyService {
    
    @Inject
    private EventManager eventManager;
    
    public void registerCustomListener() {
        DynamicListener listener = new DynamicListener();
        eventManager.registerListener(listener);
    }
    
    public void unregisterCustomListener(Listener listener) {
        eventManager.unregisterListener(listener);
    }
}
```

## Приоритеты событий Bukkit

Стандартные приоритеты событий (от самого раннего до самого позднего):

1. **LOWEST** - самый ранний вызов, для препроцессинга
2. **LOW** - для ранней обработки
3. **NORMAL** - стандартный приоритет (по умолчанию)
4. **HIGH** - для поздней обработки 
5. **HIGHEST** - для пост-обработки
6. **MONITOR** - только для мониторинга, не должен изменять событие

Порядок вызова обработчиков:
1. Все обработчики с приоритетом LOWEST (в порядке регистрации)
2. Все обработчики с приоритетом LOW (в порядке регистрации)
3. ...
4. Все обработчики с приоритетом MONITOR (в порядке регистрации)

## Лучшие практики

1. **Используйте правильные приоритеты**: 
   - LOWEST/LOW для препроцессинга и модификации событий
   - NORMAL для стандартной обработки
   - HIGH/HIGHEST для важных обработчиков, которые должны выполняться после обычных
   - MONITOR только для логирования, не модифицируйте события на этом приоритете

2. **Правильно обрабатывайте отмену событий**:
   - Используйте @IgnoreCancelled(true) для методов, которые не должны выполняться для отмененных событий
   - Проверяйте event.isCancelled() перед модификацией игрового состояния

3. **Будьте осторожны с асинхронными событиями**:
   - Из асинхронных обработчиков обращайтесь только к потокобезопасным методам Bukkit API
   - Используйте Bukkit.getScheduler().runTask() для выполнения синхронного кода из асинхронных обработчиков

4. **Не злоупотребляйте отменой событий**:
   - Отменяйте событие только если необходимо остановить действие
   - Используйте event.setCancelled(true) для отмены действия

5. **Документируйте ваши кастомные события**:
   - Указывайте, является ли событие отменяемым
   - Объясняйте, что происходит при отмене
   - Документируйте все поля и методы

6. **Эффективно используйте слушатели**:
   - Объединяйте связанные обработчики в одном классе
   - Избегайте создания слишком большого количества классов-слушателей

## Устранение неполадок

### Слушатель не регистрируется автоматически
Проверьте:
1. Установлена ли аннотация @EventListener на классе
2. Реализует ли класс интерфейс Listener
3. Установлен ли параметр autoRegister=true (по умолчанию true)
4. Нет ли ошибок в конструкторе класса

### Обработчик события не вызывается
Проверьте:
1. Имеет ли метод аннотацию @EventHandler
2. Соответствует ли сигнатура метода (один параметр - событие)
3. Если используется @IgnoreCancelled, не является ли событие отмененным
4. Правильно ли установлен приоритет метода

### Кастомное событие не обрабатывается
Проверьте:
1. Переопределен ли метод getHandlers() в классе события
2. Реализован ли статический метод getHandlerList()
3. Правильно ли создан и вызван HandlerList

### Ошибки при внедрении зависимостей
Проверьте:
1. Зарегистрированы ли все зависимости в контейнере DI
2. Помечены ли поля аннотацией @Inject
3. Имеют ли зависимые классы публичный конструктор

## Заключение
Система событий FCore предоставляет мощный и гибкий механизм для обработки игровых событий и создания собственных событий. Используя аннотации и интеграцию с системой DI, вы можете значительно упростить разработку и поддержку вашего кода. 