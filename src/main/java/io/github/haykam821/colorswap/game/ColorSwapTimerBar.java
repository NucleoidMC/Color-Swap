package io.github.haykam821.colorswap.game;

import java.util.ArrayList;
import java.util.List;

import io.github.haykam821.colorswap.game.phase.ColorSwapActivePhase;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

public class ColorSwapTimerBar {
	private static final List<Formatting> TITLE_COLORS = new ArrayList<>();

	private Formatting color = Formatting.RED;
	private final ServerBossBar bar = new ServerBossBar(new LiteralText("Color Swap").formatted(this.color), BossBar.Color.RED, BossBar.Style.PROGRESS);

	public void tick(ColorSwapActivePhase phase) {
		float percent = phase.getTimerBarPercent();
		this.bar.setPercent(percent);

		if (percent == 0) {
			this.cycleTitleColor();
		}
	}

	public void remove() {
		this.bar.clearPlayers();
		this.bar.setVisible(false);
	}

	public void addPlayer(ServerPlayerEntity player) {
		this.bar.addPlayer(player);
	}

	private void cycleTitleColor() {
		this.color = TITLE_COLORS.get((TITLE_COLORS.indexOf(this.color) + 1) % TITLE_COLORS.size());
		this.bar.setName(this.bar.getName().copy().formatted(this.color));
	}

	static {
		TITLE_COLORS.add(Formatting.RED);
		TITLE_COLORS.add(Formatting.GOLD);
		TITLE_COLORS.add(Formatting.YELLOW);
		TITLE_COLORS.add(Formatting.GREEN);
		TITLE_COLORS.add(Formatting.BLUE);
		TITLE_COLORS.add(Formatting.LIGHT_PURPLE);
	}
}