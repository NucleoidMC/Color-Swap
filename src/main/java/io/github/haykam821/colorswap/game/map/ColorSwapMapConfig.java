package io.github.haykam821.colorswap.game.map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.haykam821.colorswap.Main;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.stateprovider.SimpleBlockStateProvider;

public class ColorSwapMapConfig {
	public static final Codec<ColorSwapMapConfig> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Codec.INT.fieldOf("x").forGetter(config -> config.x),
				Codec.INT.fieldOf("z").forGetter(config -> config.z),
				Codec.INT.optionalFieldOf("tileSize", 3).forGetter(config -> config.tileSize),
				BlockStateProvider.TYPE_CODEC.optionalFieldOf("initial_state_provider", new SimpleBlockStateProvider(Blocks.WHITE_WOOL.getDefaultState())).forGetter(config -> config.initialStateProvider),
				BlockStateProvider.TYPE_CODEC.optionalFieldOf("erased_state_provider", new SimpleBlockStateProvider(Blocks.AIR.getDefaultState())).forGetter(config -> config.erasedStateProvider),
				Identifier.CODEC.fieldOf("platformBlocks").forGetter(config -> config.platformBlocks)
		).apply(instance, ColorSwapMapConfig::new);
	});

	public final int x;
	public final int z;
	public final int tileSize;
	public final BlockStateProvider initialStateProvider;
	public final BlockStateProvider erasedStateProvider;
	private final Identifier platformBlocks;

	public ColorSwapMapConfig(int x, int z, int tileSize, BlockStateProvider initialStateProvider, BlockStateProvider erasedStateProvider, Identifier platformBlocks) {
		this.x = x;
		this.z = z;
		this.tileSize = tileSize;
		this.initialStateProvider = initialStateProvider;
		this.erasedStateProvider = erasedStateProvider;
		this.platformBlocks = platformBlocks;
	}

	public Tag<Block> getPlatformBlocks() {
		Tag<Block> tag = TagRegistry.block(this.platformBlocks);
		return tag == null ? Main.PLATFORM_BLOCKS : tag;
	}
}
