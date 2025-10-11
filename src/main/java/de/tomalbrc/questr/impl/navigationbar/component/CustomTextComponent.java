package de.tomalbrc.questr.impl.navigationbar.component;

import de.tomalbrc.dialogutils.util.ComponentAligner;
import de.tomalbrc.dialogutils.util.TextUtil;
import de.tomalbrc.questr.QuestrMod;
import de.tomalbrc.questr.impl.navigationbar.NavigationBarConfig;
import de.tomalbrc.questr.impl.util.Util;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

public class CustomTextComponent implements NavigationBarComponent {
    final NavigationBarConfig config;
    final int line;

    MutableComponent cached;

    public CustomTextComponent(NavigationBarConfig config, int line) {
        this.config = config;
        this.line = line;
    }

    @Override
    public MutableComponent getText(ServerPlayer serverPlayer) {
        if (cached != null)
            return cached;

        int width = config.width();
        String text = config.text() != null ? config.text() : "";
        var font = (line == 1) ? QuestrMod.NAV_FONT : QuestrMod.NAV_FONT2;

        var textComponent = ComponentAligner.align(
                Util.whiteWithFont(text, font),
                TextUtil.Alignment.CENTER,
                width - 4
        );

        cached = ComponentUtils.background(width, line)
                .append(ComponentAligner.spacer(2))
                .append(textComponent)
                .append(ComponentAligner.spacer(2));

        return cached;
    }
}