package io.github.haykam821.colorswap.game.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.haykam821.colorswap.game.prism.Prism;
import io.github.haykam821.colorswap.game.prism.Prisms;
import net.minecraft.world.item.ItemStack;

public record PrismComponent(Prism prism) {
	public static final Codec<PrismComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Prisms.REGISTRY.fieldOf("prism").forGetter(PrismComponent::prism)
    ).apply(instance, PrismComponent::new));

	public static Prism get(ItemStack stack) {
		PrismComponent component = stack.get(ColorSwapDataComponentTypes.PRISM);
		return component == null ? null : component.prism();
	}

	public static void set(ItemStack stack, Prism prism) {
		PrismComponent component = new PrismComponent(prism);
		stack.set(ColorSwapDataComponentTypes.PRISM, component);
	}
}
