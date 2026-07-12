package io.github.haykam821.colorswap.game.prism;

import io.github.haykam821.colorswap.game.phase.ColorSwapActivePhase;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class WarpPrism extends Prism {
	@Override
	public boolean activate(ColorSwapActivePhase phase, ServerPlayer player) {
		Vec3 pos;

		HitResult hit = player.pick(32, 0, false);
		if (hit instanceof BlockHitResult) {
			BlockPos blockPos = ((BlockHitResult) hit).getBlockPos();
			pos = new Vec3(blockPos.getX() + 0.5, blockPos.getY() + 1, blockPos.getZ());
		} else {
			pos = hit.getLocation();
		}

		if (hit.getType() != HitResult.Type.MISS && player.randomTeleport(pos.x(), pos.y(), pos.z(), true)) {
			phase.getLevel().playSound(null, player, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.3f, 1);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public Item getDisplayItem() {
		return Items.ENDER_PEARL;
	}
}
