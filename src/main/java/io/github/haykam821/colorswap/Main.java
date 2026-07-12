package io.github.haykam821.colorswap;

import io.github.haykam821.colorswap.game.ColorSwapConfig;
import io.github.haykam821.colorswap.game.component.ColorSwapDataComponentTypes;
import io.github.haykam821.colorswap.game.item.ColorSwapItems;
import io.github.haykam821.colorswap.game.phase.ColorSwapWaitingPhase;
import io.github.haykam821.colorswap.game.prism.Prisms;
import net.fabricmc.api.ModInitializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.Identifier;
import xyz.nucleoid.plasmid.api.game.GameType;
import xyz.nucleoid.plasmid.api.game.GameTypes;

public class Main implements ModInitializer {
	private static final String MOD_ID = "colorswap";

	private static final Identifier COLOR_SWAP_ID = Main.identifier("color_swap");
	public static final GameType<ColorSwapConfig> COLOR_SWAP_TYPE = GameTypes.register(COLOR_SWAP_ID, ColorSwapConfig.CODEC, ColorSwapWaitingPhase::open);

	private static final Identifier PLATFORM_BLOCKS_ID = Main.identifier("platform_blocks");
	public static final TagKey<Block> PLATFORM_BLOCKS = TagKey.create(Registries.BLOCK, PLATFORM_BLOCKS_ID);

	@Override
	public void onInitialize() {
		Prisms.register();

		ColorSwapDataComponentTypes.register();
		ColorSwapItems.initialize();
	}

	public static Identifier identifier(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
