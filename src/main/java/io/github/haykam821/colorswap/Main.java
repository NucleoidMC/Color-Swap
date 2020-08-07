package io.github.haykam821.colorswap;

import io.github.haykam821.colorswap.game.ColorSwapConfig;
import io.github.haykam821.colorswap.game.map.ColorSwapMapProvider;
import io.github.haykam821.colorswap.game.phase.ColorSwapWaitingPhase;
import net.fabricmc.api.ModInitializer;
import net.gegy1000.plasmid.game.GameType;
import net.gegy1000.plasmid.game.config.GameMapConfig;
import net.gegy1000.plasmid.game.map.provider.MapProvider;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

public class Main implements ModInitializer {
	public static final String MOD_ID = "colorswap";

	private static final Identifier COLOR_SWAP_ID = new Identifier(MOD_ID, "color_swap");
	public static final GameType<ColorSwapConfig> COLOR_SWAP_TYPE = GameType.register(COLOR_SWAP_ID, (server, config) -> {
		GameMapConfig<ColorSwapConfig> mapConfig = config.getMapConfig();

		RegistryKey<World> dimension = mapConfig.getDimension();
		BlockPos origin = mapConfig.getOrigin();
		ServerWorld world = server.getWorld(dimension);

		return mapConfig.getProvider().createAt(world, origin, config).thenApply(map -> {
			return ColorSwapWaitingPhase.open(map, config);
		});
	}, ColorSwapConfig.CODEC);

	@Override
	public void onInitialize() {
		MapProvider.REGISTRY.register(COLOR_SWAP_ID, ColorSwapMapProvider.CODEC);
	}
}