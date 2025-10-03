package de.tomalbrc.questr.impl.util;

import de.tomalbrc.dialogutils.util.TextUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public class Util {
    public static MutableComponent whiteWithFont(String text, FontDescription font) {
        return Component.empty().append(TextUtil.parse(text)).withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE).withFont(font));
    }
}
