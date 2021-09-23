package io.github.haykam821.colorswap.game.map;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.plasmid.game.world.generator.TemplateChunkGenerator;

public final class ColorSwapMap {
	private final MapTemplate template;
	private final BlockBounds platform;
	private final Vec3d spawnPos;

	public ColorSwapMap(MapTemplate template, BlockBounds platform) {
		this.template = template;
		this.platform = platform;

		Vec3d center = this.platform.center();
		this.spawnPos = new Vec3d(center.getX(), this.platform.max().getY() + 1, center.getZ());
	}

	public Vec3d getSpawnPos() {
		return this.spawnPos;
	}

	public boolean isBelowPlatform(ServerPlayerEntity player) {
		return player.getY() < this.platform.min().getY();
	}

	public boolean isAbovePlatform(ServerPlayerEntity player, boolean lenient) {
		return player.getY() > this.platform.min().getY() + (lenient ? 5 : 2.5);
	}

	public ChunkGenerator createGenerator(MinecraftServer server) {
		return new TemplateChunkGenerator(server, this.template);
	}
}
