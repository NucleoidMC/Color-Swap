package io.github.haykam821.colorswap.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.haykam821.colorswap.game.map.ColorSwapMapConfig;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import xyz.nucleoid.plasmid.game.config.PlayerConfig;

public class ColorSwapConfig {
	public static final Codec<ColorSwapConfig> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
			ColorSwapMapConfig.CODEC.fieldOf("map").forGetter(ColorSwapConfig::getMapConfig),
			PlayerConfig.CODEC.fieldOf("players").forGetter(ColorSwapConfig::getPlayerConfig),
			SoundEvent.CODEC.optionalFieldOf("swap_sound", SoundEvents.BLOCK_NOTE_BLOCK_SNARE).forGetter(ColorSwapConfig::getSwapSound),
			Codec.INT.optionalFieldOf("swap_time", -1).forGetter(ColorSwapConfig::getSwapTime),
			Codec.INT.optionalFieldOf("erase_time", -1).forGetter(ColorSwapConfig::getEraseTime),
			Codec.INT.optionalFieldOf("no_knockback_rounds", -1).forGetter(ColorSwapConfig::getNoKnockbackRounds)
		).apply(instance, ColorSwapConfig::new);
	});

	private final ColorSwapMapConfig mapConfig;
	private final PlayerConfig playerConfig;
	private final SoundEvent swapSound;
	private final int swapTime;
	private final int eraseTime;
	private final int noKnockbackRounds;

	public ColorSwapConfig(ColorSwapMapConfig mapConfig, PlayerConfig playerConfig, SoundEvent swapSound, int swapTime, int eraseTime, int noKnockbackRounds) {
		this.mapConfig = mapConfig;
		this.playerConfig = playerConfig;
		this.swapSound = swapSound;
		this.swapTime = swapTime;
		this.eraseTime = eraseTime;
		this.noKnockbackRounds = noKnockbackRounds;
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

	public int getNoKnockbackRounds() {
		return this.noKnockbackRounds;
	}
}
