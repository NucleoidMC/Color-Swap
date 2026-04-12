package io.github.haykam821.colorswap.game.prism;

import io.github.haykam821.colorswap.Main;
import net.minecraft.resources.Identifier;
import xyz.nucleoid.plasmid.api.util.TinyRegistry;

public final class Prisms {
	public static final TinyRegistry<Prism> REGISTRY = TinyRegistry.create();

	private Prisms() {
    }

	private static void register(String path, Prism prism) {
		Identifier id = Main.identifier(path);
		REGISTRY.register(id, prism);
	}

	public static void register() {
		Prisms.register("leap", new LeapPrism());
		Prisms.register("splash", new SplashPrism());
		Prisms.register("warp", new WarpPrism());
	}
}
