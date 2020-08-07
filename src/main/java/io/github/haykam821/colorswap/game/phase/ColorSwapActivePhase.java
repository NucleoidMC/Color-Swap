package io.github.haykam821.colorswap.game.phase;

import java.util.Set;

import io.github.haykam821.colorswap.game.ColorSwapConfig;
import io.github.haykam821.colorswap.game.map.ColorSwapMapProvider;
import net.gegy1000.plasmid.game.Game;
import net.gegy1000.plasmid.game.event.GameOpenListener;
import net.gegy1000.plasmid.game.event.GameTickListener;
import net.gegy1000.plasmid.game.event.PlayerAddListener;
import net.gegy1000.plasmid.game.event.PlayerDeathListener;
import net.gegy1000.plasmid.game.event.PlayerRejoinListener;
import net.gegy1000.plasmid.game.map.GameMap;
import net.gegy1000.plasmid.game.rule.GameRule;
import net.gegy1000.plasmid.game.rule.RuleResult;
import net.gegy1000.plasmid.util.PlayerRef;
import net.gegy1000.plasmid.world.BlockBounds;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.tag.BlockTags;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;

public class ColorSwapActivePhase {
	private final ColorSwapConfig config;
	private final Set<PlayerRef> players;
	private int ticksUntilSwap = 20 * 4;
	private Block swapBlock;
	private boolean singleplayer;

	public ColorSwapActivePhase(ColorSwapConfig config, Set<PlayerRef> players) {
		this.config = config;
		this.players = players;
	}

	public static void setRules(Game.Builder builder) {
		builder.setRule(GameRule.ALLOW_CRAFTING, RuleResult.DENY);
		builder.setRule(GameRule.ALLOW_PORTALS, RuleResult.DENY);
		builder.setRule(GameRule.ALLOW_PVP, RuleResult.DENY);
		builder.setRule(GameRule.FALL_DAMAGE, RuleResult.DENY);
		builder.setRule(GameRule.ENABLE_HUNGER, RuleResult.DENY);
	}

	public static Game open(GameMap map, ColorSwapConfig config, Set<PlayerRef> players) {
		ColorSwapActivePhase game = new ColorSwapActivePhase(config, players);

		Game.Builder builder = Game.builder();
		builder.setMap(map);

		ColorSwapActivePhase.setRules(builder);

		// Listeners
		builder.on(GameOpenListener.EVENT, game::open);
		builder.on(GameTickListener.EVENT, game::tick);
		builder.on(PlayerAddListener.EVENT, game::addPlayer);
		builder.on(PlayerDeathListener.EVENT, game::onPlayerDeath);
		builder.on(PlayerRejoinListener.EVENT, game::rejoinPlayer);

		return builder.build();
	}

	public void open(Game game) {
		this.swap(game);

		this.singleplayer = this.players.size() == 1;
 		for (PlayerRef playerRef : this.players) {
			playerRef.ifOnline(game.getWorld(), player -> {
				player.setGameMode(GameMode.ADVENTURE);
				ColorSwapActivePhase.spawn(game.getMap(), player);
			});
		}
	}

	public boolean isBelowPlatform(PlayerEntity player, Game game) {
		BlockBounds platform = game.getMap().getFirstRegion("platform");
		return player.getY() < platform.getMin().getY();
	}

	public void eraseTile(Game game, BlockPos.Mutable origin, int size, BlockState emptyState) {
		World world = game.getMap().getWorld();

		boolean keep = world.getBlockState(origin).isOf(this.swapBlock);

		BlockPos.Mutable pos = origin.mutableCopy();
		for (int x = origin.getX(); x < origin.getX() + size; x++) {
			for (int z = origin.getZ(); z < origin.getZ() + size; z++) {
				pos.set(x, origin.getY(), z);

				if (!keep) {
					BlockState oldState = world.getBlockState(pos);
					world.getWorldChunk(pos).setBlockState(pos, emptyState, false);
					world.updateListeners(pos, oldState, emptyState, 0);
				}
			}
		}
	}

	public void erase(Game game) {
		BlockPos origin = this.config.getMapConfig().getOrigin();
		ColorSwapMapProvider provider = (ColorSwapMapProvider) this.config.getMapConfig().getProvider();

		BlockPos.Mutable pos = new BlockPos.Mutable();
		for (int x = origin.getX(); x < provider.x * provider.tileSize; x += provider.tileSize) {
			for (int z = origin.getZ(); z < provider.z * provider.tileSize; z += provider.tileSize) {
				pos.set(x, origin.getY(), z);
				this.eraseTile(game, pos, provider.tileSize, provider.emptyState);
			}
		}
	}

	public void placeTile(Game game, BlockPos.Mutable origin, int size, BlockState state) {
		World world = game.getMap().getWorld();

		BlockPos.Mutable pos = origin.mutableCopy();
		for (int x = origin.getX(); x < origin.getX() + size; x++) {
			for (int z = origin.getZ(); z < origin.getZ() + size; z++) {
				pos.set(x, origin.getY(), z);

				BlockState oldState = world.getBlockState(pos);
				world.getWorldChunk(pos).setBlockState(pos, state, false);
				world.updateListeners(pos, oldState, state, 0);
			}
		}
	}

	public void swap(Game game) {
		BlockPos origin = this.config.getMapConfig().getOrigin();
		ColorSwapMapProvider provider = (ColorSwapMapProvider) this.config.getMapConfig().getProvider();

		BlockPos.Mutable pos = new BlockPos.Mutable();
		for (int x = origin.getX(); x < provider.x * provider.tileSize; x += provider.tileSize) {
			for (int z = origin.getZ(); z < provider.z * provider.tileSize; z += provider.tileSize) {
				pos.set(x, origin.getY(), z);

				BlockState state = BlockTags.WOOL.getRandom(game.getWorld().getRandom()).getDefaultState();
				this.placeTile(game, pos, provider.tileSize, state);
			}
		}
	}

	private void giveSwapBlocks(Game game) {
		ItemStack stack = new ItemStack(this.swapBlock);

		for (PlayerRef playerRef : this.players) {
			playerRef.ifOnline(game.getWorld(), player -> {
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

	public void tick(Game game) {
		this.ticksUntilSwap -= 1;
		if (this.ticksUntilSwap == 0) {
			if (this.swapBlock == null) {
				this.swap(game);

				this.swapBlock = BlockTags.WOOL.getRandom(game.getWorld().getRandom());
				this.giveSwapBlocks(game);

				this.ticksUntilSwap = 20 * 4;
			} else {
				this.erase(game);
				this.swapBlock = null;

				this.ticksUntilSwap = 20 * 2;
			}
		}

		for (PlayerRef playerRef : this.players) {
			playerRef.ifOnline(game.getWorld(), player -> {
				if (this.isBelowPlatform(player, game)) {
					this.eliminate(game, player);
				}
			});
		}

		if (this.players.size() < 2) {
			if (this.players.size() == 1 && this.singleplayer) return;
			
			Text endingMessage = this.getEndingMessage(game);
			game.onlinePlayers().forEach(player -> {
				player.sendMessage(endingMessage, false);
			});

			game.close();
		}
	}

	private Text getEndingMessage(Game game) {
		if (this.players.size() == 1) {
			PlayerRef winnerRef = this.players.iterator().next();
			if (winnerRef.isOnline(game.getWorld())) {
				PlayerEntity winner = winnerRef.getEntity(game.getWorld());
				return winner.getDisplayName().shallowCopy().append(" has been eliminated!").formatted(Formatting.RED);
			}
		}
		return new LiteralText("Nobody won the game!").formatted(Formatting.GOLD);
	}

	private void setSpectator(PlayerEntity player) {
		player.setGameMode(GameMode.SPECTATOR);
	}

	public void addPlayer(Game game, PlayerEntity player) {
		if (!this.players.contains(PlayerRef.of(player))) {
			this.setSpectator(player);
		}
	}

	public void eliminate(Game game, PlayerEntity eliminatedPlayer) {
		Text message = eliminatedPlayer.getDisplayName().shallowCopy().append(" has been eliminated!").formatted(Formatting.RED);
		game.onlinePlayers().forEach(player -> {
			player.sendMessage(message, false);
		});

		this.players.remove(PlayerRef.of(eliminatedPlayer));
		this.setSpectator(eliminatedPlayer);
	}

	public boolean onPlayerDeath(Game game, PlayerEntity player, DamageSource source) {
		this.eliminate(game, player);
		return true;
	}

	public void rejoinPlayer(Game game, PlayerEntity player) {
		this.eliminate(game, player);
	}

	public static void spawn(GameMap map, ServerPlayerEntity player) {
		Vec3d center = map.getFirstRegion("platform").getCenter();
		player.teleport(map.getWorld(), center.getX(), center.getY() + 0.5, center.getZ(), 0, 0);
	}
}