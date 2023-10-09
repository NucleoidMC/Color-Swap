package io.github.haykam821.colorswap.game.prism.spawner;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.ChunkAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.TextDisplayElement;
import io.github.haykam821.colorswap.Main;
import io.github.haykam821.colorswap.game.item.PrismItem;
import io.github.haykam821.colorswap.game.phase.ColorSwapActivePhase;
import io.github.haykam821.colorswap.game.prism.Prism;
import io.github.haykam821.colorswap.game.prism.PrismConfig;
import net.minecraft.entity.decoration.DisplayEntity.BillboardMode;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import xyz.nucleoid.plasmid.util.PlayerRef;

public class SpawnedPrism {
	private static final Vec3d TEXT_OFFSET = new Vec3d(0, 1, 0);

	private static final float ITEM_SCALE = 0.6f;
	private static final float ITEM_SCALE_VARIANCE = 0.05f;

	private static final ItemStack CRYSTAL_STACK = Prism.createGlintStack(Items.WHITE_STAINED_GLASS);
	private static final float CRYSTAL_SCALE = 0.8f;

	private static final ParticleEffect PARTICLE = ParticleTypes.SNOWFLAKE;

	private final PrismSpawner spawner;
	private final PrismConfig config;
	private final Prism prism;

	private final Vec3d pos;
	private final Box box;

	private final ItemDisplayElement item;
	private final ItemDisplayElement crystal = new ItemDisplayElement(CRYSTAL_STACK);

	private final HolderAttachment attachment;

	public SpawnedPrism(PrismSpawner spawner, PrismConfig config, Prism prism, BlockPos pos) {
		this.spawner = spawner;
		this.config = config;
		this.prism = prism;

		Random random = spawner.getPhase().getWorld().getRandom();
		double size = config.size().get(random);

		this.pos = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
		this.box = Box.of(this.pos, size, size, size);

		// Text display element for prism name
		TextDisplayElement text = new TextDisplayElement(this.prism.getName());

		text.setOffset(TEXT_OFFSET);
		text.setBillboardMode(BillboardMode.CENTER);

		// Item display element for prism display stack
		this.item = new ItemDisplayElement(this.prism.createDisplayStack());

		this.item.setInterpolationDuration(1);
		this.item.setLeftRotation(RotationAxis.POSITIVE_Y.rotation(MathHelper.PI));
		this.item.setScale(new Vector3f(ITEM_SCALE));
		this.item.setBillboardMode(BillboardMode.CENTER);

		// Rotating crystal display element
		this.crystal.setInterpolationDuration(1);
		this.crystal.setScale(new Vector3f(CRYSTAL_SCALE));

		ElementHolder holder = new ElementHolder();

		holder.addElement(text);
		holder.addElement(this.item);
		holder.addElement(this.crystal);

		ColorSwapActivePhase phase = this.spawner.getPhase();
		this.attachment = ChunkAttachment.ofTicking(holder, phase.getWorld(), this.pos);
	}

	public void remove() {
		this.attachment.destroy();
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

		Text message = Text.translatable("text.colorswap.prism.picked_up", player.getDisplayName()).formatted(Formatting.GOLD);
		this.spawner.getPhase().sendMessage(message);

		return true;
	}

	private boolean isPlayerIntersecting(ServerPlayerEntity player) {
		return player != null && this.box.intersects(player.getBoundingBox());
	}

	private void updateDisplays() {
		long time = this.attachment.getWorld().getTime();
		float angle = time / 10f;

		Quaternionf rotation = new Quaternionf()
			.rotateY(MathHelper.PI + angle)
			.rotateZ(angle);

		this.crystal.setLeftRotation(rotation);
		this.crystal.startInterpolation();

		float scale = MathHelper.cos(time / 3.5f) * ITEM_SCALE_VARIANCE;

		this.item.setScale(new Vector3f(ITEM_SCALE + scale, ITEM_SCALE + scale, ITEM_SCALE));
		this.item.startInterpolation();
	}

	private void spawnParticles() {
		ColorSwapActivePhase phase = this.spawner.getPhase();
		Random random = phase.getWorld().getRandom();

		double x = this.box.minX + random.nextDouble() * this.box.getXLength();
		double y = this.box.minY + random.nextDouble() * this.box.getYLength();
		double z = this.box.minZ + random.nextDouble() * this.box.getZLength();

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

		this.updateDisplays();
		this.spawnParticles();
	}

	@Override
	public String toString() {
		return "SpawnedPrism{box=" + this.box + ", prism=" + this.prism + "}";
	}
}
