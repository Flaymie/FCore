package dev.flaymie.fcore.api.gui.impl;

import dev.flaymie.fcore.api.gui.AnimatedGui;
import dev.flaymie.fcore.api.gui.ClickHandler;
import dev.flaymie.fcore.FCore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class AnimatedGuiImpl extends GuiImpl implements AnimatedGui {
    
    private final Map<Integer, Map<Integer, ItemStack>> frames;
    private final Map<Integer, Map<Integer, ClickHandler>> frameHandlers;
    
    private int frameCount;
    private int currentFrame;
    private long interval;
    private boolean loop;
    private boolean running;
    private BukkitTask animationTask;
    private Consumer<Integer> frameHandler;
    
    public AnimatedGuiImpl(String title, int rows) {
        super(title, rows);
        this.frames = new HashMap<>();
        this.frameHandlers = new HashMap<>();
        this.frameCount = 1;
        this.currentFrame = 0;
        this.interval = 10L; // 0.5 секунды по умолчанию
        this.loop = true;
        this.running = false;
    }
    
    @Override
    public AnimatedGui startAnimation() {
        if (running) return this;
        
        running = true;
        animationTask = Bukkit.getScheduler().runTaskTimer(FCore.getInstance(), () -> {
            if (frameHandler != null) {
                frameHandler.accept(currentFrame);
            }
            
            // Обновляем предметы для текущего кадра
            Map<Integer, ItemStack> frameItems = frames.getOrDefault(currentFrame, new HashMap<>());
            Map<Integer, ClickHandler> handlers = frameHandlers.getOrDefault(currentFrame, new HashMap<>());
            
            for (Map.Entry<Integer, ItemStack> entry : frameItems.entrySet()) {
                int slot = entry.getKey();
                ItemStack item = entry.getValue();
                ClickHandler handler = handlers.get(slot);
                
                setItem(slot, item);
                if (handler != null) {
                    setClickHandler(slot, handler);
                }
            }
            
            // Обновляем инвентарь для всех просматривающих
            viewers.forEach(uuid -> {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    player.updateInventory();
                }
            });
            
            // Переходим к следующему кадру
            currentFrame++;
            if (currentFrame >= frameCount) {
                if (loop) {
                    currentFrame = 0;
                } else {
                    stopAnimation();
                }
            }
        }, 0L, interval);
        
        return this;
    }
    
    @Override
    public AnimatedGui stopAnimation() {
        if (!running) return this;
        
        running = false;
        if (animationTask != null) {
            animationTask.cancel();
            animationTask = null;
        }
        
        return this;
    }
    
    @Override
    public boolean isAnimationRunning() {
        return running;
    }
    
    @Override
    public AnimatedGui setAnimationInterval(long interval) {
        this.interval = interval;
        
        // Перезапускаем анимацию, если она была запущена
        if (running) {
            stopAnimation();
            startAnimation();
        }
        
        return this;
    }
    
    @Override
    public long getAnimationInterval() {
        return interval;
    }
    
    @Override
    public AnimatedGui addAnimationFrame(int frameIndex, int slot, ItemStack item) {
        return addAnimationFrame(frameIndex, slot, item, null);
    }
    
    @Override
    public AnimatedGui addAnimationFrame(int frameIndex, int slot, ItemStack item, ClickHandler clickHandler) {
        if (frameIndex >= frameCount) {
            frameCount = frameIndex + 1;
        }
        
        frames.computeIfAbsent(frameIndex, k -> new HashMap<>()).put(slot, item);
        
        if (clickHandler != null) {
            frameHandlers.computeIfAbsent(frameIndex, k -> new HashMap<>()).put(slot, clickHandler);
        }
        
        return this;
    }
    
    @Override
    public AnimatedGui removeAnimationFrame(int frameIndex, int slot) {
        if (frames.containsKey(frameIndex)) {
            frames.get(frameIndex).remove(slot);
        }
        
        if (frameHandlers.containsKey(frameIndex)) {
            frameHandlers.get(frameIndex).remove(slot);
        }
        
        return this;
    }
    
    @Override
    public AnimatedGui clearAnimationFrames() {
        frames.clear();
        frameHandlers.clear();
        frameCount = 1;
        currentFrame = 0;
        
        return this;
    }
    
    @Override
    public AnimatedGui onFrame(Consumer<Integer> frameHandler) {
        this.frameHandler = frameHandler;
        return this;
    }
    
    @Override
    public AnimatedGui setFrameCount(int frameCount) {
        this.frameCount = Math.max(1, frameCount);
        return this;
    }
    
    @Override
    public int getFrameCount() {
        return frameCount;
    }
    
    @Override
    public int getCurrentFrame() {
        return currentFrame;
    }
    
    @Override
    public AnimatedGui setCurrentFrame(int frame) {
        this.currentFrame = Math.max(0, Math.min(frame, frameCount - 1));
        return this;
    }
    
    @Override
    public AnimatedGui setLoop(boolean loop) {
        this.loop = loop;
        return this;
    }
    
    @Override
    public boolean isLoop() {
        return loop;
    }
    
    @Override
    public void open(Player player) {
        super.open(player);
        
        // Если анимация не запущена при первом открытии
        if (!running && viewers.size() == 1) {
            startAnimation();
        }
    }
    
    @Override
    public void close(UUID uuid) {
        super.close(uuid);
        
        // Если никто не просматривает GUI, останавливаем анимацию
        if (viewers.isEmpty()) {
            stopAnimation();
        }
    }
} 