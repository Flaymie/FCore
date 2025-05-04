package dev.flaymie.fcore.utils.math;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Утилиты для математических расчетов
 */
public class MathUtils {
    private static final Random random = new Random();
    
    /**
     * Получает случайное число в диапазоне
     * @param min минимальное значение (включительно)
     * @param max максимальное значение (включительно)
     * @return случайное число
     */
    public static int randomInt(int min, int max) {
        if (min > max) {
            int temp = min;
            min = max;
            max = temp;
        }
        return min + random.nextInt(max - min + 1);
    }
    
    /**
     * Получает случайное число с плавающей точкой в диапазоне
     * @param min минимальное значение (включительно)
     * @param max максимальное значение (включительно)
     * @return случайное число
     */
    public static double randomDouble(double min, double max) {
        if (min > max) {
            double temp = min;
            min = max;
            max = temp;
        }
        return min + (max - min) * random.nextDouble();
    }
    
    /**
     * Проверяет, попадает ли число в диапазон
     * @param value проверяемое число
     * @param min минимальное значение (включительно)
     * @param max максимальное значение (включительно)
     * @return true, если число в диапазоне
     */
    public static boolean isInRange(double value, double min, double max) {
        if (min > max) {
            double temp = min;
            min = max;
            max = temp;
        }
        return value >= min && value <= max;
    }
    
    /**
     * Ограничивает число указанным диапазоном
     * @param value число для ограничения
     * @param min минимальное значение
     * @param max максимальное значение
     * @return ограниченное число
     */
    public static double clamp(double value, double min, double max) {
        if (min > max) {
            double temp = min;
            min = max;
            max = temp;
        }
        return Math.max(min, Math.min(max, value));
    }
    
    /**
     * Ограничивает целое число указанным диапазоном
     * @param value число для ограничения
     * @param min минимальное значение
     * @param max максимальное значение
     * @return ограниченное число
     */
    public static int clamp(int value, int min, int max) {
        if (min > max) {
            int temp = min;
            min = max;
            max = temp;
        }
        return Math.max(min, Math.min(max, value));
    }
    
    /**
     * Интерполирует между двумя значениями
     * @param start начальное значение
     * @param end конечное значение
     * @param t коэффициент интерполяции (0-1)
     * @return интерполированное значение
     */
    public static double lerp(double start, double end, double t) {
        return start + (end - start) * clamp(t, 0, 1);
    }
    
    /**
     * Рассчитывает дистанцию между двумя точками в 2D
     * @param x1 X первой точки
     * @param y1 Y первой точки
     * @param x2 X второй точки
     * @param y2 Y второй точки
     * @return расстояние
     */
    public static double distance2D(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    /**
     * Рассчитывает дистанцию между двумя точками в 3D
     * @param x1 X первой точки
     * @param y1 Y первой точки
     * @param z1 Z первой точки
     * @param x2 X второй точки
     * @param y2 Y второй точки
     * @param z2 Z второй точки
     * @return расстояние
     */
    public static double distance3D(double x1, double y1, double z1, double x2, double y2, double z2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
    
    /**
     * Возвращает угол между двумя точками в градусах (0-360)
     * @param x1 X первой точки
     * @param z1 Z первой точки
     * @param x2 X второй точки
     * @param z2 Z второй точки
     * @return угол в градусах
     */
    public static double angleBetweenPoints(double x1, double z1, double x2, double z2) {
        double dx = x2 - x1;
        double dz = z2 - z1;
        double angle = Math.toDegrees(Math.atan2(dz, dx));
        
        if (angle < 0) {
            angle += 360;
        }
        
        return angle;
    }
    
    /**
     * Создает список точек, образующих окружность
     * @param center центр окружности
     * @param radius радиус
     * @param points количество точек
     * @return список точек
     */
    public static List<Location> getCirclePoints(Location center, double radius, int points) {
        List<Location> locations = new ArrayList<>();
        if (center == null || center.getWorld() == null || points <= 0) return locations;
        
        World world = center.getWorld();
        double angleIncrement = 2 * Math.PI / points;
        
        for (int i = 0; i < points; i++) {
            double angle = i * angleIncrement;
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            
            locations.add(new Location(world, x, center.getY(), z));
        }
        
        return locations;
    }
    
    /**
     * Создает список точек, образующих сферу
     * @param center центр сферы
     * @param radius радиус
     * @param points приблизительное количество точек
     * @return список точек
     */
    public static List<Location> getSpherePoints(Location center, double radius, int points) {
        List<Location> locations = new ArrayList<>();
        if (center == null || center.getWorld() == null || points <= 0) return locations;
        
        World world = center.getWorld();
        int pointsPerCircle = Math.max(8, points / 8);
        int circles = Math.max(4, points / pointsPerCircle);
        
        for (int i = 0; i < circles; i++) {
            double phi = Math.PI * i / (circles - 1);
            double y = center.getY() + radius * Math.cos(phi);
            double circleRadius = radius * Math.sin(phi);
            
            for (int j = 0; j < pointsPerCircle; j++) {
                double theta = 2 * Math.PI * j / pointsPerCircle;
                double x = center.getX() + circleRadius * Math.cos(theta);
                double z = center.getZ() + circleRadius * Math.sin(theta);
                
                locations.add(new Location(world, x, y, z));
            }
        }
        
        return locations;
    }
    
    /**
     * Рассчитывает скалярное произведение векторов
     * @param v1 первый вектор
     * @param v2 второй вектор
     * @return скалярное произведение
     */
    public static double dotProduct(Vector v1, Vector v2) {
        if (v1 == null || v2 == null) return 0;
        return v1.getX() * v2.getX() + v1.getY() * v2.getY() + v1.getZ() * v2.getZ();
    }
    
    /**
     * Рассчитывает векторное произведение векторов
     * @param v1 первый вектор
     * @param v2 второй вектор
     * @return новый вектор - результат векторного произведения
     */
    public static Vector crossProduct(Vector v1, Vector v2) {
        if (v1 == null || v2 == null) return new Vector(0, 0, 0);
        
        double x = v1.getY() * v2.getZ() - v1.getZ() * v2.getY();
        double y = v1.getZ() * v2.getX() - v1.getX() * v2.getZ();
        double z = v1.getX() * v2.getY() - v1.getY() * v2.getX();
        
        return new Vector(x, y, z);
    }
    
    /**
     * Вычисляет угол между векторами в радианах
     * @param v1 первый вектор
     * @param v2 второй вектор
     * @return угол в радианах
     */
    public static double angleBetweenVectors(Vector v1, Vector v2) {
        if (v1 == null || v2 == null) return 0;
        
        double dot = dotProduct(v1, v2);
        double lengths = v1.length() * v2.length();
        
        if (lengths < 1e-6) return 0;
        
        return Math.acos(clamp(dot / lengths, -1, 1));
    }
    
    /**
     * Проверяет, находится ли точка внутри сферы
     * @param point проверяемая точка
     * @param center центр сферы
     * @param radius радиус сферы
     * @return true, если точка внутри сферы
     */
    public static boolean isInSphere(Location point, Location center, double radius) {
        if (point == null || center == null || point.getWorld() != center.getWorld()) return false;
        
        return point.distanceSquared(center) <= radius * radius;
    }
    
    /**
     * Создает направление от одной точки к другой
     * @param from исходная точка
     * @param to целевая точка
     * @return нормализованный вектор направления
     */
    public static Vector getDirection(Location from, Location to) {
        if (from == null || to == null || from.getWorld() != to.getWorld()) {
            return new Vector(0, 0, 0);
        }
        
        return to.toVector().subtract(from.toVector()).normalize();
    }
    
    /**
     * Проверяет, пересекаются ли сферы
     * @param center1 центр первой сферы
     * @param radius1 радиус первой сферы
     * @param center2 центр второй сферы
     * @param radius2 радиус второй сферы
     * @return true, если сферы пересекаются
     */
    public static boolean doSpheresIntersect(Location center1, double radius1, Location center2, double radius2) {
        if (center1 == null || center2 == null || center1.getWorld() != center2.getWorld()) return false;
        
        double distanceSquared = center1.distanceSquared(center2);
        double radiusSum = radius1 + radius2;
        
        return distanceSquared <= radiusSum * radiusSum;
    }
    
    /**
     * Вычисляет случайную точку внутри прямоугольной области
     * @param min нижний левый угол области
     * @param max верхний правый угол области
     * @return случайная точка внутри области
     */
    public static Location getRandomLocationInBox(Location min, Location max) {
        if (min == null || max == null || min.getWorld() != max.getWorld()) return null;
        
        World world = min.getWorld();
        double x = randomDouble(min.getX(), max.getX());
        double y = randomDouble(min.getY(), max.getY());
        double z = randomDouble(min.getZ(), max.getZ());
        
        return new Location(world, x, y, z);
    }
    
    /**
     * Проектирует точку на линию, заданную двумя точками
     * @param point проектируемая точка
     * @param lineStart начало линии
     * @param lineEnd конец линии
     * @return спроектированная точка
     */
    public static Location projectPointToLine(Location point, Location lineStart, Location lineEnd) {
        if (point == null || lineStart == null || lineEnd == null || 
            point.getWorld() != lineStart.getWorld() || lineStart.getWorld() != lineEnd.getWorld()) {
            return null;
        }
        
        Vector v1 = point.toVector().subtract(lineStart.toVector());
        Vector v2 = lineEnd.toVector().subtract(lineStart.toVector());
        
        double lengthSquared = v2.lengthSquared();
        if (lengthSquared < 1e-6) return lineStart.clone();
        
        double t = clamp(dotProduct(v1, v2) / lengthSquared, 0, 1);
        
        Vector projection = lineStart.toVector().add(v2.multiply(t));
        return projection.toLocation(point.getWorld());
    }
} 