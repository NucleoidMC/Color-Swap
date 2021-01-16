package io.github.haykam821.colorswap.game.map;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import xyz.nucleoid.plasmid.map.template.MapTemplate;
import xyz.nucleoid.plasmid.map.template.TemplateChunkGenerator;
import xyz.nucleoid.plasmid.util.BlockBounds;

public final class ColorSwapMap {
	private final MapTemplate template;
	private final BlockBounds platform;
	private final Vec3d spawnPos;

	public ColorSwapMap(MapTemplate template, BlockBounds platform) {
		this.template = template;
		this.platform = platform;

		Vec3d center = this.platform.getCenter();
		this.spawnPos = new Vec3d(center.getX(), this.platform.getMax().getY() + 1, center.getZ());
	}

	public Vec3d getSpawnPos() {
		return this.spawnPos;
	}

	public boolean isBelowPlatform(ServerPlayerEntity player) {
		return player.getY() < this.platform.getMin().getY();
	}

	public boolean isAbovePlatform(ServerPlayerEntity player) {
		return player.getY() > this.platform.getMin().getY() + 2.5;
	}

	public ChunkGenerator createGenerator(MinecraftServer server) {
		return new TemplateChunkGenerator(server, this.template);
	}
}
