package io.github.haykam821.colorswap.game.prism.spawner;

import io.github.haykam821.colorswap.game.phase.ColorSwapActivePhase;
import io.github.haykam821.colorswap.game.prism.Prism;
import io.github.haykam821.colorswap.game.prism.PrismConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import xyz.nucleoid.map_templates.BlockBounds;

public class PrismSpawner {
	private static final int SPAWN_HEIGHT = 2;
	private static final Component PRISM_SPAWNED_MESSAGE = Component.translatable("text.colorswap.prism.spawned").withStyle(ChatFormatting.GOLD);

	private final ColorSwapActivePhase phase;
	private final PrismConfig config;
	private final RandomSource random;

	private SpawnedPrism spawnedPrism;
	private int roundsUntilSpawn;

	public PrismSpawner(ColorSwapActivePhase phase, PrismConfig config, RandomSource random) {
		this.phase = phase;
		this.config = config;
		this.random = random;

		this.resetRoundsUntilSpawn();
	}

	private void resetRoundsUntilSpawn() {
		this.roundsUntilSpawn = this.config.roundsBetweenSpawns().sample(this.random);
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

		int x = Mth.randomBetweenInclusive(this.random, min.getX() + padding, max.getX() - padding);
		int z = Mth.randomBetweenInclusive(this.random, min.getZ() + padding, max.getZ() - padding);

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
