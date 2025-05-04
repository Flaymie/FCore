package dev.flaymie.fcore.api.event;

import org.bukkit.entity.Player;

/**
 * Пример кастомного события, связанного с игроком
 * Может быть использован для системы сервисов
 */
public class PlayerServiceEvent extends FCoreEvent {
    
    public enum ServiceAction {
        CREATED,
        LOADED,
        SAVED,
        DESTROYED
    }
    
    private final Player player;
    private final ServiceAction action;
    private final String serviceName;
    private Object data;
    
    /**
     * Создает событие, связанное с сервисом игрока
     * @param player игрок
     * @param action действие
     * @param serviceName имя сервиса
     */
    public PlayerServiceEvent(Player player, ServiceAction action, String serviceName) {
        this(player, action, serviceName, null);
    }
    
    /**
     * Создает событие, связанное с сервисом игрока
     * @param player игрок
     * @param action действие
     * @param serviceName имя сервиса
     * @param data данные события
     */
    public PlayerServiceEvent(Player player, ServiceAction action, String serviceName, Object data) {
        super();
        this.player = player;
        this.action = action;
        this.serviceName = serviceName;
        this.data = data;
    }
    
    /**
     * Создает асинхронное событие, связанное с сервисом игрока
     * @param player игрок
     * @param action действие
     * @param serviceName имя сервиса
     * @param data данные события
     * @param isAsync флаг асинхронного выполнения
     */
    public PlayerServiceEvent(Player player, ServiceAction action, String serviceName, Object data, boolean isAsync) {
        super(isAsync);
        this.player = player;
        this.action = action;
        this.serviceName = serviceName;
        this.data = data;
    }
    
    /**
     * Получает игрока, связанного с событием
     * @return игрок
     */
    public Player getPlayer() {
        return player;
    }
    
    /**
     * Получает действие сервиса
     * @return действие
     */
    public ServiceAction getAction() {
        return action;
    }
    
    /**
     * Получает имя сервиса
     * @return имя сервиса
     */
    public String getServiceName() {
        return serviceName;
    }
    
    /**
     * Получает данные события
     * @return данные или null, если данные не установлены
     */
    public Object getData() {
        return data;
    }
    
    /**
     * Устанавливает данные события
     * @param data новые данные
     */
    public void setData(Object data) {
        this.data = data;
    }
    
    /**
     * Получает данные приведенные к указанному типу
     * @param clazz класс данных
     * @param <T> тип данных
     * @return данные указанного типа или null, если данные не установлены или не могут быть приведены
     */
    @SuppressWarnings("unchecked")
    public <T> T getDataAs(Class<T> clazz) {
        if (data == null) {
            return null;
        }
        
        if (clazz.isInstance(data)) {
            return (T) data;
        }
        
        return null;
    }
    
    @Override
    public String toString() {
        return "PlayerServiceEvent{" +
               "player=" + player.getName() +
               ", action=" + action +
               ", serviceName='" + serviceName + '\'' +
               ", data=" + data +
               ", cancelled=" + isCancelled() +
               '}';
    }
} 