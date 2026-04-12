package io.github.haykam821.colorswap.game.phase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import io.github.haykam821.colorswap.game.ColorSwapConfig;
import io.github.haykam821.colorswap.game.ColorSwapTimerBar;
import io.github.haykam821.colorswap.game.component.PrismComponent;
import io.github.haykam821.colorswap.game.item.ColorSwapItems;
import io.github.haykam821.colorswap.game.map.ColorSwapMap;
import io.github.haykam821.colorswap.game.map.ColorSwapMapConfig;
import io.github.haykam821.colorswap.game.prism.Prism;
import io.github.haykam821.colorswap.game.prism.spawner.PrismSpawner;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.ChatFormatting;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import xyz.nucleoid.packettweaker.PacketContext;
import xyz.nucleoid.plasmid.api.game.GameActivity;
import xyz.nucleoid.plasmid.api.game.GameCloseReason;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.common.GlobalWidgets;
import xyz.nucleoid.plasmid.api.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.api.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.api.game.player.JoinAcceptor;
import xyz.nucleoid.plasmid.api.game.player.JoinAcceptorResult;
import xyz.nucleoid.plasmid.api.game.player.JoinOffer;
import xyz.nucleoid.plasmid.api.game.rule.GameRuleType;
import xyz.nucleoid.plasmid.api.util.PlayerRef;
import xyz.nucleoid.stimuli.event.EventResult;
import xyz.nucleoid.stimuli.event.item.ItemUseEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDamageEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

public class ColorSwapActivePhase {
	private final ServerLevel world;
	private final GameSpace gameSpace;
	private final ColorSwapMap map;
	private final ColorSwapConfig config;
	private final List<PlayerRef> players;
	private HolderAttachment guideText;
	private int maxTicksUntilSwap;
	private int ticksUntilSwap = 0;
	private List<Block> lastSwapBlocks = new ArrayList<>();
	private Block swapBlock;
	private boolean lastErased = true;
	private boolean singleplayer;
	private final ColorSwapTimerBar timerBar;
	private final PrismSpawner prismSpawner;
	private int rounds = 0;
	private int ticksElapsed = 0;
	private int ticksUntilClose = -1;

	public ColorSwapActivePhase(ServerLevel world, GameSpace gameSpace, ColorSwapMap map, ColorSwapConfig config, List<PlayerRef> players, HolderAttachment guideText, GlobalWidgets widgets) {
		this.world = world;
		this.gameSpace = gameSpace;
		this.map = map;
		this.config = config;
		this.players = players;

		this.guideText = guideText;

		this.timerBar = new ColorSwapTimerBar(widgets);
		this.maxTicksUntilSwap = this.getSwapTime();

		this.prismSpawner = this.config.getPrismConfig().map(prismConfig -> {
			return new PrismSpawner(this, prismConfig, this.world.getRandom());
		}).orElse(null);
	}

	public static void setRules(GameActivity activity) {
		activity.deny(GameRuleType.CRAFTING);
		activity.deny(GameRuleType.FALL_DAMAGE);
		activity.deny(GameRuleType.HUNGER);
		activity.deny(GameRuleType.PORTALS);
		activity.allow(GameRuleType.PVP);
		activity.deny(GameRuleType.THROW_ITEMS);
		activity.deny(GameRuleType.MODIFY_ARMOR);
		activity.deny(GameRuleType.MODIFY_INVENTORY);
	}

	public static void open(GameSpace gameSpace, ServerLevel world, ColorSwapMap map, ColorSwapConfig config, HolderAttachment guideText) {
		gameSpace.setActivity(activity -> {
			GlobalWidgets widgets = GlobalWidgets.addTo(activity);

			List<PlayerRef> players = gameSpace.getPlayers()
				.participants()
				.stream()
				.map(PlayerRef::of)
				.collect(Collectors.toList());

			Collections.shuffle(players);

			ColorSwapActivePhase active = new ColorSwapActivePhase(world, gameSpace, map, config, players, guideText, widgets);

			ColorSwapActivePhase.setRules(activity);

			// Listeners
			activity.listen(GameActivityEvents.DISABLE, active::close);
			activity.listen(GameActivityEvents.ENABLE, active::enable);
			activity.listen(GameActivityEvents.TICK, active::tick);
			activity.listen(GamePlayerEvents.ACCEPT, active::onAcceptPlayer);
			activity.listen(GamePlayerEvents.OFFER, JoinOffer::acceptSpectators);
			activity.listen(GamePlayerEvents.REMOVE, active::removePlayer);
			activity.listen(PlayerDamageEvent.EVENT, active::onPlayerDamage);
			activity.listen(PlayerDeathEvent.EVENT, active::onPlayerDeath);
			activity.listen(ItemUseEvent.EVENT, active::onUseItem);
		});
	}

	public void enable() {
		int index = 0;
		this.singleplayer = this.players.size() == 1;

		for (PlayerRef playerRef : this.players) {
			ServerPlayer player = playerRef.getEntity(this.world);

			if (player != null) {
				this.updateRoundsExperienceLevel(player);
				player.setGameMode(GameType.ADVENTURE);

				double theta = ((double) index / this.players.size()) * 2 * Math.PI;
				float yaw = (float) theta * Mth.RAD_TO_DEG + 90;

				Vec3 spawnPos = this.map.getSpawnPos(theta);
				ColorSwapActivePhase.spawn(this.world, spawnPos, yaw, player);
			}

			index++;
		}

		for (ServerPlayer player : this.gameSpace.getPlayers().spectators()) {
			ColorSwapActivePhase.spawn(this.world, this.map.getSpectatorSpawnPos(), 0, player);
			this.setSpectator(player);
		}
	}

	public void close() {
		this.timerBar.remove();
	}

	public void updateRoundsExperienceLevel(ServerPlayer player) {
		player.setExperienceLevels(this.rounds);
	}

	private void setRounds(int rounds) {
		this.rounds = rounds;
		for (ServerPlayer player : this.gameSpace.getPlayers()) {
			this.updateRoundsExperienceLevel(player);
		}
	}

	public void eraseTile(BlockPos.MutableBlockPos origin, int xSize, int zSize, BlockStateProvider erasedStateProvider) {
		boolean keep = this.world.getBlockState(origin).is(this.swapBlock);

		BlockPos.MutableBlockPos pos = origin.mutable();
		for (int x = origin.getX(); x < origin.getX() + xSize; x++) {
			for (int z = origin.getZ(); z < origin.getZ() + zSize; z++) {
				pos.set(x, origin.getY(), z);

				if (!keep) {
					BlockState oldState = this.world.getBlockState(pos);
					BlockState newState = erasedStateProvider.getState(this.world.getRandom(), pos);

					this.world.getChunkAt(pos).setBlockState(pos, newState);
					this.world.sendBlockUpdated(pos, oldState, newState, 0);
				}
			}
		}
	}

	public void erase() {
		ColorSwapMapConfig mapConfig = this.config.getMapConfig();

 		for (ServerPlayer player : this.gameSpace.getPlayers()) {
			player.connection.send(new ClientboundSoundEntityPacket(Holder.direct(this.config.getSwapSound()), SoundSource.BLOCKS, player, 1, 1.5f, world.getRandom().nextLong()));
		}

		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

		// Iterate over blocks when necessary to avoid conflicts with the splash prism
		int xScale = this.prismSpawner == null ? mapConfig.xScale : 1;
		int zScale = this.prismSpawner == null ? mapConfig.zScale : 1;

		for (int x = 0; x < mapConfig.x * mapConfig.xScale; x += xScale) {
			for (int z = 0; z < mapConfig.z * mapConfig.zScale; z += zScale) {
				pos.set(x, 64, z);
				this.eraseTile(pos, xScale, zScale, mapConfig.erasedStateProvider);
			}
		}
	}

	private Block getSwapBlock() {
		return this.lastSwapBlocks.get(this.world.getRandom().nextInt(this.lastSwapBlocks.size()));
	}

	public Block getCurrentSwapBlock() {
		return this.swapBlock;
	}

	public boolean hasLastErased() {
		return this.lastErased;
	}

	public void placeTile(BlockPos.MutableBlockPos origin, int xSize, int zSize, BlockState state) {
		BlockPos.MutableBlockPos pos = origin.mutable();
		for (int x = origin.getX(); x < origin.getX() + xSize; x++) {
			for (int z = origin.getZ(); z < origin.getZ() + zSize; z++) {
				pos.set(x, origin.getY(), z);

				BlockState oldState = this.world.getBlockState(pos);
				this.world.getChunkAt(pos).setBlockState(pos, state);
				this.world.sendBlockUpdated(pos, oldState, state, 0);
			}
		}
	}

	public void swap() {
		ColorSwapMapConfig mapConfig = this.config.getMapConfig();
		this.lastSwapBlocks.clear();

		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int x = 0; x < mapConfig.x * mapConfig.xScale; x += mapConfig.xScale) {
			for (int z = 0; z < mapConfig.z * mapConfig.zScale; z += mapConfig.zScale) {
				pos.set(x, 64, z);

				Block block = this.config.getMapConfig().getPlatformBlock(this.world.getRandom());
				if (!this.lastSwapBlocks.contains(block)) {
					this.lastSwapBlocks.add(block);
				}

				this.placeTile(pos, mapConfig.xScale, mapConfig.zScale, block.defaultBlockState());
			}
		}
	}

	private void giveSwapBlocks() {
		ItemStack stack = new ItemStack(this.swapBlock);

		for (PlayerRef playerRef : this.players) {
			playerRef.ifOnline(this.world, player -> {
				Inventory inventory = player.getInventory();

				for (int slot = 0; slot < 9; slot++) {
					if (!inventory.getItem(slot).is(ColorSwapItems.PRISM)) {
						inventory.setItem(slot, stack.copy());
					}
				}

				// Update inventory
				player.containerMenu.broadcastChanges();
				player.inventoryMenu.slotsChanged(inventory);
			});
		}
	}

	private void checkElimination() {
		Iterator<PlayerRef> iterator = this.players.iterator();
		while (iterator.hasNext()) {
			PlayerRef playerRef = iterator.next();
			playerRef.ifOnline(this.world, player -> {
				if (this.map.isBelowPlatform(player) || (this.prismSpawner == null && this.map.isAbovePlatform(player, this.isKnockbackEnabled()))) {
					this.eliminate(player, false);
					iterator.remove();
				}
			});
		}
	}

	public float getTimerBarPercent() {
		return this.ticksUntilSwap / (float) this.maxTicksUntilSwap;
	}

	private int getSwapTime() {
		int swapTime = this.config.getSwapTime();
		if (swapTime >= 0) return swapTime;

		double swapSeconds = Math.pow(5, -0.04 * this.rounds + 1) + 0.5;
		return (int) (swapSeconds * 20);
	}

	private int getEraseTime() {
		int eraseTime = this.config.getEraseTime();
		if (eraseTime >= 0) return eraseTime;

		return this.rounds > 10 ? 20 : 20 * 2;
	}

	private Component getKnockbackEnabledText() {
		return Component.translatable("text.colorswap.knockback_enabled").withStyle(ChatFormatting.RED);
	}

	public void tick() {
		this.ticksElapsed += 1;

		if (this.guideText != null) {
			if (this.ticksElapsed == this.config.getGuideTicks()) {
				this.guideText.destroy();
				this.guideText = null;
			}
		}

		// Decrease ticks until game end to zero
		if (this.isGameEnding()) {
			if (this.ticksUntilClose == 0) {
				this.gameSpace.close(GameCloseReason.FINISHED);
			}

			this.ticksUntilClose -= 1;
			return;
		}

		this.ticksUntilSwap -= 1;
		this.timerBar.tick(this);
		if (this.ticksUntilSwap <= 0) {
			if (this.lastErased) {
				this.swap();

				this.swapBlock = this.getSwapBlock();
				this.lastErased = false;
				this.giveSwapBlocks();

				this.setRounds(this.rounds + 1);
				this.maxTicksUntilSwap = this.getSwapTime();
				if (this.rounds - 1 == this.config.getNoKnockbackRounds()) {
					this.sendMessage(this.getKnockbackEnabledText());
				}

				if (this.prismSpawner != null) {
					this.prismSpawner.onRoundEnd();
				}
			} else {
				this.erase();
				this.lastErased = true;

				this.maxTicksUntilSwap = this.getEraseTime();
			}
			this.ticksUntilSwap = this.maxTicksUntilSwap;
		}

		if (this.prismSpawner != null) {
			this.prismSpawner.tick();
		}

		this.checkElimination();

		if (this.players.size() < 2) {
			if (this.players.size() == 1 && this.singleplayer) return;

			this.sendMessage(this.getEndingMessage());

			this.endGame();
		}
	}

	private Component getEndingMessage() {
		if (this.players.size() == 1) {
			PlayerRef winnerRef = this.players.iterator().next();
			Player winner = winnerRef.getEntity(this.world);
			if (winner != null) {
				return Component.translatable("text.colorswap.win", winner.getDisplayName()).withStyle(ChatFormatting.GOLD);
			}
		}
		return Component.translatable("text.colorswap.no_winners").withStyle(ChatFormatting.GOLD);
	}

	public void sendMessage(Component message) {
		this.gameSpace.getPlayers().sendMessage(message);
	}

	private void setSpectator(ServerPlayer player) {
		player.setGameMode(GameType.SPECTATOR);
	}

	public JoinAcceptorResult onAcceptPlayer(JoinAcceptor acceptor) {
		return acceptor.teleport(this.world, this.map.getSpectatorSpawnPos()).thenRunForEach(player -> {
			this.updateRoundsExperienceLevel(player);
			this.setSpectator(player);
		});
	}

	public void removePlayer(ServerPlayer player) {
		this.eliminate(player, true);
	}

	private boolean isKnockbackEnabled() {
		if (this.config.getNoKnockbackRounds() < 0) return false;
		return this.rounds - 1 >= this.config.getNoKnockbackRounds();
	}

	private EventResult onPlayerDamage(ServerPlayer player, DamageSource source, float amount) {
		return this.isKnockbackEnabled() ? EventResult.ALLOW : EventResult.DENY;
	}

	public void eliminate(ServerPlayer eliminatedPlayer, boolean remove) {
		if (this.isGameEnding()) return;

		PlayerRef eliminatedRef = PlayerRef.of(eliminatedPlayer);
		if (!this.players.contains(eliminatedRef)) return;

		Component message = Component.translatable("text.colorswap.eliminated", eliminatedPlayer.getDisplayName()).withStyle(ChatFormatting.RED);
		this.sendMessage(message);

		if (remove) {
			this.players.remove(eliminatedRef);
		}
		this.setSpectator(eliminatedPlayer);
	}

	private void endGame() {
		this.ticksUntilClose = this.config.getTicksUntilClose().sample(this.world.getRandom());
	}

	private boolean isGameEnding() {
		return this.ticksUntilClose >= 0;
	}

	public EventResult onPlayerDeath(ServerPlayer player, DamageSource source) {
		this.eliminate(player, true);
		return EventResult.ALLOW;
	}

	public InteractionResult onUseItem(ServerPlayer player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);

		PlayerRef ref = PlayerRef.of(player);
		if (this.players.contains(ref) && stack.is(ColorSwapItems.PRISM)) {
			Prism prism = PrismComponent.get(stack);

			if (prism != null && prism.activate(this, player)) {
				ItemStack newStack = new ItemStack(this.swapBlock);
				player.setItemInHand(hand, newStack);

				return InteractionResult.SUCCESS_SERVER.heldItemTransformedTo(newStack);
			}
		}

		if (PolymerItemUtils.getPolymerItemStack(stack, PacketContext.create(player)).is(Items.ENDER_PEARL)) {
			player.setItemInHand(hand, stack.copy());
		}

		return InteractionResult.PASS;
	}

	public static void spawn(ServerLevel world, Vec3 spawnPos, float yaw, ServerPlayer player) {
		player.teleportTo(world, spawnPos.x(), spawnPos.y(), spawnPos.z(), Set.of(), yaw, 0, false);

		player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, MobEffectInstance.INFINITE_DURATION, 0, true, false));
		player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, MobEffectInstance.INFINITE_DURATION, 127, true, false));
	}

	public ColorSwapMap getMap() {
		return this.map;
	}

	public ServerLevel getWorld() {
		return this.world;
	}

	public List<PlayerRef> getPlayers() {
		return this.players;
	}
}
