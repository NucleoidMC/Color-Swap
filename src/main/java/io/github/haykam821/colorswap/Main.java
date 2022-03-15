package io.github.haykam821.colorswap;

import io.github.haykam821.colorswap.game.ColorSwapConfig;
import io.github.haykam821.colorswap.game.phase.ColorSwapWaitingPhase;
import net.fabricmc.api.ModInitializer;
import xyz.nucleoid.plasmid.game.GameType;
import net.minecraft.block.Block;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class Main implements ModInitializer {
	public static final String MOD_ID = "colorswap";

	private static final Identifier COLOR_SWAP_ID = new Identifier(MOD_ID, "color_swap");
	public static final GameType<ColorSwapConfig> COLOR_SWAP_TYPE = GameType.register(COLOR_SWAP_ID, ColorSwapConfig.CODEC, ColorSwapWaitingPhase::open);

	private static final Identifier PLATFORM_BLOCKS_ID = new Identifier(MOD_ID, "platform_blocks");
	public static final TagKey<Block> PLATFORM_BLOCKS = TagKey.of(Registry.BLOCK_KEY, PLATFORM_BLOCKS_ID);

	@Override
	public void onInitialize() {
	}
}
