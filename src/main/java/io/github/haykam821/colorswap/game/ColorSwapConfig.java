package io.github.haykam821.colorswap.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.haykam821.colorswap.game.map.ColorSwapMapConfig;
import net.gegy1000.plasmid.game.config.GameConfig;
import net.gegy1000.plasmid.game.config.PlayerConfig;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

public class ColorSwapConfig implements GameConfig {
	public static final Codec<ColorSwapConfig> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
			ColorSwapMapConfig.CODEC.fieldOf("map").forGetter(ColorSwapConfig::getMapConfig),
			PlayerConfig.CODEC.fieldOf("players").forGetter(ColorSwapConfig::getPlayerConfig),
			SoundEvent.field_24628.optionalFieldOf("swap_sound", SoundEvents.BLOCK_NOTE_BLOCK_SNARE).forGetter(ColorSwapConfig::getSwapSound),
			Codec.INT.optionalFieldOf("swap_time", 20 * 4).forGetter(ColorSwapConfig::getSwapTime),
			Codec.INT.optionalFieldOf("erase_time", 20 * 2).forGetter(ColorSwapConfig::getEraseTime)
		).apply(instance, ColorSwapConfig::new);
	});

	private final ColorSwapMapConfig mapConfig;
	private final PlayerConfig playerConfig;
	private final SoundEvent swapSound;
	private final int swapTime;
	private final int eraseTime;

	public ColorSwapConfig(ColorSwapMapConfig mapConfig, PlayerConfig playerConfig, SoundEvent swapSound, int swapTime, int eraseTime) {
		this.mapConfig = mapConfig;
		this.playerConfig = playerConfig;
		this.swapSound = swapSound;
		this.swapTime = swapTime;
		this.eraseTime = eraseTime;
	}

	public ColorSwapMapConfig getMapConfig() {
		return this.mapConfig;
	}

	public PlayerConfig getPlayerConfig() {
		return this.playerConfig;
	}

	public SoundEvent getSwapSound() {
		return this.swapSound;
	}

	public int getSwapTime() {
		return this.swapTime;
	}

	public int getEraseTime() {
		return this.eraseTime;
	}
}
