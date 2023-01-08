package io.github.haykam821.colorswap.game;

import io.github.haykam821.colorswap.game.phase.ColorSwapActivePhase;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import xyz.nucleoid.plasmid.game.common.GlobalWidgets;
import xyz.nucleoid.plasmid.game.common.widget.BossBarWidget;

import java.util.ArrayList;
import java.util.List;

public class ColorSwapTimerBar {
	private static final List<Formatting> TITLE_COLORS = new ArrayList<>();

	private Formatting color = Formatting.RED;
	private final BossBarWidget bar;

	public ColorSwapTimerBar(GlobalWidgets widgets) {
		this.bar = widgets.addBossBar(this.getTitleWithColor(this.color), BossBar.Color.RED, BossBar.Style.PROGRESS);
	}

	public void tick(ColorSwapActivePhase phase) {
		float percent = phase.getTimerBarPercent();
		this.bar.setProgress(percent);

		if (percent == 0) {
			this.cycleTitleColor();
		}
	}

	public void remove() {
		this.bar.close();
	}

	private void cycleTitleColor() {
		this.color = TITLE_COLORS.get((TITLE_COLORS.indexOf(this.color) + 1) % TITLE_COLORS.size());
		this.bar.setTitle(this.getTitleWithColor(this.color));
	}

	private Text getTitleWithColor(Formatting formatting) {
		return Text.translatable("gameType.colorswap.color_swap").formatted(formatting);
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
