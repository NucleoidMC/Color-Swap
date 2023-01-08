package io.github.haykam821.colorswap.game.item;

import eu.pb4.polymer.core.api.item.PolymerItem;
import io.github.haykam821.colorswap.game.prism.Prism;
import io.github.haykam821.colorswap.game.prism.Prisms;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class PrismItem extends Item implements PolymerItem {
	private static final String PRISM_KEY = "Prism";

	public PrismItem(Item.Settings settings) {
		super(settings);
	}

	@Override
	public ItemStack getPolymerItemStack(ItemStack stack, TooltipContext context, ServerPlayerEntity player) {
		Prism prism = PrismItem.getPrism(stack);
		ItemStack displayStack = prism == null ? PolymerItem.super.getPolymerItemStack(stack, context, player) : prism.createDisplayStack();

		return PolymerItem.super.getPolymerItemStack(displayStack, context, player);
	}

	@Override
	public Item getPolymerItem(ItemStack stack, ServerPlayerEntity player) {
		Prism prism = PrismItem.getPrism(stack);
		return prism == null ? Items.NETHER_STAR : prism.getDisplayItem();
	}

	public static Prism getPrism(ItemStack stack) {
		if (stack.hasNbt()) {
			Identifier id = Identifier.tryParse(stack.getNbt().getString(PRISM_KEY));
			return Prisms.REGISTRY.get(id);
		}

		return null;
	}

	public static void setPrism(ItemStack stack, Prism prism) {
		NbtCompound nbt = stack.getOrCreateNbt();
		Identifier id = Prisms.REGISTRY.getIdentifier(prism);

		nbt.putString(PRISM_KEY, id.toString());
	}
}
