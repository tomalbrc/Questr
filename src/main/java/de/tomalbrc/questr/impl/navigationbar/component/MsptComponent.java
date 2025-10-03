package de.tomalbrc.questr.impl.navigationbar.component;

import de.tomalbrc.dialogutils.util.ComponentAligner;
import de.tomalbrc.dialogutils.util.TextUtil;
import de.tomalbrc.questr.QuestrMod;
import de.tomalbrc.questr.impl.util.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;

import java.util.Locale;

public class MsptComponent implements NavBarComponent {
    @Override
    public MutableComponent build(Context context) {
        MinecraftServer server = QuestrMod.SERVER;
        if (server == null) return Component.empty();
        
        String msptText = String.format(Locale.ROOT, "%.1f MSPT", server.tickRateManager().millisecondsPerTick());
        int width = context.config().width();
        var font = context.lineNumber() == 1 ? QuestrMod.NAV_FONT : QuestrMod.NAV_FONT2;

        var textComponent = ComponentAligner.align(
                Util.whiteWithFont(msptText, font),
                TextUtil.Alignment.CENTER,
                width - 4
        );

        return ComponentUtils.background(width, context.lineNumber())
                .append(ComponentAligner.spacer(2))
                .append(textComponent)
                .append(ComponentAligner.spacer(2));
    }
}