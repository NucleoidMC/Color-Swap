package io.github.haykam821.colorswap.game.item;

import java.util.function.Function;

import io.github.haykam821.colorswap.Main;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Rarity;

public final class ColorSwapItems {
	public static final Item PRISM = register("prism", PrismItem::new, new Item.Properties()
		.stacksTo(1)
		.rarity(Rarity.RARE)
		.component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true));

	public static void initialize() {
    }

	private static Item register(String path, Function<Item.Properties, Item> factory, Item.Properties settings) {
		Identifier id = Main.identifier(path);
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);

		Item item = factory.apply(settings.setId(key));

		return Registry.register(BuiltInRegistries.ITEM, Main.identifier(path), item);
	}
}
