package io.github.haykam821.colorswap.game.map;

import java.util.Optional;
import java.util.Random;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryCodecs;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryEntryList;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

public class ColorSwapMapConfig {
	public static final Codec<ColorSwapMapConfig> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Codec.INT.fieldOf("x").forGetter(config -> config.x),
				Codec.INT.fieldOf("z").forGetter(config -> config.z),
				Codec.INT.optionalFieldOf("x_scale", 3).forGetter(config -> config.xScale),
				Codec.INT.optionalFieldOf("z_scale", 3).forGetter(config -> config.zScale),
				Codec.DOUBLE.optionalFieldOf("spawn_radius_padding", 4d).forGetter(config -> config.spawnRadiusPadding),
				BlockStateProvider.TYPE_CODEC.optionalFieldOf("initial_state_provider", BlockStateProvider.of(Blocks.WHITE_WOOL)).forGetter(config -> config.initialStateProvider),
				BlockStateProvider.TYPE_CODEC.optionalFieldOf("erased_state_provider", BlockStateProvider.of(Blocks.AIR)).forGetter(config -> config.erasedStateProvider),
				RegistryCodecs.entryList(Registry.BLOCK_KEY).fieldOf("platform_blocks").forGetter(config -> config.platformBlocks)
		).apply(instance, ColorSwapMapConfig::new);
	});

	public final int x;
	public final int z;
	public final int xScale;
	public final int zScale;
	public final double spawnRadiusPadding;
	public final BlockStateProvider initialStateProvider;
	public final BlockStateProvider erasedStateProvider;
	private final RegistryEntryList<Block> platformBlocks;

	public ColorSwapMapConfig(int x, int z, int xScale, int zScale, double spawnRadiusPadding, BlockStateProvider initialStateProvider, BlockStateProvider erasedStateProvider, RegistryEntryList<Block> platformBlocks) {
		this.x = x;
		this.z = z;
		this.xScale = xScale;
		this.zScale = zScale;
		this.spawnRadiusPadding = spawnRadiusPadding;
		this.initialStateProvider = initialStateProvider;
		this.erasedStateProvider = erasedStateProvider;
		this.platformBlocks = platformBlocks;
	}

	public Block getPlatformBlock(Random random) {
		Optional<RegistryEntry<Block>> maybeBlock = this.platformBlocks.getRandom(random);
		if (maybeBlock.isEmpty()) {
			throw new IllegalStateException("No platform block available from " + this.platformBlocks);
		}

		return maybeBlock.get().value();
	}
}
