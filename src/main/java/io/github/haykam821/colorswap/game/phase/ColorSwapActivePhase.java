package io.github.haykam821.colorswap.game.phase;

import io.github.haykam821.colorswap.game.ColorSwapConfig;
import io.github.haykam821.colorswap.game.map.ColorSwapMap;
import io.github.haykam821.colorswap.game.map.ColorSwapMapConfig;
import net.gegy1000.plasmid.game.Game;
import net.gegy1000.plasmid.game.GameWorld;
import net.gegy1000.plasmid.game.event.GameOpenListener;
import net.gegy1000.plasmid.game.event.GameTickListener;
import net.gegy1000.plasmid.game.event.PlayerAddListener;
import net.gegy1000.plasmid.game.event.PlayerDeathListener;
import net.gegy1000.plasmid.game.rule.GameRule;
import net.gegy1000.plasmid.game.rule.RuleResult;
import net.gegy1000.plasmid.util.PlayerRef;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;

import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class ColorSwapActivePhase {
	private final ServerWorld world;
	private final GameWorld gameWorld;
	private final ColorSwapMap map;
	private final ColorSwapConfig config;
	private final Set<PlayerRef> players;
	private int ticksUntilSwap = 20 * 4;
	private Block swapBlock;
	private boolean singleplayer;

	private boolean opened;

	public ColorSwapActivePhase(GameWorld gameWorld, ColorSwapMap map, ColorSwapConfig config, Set<PlayerRef> players) {
		this.world = gameWorld.getWorld();
		this.gameWorld = gameWorld;
		this.map = map;
		this.config = config;
		this.players = players;
	}

	public static void setRules(Game game) {
		game.setRule(GameRule.ALLOW_CRAFTING, RuleResult.DENY);
		game.setRule(GameRule.ALLOW_PORTALS, RuleResult.DENY);
		game.setRule(GameRule.ALLOW_PVP, RuleResult.DENY);
		game.setRule(GameRule.FALL_DAMAGE, RuleResult.DENY);
		game.setRule(GameRule.ENABLE_HUNGER, RuleResult.DENY);
	}

	public static void open(GameWorld gameWorld, ColorSwapMap map, ColorSwapConfig config) {
		Set<PlayerRef> players = gameWorld.getPlayers().stream().map(PlayerRef::of).collect(Collectors.toSet());
		ColorSwapActivePhase active = new ColorSwapActivePhase(gameWorld, map, config, players);

		gameWorld.newGame(game -> {
			ColorSwapActivePhase.setRules(game);

			// Listeners
			game.on(GameOpenListener.EVENT, active::open);
			game.on(GameTickListener.EVENT, active::tick);
			game.on(PlayerAddListener.EVENT, active::addPlayer);
			game.on(PlayerDeathListener.EVENT, active::onPlayerDeath);
		});
	}

	public void open() {
		this.swap();

		this.singleplayer = this.players.size() == 1;
		for (PlayerRef playerRef : this.players) {
			playerRef.ifOnline(this.world, player -> {
				player.setGameMode(GameMode.ADVENTURE);
				ColorSwapActivePhase.spawn(this.world, this.map, player);
			});
		}

		this.opened = true;
	}

	public boolean isBelowPlatform(PlayerEntity player) {
		return player.getY() < this.map.getPlatform().getMin().getY();
	}

	public void eraseTile(BlockPos.Mutable origin, int size, BlockState emptyState) {
		boolean keep = this.world.getBlockState(origin).isOf(this.swapBlock);

		BlockPos.Mutable pos = origin.mutableCopy();
		for (int x = origin.getX(); x < origin.getX() + size; x++) {
			for (int z = origin.getZ(); z < origin.getZ() + size; z++) {
				pos.set(x, origin.getY(), z);

				if (!keep) {
					BlockState oldState = this.world.getBlockState(pos);
					this.world.getWorldChunk(pos).setBlockState(pos, emptyState, false);
					this.world.updateListeners(pos, oldState, emptyState, 0);
				}
			}
		}
	}

	public void erase() {
		ColorSwapMapConfig mapConfig = this.config.getMapConfig();

		BlockPos.Mutable pos = new BlockPos.Mutable();
		for (int x = 0; x < mapConfig.x * mapConfig.tileSize; x += mapConfig.tileSize) {
			for (int z = 0; z < mapConfig.z * mapConfig.tileSize; z += mapConfig.tileSize) {
				pos.set(x, 64, z);
				this.eraseTile(pos, mapConfig.tileSize, mapConfig.emptyState);
			}
		}
	}

	private Block getPlatformBlock(Random random) {
		ColorSwapMapConfig mapConfig = this.config.getMapConfig();
		return mapConfig.getPlatformBlocks().getRandom(random);
	}

	public void placeTile(BlockPos.Mutable origin, int size, BlockState state) {
		BlockPos.Mutable pos = origin.mutableCopy();
		for (int x = origin.getX(); x < origin.getX() + size; x++) {
			for (int z = origin.getZ(); z < origin.getZ() + size; z++) {
				pos.set(x, origin.getY(), z);

				BlockState oldState = this.world.getBlockState(pos);
				this.world.getWorldChunk(pos).setBlockState(pos, state, false);
				this.world.updateListeners(pos, oldState, state, 0);
			}
		}
	}

	public void swap() {
		ColorSwapMapConfig mapConfig = this.config.getMapConfig();

		BlockPos.Mutable pos = new BlockPos.Mutable();
		for (int x = 0; x < mapConfig.x * mapConfig.tileSize; x += mapConfig.tileSize) {
			for (int z = 0; z < mapConfig.z * mapConfig.tileSize; z += mapConfig.tileSize) {
				pos.set(x, 64, z);

				BlockState state = this.getPlatformBlock(this.world.getRandom()).getDefaultState();
				this.placeTile(pos, mapConfig.tileSize, state);
			}
		}
	}

	private void giveSwapBlocks() {
		ItemStack stack = new ItemStack(this.swapBlock);

		for (PlayerRef playerRef : this.players) {
			playerRef.ifOnline(this.world, player -> {
				player.inventory.clear();
				for (int slot = 0; slot < 9; slot++) {
					player.inventory.setStack(slot, stack);
				}

				// Update inventory
				player.currentScreenHandler.sendContentUpdates();
				player.playerScreenHandler.onContentChanged(player.inventory);
				player.updateCursorStack();
			});
		}
	}

	private void checkElimination() {
		Iterator<PlayerRef> iterator = this.players.iterator();
		while (iterator.hasNext()) {
			PlayerRef playerRef = iterator.next();
			playerRef.ifOnline(this.world, player -> {
				if (this.isBelowPlatform(player)) {
					this.eliminate(player, false);
					iterator.remove();
				}
			});
		}
	}

	public void tick() {
		this.ticksUntilSwap -= 1;
		if (this.ticksUntilSwap == 0) {
			if (this.swapBlock == null) {
				this.swap();

				this.swapBlock = this.getPlatformBlock(this.world.getRandom());
				this.giveSwapBlocks();

				this.ticksUntilSwap = 20 * 4;
			} else {
				this.erase();
				this.swapBlock = null;

				this.ticksUntilSwap = 20 * 2;
			}
		}

		this.checkElimination();

		if (this.players.size() < 2) {
			if (this.players.size() == 1 && this.singleplayer) return;

			Text endingMessage = this.getEndingMessage();
			for (ServerPlayerEntity player : this.gameWorld.getPlayers()) {
				player.sendMessage(endingMessage, false);
			}

			this.gameWorld.closeWorld();
		}
	}

	private Text getEndingMessage() {
		if (this.players.size() == 1) {
			PlayerRef winnerRef = this.players.iterator().next();
			PlayerEntity winner = winnerRef.getEntity(this.world);
			if (winner != null) {
				return winner.getDisplayName().shallowCopy().append(" has been eliminated!").formatted(Formatting.RED);
			}
		}
		return new LiteralText("Nobody won the game!").formatted(Formatting.GOLD);
	}

	private void setSpectator(PlayerEntity player) {
		player.setGameMode(GameMode.SPECTATOR);
	}

	public void addPlayer(PlayerEntity player) {
		if (!this.players.contains(PlayerRef.of(player))) {
			this.setSpectator(player);
		} else if (this.opened) {
			this.eliminate(player, true);
		}
	}

	public void eliminate(PlayerEntity eliminatedPlayer, boolean remove) {
		Text message = eliminatedPlayer.getDisplayName().shallowCopy().append(" has been eliminated!").formatted(Formatting.RED);

		for (ServerPlayerEntity player : this.gameWorld.getPlayers()) {
			player.sendMessage(message, false);
		}

		if (remove) {
			this.players.remove(PlayerRef.of(eliminatedPlayer));
		}
		this.setSpectator(eliminatedPlayer);
	}

	public boolean onPlayerDeath(PlayerEntity player, DamageSource source) {
		this.eliminate(player, true);
		return true;
	}

	public static void spawn(ServerWorld world, ColorSwapMap map, ServerPlayerEntity player) {
		Vec3d center = map.getPlatform().getCenter();
		player.teleport(world, center.getX(), center.getY() + 0.5, center.getZ(), 0, 0);
	}
}
