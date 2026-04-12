package io.github.haykam821.colorswap.game;

import io.github.haykam821.colorswap.game.phase.ColorSwapActivePhase;
import net.minecraft.world.BossEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import xyz.nucleoid.plasmid.api.game.common.GlobalWidgets;
import xyz.nucleoid.plasmid.api.game.common.widget.BossBarWidget;

public class ColorSwapTimerBar {
	private Style titleStyle = RainbowTextColors.getInitialStyle();
	private final BossBarWidget bar;

	public ColorSwapTimerBar(GlobalWidgets widgets) {
		this.bar = widgets.addBossBar(this.getTitle(), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS);
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
		this.titleStyle = RainbowTextColors.getNextStyle(this.titleStyle);
		this.bar.setTitle(this.getTitle());
	}

	private Component getTitle() {
		return Component.translatable("gameType.colorswap.color_swap").setStyle(this.titleStyle);
	}
}
