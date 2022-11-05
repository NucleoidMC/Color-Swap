package io.github.haykam821.colorswap.game.prism;

import io.github.haykam821.colorswap.Main;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.registry.TinyRegistry;

public final class Prisms {
	public static final TinyRegistry<Prism> REGISTRY = TinyRegistry.create();

	private Prisms() {
		return;
	}

	private static void register(String path, Prism prism) {
		Identifier id = new Identifier(Main.MOD_ID, path);
		REGISTRY.register(id, prism);
	}

	public static void register() {
		Prisms.register("leap", new LeapPrism());
		Prisms.register("warp", new WarpPrism());
	}
}
