package io.github.haykam821.colorswap.game.map;

import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.elements.TextDisplayElement;
import io.github.haykam821.colorswap.game.RainbowTextColors;
import net.minecraft.world.entity.Display.BillboardConstraints;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.util.RandomSource;

public final class ColorSwapGuideText {
	private static final Component TITLE = Component.translatable("gameType.colorswap.color_swap").withStyle(ChatFormatting.BOLD);

	private static final Component STAND_ON_MATCHING_BLOCKS_LINE = Component.translatable("text.colorswap.guide.stand_on_matching_blocks");
	private static final Component KNOCK_OTHERS_OFF_LINE = Component.translatable("text.colorswap.guide.knock_others_off");
	private static final Component GRAB_PRISMS_LINE = Component.translatable("text.colorswap.guide.grab_prisms");
	private static final Component LAST_PLAYER_STANDING_LINE = Component.translatable("text.colorswap.guide.last_player_standing");

	private ColorSwapGuideText() {
		return;
	}

	public static ElementHolder createElementHolder(RandomSource random, boolean knockback, boolean prisms) {
		TextDisplayElement element = new TextDisplayElement(createText(random, knockback, prisms));

		element.setBillboardMode(BillboardConstraints.CENTER);
		element.setLineWidth(350);
		element.setInvisible(true);

		ElementHolder holder = new ElementHolder();
		holder.addElement(element);

		return holder;
	}

	private static Component createText(RandomSource random, boolean knockback, boolean prisms) {
		MutableComponent text = Component.empty()
			.append(TITLE)
			.append(CommonComponents.NEW_LINE)
			.append(STAND_ON_MATCHING_BLOCKS_LINE);

		if (knockback) {
			text
				.append(CommonComponents.NEW_LINE)
				.append(KNOCK_OTHERS_OFF_LINE);
		}

		if (prisms) {
			text
				.append(CommonComponents.NEW_LINE)
				.append(GRAB_PRISMS_LINE);
		}

		Style style = RainbowTextColors.getRandomStyle(random);

		return text
			.append(CommonComponents.NEW_LINE)
			.append(LAST_PLAYER_STANDING_LINE)
			.setStyle(style);
	}
}
