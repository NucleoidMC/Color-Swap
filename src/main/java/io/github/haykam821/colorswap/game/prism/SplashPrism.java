package io.github.haykam821.colorswap.game.prism;

import io.github.haykam821.colorswap.game.phase.ColorSwapActivePhase;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class SplashPrism extends Prism {
	private static final double MIN_RADIUS = 3;
	private static final double MAX_RADIUS = 5;

	@Override
	public boolean activate(ColorSwapActivePhase phase, ServerPlayerEntity player) {
		Block swapBlock = phase.getCurrentSwapBlock();
		if (swapBlock == null || phase.hasLastErased()) {
			return false;
		}

		ServerWorld world = phase.getWorld();
		BlockState state = swapBlock.getDefaultState();
		BlockPos.Mutable pos = new BlockPos.Mutable();

		double radius = MathHelper.nextDouble(world.getRandom(), MIN_RADIUS, MAX_RADIUS);
		double radius2 = radius * radius;

		for (int x = -MathHelper.floor(radius); x < radius; x++) {
			for (int z = -MathHelper.floor(radius); z < radius; z++) {
				if (x * x + z * z < radius2) {
					pos.set(player.getX() + x, 64, player.getZ() + z);
					if (!world.getBlockState(pos).isAir()) {
						world.setBlockState(pos, state);
					}
				}
			}
		}

		world.playSoundFromEntity(null, player, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.PLAYERS, 0.3f, 1);
		return true;
	}

	@Override
	public Item getDisplayItem() {
		return Items.WATER_BUCKET;
	}
}
