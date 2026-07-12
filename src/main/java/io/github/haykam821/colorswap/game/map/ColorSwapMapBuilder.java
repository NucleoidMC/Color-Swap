package io.github.haykam821.colorswap.game.map;

import io.github.haykam821.colorswap.game.ColorSwapConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.map_templates.MapTemplate;

public class ColorSwapMapBuilder {
	private final ColorSwapConfig config;

	public ColorSwapMapBuilder(ColorSwapConfig config) {
		this.config = config;
	}

	public ColorSwapMap create(RandomSource random) {
		ColorSwapMapConfig mapConfig = this.config.getMapConfig();

		MapTemplate template = MapTemplate.createEmpty();

		BlockPos origin = new BlockPos(0, 64, 0);
		BlockBounds platform = BlockBounds.of(
				origin,
				origin.offset(mapConfig.x * mapConfig.xScale - 1, 0, mapConfig.z * mapConfig.zScale - 1)
		);

		for (BlockPos pos : platform) {
			template.setBlockState(pos, this.config.getMapConfig().initialStateProvider);
		}

		return new ColorSwapMap(template, platform, mapConfig.spawnRadiusPadding);
	}
}
