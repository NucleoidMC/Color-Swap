package io.github.haykam821.colorswap.game.prism.spawner;

import java.util.Random;

import eu.pb4.holograms.api.Holograms;
import eu.pb4.holograms.api.elements.HologramElement;
import eu.pb4.holograms.api.elements.SpacingHologramElement;
import eu.pb4.holograms.api.elements.item.SpinningItemHologramElement;
import eu.pb4.holograms.api.elements.text.MovingTextHologramElement;
import eu.pb4.holograms.api.holograms.AbstractHologram;
import io.github.haykam821.colorswap.Main;
import io.github.haykam821.colorswap.game.item.PrismItem;
import io.github.haykam821.colorswap.game.phase.ColorSwapActivePhase;
import io.github.haykam821.colorswap.game.prism.Prism;
import io.github.haykam821.colorswap.game.prism.PrismConfig;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import xyz.nucleoid.plasmid.util.PlayerRef;

public class SpawnedPrism {
	private static final ParticleEffect PARTICLE = ParticleTypes.SNOWFLAKE;

	private final PrismSpawner spawner;
	private final PrismConfig config;
	private final Prism prism;

	private final Vec3d pos;
	private final Box box;

	private final AbstractHologram hologram;

	public SpawnedPrism(PrismSpawner spawner, PrismConfig config, Prism prism, BlockPos pos) {
		this.spawner = spawner;
		this.config = config;
		this.prism = prism;

		Random random = spawner.getPhase().getWorld().getRandom();
		double size = config.size().get(random);

		this.pos = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
		this.box = Box.of(this.pos, size, size, size);

		ColorSwapActivePhase phase = this.spawner.getPhase();
		this.hologram = Holograms.create(phase.getWorld(), this.pos, new HologramElement[] {
			new MovingTextHologramElement(this.prism.getName()),
			new SpacingHologramElement(0.5),
			new SpinningItemHologramElement(this.prism.createDisplayStack())
		});

		for (PlayerRef player : phase.getPlayers()) {
			player.ifOnline(phase.getWorld(), this.hologram::addPlayer);
		}
		this.hologram.show();
	}

	public void addPlayer(ServerPlayerEntity player) {
		this.hologram.addPlayer(player);
	}

	public void removePlayer(ServerPlayerEntity player) {
		this.hologram.removePlayer(player);
	}

	public void remove() {
		this.hologram.hide();
		this.spawner.removeSpawnedPrism();
	}

	private boolean tryCollect(ServerPlayerEntity player) {
		if (!this.isPlayerIntersecting(player)) {
			return false;
		}

		PlayerInventory inventory = player.getInventory();

		int count = inventory.count(Main.PRISM);
		int maximumHeld = this.config.maximumHeld();

		if (count >= maximumHeld) {
			return false;
		}

		ItemStack stack = new ItemStack(Main.PRISM);
		PrismItem.setPrism(stack, this.prism);

		int slot = (9 - maximumHeld) / 2 + count;
		inventory.setStack(slot, stack);

		Text message = new TranslatableText("text.colorswap.prism.picked_up", player.getDisplayName()).formatted(Formatting.GOLD);
		this.spawner.getPhase().sendMessage(message);

		return true;
	}

	private boolean isPlayerIntersecting(ServerPlayerEntity player) {
		return player != null && this.box.intersects(player.getBoundingBox());
	}

	private void spawnParticles() {
		ColorSwapActivePhase phase = this.spawner.getPhase();
		Random random = phase.getWorld().getRandom();

		double x = this.box.minX + random.nextDouble(this.box.getXLength());
		double y = this.box.minY + random.nextDouble(this.box.getYLength());
		double z = this.box.minZ + random.nextDouble(this.box.getZLength());

		phase.getWorld().spawnParticles(PARTICLE, x, y, z, 1, 0, 0, 0, 0);
	}

	public void tick() {
		ColorSwapActivePhase phase = this.spawner.getPhase();
		for (PlayerRef ref : phase.getPlayers()) {
			ServerPlayerEntity player = ref.getEntity(phase.getWorld());

			if (this.tryCollect(player)) {
				this.remove();
				return;
			}
		}

		this.spawnParticles();
	}

	@Override
	public String toString() {
		return "SpawnedPrism{box=" + this.box + ", prism=" + this.prism + "}";
	}
}
