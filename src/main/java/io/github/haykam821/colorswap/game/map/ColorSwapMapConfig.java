package io.github.haykam821.colorswap.game.map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.haykam821.colorswap.Main;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;

public class ColorSwapMapConfig {
	public static final Codec<ColorSwapMapConfig> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Codec.INT.fieldOf("x").forGetter(config -> config.x),
				Codec.INT.fieldOf("z").forGetter(config -> config.z),
				Codec.INT.optionalFieldOf("tileSize", 3).forGetter(config -> config.tileSize),
				BlockState.CODEC.optionalFieldOf("defaultState", Blocks.WHITE_WOOL.getDefaultState()).forGetter(config -> config.defaultState),
				BlockState.CODEC.optionalFieldOf("emptyState", Blocks.AIR.getDefaultState()).forGetter(config -> config.emptyState),
				Identifier.CODEC.fieldOf("platformBlocks").forGetter(config -> config.platformBlocks)
		).apply(instance, ColorSwapMapConfig::new);
	});

	public final int x;
	public final int z;
	public final int tileSize;
	public final BlockState defaultState;
	public final BlockState emptyState;
	private final Identifier platformBlocks;

	public ColorSwapMapConfig(int x, int z, int tileSize, BlockState defaultState, BlockState emptyState, Identifier platformBlocks) {
		this.x = x;
		this.z = z;
		this.tileSize = tileSize;
		this.defaultState = defaultState;
		this.emptyState = emptyState;
		this.platformBlocks = platformBlocks;
	}

	public Tag<Block> getPlatformBlocks() {
		Tag<Block> tag = BlockTags.getContainer().get(this.platformBlocks);
		return tag == null ? Main.PLATFORM_BLOCKS : tag;
	}
}
