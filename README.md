# FCore - Ядро для плагинов Minecraft

![Версия](https://img.shields.io/badge/версия-1.1-blue)
![API](https://img.shields.io/badge/API-Spigot%201.16.5-yellow)
![Java](https://img.shields.io/badge/Java-8%2B-orange)

Простое ядро для плагинов, предоставляющее базовые инструменты для ускорения разработки.

## Содержание
- [Установка](#установка)
- [Ключевые возможности](#ключевые-возможности)
- [Система зависимостей (DI)](#система-зависимостей-di)
- [Система команд](#система-команд)
- [Система событий](#система-событий)
- [Система конфигураций](#система-конфигураций)
- [API Безопасности](#api-безопасности)
- [Пример плагина](#пример-плагина)

## Установка

### Требования
- Java 8 или выше
- Сервер Spigot/Paper 1.16.5
- Maven или Gradle

### Установка на сервер

1.  Скачайте последнюю версию FCore.
2.  Поместите файл `fcore-1.1.jar` в директорию `plugins` вашего сервера.
3.  Перезапустите сервер.

### Подключение к проекту

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

#### Gradle

```groovy
repositories {
    maven { url 'https://raw.githubusercontent.com/Flaymie/FCore/main/repo/' }
}

dependencies {
    compileOnly 'dev.flaymie:fcore:1.1'
}
```

### Настройка `plugin.yml`

В вашем файле `plugin.yml` добавьте зависимость от FCore:

```yaml
name: YourPlugin
version: 1.0
main: your.package.YourPlugin
api-version: 1.16
depend: [FCore]
```

## Ключевые возможности

- **Простая система зависимостей (DI)**: Внедряйте сервисы через аннотации `@Service` и `@Inject`.
- **Система команд на аннотациях**: Создавайте команды с подкомандами, используя аннотации.
- **Менеджер сервисов**: Управляйте жизненным циклом ваших компонентов.
- **Автоматическая загрузка конфигураций**: Используйте аннотации `@ConfigFile` и `@ConfigValue` для работы с конфигами.
- **Базовые интеграции**: Поддержка PlaceholderAPI, Vault и WorldGuard (проверка наличия).

## Система зависимостей (DI)

FCore предоставляет базовый DI-контейнер для управления синглтон-сервисами.

- **@Service**: Помечает класс как сервис. FCore автоматически создаст один экземпляр этого класса при старте.
- **@Inject**: Внедряет зависимость (другой сервис) в поле класса.

### Пример

```java
// Файл: dev/flaymie/yourplugin/services/MyCustomService.java
package dev.flaymie.yourplugin.services;

import dev.flaymie.fcore.api.annotation.Service;

@Service
public class MyCustomService {
    public void doSomething() {
        System.out.println("MyCustomService работает!");
    }
}
```

```java
// Файл: dev/flaymie/yourplugin/YourPlugin.java
package dev.flaymie.yourplugin;

import dev.flaymie.fcore.FCore;
import dev.flaymie.fcore.api.annotation.Inject;
import dev.flaymie.yourplugin.services.MyCustomService;
import org.bukkit.plugin.java.JavaPlugin;

public class YourPlugin extends JavaPlugin {

    @Inject
    private MyCustomService customService;

    @Override
    public void onEnable() {
        FCore core = FCore.getInstance();
        
        // Внедряем зависимости в основной класс плагина
        core.getDependencyContainer().injectDependencies(this);
        
        // Теперь сервис доступен
        customService.doSomething(); // Выведет "MyCustomService работает!"
    }
}
```

## Система команд

Создавайте команды с помощью аннотаций.

- **@Command**: Помечает класс как обработчик команды.
- **@Subcommand**: Помечает метод как обработчик подкоманды.
- **@Permission**: Указывает право, необходимое для выполнения команды.

### Пример

```java
// Файл: dev/flaymie/yourplugin/commands/ExampleCommand.java
package dev.flaymie.yourplugin.commands;

import dev.flaymie.fcore.api.annotation.Command;
import dev.flaymie.fcore.api.annotation.Permission;
import dev.flaymie.fcore.api.annotation.Subcommand;
import org.bukkit.entity.Player;

@Command(name = "example", description = "Пример команды", aliases = {"ex"})
@Permission("yourplugin.example")
public class ExampleCommand {

    @Subcommand(value = "hello", description = "Говорит привет")
    public void helloSubcommand(Player sender) {
        sender.sendMessage("Привет, " + sender.getName() + "!");
    }
    
    // Пустой value означает, что это команда по умолчанию (просто /example)
    @Subcommand(value = "", description = "Основная команда")
    public void defaultCommand(Player sender) {
        sender.sendMessage("Вы использовали основную команду. Попробуйте /example hello");
    }
}
```

### Регистрация команд

Вам нужно **явно** зарегистрировать каждый класс команды в FCore.

```java
// В методе onEnable() вашего плагина
@Override
public void onEnable() {
    FCore.getInstance()
         .getCommandManager()
         .registerCommand(ExampleCommand.class);
}
```

## Система событий

FCore использует стандартную систему событий Bukkit. Вы можете создавать классы-слушатели и регистрировать их.

### Пример

```java
// Файл: dev/flaymie/yourplugin/listeners/PlayerJoinListener.java
package dev.flaymie.yourplugin.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.setJoinMessage("Игрок " + event.getPlayer().getName() + " вошел, используя FCore!");
    }
}
```

### Регистрация слушателей

Регистрация происходит через стандартный `PluginManager` Bukkit.

```java
// В методе onEnable() вашего плагина
@Override
public void onEnable() {
    getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
}
```

## Система конфигураций

Загружайте значения из YAML файлов с помощью аннотаций.

- **@ConfigFile("filename.yml")**: Привязывает класс к конкретному файлу в папке вашего плагина.
- **@ConfigValue("path.to.value")**: Внедряет значение из конфига в поле класса.

### Пример

**Файл `plugins/YourPlugin/settings.yml`:**
```yaml
welcome-message: "Добро пожаловать на наш сервер!"
features:
  chat-color: true
```

**Класс конфига:**
```java
// Файл: dev/flaymie/yourplugin/config/SettingsConfig.java
package dev.flaymie.yourplugin.config;

import dev.flaymie.fcore.core.data.config.ConfigFile;
import dev.flaymie.fcore.core.data.config.ConfigValue;
import lombok.Getter;

@Getter
@ConfigFile("settings.yml")
public class SettingsConfig {

    @ConfigValue("welcome-message")
    private String welcomeMessage = "Стандартное сообщение.";
    
    @ConfigValue("features.chat-color")
    private boolean chatColorEnabled = false;

}
```

### Использование конфига

FCore **не загружает** эти конфиги автоматически. Вам нужно вручную загрузить его через `ConfigManager` ядра.

```java
// В методе onEnable() вашего плагина
@Override
public void onEnable() {
    ConfigManager configManager = FCore.getInstance().getServiceManager().getService(ConfigManager.class);
    SettingsConfig settings = configManager.load(SettingsConfig.class);

    getLogger().info("Сообщение из конфига: " + settings.getWelcomeMessage()); // "Добро пожаловать на наш сервер!"
}
```

## API Безопасности

`SecurityManager` в FCore предназначен для **проверки зависимостей** плагинов, а не для лицензирования. Он позволяет убедиться, что плагин, использующий FCore, корректно от него зависит.

### Пример использования

Вы можете добавить простую проверку в `onEnable` вашего плагина, чтобы убедиться, что он правильно настроен.

```java
// В onEnable() вашего плагина
import dev.flaymie.fcore.core.security.PluginVerificationResult;
import dev.flaymie.fcore.core.security.PluginVerificationStatus;
import dev.flaymie.fcore.core.security.SecurityManager;

// ...

    @Override
    public void onEnable() {
        FCore core = FCore.getInstance();
        
        SecurityManager securityManager = core.getServiceManager().getService(SecurityManager.class);
        PluginVerificationResult result = securityManager.verifyPlugin(this);

        if (result.getStatus() != PluginVerificationStatus.VERIFIED) {
            getLogger().severe("========================================");
            getLogger().severe("      ПРОВЕРКА ПЛАГИНА ПРОВАЛЕНА        ");
            for(String issue : result.getIssues()){
                getLogger().severe(" - " + issue);
            }
            getLogger().severe("========================================");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        getLogger().info("Плагин успешно прошел проверку FCore.");
        // ... остальная логика вашего плагина
    }
```
Для работы этой проверки, убедитесь, что в `plugin.yml` указана зависимость: `depend: [FCore]`.

## Пример плагина

Вот как может выглядеть основной класс простого плагина, использующего FCore.

```java
package dev.flaymie.myplugin;

import dev.flaymie.fcore.FCore;
import dev.flaymie.fcore.api.annotation.Inject;
import dev.flaymie.myplugin.commands.ExampleCommand;
import dev.flaymie.myplugin.config.SettingsConfig;
import dev.flaymie.myplugin.listeners.PlayerJoinListener;
import dev.flaymie.myplugin.services.MyCustomService;
import dev.flaymie.fcore.core.data.config.ConfigManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class MyPlugin extends JavaPlugin {

    @Inject
    private MyCustomService customService;

    private SettingsConfig settings;

    @Override
    public void onEnable() {
        // Получаем экземпляр FCore
        FCore core = FCore.getInstance();

        // Внедряем зависимости (@Inject)
        core.getDependencyContainer().injectDependencies(this);

        // Загружаем конфиг
        ConfigManager configManager = core.getServiceManager().getService(ConfigManager.class);
        this.settings = configManager.load(SettingsConfig.class);
        getLogger().info("Конфигурация загружена. Приветственное сообщение: " + settings.getWelcomeMessage());
        
        // Регистрируем команды
        core.getCommandManager().registerCommand(ExampleCommand.class);
        
        // Регистрируем слушатели
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this.settings), this);

        // Используем наш сервис
        customService.doSomething();

        getLogger().info("MyPlugin успешно запущен!");
    }

    @Override
    public void onDisable() {
        getLogger().info("MyPlugin отключен.");
    }

    public SettingsConfig getSettings() {
        return settings;
    }
}
```