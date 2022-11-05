package io.github.haykam821.colorswap.game.prism;

import io.github.haykam821.colorswap.game.phase.ColorSwapActivePhase;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class WarpPrism extends Prism {
	@Override
	public void activate(ColorSwapActivePhase phase, ServerPlayerEntity player) {
		Vec3d pos;

		HitResult hit = player.raycast(32, 0, false);
		if (hit instanceof BlockHitResult) {
			BlockPos blockPos = ((BlockHitResult) hit).getBlockPos();
			pos = new Vec3d(blockPos.getX() + 0.5, blockPos.getY() + 1, blockPos.getZ());
		} else {
			pos = hit.getPos();
		}

		player.teleport(pos.getX(), pos.getY(), pos.getZ(), true);
		phase.getWorld().playSoundFromEntity(null, player, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 0.3f, 1);
	}

	@Override
	public Item getDisplayItem() {
		return Items.ENDER_PEARL;
	}
}
