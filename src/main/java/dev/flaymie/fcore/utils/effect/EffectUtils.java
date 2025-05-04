package dev.flaymie.fcore.utils.effect;

import dev.flaymie.fcore.FCore;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * Утилиты для работы с визуальными эффектами и партиклами
 */
public class EffectUtils {
    private static final FCore plugin = FCore.getInstance();
    
    /**
     * Создает частицы в указанной локации
     * @param location локация
     * @param particle тип частиц
     * @param count количество частиц
     * @param offsetX разброс по X
     * @param offsetY разброс по Y
     * @param offsetZ разброс по Z
     * @param speed скорость частиц
     */
    public static void spawnParticle(Location location, Particle particle, int count, 
                                     double offsetX, double offsetY, double offsetZ, 
                                     double speed) {
        if (location == null || location.getWorld() == null || particle == null) return;
        
        location.getWorld().spawnParticle(
                particle,
                location.getX(), location.getY(), location.getZ(),
                count,
                offsetX, offsetY, offsetZ,
                speed
        );
    }
    
    /**
     * Создает частицы в указанной локации с настройками по умолчанию
     * @param location локация
     * @param particle тип частиц
     * @param count количество частиц
     */
    public static void spawnParticle(Location location, Particle particle, int count) {
        spawnParticle(location, particle, count, 0, 0, 0, 0);
    }
    
    /**
     * Создает цветные частицы пыли
     * @param location локация
     * @param color цвет
     * @param count количество частиц
     * @param offsetX разброс по X
     * @param offsetY разброс по Y
     * @param offsetZ разброс по Z
     */
    public static void spawnColoredParticle(Location location, Color color, int count,
                                           double offsetX, double offsetY, double offsetZ) {
        if (location == null || location.getWorld() == null || color == null) return;
        
        Particle.DustOptions dustOptions = new Particle.DustOptions(color, 1.0f);
        location.getWorld().spawnParticle(
                Particle.REDSTONE,
                location,
                count,
                offsetX, offsetY, offsetZ,
                0,
                dustOptions
        );
    }
    
    /**
     * Создает цветные частицы пыли с настройками по умолчанию
     * @param location локация
     * @param color цвет
     * @param count количество частиц
     */
    public static void spawnColoredParticle(Location location, Color color, int count) {
        spawnColoredParticle(location, color, count, 0, 0, 0);
    }
    
    /**
     * Создает линию из частиц между двумя точками
     * @param start начальная точка
     * @param end конечная точка
     * @param particle тип частиц
     * @param density плотность частиц (количество на блок)
     */
    public static void drawLine(Location start, Location end, Particle particle, double density) {
        if (start == null || end == null || start.getWorld() == null || 
            !start.getWorld().equals(end.getWorld()) || particle == null) return;
        
        double distance = start.distance(end);
        Vector direction = end.toVector().subtract(start.toVector()).normalize();
        
        for (double i = 0; i < distance; i += 1 / density) {
            Location point = start.clone().add(direction.clone().multiply(i));
            spawnParticle(point, particle, 1);
        }
    }
    
    /**
     * Создает линию из цветных частиц пыли между двумя точками
     * @param start начальная точка
     * @param end конечная точка
     * @param color цвет
     * @param density плотность частиц (количество на блок)
     */
    public static void drawColoredLine(Location start, Location end, Color color, double density) {
        if (start == null || end == null || start.getWorld() == null || 
            !start.getWorld().equals(end.getWorld()) || color == null) return;
        
        double distance = start.distance(end);
        Vector direction = end.toVector().subtract(start.toVector()).normalize();
        
        for (double i = 0; i < distance; i += 1 / density) {
            Location point = start.clone().add(direction.clone().multiply(i));
            spawnColoredParticle(point, color, 1);
        }
    }
    
    /**
     * Создает круг из частиц
     * @param center центр круга
     * @param radius радиус
     * @param particle тип частиц
     * @param density плотность частиц
     */
    public static void drawCircle(Location center, double radius, Particle particle, double density) {
        if (center == null || center.getWorld() == null || particle == null) return;
        
        World world = center.getWorld();
        double increment = (2 * Math.PI) / (density * radius);
        
        for (double angle = 0; angle < 2 * Math.PI; angle += increment) {
            double x = center.getX() + (radius * Math.cos(angle));
            double z = center.getZ() + (radius * Math.sin(angle));
            
            Location location = new Location(world, x, center.getY(), z);
            spawnParticle(location, particle, 1);
        }
    }
    
    /**
     * Создает сферу из частиц
     * @param center центр сферы
     * @param radius радиус
     * @param particle тип частиц
     * @param density плотность частиц
     */
    public static void drawSphere(Location center, double radius, Particle particle, double density) {
        if (center == null || center.getWorld() == null || particle == null) return;
        
        for (double phi = 0; phi <= Math.PI; phi += Math.PI / (density * radius)) {
            double y = center.getY() + (radius * Math.cos(phi));
            double radiusAtHeight = radius * Math.sin(phi);
            
            drawCircle(center.clone().add(0, y - center.getY(), 0), radiusAtHeight, particle, density);
        }
    }
    
    /**
     * Создает спираль из частиц
     * @param center центр спирали
     * @param radius радиус
     * @param height высота
     * @param particle тип частиц
     * @param density плотность частиц
     * @param rotations количество оборотов
     */
    public static void drawSpiral(Location center, double radius, double height, 
                                 Particle particle, double density, double rotations) {
        if (center == null || center.getWorld() == null || particle == null) return;
        
        World world = center.getWorld();
        double totalPoints = density * rotations * 10;
        double increment = (2 * Math.PI * rotations) / totalPoints;
        
        for (double angle = 0; angle < 2 * Math.PI * rotations; angle += increment) {
            double x = center.getX() + (radius * Math.cos(angle));
            double z = center.getZ() + (radius * Math.sin(angle));
            double y = center.getY() + (height * angle / (2 * Math.PI * rotations));
            
            Location location = new Location(world, x, y, z);
            spawnParticle(location, particle, 1);
        }
    }
    
    /**
     * Создает анимацию из частиц, выполняя действие с задержкой
     * @param location начальная локация
     * @param action действие для каждого шага анимации
     * @param duration общая длительность анимации в тиках
     * @param steps количество шагов анимации
     */
    public static void animateParticle(Location location, Consumer<Location> action, 
                                     long duration, int steps) {
        if (location == null || action == null || plugin == null) return;
        
        long delay = duration / steps;
        
        for (int i = 0; i < steps; i++) {
            int step = i;
            new BukkitRunnable() {
                @Override
                public void run() {
                    action.accept(location.clone());
                }
            }.runTaskLater(plugin, delay * step);
        }
    }
    
    /**
     * Проигрывает эффект частиц только для указанных игроков
     * @param location локация
     * @param particle тип частиц
     * @param count количество частиц
     * @param offsetX разброс по X
     * @param offsetY разброс по Y
     * @param offsetZ разброс по Z
     * @param speed скорость частиц
     * @param viewers список игроков, которые увидят частицы
     */
    public static void spawnParticleForPlayers(Location location, Particle particle, int count,
                                              double offsetX, double offsetY, double offsetZ,
                                              double speed, Collection<Player> viewers) {
        if (location == null || particle == null || viewers == null || viewers.isEmpty()) return;
        
        for (Player player : viewers) {
            player.spawnParticle(
                    particle,
                    location.getX(), location.getY(), location.getZ(),
                    count,
                    offsetX, offsetY, offsetZ,
                    speed
            );
        }
    }
    
    /**
     * Создает эффект следа частиц за сущностью
     * @param entity сущность
     * @param particle тип частиц
     * @param delay задержка между частицами в тиках
     * @param duration длительность эффекта в тиках
     */
    public static void createEntityTrail(Entity entity, Particle particle, long delay, long duration) {
        if (entity == null || particle == null || plugin == null) return;
        
        BukkitRunnable runnable = new BukkitRunnable() {
            private long elapsed = 0;
            
            @Override
            public void run() {
                if (elapsed >= duration || !entity.isValid()) {
                    cancel();
                    return;
                }
                
                Location location = entity.getLocation();
                spawnParticle(location, particle, 1);
                
                elapsed += delay;
            }
        };
        
        runnable.runTaskTimer(plugin, 0, delay);
    }
    
    /**
     * Создает эффект взрыва частиц
     * @param location центр взрыва
     * @param particle тип частиц
     * @param count количество частиц
     * @param speed скорость разлета частиц
     */
    public static void createExplosionEffect(Location location, Particle particle, int count, double speed) {
        if (location == null || location.getWorld() == null || particle == null) return;
        
        spawnParticle(location, particle, count, 0.5, 0.5, 0.5, speed);
    }
    
    /**
     * Создает эффект мерцания частиц
     * @param location локация
     * @param particle тип частиц
     * @param count количество частиц
     * @param duration длительность эффекта в тиках
     * @param interval интервал между миганиями в тиках
     */
    public static void createBlinkingEffect(Location location, Particle particle, int count, 
                                           long duration, long interval) {
        if (location == null || particle == null || plugin == null) return;
        
        BukkitRunnable runnable = new BukkitRunnable() {
            private long elapsed = 0;
            private boolean visible = true;
            
            @Override
            public void run() {
                if (elapsed >= duration) {
                    cancel();
                    return;
                }
                
                if (visible) {
                    spawnParticle(location, particle, count);
                }
                
                visible = !visible;
                elapsed += interval;
            }
        };
        
        runnable.runTaskTimer(plugin, 0, interval);
    }
} 