package de.tomalbrc.questr.impl.navigationbar.component;

import de.tomalbrc.questr.QuestrMod;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public class ComponentUtils {
    private static final String BACKGROUND_EDGE = "\uE100\uE200";
    private static final String BACKGROUND = "\uE101\uE200";
    private static final String NEGATIVE_SPACE = "\uE200";

    public static MutableComponent background(int width, int line) {
        var font = (line == 1) ? QuestrMod.NAV_FONT : QuestrMod.NAV_FONT2;
        return Component.literal(BACKGROUND_EDGE + BACKGROUND.repeat(width - 2) + BACKGROUND_EDGE + NEGATIVE_SPACE.repeat(width))
                .withStyle(Style.EMPTY.withFont(font).withShadowColor(0).withColor(0));
    }

    public static MutableComponent icon(String icon, int line) {
        var font = (line == 1) ? QuestrMod.ICON_FONT_NAV : QuestrMod.ICON_FONT_NAV2;
        return Component.literal(icon).withStyle(Style.EMPTY.withFont(font).withColor(ChatFormatting.WHITE));
    }
}