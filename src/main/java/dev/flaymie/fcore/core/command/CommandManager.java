package dev.flaymie.fcore.core.command;

import dev.flaymie.fcore.FCore;
import dev.flaymie.fcore.api.annotation.Argument;
import dev.flaymie.fcore.api.annotation.Command;
import dev.flaymie.fcore.api.annotation.Subcommand;
import dev.flaymie.fcore.api.service.FCoreService;
import dev.flaymie.fcore.core.di.DependencyContainer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.logging.Logger;

/**
 * Менеджер команд, управляет регистрацией и выполнением команд
 */
public class CommandManager implements FCoreService {
    
    private final FCore plugin;
    private final Logger logger;
    private final DependencyContainer dependencyContainer;
    
    private final Map<String, CommandInfo> commands;
    private final Map<CommandInfo, List<SubcommandInfo>> subcommands;
    private final Map<String, CommandInfo> aliasesMap;
    
    public CommandManager(FCore plugin, DependencyContainer dependencyContainer) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.dependencyContainer = dependencyContainer;
        
        this.commands = new HashMap<>();
        this.subcommands = new HashMap<>();
        this.aliasesMap = new HashMap<>();
    }
    
    @Override
    public void onEnable() {
        logger.info("Менеджер команд инициализирован");
        registerCoreCommands();
    }
    
    @Override
    public void onDisable() {
        commands.clear();
        subcommands.clear();
        aliasesMap.clear();
        logger.info("Менеджер команд отключен");
    }
    
    @Override
    public String getName() {
        return "CommandManager";
    }
    
    /**
     * Регистрирует все команды из указанного пакета
     * @param packageName имя пакета
     */
    public void registerCommands(String packageName) {
        // TODO: Реализовать сканирование классов в пакете и регистрацию команд
    }
    
    /**
     * Регистрирует команду
     * @param commandClass класс с аннотацией @Command
     */
    public void registerCommand(Class<?> commandClass) {
        try {
            // Проверяем, есть ли у класса аннотация @Command
            if (!commandClass.isAnnotationPresent(Command.class)) {
                logger.warning("Класс " + commandClass.getName() + " не имеет аннотации @Command");
                return;
            }
            
            // Создаем экземпляр команды
            Object commandInstance = createCommandInstance(commandClass);
            
            // Создаем объект с информацией о команде
            CommandInfo commandInfo = new CommandInfo(commandClass, commandInstance);
            
            // Добавляем команду в список
            commands.put(commandInfo.getName().toLowerCase(), commandInfo);
            
            // Добавляем алиасы
            for (String alias : commandInfo.getAliases()) {
                aliasesMap.put(alias.toLowerCase(), commandInfo);
            }
            
            // Ищем и регистрируем подкоманды
            registerSubcommands(commandInfo);
            
            // Регистрируем команду в Bukkit
            registerBukkitCommand(commandInfo);
            
            logger.info("Команда " + commandInfo.getName() + " зарегистрирована");
        } catch (Exception e) {
            logger.severe("Ошибка при регистрации команды " + commandClass.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Создает экземпляр команды
     * @param commandClass класс команды
     * @return экземпляр команды
     */
    private Object createCommandInstance(Class<?> commandClass) throws Exception {
        try {
            // Проверяем, есть ли конструктор с FCore
            Constructor<?> constructor = commandClass.getDeclaredConstructor(FCore.class);
            return constructor.newInstance(plugin);
        } catch (NoSuchMethodException e) {
            // Если нет, пробуем создать без параметров
            Object instance = commandClass.getDeclaredConstructor().newInstance();
            
            // Внедряем зависимости
            dependencyContainer.injectDependencies(instance);
            
            return instance;
        }
    }
    
    /**
     * Ищет и регистрирует подкоманды
     * @param commandInfo информация о команде
     */
    private void registerSubcommands(CommandInfo commandInfo) {
        List<SubcommandInfo> cmdSubcommands = new ArrayList<>();
        
        // Перебираем все методы класса
        for (Method method : commandInfo.getCommandClass().getDeclaredMethods()) {
            // Проверяем, есть ли у метода аннотация @Subcommand
            if (method.isAnnotationPresent(Subcommand.class)) {
                try {
                    // Создаем объект с информацией о подкоманде
                    SubcommandInfo subcommandInfo = new SubcommandInfo(method, commandInfo);
                    cmdSubcommands.add(subcommandInfo);
                    
                    logger.info("Подкоманда " + commandInfo.getName() + " " + 
                               subcommandInfo.getName() + " зарегистрирована");
                } catch (Exception e) {
                    logger.severe("Ошибка при регистрации подкоманды " + method.getName() + ": " + e.getMessage());
                }
            }
        }
        
        // Сохраняем подкоманды
        subcommands.put(commandInfo, cmdSubcommands);
    }
    
    /**
     * Регистрирует команду в Bukkit
     * @param commandInfo информация о команде
     */
    private void registerBukkitCommand(CommandInfo commandInfo) {
        try {
            PluginCommand pluginCommand = createPluginCommand(commandInfo.getName(), plugin);
            
            if (pluginCommand != null) {
                // Устанавливаем данные из аннотации
                if (!commandInfo.getDescription().isEmpty()) {
                    pluginCommand.setDescription(commandInfo.getDescription());
                }
                
                if (!commandInfo.getUsage().isEmpty()) {
                    pluginCommand.setUsage(commandInfo.getUsage());
                }
                
                if (!commandInfo.getAliases().isEmpty()) {
                    pluginCommand.setAliases(commandInfo.getAliases());
                }
                
                if (commandInfo.getPermission() != null) {
                    pluginCommand.setPermission(commandInfo.getPermission());
                    pluginCommand.setPermissionMessage(commandInfo.getPermissionMessage());
                }
                
                // Создаем и устанавливаем обработчик команды
                CommandHandler handler = new CommandHandler(commandInfo, this);
                pluginCommand.setExecutor(handler);
                pluginCommand.setTabCompleter(handler);
                
                // Регистрируем команду
                getCommandMap().register(plugin.getName().toLowerCase(), pluginCommand);
            }
        } catch (Exception e) {
            logger.severe("Ошибка при регистрации команды в Bukkit: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Создает объект PluginCommand через рефлексию
     * @param name название команды
     * @param plugin экземпляр плагина
     * @return созданный объект или null, если не удалось
     */
    private PluginCommand createPluginCommand(String name, Plugin plugin) {
        try {
            Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            constructor.setAccessible(true);
            return constructor.newInstance(name, plugin);
        } catch (Exception e) {
            logger.severe("Не удалось создать команду '" + name + "': " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Получает CommandMap сервера через рефлексию
     * @return CommandMap или исключение, если не найдено
     */
    private CommandMap getCommandMap() throws Exception {
        SimplePluginManager pluginManager = (SimplePluginManager) Bukkit.getPluginManager();
        Field commandMapField = SimplePluginManager.class.getDeclaredField("commandMap");
        commandMapField.setAccessible(true);
        return (CommandMap) commandMapField.get(pluginManager);
    }
    
    /**
     * Находит подкоманду по названию
     * @param commandInfo родительская команда
     * @param name название подкоманды
     * @return найденная подкоманда или null
     */
    public SubcommandInfo findSubcommand(CommandInfo commandInfo, String name) {
        List<SubcommandInfo> cmdSubcommands = subcommands.get(commandInfo);
        if (cmdSubcommands == null) {
            return null;
        }
        
        name = name.toLowerCase();
        
        // Ищем по имени
        for (SubcommandInfo subcommand : cmdSubcommands) {
            if (subcommand.getName().equalsIgnoreCase(name)) {
                return subcommand;
            }
        }
        
        // Ищем по алиасам
        for (SubcommandInfo subcommand : cmdSubcommands) {
            for (String alias : subcommand.getAliases()) {
                if (alias.equalsIgnoreCase(name)) {
                    return subcommand;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Получает список подкоманд для команды
     * @param commandInfo команда
     * @return список подкоманд
     */
    public List<SubcommandInfo> getSubcommands(CommandInfo commandInfo) {
        List<SubcommandInfo> cmdSubcommands = subcommands.get(commandInfo);
        if (cmdSubcommands == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(cmdSubcommands);
    }
    
    /**
     * Выполняет команду
     * @param sender отправитель
     * @param commandInfo информация о команде
     * @param args аргументы
     * @return true, если команда выполнена
     */
    public boolean executeCommand(CommandSender sender, CommandInfo commandInfo, String[] args) {
        // Проверяем, нужно ли выполнять команду только от имени игрока
        if (commandInfo.isPlayerOnly() && !(sender instanceof Player)) {
            sender.sendMessage(commandInfo.getPlayerOnlyMessage());
            return true;
        }
        
        // Проверяем права на команду
        String permission = commandInfo.getPermission();
        if (permission != null && !sender.hasPermission(permission)) {
            sender.sendMessage(commandInfo.getPermissionMessage());
            return true;
        }
        
        // Если первый аргумент есть, пытаемся найти подкоманду
        if (args.length > 0) {
            SubcommandInfo subcommand = findSubcommand(commandInfo, args[0]);
            if (subcommand != null) {
                return executeSubcommand(sender, commandInfo, subcommand, args);
            }
        }
        
        // Здесь можно добавить общую логику выполнения команды
        // В этой базовой реализации просто показываем список подкоманд
        
        List<SubcommandInfo> cmdSubcommands = subcommands.get(commandInfo);
        if (cmdSubcommands != null && !cmdSubcommands.isEmpty()) {
            sender.sendMessage("§6=== Помощь по команде " + commandInfo.getName() + " ===");
            for (SubcommandInfo subcommand : cmdSubcommands) {
                // Показываем только те подкоманды, на которые есть права
                if (subcommand.getPermission() == null || sender.hasPermission(subcommand.getPermission())) {
                    sender.sendMessage("§e" + subcommand.getUsage() + " §7- " + subcommand.getDescription());
                }
            }
            return true;
        }
        
        // Если нет подкоманд, пытаемся показать справку
        sender.sendMessage("§eКоманда: §6" + commandInfo.getName());
        sender.sendMessage("§eОписание: §7" + commandInfo.getDescription());
        sender.sendMessage("§eИспользование: §7" + commandInfo.getUsage());
        
        return true;
    }
    
    /**
     * Выполняет подкоманду
     * @param sender отправитель
     * @param commandInfo родительская команда
     * @param subcommandInfo подкоманда
     * @param args аргументы (первый аргумент - название подкоманды)
     * @return true, если подкоманда выполнена
     */
    public boolean executeSubcommand(CommandSender sender, CommandInfo commandInfo, SubcommandInfo subcommandInfo, String[] args) {
        // Проверяем, нужно ли выполнять подкоманду только от имени игрока
        if (subcommandInfo.isPlayerOnly() && !(sender instanceof Player)) {
            sender.sendMessage(subcommandInfo.getPlayerOnlyMessage());
            return true;
        }
        
        // Проверяем права на подкоманду
        String permission = subcommandInfo.getPermission();
        if (permission != null && !sender.hasPermission(permission)) {
            sender.sendMessage(subcommandInfo.getPermissionMessage());
            return true;
        }
        
        // Проверяем количество аргументов
        int argCount = args.length - 1; // Не считаем саму подкоманду
        if (argCount < subcommandInfo.getMinArgs()) {
            sender.sendMessage("§cНедостаточно аргументов. Используйте: " + subcommandInfo.getUsage());
            return true;
        }
        
        if (subcommandInfo.getMaxArgs() >= 0 && argCount > subcommandInfo.getMaxArgs()) {
            sender.sendMessage("§cСлишком много аргументов. Используйте: " + subcommandInfo.getUsage());
            return true;
        }
        
        // Преобразуем аргументы и вызываем метод
        try {
            // Создаем массив аргументов для метода
            // Первый аргумент - отправитель команды
            Object[] methodArgs = new Object[subcommandInfo.getMethod().getParameterCount()];
            methodArgs[0] = sender;
            
            // Преобразуем остальные аргументы
            Parameter[] parameters = subcommandInfo.getMethod().getParameters();
            
            int paramIndex = 1; // Начинаем с 1, так как первый параметр - отправитель
            int argIndex = 1;   // Начинаем с 1, так как args[0] - название подкоманды
            
            for (; paramIndex < parameters.length && argIndex < args.length; paramIndex++) {
                Parameter param = parameters[paramIndex];
                Class<?> paramType = param.getType();
                
                // Если параметр примитивный или строка
                if (paramType.isPrimitive() || paramType == String.class) {
                    // Преобразуем аргумент в нужный тип
                    methodArgs[paramIndex] = convertArgument(args[argIndex], paramType);
                    argIndex++;
                } else if (paramType == Player.class) {
                    // Если параметр - игрок, ищем по имени
                    Player player = Bukkit.getPlayer(args[argIndex]);
                    if (player == null) {
                        sender.sendMessage("§cИгрок " + args[argIndex] + " не найден");
                        return true;
                    }
                    methodArgs[paramIndex] = player;
                    argIndex++;
                }
                // Можно добавить другие типы
            }
            
            // Вызываем метод подкоманды
            subcommandInfo.getMethod().invoke(commandInfo.getCommandInstance(), methodArgs);
            return true;
        } catch (Exception e) {
            sender.sendMessage("§cОшибка при выполнении команды: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Конвертирует строковый аргумент в нужный тип
     * @param arg строковое значение
     * @param type целевой тип
     * @return объект нужного типа
     */
    private Object convertArgument(String arg, Class<?> type) {
        if (type == String.class) {
            return arg;
        } else if (type == int.class || type == Integer.class) {
            return Integer.parseInt(arg);
        } else if (type == double.class || type == Double.class) {
            return Double.parseDouble(arg);
        } else if (type == float.class || type == Float.class) {
            return Float.parseFloat(arg);
        } else if (type == long.class || type == Long.class) {
            return Long.parseLong(arg);
        } else if (type == boolean.class || type == Boolean.class) {
            return Boolean.parseBoolean(arg);
        } else if (type == Player.class) {
            return Bukkit.getPlayerExact(arg);
        }
        
        // Если тип не поддерживается, возвращаем строку
        return arg;
    }
    
    /**
     * Получает табкомплитер для подкоманды
     * @param sender отправитель
     * @param subcommandInfo информация о подкоманде
     * @param args аргументы (включая название подкоманды)
     * @return список предложений для автодополнения
     */
    public List<String> getSubcommandTabCompleter(CommandSender sender, SubcommandInfo subcommandInfo, String[] args) {
        // Уже введена подкоманда, теперь нужно подсказать аргументы
        
        // Получаем информацию о параметрах
        Method method = subcommandInfo.getMethod();
        Parameter[] parameters = method.getParameters();
        
        // Если первый параметр - CommandSender или Player, пропускаем его
        int skipParams = 0;
        if (parameters.length > 0) {
            if (parameters[0].getType() == CommandSender.class || parameters[0].getType() == Player.class) {
                skipParams = 1;
            }
        }
        
        // Определяем, какой по счету аргумент вводится
        int argIndex = args.length - 2; // -1 за подкоманду, -1 за текущий вводимый аргумент
        
        // Если индекс аргумента больше, чем количество параметров, ничего не предлагаем
        if (argIndex >= parameters.length - skipParams) {
            return Collections.emptyList();
        }
        
        // Получаем текущий вводимый параметр
        Parameter param = parameters[argIndex + skipParams];
        Class<?> paramType = param.getType();
        
        // Предлагаем значения в зависимости от типа
        if (paramType == Player.class) {
            // Подсказываем имена игроков
            List<String> playerNames = new ArrayList<>();
            String prefix = args[args.length - 1].toLowerCase();
            
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(prefix)) {
                    playerNames.add(player.getName());
                }
            }
            
            return playerNames;
        } else if (paramType == boolean.class || paramType == Boolean.class) {
            // Для булевых значений предлагаем true/false
            List<String> boolValues = new ArrayList<>();
            String prefix = args[args.length - 1].toLowerCase();
            
            if ("true".startsWith(prefix)) {
                boolValues.add("true");
            }
            if ("false".startsWith(prefix)) {
                boolValues.add("false");
            }
            
            return boolValues;
        }
        
        // Для остальных типов не предлагаем ничего
        return Collections.emptyList();
    }
    
    /**
     * Регистрирует все стандартные команды ядра
     */
    private void registerCoreCommands() {
        registerCommand(FCoreCommand.class);
        registerCommand(dev.flaymie.fcore.commands.ActionDebugCommand.class);
        registerCommand(dev.flaymie.fcore.commands.DebugCommand.class);
        registerCommand(dev.flaymie.fcore.commands.PermissionCommand.class);
        registerCommand(dev.flaymie.fcore.commands.PluginCommand.class);
        registerCommand(dev.flaymie.fcore.commands.SecurityCommand.class);
        
        logger.info("Все базовые команды FCore зарегистрированы");
    }
} 