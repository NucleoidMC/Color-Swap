package io.github.haykam821.colorswap.game.map;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.plasmid.game.world.generator.TemplateChunkGenerator;

public final class ColorSwapMap {
	private final MapTemplate template;
	private final BlockBounds platform;

	private final Vec3d center;
	private final double spawnRadius;

	public ColorSwapMap(MapTemplate template, BlockBounds platform, double spawnRadiusPadding) {
		this.template = template;
		this.platform = platform;

		Vec3d center = this.platform.center();
		this.center = new Vec3d(center.getX(), this.platform.max().getY() + 1, center.getZ());

		BlockPos size = this.platform.size();
		int min = Math.min(size.getX(), size.getZ());

		this.spawnRadius = min > spawnRadiusPadding ? min / 2d - spawnRadiusPadding : 0;
	}

	public Vec3d getCenter() {
		return this.center;
	}

	public double getSpawnRadius() {
		return this.spawnRadius;
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
