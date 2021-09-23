package io.github.haykam821.colorswap.game.map;

import java.util.Random;

import io.github.haykam821.colorswap.game.ColorSwapConfig;
import net.minecraft.util.math.BlockPos;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.map_templates.MapTemplate;

public class ColorSwapMapBuilder {
	private final ColorSwapConfig config;

	public ColorSwapMapBuilder(ColorSwapConfig config) {
		this.config = config;
	}

	public ColorSwapMap create() {
		ColorSwapMapConfig mapConfig = this.config.getMapConfig();

		MapTemplate template = MapTemplate.createEmpty();

		BlockPos origin = new BlockPos(0, 64, 0);
		BlockBounds platform = BlockBounds.of(
				origin,
				origin.add(mapConfig.x * mapConfig.xScale - 1, 0, mapConfig.z * mapConfig.zScale - 1)
		);

		Random random = new Random();
		for (BlockPos pos : platform) {
			template.setBlockState(pos, this.config.getMapConfig().initialStateProvider.getBlockState(random, pos));
		}

		return new ColorSwapMap(template, platform);
	}
}
