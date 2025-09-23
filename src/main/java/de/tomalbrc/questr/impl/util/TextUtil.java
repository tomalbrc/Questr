package de.tomalbrc.questr.impl.util;

import eu.pb4.placeholders.api.TextParserUtils;
import net.minecraft.network.chat.Component;

public class TextUtil {
    public static Component format(String text) {
        return TextParserUtils.formatText(text);
    }
}
