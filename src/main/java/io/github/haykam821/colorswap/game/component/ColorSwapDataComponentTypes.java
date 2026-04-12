package io.github.haykam821.colorswap.game.component;

import eu.pb4.polymer.core.api.other.PolymerComponent;
import io.github.haykam821.colorswap.Main;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;

public final class ColorSwapDataComponentTypes {
	private static final Identifier PRISM_ID = Main.identifier("prism");

	public static final DataComponentType<PrismComponent> PRISM = DataComponentType.<PrismComponent>builder()
		.persistent(PrismComponent.CODEC)
		.cacheEncoding()
		.build();

	public static void register() {
		Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, PRISM_ID, PRISM);
		PolymerComponent.registerDataComponent(PRISM);
	}
}
