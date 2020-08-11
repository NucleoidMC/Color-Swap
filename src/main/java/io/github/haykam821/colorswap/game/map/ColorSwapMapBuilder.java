package io.github.haykam821.colorswap.game.map;

import io.github.haykam821.colorswap.game.ColorSwapConfig;
import xyz.nucleoid.plasmid.game.map.template.MapTemplate;
import xyz.nucleoid.plasmid.util.BlockBounds;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;

import java.util.concurrent.CompletableFuture;

public class ColorSwapMapBuilder {
	private final ColorSwapConfig config;

	public ColorSwapMapBuilder(ColorSwapConfig config) {
		this.config = config;
	}

	public CompletableFuture<ColorSwapMap> create() {
		return CompletableFuture.supplyAsync(() -> {
			ColorSwapMapConfig mapConfig = this.config.getMapConfig();

			MapTemplate template = MapTemplate.createEmpty();

            BlockPos origin = new BlockPos(0, 64, 0);
            BlockBounds platform = new BlockBounds(
                    origin,
                    origin.add(mapConfig.x * mapConfig.tileSize - 1, 0, mapConfig.z * mapConfig.tileSize - 1)
            );

			for (BlockPos pos : platform.iterate()) {
				template.setBlockState(pos, this.config.getMapConfig().defaultState);
			}

			return new ColorSwapMap(template, platform);
		}, Util.getServerWorkerExecutor());
	}
}
