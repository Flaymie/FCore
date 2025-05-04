package dev.flaymie.fcore.utils.location;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Утилиты для работы с локациями и мирами
 */
public class LocationUtils {
    private static final Random random = new Random();

    /**
     * Преобразует локацию в строку формата "мир,x,y,z,yaw,pitch"
     * @param location локация
     * @return строковое представление
     */
    public static String toString(Location location) {
        if (location == null || location.getWorld() == null) return null;
        
        return String.format("%s,%f,%f,%f,%f,%f",
                location.getWorld().getName(),
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getYaw(),
                location.getPitch()
        );
    }
    
    /**
     * Преобразует строку формата "мир,x,y,z[,yaw,pitch]" в локацию
     * @param str строка
     * @return локация или null, если формат некорректен
     */
    public static Location fromString(String str) {
        if (str == null || str.isEmpty()) return null;
        
        String[] parts = str.split(",");
        if (parts.length < 4) return null;
        
        try {
            World world = Bukkit.getWorld(parts[0]);
            if (world == null) return null;
            
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);
            
            Location location = new Location(world, x, y, z);
            
            if (parts.length >= 6) {
                float yaw = Float.parseFloat(parts[4]);
                float pitch = Float.parseFloat(parts[5]);
                location.setYaw(yaw);
                location.setPitch(pitch);
            }
            
            return location;
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Сохраняет локацию в конфигурационную секцию
     * @param section секция конфигурации
     * @param location локация
     */
    public static void saveToConfig(ConfigurationSection section, Location location) {
        if (section == null || location == null || location.getWorld() == null) return;
        
        section.set("world", location.getWorld().getName());
        section.set("x", location.getX());
        section.set("y", location.getY());
        section.set("z", location.getZ());
        section.set("yaw", location.getYaw());
        section.set("pitch", location.getPitch());
    }
    
    /**
     * Загружает локацию из конфигурационной секции
     * @param section секция конфигурации
     * @return локация или null, если не удалось загрузить
     */
    public static Location loadFromConfig(ConfigurationSection section) {
        if (section == null) return null;
        
        String worldName = section.getString("world");
        if (worldName == null) return null;
        
        World world = Bukkit.getWorld(worldName);
        if (world == null) return null;
        
        double x = section.getDouble("x");
        double y = section.getDouble("y");
        double z = section.getDouble("z");
        float yaw = (float) section.getDouble("yaw", 0.0);
        float pitch = (float) section.getDouble("pitch", 0.0);
        
        return new Location(world, x, y, z, yaw, pitch);
    }
    
    /**
     * Проверяет, находятся ли две локации в одном и том же мире
     * @param loc1 первая локация
     * @param loc2 вторая локация
     * @return true, если локации в одном мире
     */
    public static boolean isSameWorld(Location loc1, Location loc2) {
        if (loc1 == null || loc2 == null) return false;
        if (loc1.getWorld() == null || loc2.getWorld() == null) return false;
        
        return loc1.getWorld().equals(loc2.getWorld());
    }
    
    /**
     * Получает расстояние между двумя локациями, игнорируя ось Y
     * @param loc1 первая локация
     * @param loc2 вторая локация
     * @return расстояние или -1, если локации в разных мирах
     */
    public static double getDistanceXZ(Location loc1, Location loc2) {
        if (!isSameWorld(loc1, loc2)) return -1;
        
        double dx = loc1.getX() - loc2.getX();
        double dz = loc1.getZ() - loc2.getZ();
        
        return Math.sqrt(dx * dx + dz * dz);
    }
    
    /**
     * Получает расстояние между двумя локациями в трехмерном пространстве
     * @param loc1 первая локация
     * @param loc2 вторая локация
     * @return расстояние или -1, если локации в разных мирах
     */
    public static double getDistance3D(Location loc1, Location loc2) {
        if (!isSameWorld(loc1, loc2)) return -1;
        
        return loc1.distance(loc2);
    }
    
    /**
     * Получает случайную локацию в заданном радиусе от центральной локации
     * @param center центральная локация
     * @param radiusX радиус по X
     * @param radiusY радиус по Y
     * @param radiusZ радиус по Z
     * @return случайная локация
     */
    public static Location getRandomLocation(Location center, double radiusX, double radiusY, double radiusZ) {
        if (center == null || center.getWorld() == null) return null;
        
        double x = center.getX() + (random.nextDouble() * 2 - 1) * radiusX;
        double y = center.getY() + (random.nextDouble() * 2 - 1) * radiusY;
        double z = center.getZ() + (random.nextDouble() * 2 - 1) * radiusZ;
        
        return new Location(center.getWorld(), x, y, z, center.getYaw(), center.getPitch());
    }
    
    /**
     * Получает случайную локацию на поверхности в заданном радиусе от центральной локации
     * @param center центральная локация
     * @param radius радиус
     * @return случайная локация на поверхности
     */
    public static Location getRandomSurfaceLocation(Location center, double radius) {
        if (center == null || center.getWorld() == null) return null;
        
        double angle = random.nextDouble() * 2 * Math.PI;
        double r = random.nextDouble() * radius;
        
        double x = center.getX() + r * Math.cos(angle);
        double z = center.getZ() + r * Math.sin(angle);
        
        // Найти верхний непустой блок
        int y = center.getWorld().getHighestBlockYAt((int) x, (int) z);
        
        return new Location(center.getWorld(), x, y + 1, z);
    }
    
    /**
     * Получает список блоков в кубической области
     * @param center центральная локация
     * @param radius радиус области
     * @return список блоков
     */
    public static List<Block> getBlocksInRadius(Location center, int radius) {
        List<Block> blocks = new ArrayList<>();
        if (center == null || center.getWorld() == null) return blocks;
        
        World world = center.getWorld();
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        
        for (int x = cx - radius; x <= cx + radius; x++) {
            for (int y = cy - radius; y <= cy + radius; y++) {
                for (int z = cz - radius; z <= cz + radius; z++) {
                    blocks.add(world.getBlockAt(x, y, z));
                }
            }
        }
        
        return blocks;
    }
    
    /**
     * Получает список блоков в сферической области
     * @param center центральная локация
     * @param radius радиус области
     * @return список блоков
     */
    public static List<Block> getBlocksInSphere(Location center, int radius) {
        List<Block> blocks = new ArrayList<>();
        if (center == null || center.getWorld() == null) return blocks;
        
        World world = center.getWorld();
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        int rSquared = radius * radius;
        
        for (int x = cx - radius; x <= cx + radius; x++) {
            for (int y = cy - radius; y <= cy + radius; y++) {
                for (int z = cz - radius; z <= cz + radius; z++) {
                    int dx = x - cx;
                    int dy = y - cy;
                    int dz = z - cz;
                    
                    if (dx * dx + dy * dy + dz * dz <= rSquared) {
                        blocks.add(world.getBlockAt(x, y, z));
                    }
                }
            }
        }
        
        return blocks;
    }
    
    /**
     * Получает список сущностей в радиусе от локации
     * @param location центральная локация
     * @param radius радиус
     * @return список сущностей
     */
    public static Collection<Entity> getEntitiesInRadius(Location location, double radius) {
        if (location == null || location.getWorld() == null) return new ArrayList<>();
        
        return location.getWorld().getNearbyEntities(location, radius, radius, radius);
    }
    
    /**
     * Получает список игроков в радиусе от локации
     * @param location центральная локация
     * @param radius радиус
     * @return список игроков
     */
    public static List<Player> getPlayersInRadius(Location location, double radius) {
        List<Player> players = new ArrayList<>();
        if (location == null || location.getWorld() == null) return players;
        
        double radiusSquared = radius * radius;
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getWorld().equals(location.getWorld())) {
                if (player.getLocation().distanceSquared(location) <= radiusSquared) {
                    players.add(player);
                }
            }
        }
        
        return players;
    }
    
    /**
     * Проверяет, находится ли локация внутри кубической области
     * @param location проверяемая локация
     * @param corner1 первый угол области
     * @param corner2 второй угол области
     * @return true, если локация внутри области
     */
    public static boolean isInCuboid(Location location, Location corner1, Location corner2) {
        if (location == null || corner1 == null || corner2 == null) return false;
        if (!isSameWorld(location, corner1) || !isSameWorld(location, corner2)) return false;
        
        double minX = Math.min(corner1.getX(), corner2.getX());
        double minY = Math.min(corner1.getY(), corner2.getY());
        double minZ = Math.min(corner1.getZ(), corner2.getZ());
        
        double maxX = Math.max(corner1.getX(), corner2.getX());
        double maxY = Math.max(corner1.getY(), corner2.getY());
        double maxZ = Math.max(corner1.getZ(), corner2.getZ());
        
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        
        return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
    }
    
    /**
     * Получает центр блока
     * @param location локация
     * @return центр блока
     */
    public static Location getBlockCenter(Location location) {
        if (location == null || location.getWorld() == null) return null;
        
        return new Location(
                location.getWorld(),
                location.getBlockX() + 0.5,
                location.getBlockY() + 0.5,
                location.getBlockZ() + 0.5,
                location.getYaw(),
                location.getPitch()
        );
    }
} 