package de.tomalbrc.questr.impl.navigationbar.component;

import de.tomalbrc.dialogutils.util.ComponentAligner;
import de.tomalbrc.dialogutils.util.TextUtil;
import de.tomalbrc.questr.QuestrMod;
import de.tomalbrc.questr.impl.util.Util;
import net.minecraft.network.chat.MutableComponent;

public class CustomTextComponent implements NavBarComponent {
    @Override
    public MutableComponent build(NavBarComponent.Context context) {
        int width = context.config().width();
        int line = context.lineNumber();
        String text = context.config().text() != null ? context.config().text() : "";
        var font = (line == 1) ? QuestrMod.NAV_FONT : QuestrMod.NAV_FONT2;

        var textComponent = ComponentAligner.align(
                Util.whiteWithFont(text, font),
                TextUtil.Alignment.CENTER,
                width - 4
        );

        return ComponentUtils.background(width, line)
                .append(ComponentAligner.spacer(2))
                .append(textComponent)
                .append(ComponentAligner.spacer(2));
    }
}