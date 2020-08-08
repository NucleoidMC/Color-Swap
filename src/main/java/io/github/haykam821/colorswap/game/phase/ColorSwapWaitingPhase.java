package io.github.haykam821.colorswap.game.phase;

import io.github.haykam821.colorswap.game.ColorSwapConfig;
import io.github.haykam821.colorswap.game.map.ColorSwapMap;
import io.github.haykam821.colorswap.game.map.ColorSwapMapBuilder;
import net.gegy1000.plasmid.game.GameWorld;
import net.gegy1000.plasmid.game.GameWorldState;
import net.gegy1000.plasmid.game.StartResult;
import net.gegy1000.plasmid.game.config.PlayerConfig;
import net.gegy1000.plasmid.game.event.OfferPlayerListener;
import net.gegy1000.plasmid.game.event.PlayerAddListener;
import net.gegy1000.plasmid.game.event.PlayerDeathListener;
import net.gegy1000.plasmid.game.event.RequestStartListener;
import net.gegy1000.plasmid.game.player.JoinResult;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;

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

	public static CompletableFuture<Void> open(GameWorldState gameState, ColorSwapConfig config) {
		ColorSwapMapBuilder mapBuilder = new ColorSwapMapBuilder(config);

		return mapBuilder.create().thenAccept(map -> {
			GameWorld gameWorld = gameState.openWorld(map.createGenerator());

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
