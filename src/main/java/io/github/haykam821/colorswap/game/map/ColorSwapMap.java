package io.github.haykam821.colorswap.game.map;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.chunk.ChunkGenerator;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.plasmid.api.game.level.generator.TemplateChunkGenerator;

public final class ColorSwapMap {
	private final MapTemplate template;
	private final BlockBounds platform;

	private final Vec3 center;
	private final double spawnRadius;

	public ColorSwapMap(MapTemplate template, BlockBounds platform, double spawnRadiusPadding) {
		this.template = template;
		this.platform = platform;

		this.center = this.createCenterPos(0, 0);

		BlockPos size = this.platform.size();
		int min = Math.min(size.getX(), size.getZ());

		this.spawnRadius = min > spawnRadiusPadding ? min / 2d - spawnRadiusPadding : 0;
	}

	public BlockBounds getPlatform() {
		return this.platform;
	}

	// Spawn positions
	public Vec3 getGuideTextPos() {
		return this.createCenterPos(1.2, 0);
	}

	public Vec3 getWaitingSpawnPos() {
		return this.createCenterPos(0, 4);
	}

	public Vec3 getSpawnPos(double theta) {
		double x = this.center.x() + Math.cos(theta) * spawnRadius;
		double z = this.center.z() + Math.sin(theta) * spawnRadius;

		return new Vec3(x, this.center.y(), z);
	}

	public Vec3 getSpectatorSpawnPos() {
		return this.createCenterPos(3, 0);
	}

	// Elimination detection
	public boolean isBelowPlatform(ServerPlayer player) {
		return player.getY() < this.platform.min().getY();
	}

	public boolean isAbovePlatform(ServerPlayer player, boolean lenient) {
		return player.getY() > this.platform.min().getY() + (lenient ? 5 : 2.5);
	}

	public ChunkGenerator createGenerator(MinecraftServer server) {
		return new TemplateChunkGenerator(server, this.template);
	}

	private Vec3 createCenterPos(double offsetY, double offsetZ) {
		Vec3 center = this.getPlatform().centerTop();

		double maxOffsetZ = (double) this.platform.size().getZ() / 2 - 0.5;
		double clampedOffsetZ = Mth.clamp(offsetZ, -maxOffsetZ, maxOffsetZ);

		return new Vec3(center.x(), center.y() + offsetY, center.z() - clampedOffsetZ);
	}
}
