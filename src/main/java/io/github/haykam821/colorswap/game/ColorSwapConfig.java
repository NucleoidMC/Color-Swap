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
				SoundEvent.field_24628.optionalFieldOf("swap_sound", SoundEvents.BLOCK_NOTE_BLOCK_SNARE).forGetter(ColorSwapConfig::getSwapSound)
		).apply(instance, ColorSwapConfig::new);
	});

	private final ColorSwapMapConfig mapConfig;
	private final PlayerConfig playerConfig;
	private final SoundEvent swapSound;

	public ColorSwapConfig(ColorSwapMapConfig mapConfig, PlayerConfig playerConfig, SoundEvent swapSound) {
		this.mapConfig = mapConfig;
		this.playerConfig = playerConfig;
		this.swapSound = swapSound;
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
}
