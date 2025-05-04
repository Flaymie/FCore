package dev.flaymie.fcore.integration.worldguard;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import dev.flaymie.fcore.FCore;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Класс-обертка для упрощенной работы с регионами WorldGuard
 */
public class RegionManager {
    
    private final FCore plugin;
    private final boolean worldGuardAvailable;
    
    public RegionManager(FCore plugin) {
        this.plugin = plugin;
        this.worldGuardAvailable = isWorldGuardAvailable();
    }
    
    /**
     * Проверяет, доступен ли WorldGuard
     * @return true, если WorldGuard доступен
     */
    public boolean isWorldGuardAvailable() {
        try {
            Class.forName("com.sk89q.worldguard.WorldGuard");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    
    /**
     * Получает список регионов, в которых находится игрок
     * @param player игрок
     * @return список регионов или пустой список, если WorldGuard недоступен
     */
    public List<String> getPlayerRegions(Player player) {
        if (!worldGuardAvailable || player == null) {
            return new ArrayList<>();
        }
        
        Location location = player.getLocation();
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(location));
        
        return set.getRegions().stream()
                .map(ProtectedRegion::getId)
                .collect(Collectors.toList());
    }
    
    /**
     * Проверяет, находится ли игрок в указанном регионе
     * @param player игрок
     * @param regionId идентификатор региона
     * @return true, если игрок находится в регионе
     */
    public boolean isPlayerInRegion(Player player, String regionId) {
        if (!worldGuardAvailable || player == null || regionId == null) {
            return false;
        }
        
        return getPlayerRegions(player).contains(regionId);
    }
    
    /**
     * Проверяет, является ли игрок владельцем региона
     * @param player игрок
     * @param regionId идентификатор региона
     * @return true, если игрок является владельцем региона
     */
    public boolean isPlayerOwnerOfRegion(Player player, String regionId) {
        if (!worldGuardAvailable || player == null || regionId == null) {
            return false;
        }
        
        World world = player.getWorld();
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        com.sk89q.worldedit.world.World worldEditWorld = BukkitAdapter.adapt(world);
        
        if (container.get(worldEditWorld) == null) {
            return false;
        }
        
        ProtectedRegion region = container.get(worldEditWorld).getRegion(regionId);
        if (region == null) {
            return false;
        }
        
        return region.getOwners().contains(player.getUniqueId());
    }
    
    /**
     * Проверяет, является ли игрок участником региона
     * @param player игрок
     * @param regionId идентификатор региона
     * @return true, если игрок является участником региона
     */
    public boolean isPlayerMemberOfRegion(Player player, String regionId) {
        if (!worldGuardAvailable || player == null || regionId == null) {
            return false;
        }
        
        World world = player.getWorld();
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        com.sk89q.worldedit.world.World worldEditWorld = BukkitAdapter.adapt(world);
        
        if (container.get(worldEditWorld) == null) {
            return false;
        }
        
        ProtectedRegion region = container.get(worldEditWorld).getRegion(regionId);
        if (region == null) {
            return false;
        }
        
        return region.getMembers().contains(player.getUniqueId()) || isPlayerOwnerOfRegion(player, regionId);
    }
    
    /**
     * Получает флаг региона
     * @param location локация
     * @param flag флаг для проверки
     * @param <T> тип флага
     * @return значение флага или null, если WorldGuard недоступен
     */
    public <T> T getRegionFlag(Location location, Flag<T> flag) {
        if (!worldGuardAvailable || location == null || flag == null) {
            return null;
        }
        
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        return query.queryValue(BukkitAdapter.adapt(location), null, flag);
    }
    
    /**
     * Проверяет, разрешен ли флаг состояния в регионе
     * @param location локация
     * @param flag флаг для проверки
     * @return true, если флаг разрешен
     */
    public boolean testStateFlag(Location location, StateFlag flag) {
        if (!worldGuardAvailable || location == null || flag == null) {
            return true; // По умолчанию разрешено, если WorldGuard недоступен
        }
        
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        return query.testState(BukkitAdapter.adapt(location), null, flag);
    }
    
    /**
     * Получает все регионы в указанном мире
     * @param world мир
     * @return список идентификаторов регионов или пустой список, если WorldGuard недоступен
     */
    public List<String> getRegionsInWorld(World world) {
        if (!worldGuardAvailable || world == null) {
            return new ArrayList<>();
        }
        
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        com.sk89q.worldedit.world.World worldEditWorld = BukkitAdapter.adapt(world);
        
        if (container.get(worldEditWorld) == null) {
            return new ArrayList<>();
        }
        
        Set<String> regions = container.get(worldEditWorld).getRegions().keySet();
        return new ArrayList<>(regions);
    }
} 