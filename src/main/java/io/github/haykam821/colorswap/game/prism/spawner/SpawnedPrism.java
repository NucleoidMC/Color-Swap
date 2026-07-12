package io.github.haykam821.colorswap.game.prism.spawner;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.ChunkAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.TextDisplayElement;
import io.github.haykam821.colorswap.game.component.PrismComponent;
import io.github.haykam821.colorswap.game.item.ColorSwapItems;
import io.github.haykam821.colorswap.game.phase.ColorSwapActivePhase;
import io.github.haykam821.colorswap.game.prism.Prism;
import io.github.haykam821.colorswap.game.prism.PrismConfig;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Display.BillboardConstraints;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.util.Mth;
import com.mojang.math.Axis;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.RandomSource;
import xyz.nucleoid.plasmid.api.util.PlayerRef;

public class SpawnedPrism {
	private static final Vec3 TEXT_OFFSET = new Vec3(0, 1, 0);

	private static final float ITEM_SCALE = 0.6f;
	private static final float ITEM_SCALE_VARIANCE = 0.05f;

	private static final ItemStack CRYSTAL_STACK = createGlintStack(Items.STAINED_GLASS.white());
	private static final float CRYSTAL_SCALE = 0.8f;

	private static final ParticleOptions PARTICLE = ParticleTypes.SNOWFLAKE;

	private final PrismSpawner spawner;
	private final PrismConfig config;
	private final Prism prism;

	private final Vec3 pos;
	private final AABB box;

	private final ItemDisplayElement item;
	private final ItemDisplayElement crystal = new ItemDisplayElement(CRYSTAL_STACK);

	private final ElementHolder holder = new ElementHolder();
	private final HolderAttachment attachment;

	public SpawnedPrism(PrismSpawner spawner, PrismConfig config, Prism prism, BlockPos pos) {
		this.spawner = spawner;
		this.config = config;
		this.prism = prism;

		RandomSource random = spawner.getPhase().getLevel().getRandom();
		double size = config.size().sample(random);

		this.pos = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
		this.box = AABB.ofSize(this.pos, size, size, size);

		// Text display element for prism name
		TextDisplayElement text = new TextDisplayElement(this.prism.getName());

		text.setOffset(TEXT_OFFSET);
		text.setBillboardMode(BillboardConstraints.CENTER);

		// Item display element for prism display stack
		ItemStack stack = new ItemStack(ColorSwapItems.PRISM);
		PrismComponent.set(stack, this.prism);

		this.item = new ItemDisplayElement(stack);

		this.item.setInterpolationDuration(1);
		this.item.setLeftRotation(Axis.YP.rotation(Mth.PI));
		this.item.setScale(new Vector3f(ITEM_SCALE));
		this.item.setBillboardMode(BillboardConstraints.CENTER);

		// Rotating crystal display element
		this.crystal.setInterpolationDuration(1);
		this.crystal.setScale(new Vector3f(CRYSTAL_SCALE));

		this.holder.addElement(text);
		this.holder.addElement(this.item);
		this.holder.addElement(this.crystal);

		this.updateDisplays();

		ColorSwapActivePhase phase = this.spawner.getPhase();
		this.attachment = ChunkAttachment.of(this.holder, phase.getLevel(), this.pos);
	}

	public void remove() {
		this.attachment.destroy();
		this.spawner.removeSpawnedPrism();
	}

	private boolean tryCollect(ServerPlayer player) {
		if (!this.isPlayerIntersecting(player)) {
			return false;
		}

		Inventory inventory = player.getInventory();

		int count = inventory.countItem(ColorSwapItems.PRISM);
		int maximumHeld = this.config.maximumHeld();

		if (count >= maximumHeld) {
			return false;
		}

		ItemStack stack = new ItemStack(ColorSwapItems.PRISM);
		PrismComponent.set(stack, this.prism);

		int slot = (9 - maximumHeld) / 2 + count;
		inventory.setItem(slot, stack);

		Component message = Component.translatable("text.colorswap.prism.picked_up", player.getDisplayName()).withStyle(ChatFormatting.GOLD);
		this.spawner.getPhase().sendMessage(message);

		return true;
	}

	private boolean isPlayerIntersecting(ServerPlayer player) {
		return player != null && this.box.intersects(player.getBoundingBox());
	}

	private void updateDisplays() {
		long time = this.spawner.getPhase().getLevel().getGameTime();
		float angle = time / 10f;

		Quaternionf rotation = new Quaternionf()
			.rotateY(Mth.PI + angle)
			.rotateZ(angle);

		this.crystal.setLeftRotation(rotation);
		this.crystal.startInterpolation();

		float scale = Mth.cos(time / 3.5f) * ITEM_SCALE_VARIANCE;

		this.item.setScale(new Vector3f(ITEM_SCALE + scale, ITEM_SCALE + scale, ITEM_SCALE));
		this.item.startInterpolation();

		this.holder.tick();
	}

	private void spawnParticles() {
		ColorSwapActivePhase phase = this.spawner.getPhase();
		RandomSource random = phase.getLevel().getRandom();

		double x = this.box.minX + random.nextDouble() * this.box.getXsize();
		double y = this.box.minY + random.nextDouble() * this.box.getYsize();
		double z = this.box.minZ + random.nextDouble() * this.box.getZsize();

		phase.getLevel().sendParticles(PARTICLE, x, y, z, 1, 0, 0, 0, 0);
	}

	public void tick() {
		ColorSwapActivePhase phase = this.spawner.getPhase();
		for (PlayerRef ref : phase.getPlayers()) {
			ServerPlayer player = ref.getEntity(phase.getLevel());

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

	private static ItemStack createGlintStack(ItemLike item) {
		ItemStack stack = new ItemStack(item);
		stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
		return stack;
	}
}
