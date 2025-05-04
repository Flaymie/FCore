package dev.flaymie.fcore.core.action;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import java.util.concurrent.CompletableFuture;

/**
 * Действие для отображения частиц
 */
public class ParticleAction extends AbstractAction {
    
    private final Particle particle;
    private final int count;
    private final double offsetX;
    private final double offsetY;
    private final double offsetZ;
    private final double speed;
    private final boolean usePlayerLocation;
    private final Location customLocation;
    
    /**
     * Создает новое действие с отображением частиц в локации игрока
     *
     * @param particle частица
     * @param count количество частиц
     * @param offsetX смещение по X
     * @param offsetY смещение по Y
     * @param offsetZ смещение по Z
     * @param speed скорость частиц
     */
    public ParticleAction(Particle particle, int count, double offsetX, double offsetY, double offsetZ, double speed) {
        super("Particle", "Отображает частицы " + particle.name(), false);
        this.particle = particle;
        this.count = count;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.speed = speed;
        this.usePlayerLocation = true;
        this.customLocation = null;
    }
    
    /**
     * Создает новое действие с отображением частиц в указанной локации
     *
     * @param particle частица
     * @param location локация
     * @param count количество частиц
     * @param offsetX смещение по X
     * @param offsetY смещение по Y
     * @param offsetZ смещение по Z
     * @param speed скорость частиц
     */
    public ParticleAction(Particle particle, Location location, int count, double offsetX, double offsetY, double offsetZ, double speed) {
        super("Particle", "Отображает частицы " + particle.name() + " в указанной локации", false);
        this.particle = particle;
        this.count = count;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.speed = speed;
        this.usePlayerLocation = false;
        this.customLocation = location;
    }
    
    /**
     * Создает новое действие с отображением частиц в локации игрока с простыми параметрами
     *
     * @param particle частица
     */
    public ParticleAction(Particle particle) {
        this(particle, 10, 0.5, 0.5, 0.5, 0.1);
    }
    
    /**
     * Создает новое действие с отображением частиц в локации игрока с простыми параметрами
     *
     * @param particle частица
     * @param count количество частиц
     */
    public ParticleAction(Particle particle, int count) {
        this(particle, count, 0.5, 0.5, 0.5, 0.1);
    }
    
    @Override
    public CompletableFuture<Void> execute(Player player, ActionContext context) {
        Location location = usePlayerLocation ? player.getLocation() : customLocation;
        
        // Если не удалось получить локацию
        if (location == null) {
            CompletableFuture<Void> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalStateException("Не удалось получить локацию для отображения частиц"));
            return future;
        }
        
        player.spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, speed);
        return CompletableFuture.completedFuture(null);
    }
    
    /**
     * Получает тип частицы
     *
     * @return тип частицы
     */
    public Particle getParticle() {
        return particle;
    }
    
    /**
     * Получает количество частиц
     *
     * @return количество частиц
     */
    public int getCount() {
        return count;
    }
} 