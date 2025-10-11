package de.tomalbrc.questr.impl.navigationbar.component;

import de.tomalbrc.dialogutils.util.ComponentAligner;
import de.tomalbrc.dialogutils.util.TextUtil;
import de.tomalbrc.questr.QuestrMod;
import de.tomalbrc.questr.impl.navigationbar.NavigationBarConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.Locale;

public class MsptComponent implements NavigationBarComponent {
    final NavigationBarConfig config;
    final int line;

    int lastT;
    MutableComponent cached;

    public MsptComponent(NavigationBarConfig config, int line) {
        this.config = config;
        this.line = line;
    }

    @Override
    public MutableComponent getText(ServerPlayer serverPlayer) {
        MinecraftServer server = QuestrMod.SERVER;
        if (server == null) return Component.empty();

        double mspt = server.tickRateManager().millisecondsPerTick();
        int t = (int)(mspt*10);
        if (t == lastT && cached != null)
            return cached;

        lastT = t;

        String msptText = String.format(Locale.ROOT, "%.1f MSPT", mspt);

        ChatFormatting color;
        if (mspt <= 50.0) {
            color = ChatFormatting.GREEN;   // healthy
        } else if (mspt <= 60.0) {
            color = ChatFormatting.YELLOW;  // slight lag
        } else if (mspt <= 100.0) {
            color = ChatFormatting.GOLD;    // moderate lag
        } else {
            color = ChatFormatting.RED;     // severe lag
        }

        int width = config.width();
        var font = line == 1 ? QuestrMod.NAV_FONT : QuestrMod.NAV_FONT2;

        var coloredComponent = Component.literal(msptText).withStyle(
                Style.EMPTY.withColor(color).withFont(font)
        );

        var textComponent = ComponentAligner.align(
                coloredComponent,
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
