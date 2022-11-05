package io.github.haykam821.colorswap.game.prism;

import io.github.haykam821.colorswap.game.phase.ColorSwapActivePhase;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemStack.TooltipSection;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import xyz.nucleoid.plasmid.util.ItemStackBuilder;

public abstract class Prism {
	private Text name;

	public abstract void activate(ColorSwapActivePhase phase, ServerPlayerEntity player);

	public abstract Item getDisplayItem();

	public ItemStack createDisplayStack() {
		return ItemStackBuilder.of(this.getDisplayItem())
			.addEnchantment(Enchantments.POWER, 0)
			.hideFlag(TooltipSection.ENCHANTMENTS)
			.build();
	}

	public Text getName() {
		if (this.name == null) {
			Identifier id = Prisms.REGISTRY.getIdentifier(this);
			return new TranslatableText(Util.createTranslationKey("prism", id));
		}

		return this.name;
	}
}