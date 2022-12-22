package io.github.haykam821.colorswap;

import io.github.haykam821.colorswap.game.ColorSwapConfig;
import io.github.haykam821.colorswap.game.item.PrismItem;
import io.github.haykam821.colorswap.game.phase.ColorSwapWaitingPhase;
import io.github.haykam821.colorswap.game.prism.Prisms;
import net.fabricmc.api.ModInitializer;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import xyz.nucleoid.plasmid.game.GameType;

public class Main implements ModInitializer {
	public static final String MOD_ID = "colorswap";

	private static final Identifier COLOR_SWAP_ID = new Identifier(MOD_ID, "color_swap");
	public static final GameType<ColorSwapConfig> COLOR_SWAP_TYPE = GameType.register(COLOR_SWAP_ID, ColorSwapConfig.CODEC, ColorSwapWaitingPhase::open);

	private static final Identifier PLATFORM_BLOCKS_ID = new Identifier(MOD_ID, "platform_blocks");
	public static final TagKey<Block> PLATFORM_BLOCKS = TagKey.of(RegistryKeys.BLOCK, PLATFORM_BLOCKS_ID);

	private static final Identifier PRISM_ID = new Identifier(MOD_ID, "prism");
	public static final Item PRISM = new PrismItem(new Item.Settings().maxCount(1).rarity(Rarity.RARE));

	@Override
	public void onInitialize() {
		Prisms.register();

		Registry.register(Registries.ITEM, PRISM_ID, PRISM);
	}
}
