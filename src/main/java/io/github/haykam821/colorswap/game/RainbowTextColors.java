package io.github.haykam821.colorswap.game;

import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.network.chat.Style;
import net.minecraft.ChatFormatting;
import net.minecraft.util.Util;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.UnknownNullability;

public final class RainbowTextColors {
	private static final List<Style> STYLES = createStyles(new ChatFormatting[] {
		ChatFormatting.RED,
		ChatFormatting.GOLD,
		ChatFormatting.YELLOW,
		ChatFormatting.GREEN,
		ChatFormatting.BLUE,
		ChatFormatting.LIGHT_PURPLE,
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

	private static List<Style> createStyles(ChatFormatting @UnknownNullability [] colors) {
		ImmutableList.Builder<Style> styles = ImmutableList.builderWithExpectedSize(colors.length);

		for (ChatFormatting color : colors) {
			styles.add(Style.EMPTY.withColor(color));
		}

		return styles.build();
	}
}
