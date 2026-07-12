package io.github.haykam821.colorswap.game.map;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class ColorSwapMapConfig {
	public static final Codec<ColorSwapMapConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("x").forGetter(config -> config.x),
            Codec.INT.fieldOf("z").forGetter(config -> config.z),
            Codec.INT.optionalFieldOf("x_scale", 3).forGetter(config -> config.xScale),
            Codec.INT.optionalFieldOf("z_scale", 3).forGetter(config -> config.zScale),
            Codec.DOUBLE.optionalFieldOf("spawn_radius_padding", 4d).forGetter(config -> config.spawnRadiusPadding),
            BlockState.CODEC.optionalFieldOf("initial_state_provider", Blocks.WOOL.white().defaultBlockState()).forGetter(config -> config.initialStateProvider),
            BlockState.CODEC.optionalFieldOf("erased_state_provider", Blocks.AIR.defaultBlockState()).forGetter(config -> config.erasedStateProvider),
            RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("platform_blocks").forGetter(config -> config.platformBlocks)
    ).apply(instance, ColorSwapMapConfig::new));

	public final int x;
	public final int z;
	public final int xScale;
	public final int zScale;
	public final double spawnRadiusPadding;
	public final BlockState initialStateProvider;
	public final BlockState erasedStateProvider;
	private final HolderSet<Block> platformBlocks;

	public ColorSwapMapConfig(int x, int z, int xScale, int zScale, double spawnRadiusPadding, BlockState initialStateProvider, BlockState erasedStateProvider, HolderSet<Block> platformBlocks) {
		this.x = x;
		this.z = z;
		this.xScale = xScale;
		this.zScale = zScale;
		this.spawnRadiusPadding = spawnRadiusPadding;
		this.initialStateProvider = initialStateProvider;
		this.erasedStateProvider = erasedStateProvider;
		this.platformBlocks = platformBlocks;
	}

	public Block getPlatformBlock(RandomSource random) {
		Optional<Holder<Block>> maybeBlock = this.platformBlocks.getRandomElement(random);
		if (maybeBlock.isEmpty()) {
			throw new IllegalStateException("No platform block available from " + this.platformBlocks);
		}

		return maybeBlock.get().value();
	}
}
