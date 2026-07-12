package io.github.haykam821.colorswap.game.item;

import eu.pb4.polymer.core.api.item.PolymerItem;
import io.github.haykam821.colorswap.game.component.PrismComponent;
import io.github.haykam821.colorswap.game.prism.Prism;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.resources.Identifier;

public class PrismItem extends Item implements PolymerItem {
	public PrismItem(Item.Properties settings) {
		super(settings);
	}

	@Override
	public Item getPolymerItem(ItemStack stack, PacketContext context) {
		Prism prism = PrismComponent.get(stack);
		return prism == null ? Items.NETHER_STAR : prism.getDisplayItem();
	}

	@Override
	public Identifier getPolymerItemModel(ItemStack stack, PacketContext context, HolderLookup.Provider lookup) {
		return null;
	}
}
