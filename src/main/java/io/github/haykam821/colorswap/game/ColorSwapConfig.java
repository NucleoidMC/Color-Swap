package io.github.haykam821.colorswap.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.gegy1000.plasmid.game.config.GameConfig;
import net.gegy1000.plasmid.game.config.GameMapConfig;
import net.gegy1000.plasmid.game.config.PlayerConfig;

public class ColorSwapConfig implements GameConfig {
	public static final Codec<ColorSwapConfig> CODEC = RecordCodecBuilder.create(instance -> {
		Codec<GameMapConfig<ColorSwapConfig>> mapCodec = GameMapConfig.codec();
		return instance.group(
			mapCodec.fieldOf("map").forGetter(ColorSwapConfig::getMapConfig),
			PlayerConfig.CODEC.fieldOf("players").forGetter(ColorSwapConfig::getPlayerConfig)
		).apply(instance, ColorSwapConfig::new);
	});

	private final GameMapConfig<ColorSwapConfig> mapConfig;
	private final PlayerConfig playerConfig;

	public ColorSwapConfig(GameMapConfig<ColorSwapConfig> mapConfig, PlayerConfig playerConfig) {
		this.mapConfig = mapConfig;
		this.playerConfig = playerConfig;
	}

	public GameMapConfig<ColorSwapConfig> getMapConfig() {
		return this.mapConfig;
	}

	public PlayerConfig getPlayerConfig() {
		return this.playerConfig;
	}
}