package io.github.haykam821.colorswap.game.prism;

import io.github.haykam821.colorswap.game.phase.ColorSwapActivePhase;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;

public class SplashPrism extends Prism {
	private static final double MIN_RADIUS = 3;
	private static final double MAX_RADIUS = 5;

	@Override
	public boolean activate(ColorSwapActivePhase phase, ServerPlayer player) {
		Block swapBlock = phase.getCurrentSwapBlock();
		if (swapBlock == null || phase.hasLastErased()) {
			return false;
		}

		ServerLevel world = phase.getLevel();
		BlockState state = swapBlock.defaultBlockState();
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

		double radius = Mth.nextDouble(world.getRandom(), MIN_RADIUS, MAX_RADIUS);
		double radius2 = radius * radius;

		for (int x = -Mth.floor(radius); x < radius; x++) {
			for (int z = -Mth.floor(radius); z < radius; z++) {
				if (x * x + z * z < radius2) {
					pos.set(player.getX() + x, 64, player.getZ() + z);
					if (!world.getBlockState(pos).isAir()) {
						world.setBlockAndUpdate(pos, state);
					}
				}
			}
		}

		world.playSound(null, player, SoundEvents.BUCKET_EMPTY, SoundSource.PLAYERS, 0.3f, 1);
		return true;
	}

	@Override
	public Item getDisplayItem() {
		return Items.WATER_BUCKET;
	}
}
