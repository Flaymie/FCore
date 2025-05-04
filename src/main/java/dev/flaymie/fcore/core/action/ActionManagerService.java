package dev.flaymie.fcore.core.action;

import dev.flaymie.fcore.FCore;
import dev.flaymie.fcore.api.service.FCoreService;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import java.util.logging.Level;

/**
 * Сервис для управления действиями
 */
public class ActionManagerService implements FCoreService {
    
    private final FCore plugin;
    private final ActionManager actionManager;
    
    /**
     * Создает новый сервис для менеджера действий
     *
     * @param plugin экземпляр плагина
     * @param actionManager менеджер действий
     */
    public ActionManagerService(FCore plugin, ActionManager actionManager) {
        this.plugin = plugin;
        this.actionManager = actionManager;
    }
    
    @Override
    public void onEnable() {
        plugin.getLogger().info("Инициализация Action-системы...");
        
        // Регистрируем демонстрационные действия
        registerExampleActions();
        
        plugin.getLogger().info("Action-система успешно инициализирована");
    }
    
    @Override
    public void onDisable() {
        plugin.getLogger().info("Отключение Action-системы...");
        
        // Деактивируем все триггеры
        actionManager.deactivateAllTriggers();
        
        plugin.getLogger().info("Action-система успешно отключена");
    }
    
    @Override
    public String getName() {
        return "ActionManager";
    }
    
    /**
     * Регистрирует демонстрационные действия
     */
    private void registerExampleActions() {
        try {
            // Создаем последовательность действий для приветствия
            ActionSequence welcomeSequence = ActionSequence.create("welcome", "Приветствие нового игрока");
            
            welcomeSequence
                .title("&6Добро пожаловать!", "&eНа наш сервер")
                .wait(40)
                .message("&aПривет! &6Добро пожаловать на сервер!")
                .message("&eНадеемся, тебе понравится у нас!")
                .sound(Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            
            // Регистрируем последовательность
            actionManager.registerSequence(welcomeSequence);
            
            // Создаем последовательность с параллельным выполнением
            ActionSequence effectsSequence = ActionSequence.create("effects", "Демонстрация эффектов");
            
            // Создаем несколько параллельных эффектов
            effectsSequence
                .message("&6Начинаем демонстрацию эффектов...")
                .wait(20)
                .parallel(parallel -> parallel
                    .add(new SoundAction(Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1.0f, 0.5f))
                    .add(new ParticleAction(org.bukkit.Particle.FIREWORKS_SPARK, 50, 0.5, 0.5, 0.5, 0.1))
                    .add(ActionSequence.create()
                        .title("&cЭффекты", "&eИдут параллельно")
                        .wait(20)
                        .title("&bКруто", "&aНе правда ли?")))
                .wait(40)
                .message("&aДемонстрация завершена!");
            
            // Регистрируем последовательность
            actionManager.registerSequence(effectsSequence);
            
            plugin.getLogger().info("Зарегистрировано демонстрационное действие: effects");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка при регистрации демонстрационных действий", e);
        }
    }
    
    /**
     * Выполняет действие приветствия для игрока
     *
     * @param player игрок
     */
    public void executeWelcomeAction(Player player) {
        actionManager.executeAction("welcome", player);
    }
} 