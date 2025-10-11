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

public class TpsComponent implements NavigationBarComponent {
    final NavigationBarConfig config;
    final int line;

    int lastT = 0;
    MutableComponent cached;

    public TpsComponent(NavigationBarConfig config, int line) {
        this.config = config;
        this.line = line;
    }

    @Override
    public MutableComponent getText(ServerPlayer serverPlayer) {
        MinecraftServer server = QuestrMod.SERVER;
        if (server == null) return Component.empty();

        double mspt = server.tickRateManager().tickrate();
        double tps = Math.min(20.0, 1000.0 / mspt);
        int t = (int)(tps*10);
        if (t == lastT && cached != null)
            return cached;

        lastT = t;

        String tpsText = String.format(Locale.ROOT, "%.1f TPS", tps);

        ChatFormatting color;
        if (tps >= 19.5) {
            color = ChatFormatting.GREEN;
        } else if (tps >= 18.0) {
            color = ChatFormatting.YELLOW;
        } else if (tps >= 15.0) {
            color = ChatFormatting.GOLD;
        } else {
            color = ChatFormatting.RED;
        }

        int width = config.width();
        var font = line == 1 ? QuestrMod.NAV_FONT : QuestrMod.NAV_FONT2;

        var coloredComponent = Component.literal(tpsText).withStyle(Style.EMPTY.withColor(color).withFont(font));

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
