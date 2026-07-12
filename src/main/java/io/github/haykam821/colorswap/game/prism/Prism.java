package io.github.haykam821.colorswap.game.prism;

import io.github.haykam821.colorswap.game.phase.ColorSwapActivePhase;
import net.minecraft.world.item.Item;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

public abstract class Prism {
	private Component name;

	public abstract boolean activate(ColorSwapActivePhase phase, ServerPlayer player);

	public abstract Item getDisplayItem();

	public Component getName() {
		if (this.name == null) {
			Identifier id = Prisms.REGISTRY.getIdentifier(this);
			return Component.translatable(Util.makeDescriptionId("prism", id));
		}

		return this.name;
	}
}
