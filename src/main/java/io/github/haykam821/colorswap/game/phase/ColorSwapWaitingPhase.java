package io.github.haykam821.colorswap.game.phase;

import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.ChunkAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import io.github.haykam821.colorswap.game.ColorSwapConfig;
import io.github.haykam821.colorswap.game.map.ColorSwapGuideText;
import io.github.haykam821.colorswap.game.map.ColorSwapMap;
import io.github.haykam821.colorswap.game.map.ColorSwapMapBuilder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.GameType;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.plasmid.api.game.GameOpenContext;
import xyz.nucleoid.plasmid.api.game.GameOpenProcedure;
import xyz.nucleoid.plasmid.api.game.GameResult;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.common.GameWaitingLobby;
import xyz.nucleoid.plasmid.api.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.api.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.api.game.player.JoinAcceptor;
import xyz.nucleoid.plasmid.api.game.player.JoinAcceptorResult;
import xyz.nucleoid.plasmid.api.game.player.JoinOffer;
import xyz.nucleoid.plasmid.api.game.rule.GameRuleType;
import xyz.nucleoid.stimuli.event.EventResult;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

public class ColorSwapWaitingPhase {
	private final GameSpace gameSpace;
	private final ServerLevel world;
	private final ColorSwapMap map;
	private final ColorSwapConfig config;

	private HolderAttachment guideText;

	public ColorSwapWaitingPhase(GameSpace gameSpace, ServerLevel world, ColorSwapMap map, ColorSwapConfig config) {
		this.gameSpace = gameSpace;
		this.world = world;
		this.map = map;
		this.config = config;
	}

	public static GameOpenProcedure open(GameOpenContext<ColorSwapConfig> context) {
		ColorSwapConfig config = context.game().config();
		ColorSwapMapBuilder mapBuilder = new ColorSwapMapBuilder(config);

		ColorSwapMap map = mapBuilder.create(RandomSource.createNewThreadLocalInstance());
		RuntimeWorldConfig worldConfig = new RuntimeWorldConfig()
				.setGenerator(map.createGenerator(context.server()));

		return context.openWithWorld(worldConfig, (game, world) -> {
			ColorSwapWaitingPhase waiting = new ColorSwapWaitingPhase(game.getGameSpace(), world, map, config);

			GameWaitingLobby.addTo(game, config.getPlayerConfig());
			ColorSwapActivePhase.setRules(game);
			game.deny(GameRuleType.PVP);

			// Listeners
			game.listen(GameActivityEvents.ENABLE, waiting::enable);
			game.listen(GameActivityEvents.TICK, waiting::tick);
			game.listen(PlayerDeathEvent.EVENT, waiting::onPlayerDeath);
			game.listen(GamePlayerEvents.ACCEPT, waiting::onAcceptPlayer);
			game.listen(GamePlayerEvents.OFFER, JoinOffer::accept);
			game.listen(GameActivityEvents.REQUEST_START, waiting::requestStart);
		});
	}

	private void enable() {
		// Spawn guide text
		Vec3 guideTextPos = this.map.getGuideTextPos();

		if (guideTextPos != null) {
			RandomSource random = this.world.getRandom();

			boolean knockback = this.config.getNoKnockbackRounds() >= 0;
			boolean prisms = this.config.getPrismConfig().isPresent();

			ElementHolder holder = ColorSwapGuideText.createElementHolder(random, knockback, prisms);
			this.guideText = ChunkAttachment.of(holder, world, guideTextPos);
		}
	}

	private void tick() {
		for (ServerPlayer player : this.gameSpace.getPlayers()) {
			if (this.map.isBelowPlatform(player)) {
				this.spawn(player);
			}
		}
	}

	private JoinAcceptorResult onAcceptPlayer(JoinAcceptor acceptor) {
		return acceptor.teleport(this.world, this.map.getWaitingSpawnPos())
			.thenRunForEach(player -> player.setGameMode(GameType.ADVENTURE));
	}

	public GameResult requestStart() {
		ColorSwapActivePhase.open(this.gameSpace, this.world, this.map, this.config, this.guideText);
		return GameResult.ok();
	}

	public EventResult onPlayerDeath(ServerPlayer player, DamageSource source) {
		// Respawn player at the start
		this.spawn(player);
		return EventResult.DENY;
	}

	private void spawn(ServerPlayer player) {
		Vec3 spawnPos = map.getWaitingSpawnPos();
		ColorSwapActivePhase.spawn(this.world, spawnPos, 0, player);
	}
}
