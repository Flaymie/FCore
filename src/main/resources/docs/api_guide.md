# Руководство по API FCore

## Содержание
1. [Введение](#введение)
2. [Интеграции](#интеграции)
   - [PlaceholderAPI](#placeholderapi)
   - [Vault](#vault)
   - [WorldGuard](#worldguard)
3. [Создание плагинов для FCore](#создание-плагинов-для-fcore)
   - [Структура плагина](#структура-плагина)
   - [Жизненный цикл](#жизненный-цикл)
   - [Зависимости](#зависимости)
4. [Примеры](#примеры)

## Введение

FCore предоставляет мощный API для разработки плагинов и интеграции с другими популярными плагинами Minecraft. Этот документ описывает основные возможности API и приводит примеры его использования.

## Интеграции

### PlaceholderAPI

FCore предоставляет интеграцию с PlaceholderAPI, позволяющую создавать и использовать плейсхолдеры в вашем плагине.

#### Получение доступа к API PlaceholderAPI

```java
// Получение менеджера интеграций
IntegrationManager integrationManager = FCore.getInstance().getIntegrationManager();

// Получение менеджера PlaceholderAPI
PlaceholderAPIManager placeholderManager = integrationManager.getPlaceholderAPIManager();

// Проверка доступности PlaceholderAPI
if (placeholderManager != null && placeholderManager.isEnabled()) {
    // PlaceholderAPI доступен
}
```

#### Регистрация плейсхолдеров

```java
// Регистрация плейсхолдера %fcore_example%
placeholderManager.registerPlaceholder("example", player -> {
    if (player == null) return "Нет игрока";
    return "Привет, " + player.getName() + "!";
});
```

#### Форматирование текста с плейсхолдерами

```java
String text = "Ваше имя: %player_name%";
String formatted = placeholderManager.setPlaceholders(player, text);
player.sendMessage(formatted);
```

### Vault

FCore интегрируется с Vault для обеспечения экономики и управления правами.

#### Получение доступа к API Vault

```java
// Получение менеджера Vault
VaultManager vaultManager = integrationManager.getVaultManager();

// Проверка доступности экономики Vault
if (vaultManager != null && vaultManager.isEconomyEnabled()) {
    // Экономика Vault доступна
    Economy economy = vaultManager.getEconomy();
}
```

#### Работа с экономикой

```java
// Получение баланса игрока
double balance = economy.getBalance(player);

// Добавление денег игроку
economy.depositPlayer(player, 100);

// Снятие денег с игрока
economy.withdrawPlayer(player, 50);

// Форматирование суммы
String formatted = economy.format(balance);
```

### WorldGuard

FCore предоставляет интеграцию с WorldGuard для работы с регионами.

#### Получение доступа к API WorldGuard

```java
// Получение менеджера WorldGuard
WorldGuardManager worldGuardManager = integrationManager.getWorldGuardManager();

// Проверка доступности WorldGuard
if (worldGuardManager != null && worldGuardManager.isEnabled()) {
    // WorldGuard доступен
    RegionManager regionManager = worldGuardManager.getRegionManager();
}
```

#### Работа с регионами

```java
// Получение списка регионов, в которых находится игрок
List<String> regions = regionManager.getPlayerRegions(player);

// Проверка, находится ли игрок в регионе
boolean inRegion = regionManager.isPlayerInRegion(player, "spawn");

// Проверка, является ли игрок владельцем региона
boolean isOwner = regionManager.isPlayerOwnerOfRegion(player, "plot123");

// Получение всех регионов в мире
List<String> worldRegions = regionManager.getRegionsInWorld(player.getWorld());
```

## Создание плагинов для FCore

FCore предоставляет API для создания плагинов, которые будут работать внутри его экосистемы.

### Структура плагина

Для создания плагина для FCore необходимо:

1. Создать класс, наследующий `FCorePlugin`
2. Создать файл `plugin.yml` с информацией о плагине

#### Пример класса плагина

```java
package com.example.plugin;

import dev.flaymie.fcore.FCore;
import dev.flaymie.fcore.api.plugin.FCorePlugin;
import org.bukkit.event.Listener;

public class ExamplePlugin extends FCorePlugin implements Listener {
    
    public ExamplePlugin(FCore core) {
        super(core, "ExamplePlugin", "1.0");
    }
    
    @Override
    public void onEnable() {
        // Код инициализации плагина
        core.getLogger().info("Пример плагина включен");
    }
    
    @Override
    public void onDisable() {
        // Код выгрузки плагина
        core.getLogger().info("Пример плагина отключен");
    }
}
```

#### Пример plugin.yml

```yaml
name: ExamplePlugin
version: 1.0
main: com.example.plugin.ExamplePlugin
description: Пример плагина для FCore
author: YourName
depends: [FCore]
```

### Жизненный цикл

Жизненный цикл плагина FCore состоит из следующих этапов:

1. **Загрузка (onLoad)**: Вызывается при загрузке плагина
2. **Включение (onEnable)**: Вызывается при включении плагина
3. **Отключение (onDisable)**: Вызывается при отключении плагина

### Зависимости

Вы можете указать зависимости вашего плагина от других плагинов FCore в файле `plugin.yml`:

```yaml
depends: [FCore, OtherFCorePlugin]
```

## Примеры

В пакете `dev.flaymie.fcore.examples` находятся примеры плагинов, демонстрирующие различные возможности API:

- `SimplePlugin`: Простой пример плагина FCore
- `IntegrationExamplePlugin`: Пример использования интеграций с PlaceholderAPI, Vault и WorldGuard

Эти примеры можно использовать как шаблон для создания своих плагинов. 