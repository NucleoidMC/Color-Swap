package io.github.haykam821.colorswap.game.prism.spawner;

import io.github.haykam821.colorswap.game.phase.ColorSwapActivePhase;
import io.github.haykam821.colorswap.game.prism.Prism;
import io.github.haykam821.colorswap.game.prism.PrismConfig;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import xyz.nucleoid.map_templates.BlockBounds;

public class PrismSpawner {
	private static final int SPAWN_HEIGHT = 2;
	private static final Text PRISM_SPAWNED_MESSAGE = Text.translatable("text.colorswap.prism.spawned").formatted(Formatting.GOLD);

	private final ColorSwapActivePhase phase;
	private final PrismConfig config;
	private final Random random;

	private SpawnedPrism spawnedPrism;
	private int roundsUntilSpawn;

	public PrismSpawner(ColorSwapActivePhase phase, PrismConfig config, Random random) {
		this.phase = phase;
		this.config = config;
		this.random = random;

		this.resetRoundsUntilSpawn();
	}

	private void resetRoundsUntilSpawn() {
		this.roundsUntilSpawn = this.config.roundsBetweenSpawns().get(this.random);
	}

	private BlockPos getSpawnPos() {
		BlockBounds platform = this.phase.getMap().getPlatform();
		BlockPos size = platform.size();

		BlockPos min = platform.min();
		BlockPos max = platform.max();

		int y = max.getY() + SPAWN_HEIGHT;

		int padding = this.config.spawnPadding();
		if (size.getX() <= padding * 2 || size.getZ() <= padding * 2) {
			return new BlockPos(padding, y, padding);
		}

		int x = MathHelper.nextBetween(this.random, min.getX() + padding, max.getX() - padding);
		int z = MathHelper.nextBetween(this.random, min.getZ() + padding, max.getZ() - padding);

		return new BlockPos(x, y, z);
	}

	private void spawnPrism() {
		if (!this.config.randomlySpawnable().isEmpty()) {
			Prism prism = Util.getRandom(this.config.randomlySpawnable(), this.random);
			this.spawnedPrism = new SpawnedPrism(this, this.config, prism, this.getSpawnPos());
		}

		this.phase.sendMessage(PRISM_SPAWNED_MESSAGE);
		this.resetRoundsUntilSpawn();
	}

	public void removeSpawnedPrism() {
		this.spawnedPrism = null;
	}

	public void onRoundEnd() {
		if (this.spawnedPrism == null) {
			this.roundsUntilSpawn -= 1;

			if (this.roundsUntilSpawn == 0) {
				this.spawnPrism();
			}
		}
	}

	public void addPlayer(ServerPlayerEntity player) {
		if (this.spawnedPrism != null) {
			this.spawnedPrism.addPlayer(player);
		}
	}

	public void removePlayer(ServerPlayerEntity player) {
		if (this.spawnedPrism != null) {
			this.spawnedPrism.removePlayer(player);
		}
	}

	public void tick() {
		if (this.spawnedPrism != null) {
			this.spawnedPrism.tick();
		}
	}

	public ColorSwapActivePhase getPhase() {
		return this.phase;
	}

	@Override
	public String toString() {
		return "PrismSpawner{phase=" + this.phase + ", config=" + this.config + ", spawnedPrism=" + this.spawnedPrism + "}";
	}
}
