package io.github.haykam821.colorswap.game.map;

import io.github.haykam821.colorswap.game.ColorSwapConfig;
import net.gegy1000.plasmid.game.map.template.MapTemplate;
import net.gegy1000.plasmid.util.BlockBounds;
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
            BlockBounds platform = new BlockBounds(BlockPos.ORIGIN, new BlockPos(mapConfig.x * mapConfig.tileSize - 1, 64, mapConfig.z * mapConfig.tileSize - 1));

            for (BlockPos pos : platform.iterate()) {
                template.setBlockState(pos, this.config.getMapConfig().defaultState);
            }

            return new ColorSwapMap(template, platform);
        }, Util.getServerWorkerExecutor());
    }
}
