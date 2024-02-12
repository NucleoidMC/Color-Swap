package io.github.haykam821.colorswap.game.prism;

import java.util.Collections;

import io.github.haykam821.colorswap.game.phase.ColorSwapActivePhase;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.explosion.Explosion.DestructionType;

public class LeapPrism extends Prism {
	private static final double LEAP_MULTIPLIER = 1.2;

	private static final double LEAP_MIN_Y = 0.15;
	private static final double STEALTHY_LEAP_MIN_Y = 0;

	@Override
	public boolean activate(ColorSwapActivePhase phase, ServerPlayerEntity player) {
		Vec3d velocity = LeapPrism.getLeapVelocity(player);
		Packet<?> packet = new ExplosionS2CPacket(0, 0, 0, 0, Collections.emptyList(), velocity, DestructionType.KEEP, ParticleTypes.EXPLOSION, ParticleTypes.EXPLOSION_EMITTER, SoundEvents.INTENTIONALLY_EMPTY);

		player.networkHandler.sendPacket(packet);
		phase.getWorld().playSoundFromEntity(null, player, SoundEvents.ENTITY_HORSE_SADDLE, SoundCategory.PLAYERS, 0.3f, 1.1f);

		return true;
	}

	@Override
	public Item getDisplayItem() {
		return Items.FEATHER;
	}

	public static Vec3d getLeapVelocity(ServerPlayerEntity player) {
		Vec3d facing = Vec3d
			.fromPolar(player.getPitch(), player.getYaw())
			.multiply(LEAP_MULTIPLIER);

		double y = Math.max(LeapPrism.getLeapMinY(player), facing.getY());
		return new Vec3d(facing.getX(), y, facing.getZ());
	}

	private static double getLeapMinY(ServerPlayerEntity player) {
		if (player.isSneaking()) {
			return STEALTHY_LEAP_MIN_Y;
		} else {
			return LEAP_MIN_Y;
		}
	}
}
