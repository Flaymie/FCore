package dev.flaymie.fcore.core.action;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import java.util.concurrent.CompletableFuture;

/**
 * Действие для проигрывания звука игроку
 */
public class SoundAction extends AbstractAction {
    
    private final Sound sound;
    private final float volume;
    private final float pitch;
    
    /**
     * Создает новое действие проигрывания звука
     *
     * @param sound звук
     * @param volume громкость (от 0.0 до 1.0)
     * @param pitch высота (от 0.5 до 2.0)
     */
    public SoundAction(Sound sound, float volume, float pitch) {
        super("Sound", "Проигрывает звук " + sound.name() + " с громкостью " + volume + " и высотой " + pitch, false);
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
    }
    
    /**
     * Создает новое действие проигрывания звука с громкостью и высотой по умолчанию
     *
     * @param sound звук
     */
    public SoundAction(Sound sound) {
        this(sound, 1.0f, 1.0f);
    }
    
    @Override
    public CompletableFuture<Void> execute(Player player, ActionContext context) {
        player.playSound(player.getLocation(), sound, volume, pitch);
        return CompletableFuture.completedFuture(null);
    }
    
    /**
     * Получает звук
     *
     * @return звук
     */
    public Sound getSound() {
        return sound;
    }
    
    /**
     * Получает громкость
     *
     * @return громкость
     */
    public float getVolume() {
        return volume;
    }
    
    /**
     * Получает высоту
     *
     * @return высота
     */
    public float getPitch() {
        return pitch;
    }
} 