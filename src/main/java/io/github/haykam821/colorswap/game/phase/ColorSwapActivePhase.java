package io.github.haykam821.colorswap.game.phase;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.github.haykam821.colorswap.game.ColorSwapConfig;
import io.github.haykam821.colorswap.game.ColorSwapTimerBar;
import io.github.haykam821.colorswap.game.map.ColorSwapMap;
import io.github.haykam821.colorswap.game.map.ColorSwapMapConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.GameCloseReason;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.common.GlobalWidgets;
import xyz.nucleoid.plasmid.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.game.player.PlayerOffer;
import xyz.nucleoid.plasmid.game.player.PlayerOfferResult;
import xyz.nucleoid.plasmid.game.rule.GameRuleType;
import xyz.nucleoid.plasmid.util.PlayerRef;
import xyz.nucleoid.stimuli.event.player.PlayerDamageEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

public class ColorSwapActivePhase {
	private final ServerWorld world;
	private final GameSpace gameSpace;
	private final ColorSwapMap map;
	private final ColorSwapConfig config;
	private final Set<PlayerRef> players;
	private int maxTicksUntilSwap;
	private int ticksUntilSwap = 0;
	private List<Block> lastSwapBlocks = new ArrayList<>();
	private Block swapBlock;
	private boolean singleplayer;
	private final ColorSwapTimerBar timerBar;
	private int rounds = 0;

	public ColorSwapActivePhase(ServerWorld world, GameSpace gameSpace, ColorSwapMap map, ColorSwapConfig config, Set<PlayerRef> players, GlobalWidgets widgets) {
		this.world = world;
		this.gameSpace = gameSpace;
		this.map = map;
		this.config = config;
		this.players = players;

		this.timerBar = new ColorSwapTimerBar(widgets);
		this.maxTicksUntilSwap = this.getSwapTime();
	}

	public static void setRules(GameActivity activity) {
		activity.deny(GameRuleType.CRAFTING);
		activity.deny(GameRuleType.FALL_DAMAGE);
		activity.deny(GameRuleType.HUNGER);
		activity.deny(GameRuleType.PORTALS);
		activity.allow(GameRuleType.PVP);
		activity.deny(GameRuleType.THROW_ITEMS);
	}

	public static void open(GameSpace gameSpace, ServerWorld world, ColorSwapMap map, ColorSwapConfig config) {
		gameSpace.setActivity(activity -> {
			GlobalWidgets widgets = GlobalWidgets.addTo(activity);
			Set<PlayerRef> players = gameSpace.getPlayers().stream().map(PlayerRef::of).collect(Collectors.toSet());
			ColorSwapActivePhase active = new ColorSwapActivePhase(world, gameSpace, map, config, players, widgets);

			ColorSwapActivePhase.setRules(activity);

			// Listeners
			activity.listen(GameActivityEvents.DISABLE, active::close);
			activity.listen(GameActivityEvents.ENABLE, active::open);
			activity.listen(GameActivityEvents.TICK, active::tick);
			activity.listen(GamePlayerEvents.OFFER, active::offerPlayer);
			activity.listen(GamePlayerEvents.REMOVE, active::removePlayer);
			activity.listen(PlayerDamageEvent.EVENT, active::onPlayerDamage);
			activity.listen(PlayerDeathEvent.EVENT, active::onPlayerDeath);
		});
	}

	public void open() {
		this.singleplayer = this.players.size() == 1;
		for (PlayerRef playerRef : this.players) {
			playerRef.ifOnline(this.world, player -> {
				this.updateRoundsExperienceLevel(player);
				player.changeGameMode(GameMode.ADVENTURE);
				ColorSwapActivePhase.spawn(this.world, this.map, player);
			});
		}
	}

	public void close() {
		this.timerBar.remove();
	}

	public void updateRoundsExperienceLevel(ServerPlayerEntity player) {
		player.setExperienceLevel(this.rounds);
	}

	private void setRounds(int rounds) {
		this.rounds = rounds;
		for (ServerPlayerEntity player : this.gameSpace.getPlayers()) {
			this.updateRoundsExperienceLevel(player);
		}
	}

	public void eraseTile(BlockPos.Mutable origin, int xSize, int zSize, BlockStateProvider erasedStateProvider) {
		boolean keep = this.world.getBlockState(origin).isOf(this.swapBlock);

		BlockPos.Mutable pos = origin.mutableCopy();
		for (int x = origin.getX(); x < origin.getX() + xSize; x++) {
			for (int z = origin.getZ(); z < origin.getZ() + zSize; z++) {
				pos.set(x, origin.getY(), z);

				if (!keep) {
					BlockState oldState = this.world.getBlockState(pos);
					BlockState newState = erasedStateProvider.getBlockState(this.world.getRandom(), pos);

					this.world.getWorldChunk(pos).setBlockState(pos, newState, false);
					this.world.updateListeners(pos, oldState, newState, 0);
				}
			}
		}
	}

	public void erase() {
		ColorSwapMapConfig mapConfig = this.config.getMapConfig();

 		for (ServerPlayerEntity player : this.gameSpace.getPlayers()) {
			player.playSound(this.config.getSwapSound(), SoundCategory.BLOCKS, 1, 1.5f);
		}

		BlockPos.Mutable pos = new BlockPos.Mutable();
		for (int x = 0; x < mapConfig.x * mapConfig.xScale; x += mapConfig.xScale) {
			for (int z = 0; z < mapConfig.z * mapConfig.zScale; z += mapConfig.zScale) {
				pos.set(x, 64, z);
				this.eraseTile(pos, mapConfig.xScale, mapConfig.zScale, mapConfig.erasedStateProvider);
			}
		}
	}

	private Block getSwapBlock() {
		return this.lastSwapBlocks.get(this.world.getRandom().nextInt(this.lastSwapBlocks.size()));
	}

	public void placeTile(BlockPos.Mutable origin, int xSize, int zSize, BlockState state) {
		BlockPos.Mutable pos = origin.mutableCopy();
		for (int x = origin.getX(); x < origin.getX() + xSize; x++) {
			for (int z = origin.getZ(); z < origin.getZ() + zSize; z++) {
				pos.set(x, origin.getY(), z);

				BlockState oldState = this.world.getBlockState(pos);
				this.world.getWorldChunk(pos).setBlockState(pos, state, false);
				this.world.updateListeners(pos, oldState, state, 0);
			}
		}
	}

	public void swap() {
		ColorSwapMapConfig mapConfig = this.config.getMapConfig();
		this.lastSwapBlocks.clear();

		BlockPos.Mutable pos = new BlockPos.Mutable();
		for (int x = 0; x < mapConfig.x * mapConfig.xScale; x += mapConfig.xScale) {
			for (int z = 0; z < mapConfig.z * mapConfig.zScale; z += mapConfig.zScale) {
				pos.set(x, 64, z);

				Block block = this.config.getMapConfig().getPlatformBlock(this.world.getRandom());
				if (!this.lastSwapBlocks.contains(block)) {
					this.lastSwapBlocks.add(block);
				}

				this.placeTile(pos, mapConfig.xScale, mapConfig.zScale, block.getDefaultState());
			}
		}
	}

	private void giveSwapBlocks() {
		ItemStack stack = new ItemStack(this.swapBlock);

		for (PlayerRef playerRef : this.players) {
			playerRef.ifOnline(this.world, player -> {
				player.getInventory().clear();
				for (int slot = 0; slot < 9; slot++) {
					player.getInventory().setStack(slot, stack.copy());
				}

				// Update inventory
				player.currentScreenHandler.sendContentUpdates();
				player.playerScreenHandler.onContentChanged(player.getInventory());
			});
		}
	}

	private void checkElimination() {
		Iterator<PlayerRef> iterator = this.players.iterator();
		while (iterator.hasNext()) {
			PlayerRef playerRef = iterator.next();
			playerRef.ifOnline(this.world, player -> {
				if (this.map.isBelowPlatform(player) || this.map.isAbovePlatform(player, this.isKnockbackEnabled())) {
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

	private Text getKnockbackEnabledText() {
		return Text.translatable("text.colorswap.knockback_enabled").formatted(Formatting.RED);
	}

	public void tick() {
		this.ticksUntilSwap -= 1;
		this.timerBar.tick(this);
		if (this.ticksUntilSwap <= 0) {
			if (this.swapBlock == null) {
				this.swap();

				this.swapBlock = this.getSwapBlock();
				this.giveSwapBlocks();

				this.setRounds(this.rounds + 1);
				this.maxTicksUntilSwap = this.getSwapTime();
				if (this.rounds - 1 == this.config.getNoKnockbackRounds()) {
					this.gameSpace.getPlayers().sendMessage(this.getKnockbackEnabledText());
				}
			} else {
				this.erase();
				this.swapBlock = null;

				this.maxTicksUntilSwap = this.getEraseTime();
			}
			this.ticksUntilSwap = this.maxTicksUntilSwap;
		}

		this.checkElimination();

		if (this.players.size() < 2) {
			if (this.players.size() == 1 && this.singleplayer) return;

			this.gameSpace.getPlayers().sendMessage(this.getEndingMessage());

			this.gameSpace.close(GameCloseReason.FINISHED);
		}
	}

	private Text getEndingMessage() {
		if (this.players.size() == 1) {
			PlayerRef winnerRef = this.players.iterator().next();
			PlayerEntity winner = winnerRef.getEntity(this.world);
			if (winner != null) {
				return Text.translatable("text.colorswap.win", winner.getDisplayName()).formatted(Formatting.GOLD);
			}
		}
		return Text.translatable("text.colorswap.no_winners").formatted(Formatting.GOLD);
	}

	private void setSpectator(ServerPlayerEntity player) {
		player.changeGameMode(GameMode.SPECTATOR);
	}

	public PlayerOfferResult offerPlayer(PlayerOffer offer) {
		return offer.accept(this.world, this.map.getSpawnPos()).and(() -> {
			this.updateRoundsExperienceLevel(offer.player());
			this.setSpectator(offer.player());
		});
	}

	public void removePlayer(ServerPlayerEntity player) {
		this.eliminate(player, true);
	}

	private boolean isKnockbackEnabled() {
		if (this.config.getNoKnockbackRounds() < 0) return false;
		return this.rounds - 1 >= this.config.getNoKnockbackRounds();
	}

	private ActionResult onPlayerDamage(ServerPlayerEntity player, DamageSource source, float amount) {
		return this.isKnockbackEnabled() ? ActionResult.SUCCESS : ActionResult.FAIL;
	}

	public void eliminate(ServerPlayerEntity eliminatedPlayer, boolean remove) {
		PlayerRef eliminatedRef = PlayerRef.of(eliminatedPlayer);
		if (!this.players.contains(eliminatedRef)) return;

		Text message = Text.translatable("text.colorswap.eliminated", eliminatedPlayer.getDisplayName()).formatted(Formatting.RED);

		for (ServerPlayerEntity player : this.gameSpace.getPlayers()) {
			player.sendMessage(message, false);
		}

		if (remove) {
			this.players.remove(eliminatedRef);
		}
		this.setSpectator(eliminatedPlayer);
	}

	public ActionResult onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
		this.eliminate(player, true);
		return ActionResult.SUCCESS;
	}

	public static void spawn(ServerWorld world, ColorSwapMap map, ServerPlayerEntity player) {
		Vec3d spawnPos = map.getSpawnPos();
		player.teleport(world, spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), 0, 0);

		player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, Integer.MAX_VALUE, 0, true, false));
		player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, Integer.MAX_VALUE, 127, true, false));
	}
}
