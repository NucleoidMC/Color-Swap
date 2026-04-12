package io.github.haykam821.colorswap.game.prism;

import java.util.Optional;

import io.github.haykam821.colorswap.game.phase.ColorSwapActivePhase;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.phys.Vec3;

public class LeapPrism extends Prism {
	private static final Holder<SoundEvent> INTENTIONALLY_EMPTY = Holder.direct(SoundEvents.EMPTY);

	private static final double LEAP_MULTIPLIER = 1.2;

	private static final double LEAP_MIN_Y = 0.15;
	private static final double STEALTHY_LEAP_MIN_Y = 0;

	@Override
	public boolean activate(ColorSwapActivePhase phase, ServerPlayer player) {
		Vec3 velocity = LeapPrism.getLeapVelocity(player);
		Packet<?> packet = new ClientboundExplodePacket(Vec3.ZERO, 0, 0, Optional.of(velocity), ParticleTypes.EXPLOSION, INTENTIONALLY_EMPTY, WeightedList.of());

		player.connection.send(packet);
		phase.getWorld().playSound(null, player, SoundEvents.HORSE_SADDLE.value(), SoundSource.PLAYERS, 0.3f, 1.1f);

		return true;
	}

	@Override
	public Item getDisplayItem() {
		return Items.FEATHER;
	}

	public static Vec3 getLeapVelocity(ServerPlayer player) {
		Vec3 facing = Vec3
			.directionFromRotation(player.getXRot(), player.getYRot())
			.scale(LEAP_MULTIPLIER);

		double y = Math.max(LeapPrism.getLeapMinY(player), facing.y());
		return new Vec3(facing.x(), y, facing.z());
	}

	private static double getLeapMinY(ServerPlayer player) {
		if (player.isShiftKeyDown()) {
			return STEALTHY_LEAP_MIN_Y;
		} else {
			return LEAP_MIN_Y;
		}
	}
}
