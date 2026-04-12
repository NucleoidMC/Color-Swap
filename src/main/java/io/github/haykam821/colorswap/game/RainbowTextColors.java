package io.github.haykam821.colorswap.game;

import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.network.chat.Style;
import net.minecraft.ChatFormatting;
import net.minecraft.util.Util;
import net.minecraft.util.RandomSource;

public final class RainbowTextColors {
	private static final List<Style> STYLES = createStyles(new int[] {
		ChatFormatting.RED.getColor(),
		ChatFormatting.GOLD.getColor(),
		ChatFormatting.YELLOW.getColor(),
		ChatFormatting.GREEN.getColor(),
		ChatFormatting.BLUE.getColor(),
		ChatFormatting.LIGHT_PURPLE.getColor(),
	});

	private RainbowTextColors() {
		return;
	}

	public static Style getInitialStyle() {
		return STYLES.getFirst();
	}

	public static Style getRandomStyle(RandomSource random) {
		return Util.getRandom(STYLES, random);
	}

	public static Style getNextStyle(Style style) {
		return STYLES.get((STYLES.indexOf(style) + 1) % STYLES.size());
	}

	private static List<Style> createStyles(int[] colors) {
		ImmutableList.Builder<Style> styles = ImmutableList.builderWithExpectedSize(colors.length);

		for (int color : colors) {
			styles.add(Style.EMPTY.withColor(color));
		}

		return styles.build();
	}
}
