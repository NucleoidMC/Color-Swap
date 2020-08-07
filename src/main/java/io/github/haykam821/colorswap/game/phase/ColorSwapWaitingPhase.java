package io.github.haykam821.colorswap.game.phase;

import io.github.haykam821.colorswap.game.ColorSwapConfig;
import net.gegy1000.plasmid.game.Game;
import net.gegy1000.plasmid.game.JoinResult;
import net.gegy1000.plasmid.game.StartResult;
import net.gegy1000.plasmid.game.config.PlayerConfig;
import net.gegy1000.plasmid.game.event.OfferPlayerListener;
import net.gegy1000.plasmid.game.event.PlayerAddListener;
import net.gegy1000.plasmid.game.event.PlayerDeathListener;
import net.gegy1000.plasmid.game.event.RequestStartListener;
import net.gegy1000.plasmid.game.map.GameMap;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class ColorSwapWaitingPhase {
	private final ColorSwapConfig config;

	public ColorSwapWaitingPhase(GameMap map, ColorSwapConfig config) {
		this.config = config;
	}

	public static Game open(GameMap map, ColorSwapConfig config) {
		ColorSwapWaitingPhase game = new ColorSwapWaitingPhase(map, config);

		Game.Builder builder = Game.builder();
		builder.setMap(map);

		ColorSwapActivePhase.setRules(builder);

		// Listeners
		builder.on(PlayerAddListener.EVENT, game::addPlayer);
		builder.on(PlayerDeathListener.EVENT, game::onPlayerDeath);
		builder.on(OfferPlayerListener.EVENT, game::offerPlayer);
		builder.on(RequestStartListener.EVENT, game::requestStart);

		return builder.build();
	}

	private boolean isFull(Game game) {
		return game.getPlayerCount() >= this.config.getPlayerConfig().getMaxPlayers();
	}

	public JoinResult offerPlayer(Game game, ServerPlayerEntity player) {
		return this.isFull(game) ? JoinResult.gameFull() : JoinResult.ok();
	}

	public StartResult requestStart(Game game) {
		PlayerConfig playerConfig = this.config.getPlayerConfig();
		if (game.getPlayerCount() < playerConfig.getMinPlayers()) {
			return StartResult.notEnoughPlayers();
		}

		Game activeGame = ColorSwapActivePhase.open(game.getMap(), this.config, game.getPlayers());
		return StartResult.ok(activeGame);
	}

	public void addPlayer(Game game, ServerPlayerEntity player) {
		ColorSwapActivePhase.spawn(game.getMap(), player);
	}

	public boolean onPlayerDeath(Game game, ServerPlayerEntity player, DamageSource source) {
		// Respawn player at the start
		ColorSwapActivePhase.spawn(game.getMap(), player);
		return true;
	}
}