package io.github.haykam821.colorswap.game.map;

import java.util.concurrent.CompletableFuture;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.haykam821.colorswap.game.ColorSwapConfig;
import net.gegy1000.plasmid.game.map.GameMap;
import net.gegy1000.plasmid.game.map.GameMapBuilder;
import net.gegy1000.plasmid.game.map.provider.MapProvider;
import net.gegy1000.plasmid.world.BlockBounds;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class ColorSwapMapProvider implements MapProvider<ColorSwapConfig> {
	public static final Codec<ColorSwapMapProvider> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
			Codec.INT.fieldOf("x").forGetter(map -> map.x),
			Codec.INT.fieldOf("z").forGetter(map -> map.z),
			Codec.INT.optionalFieldOf("tileSize", 3).forGetter(map -> map.tileSize),
			BlockState.CODEC.optionalFieldOf("defaultState", Blocks.WHITE_WOOL.getDefaultState()).forGetter(map -> map.defaultState),
			BlockState.CODEC.optionalFieldOf("emptyState", Blocks.AIR.getDefaultState()).forGetter(map -> map.emptyState)
		).apply(instance, ColorSwapMapProvider::new);
	});

	public final int x;
	public final int z;
	public final int tileSize;
	public final BlockState defaultState;
	public final BlockState emptyState;

	public ColorSwapMapProvider(int x, int z, int tileSize, BlockState defaultState, BlockState emptyState) {
		this.x = x;
		this.z = z;
		this.tileSize = tileSize;
		this.defaultState = defaultState;
		this.emptyState = emptyState;
	}

	@Override
	public CompletableFuture<GameMap> createAt(ServerWorld world, BlockPos origin, ColorSwapConfig config) {
		BlockBounds bounds = new BlockBounds(BlockPos.ORIGIN, new BlockPos(this.x * this.tileSize - 1, 0, this.z * this.tileSize - 1));

		GameMapBuilder builder = GameMapBuilder.open(world, origin, bounds);
		builder.addRegion("platform", bounds);

		return CompletableFuture.supplyAsync(() -> {
			this.build(bounds, builder, config);
			return builder.build();
		}, world.getServer());
	}

	public void build(BlockBounds bounds, GameMapBuilder builder, ColorSwapConfig config) {
		for (BlockPos pos : bounds.iterate()) {
			builder.setBlockState(pos, this.defaultState);
		}
	}

	@Override
	public Codec<ColorSwapMapProvider> getCodec() {
		return CODEC;
	}
}