package io.github.haykam821.colorswap.game.phase;

import io.github.haykam821.colorswap.game.ColorSwapConfig;
import io.github.haykam821.colorswap.game.map.ColorSwapMap;
import io.github.haykam821.colorswap.game.map.ColorSwapMapBuilder;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;
import xyz.nucleoid.plasmid.game.GameWorld;
import xyz.nucleoid.plasmid.game.StartResult;
import xyz.nucleoid.plasmid.game.config.PlayerConfig;
import xyz.nucleoid.plasmid.game.event.OfferPlayerListener;
import xyz.nucleoid.plasmid.game.event.PlayerAddListener;
import xyz.nucleoid.plasmid.game.event.PlayerDeathListener;
import xyz.nucleoid.plasmid.game.event.RequestStartListener;
import xyz.nucleoid.plasmid.game.player.JoinResult;
import xyz.nucleoid.plasmid.game.world.bubble.BubbleWorldConfig;

import java.util.concurrent.CompletableFuture;

public class ColorSwapWaitingPhase {
	private final GameWorld gameWorld;
	private final ColorSwapMap map;
	private final ColorSwapConfig config;

	public ColorSwapWaitingPhase(GameWorld gameWorld, ColorSwapMap map, ColorSwapConfig config) {
		this.gameWorld = gameWorld;
		this.map = map;
		this.config = config;
	}

	public static CompletableFuture<Void> open(MinecraftServer server, ColorSwapConfig config) {
		ColorSwapMapBuilder mapBuilder = new ColorSwapMapBuilder(config);

		return mapBuilder.create().thenAccept(map -> {
			BubbleWorldConfig worldConfig = new BubbleWorldConfig()
					.setGenerator(map.createGenerator())
					.setDefaultGameMode(GameMode.SPECTATOR);
			GameWorld gameWorld = GameWorld.open(server, worldConfig);

			ColorSwapWaitingPhase waiting = new ColorSwapWaitingPhase(gameWorld, map, config);

			gameWorld.newGame(game -> {
				ColorSwapActivePhase.setRules(game);

				// Listeners
				game.on(PlayerAddListener.EVENT, waiting::addPlayer);
				game.on(PlayerDeathListener.EVENT, waiting::onPlayerDeath);
				game.on(OfferPlayerListener.EVENT, waiting::offerPlayer);
				game.on(RequestStartListener.EVENT, waiting::requestStart);
			});
		});
	}

	private boolean isFull() {
		return this.gameWorld.getPlayerCount() >= this.config.getPlayerConfig().getMaxPlayers();
	}

	public JoinResult offerPlayer(ServerPlayerEntity player) {
		return this.isFull() ? JoinResult.gameFull() : JoinResult.ok();
	}

	public StartResult requestStart() {
		PlayerConfig playerConfig = this.config.getPlayerConfig();
		if (this.gameWorld.getPlayerCount() < playerConfig.getMinPlayers()) {
			return StartResult.notEnoughPlayers();
		}

		ColorSwapActivePhase.open(this.gameWorld, this.map, this.config);
		return StartResult.ok();
	}

	public void addPlayer(ServerPlayerEntity player) {
		ColorSwapActivePhase.spawn(this.gameWorld.getWorld(), this.map, player);
	}

	public boolean onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
		// Respawn player at the start
		ColorSwapActivePhase.spawn(this.gameWorld.getWorld(), this.map, player);
		return true;
	}
}
