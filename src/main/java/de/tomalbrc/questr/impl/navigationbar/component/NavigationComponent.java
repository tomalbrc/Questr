package de.tomalbrc.questr.impl.navigationbar.component;

import de.tomalbrc.dialogutils.util.ComponentAligner;
import de.tomalbrc.dialogutils.util.TextUtil;
import de.tomalbrc.questr.QuestrMod;
import de.tomalbrc.questr.impl.navigationbar.NavigationBarConfig;
import de.tomalbrc.questr.impl.util.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.SegmentedAnglePrecision;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class NavigationComponent implements NavigationBarComponent {
    final NavigationBarConfig config;
    final int line;

    public NavigationComponent(NavigationBarConfig config, int line) {
        this.config = config;
        this.line = line;
    }

    @Override
    public MutableComponent getText(ServerPlayer serverPlayer) {
        int width = config.width();

        Vec3 delta = serverPlayer.position();
        double distance = delta.horizontalDistance();
        String distStr = (distance > 99999) ? "Far Away" : String.format(Locale.ROOT, "%4.0f Blocks Away", distance);
        String arrow = getArrow(delta, serverPlayer.getYHeadRot() + 180);

        var font = line == 1 ? QuestrMod.NAV_FONT:QuestrMod.NAV_FONT2;
        var icon = Component.literal(arrow).withStyle(Style.EMPTY.withFont(font).withColor(0xFFFFFF));
        var componentText = Util.whiteWithFont(distStr, font);
        var alignedMessage = ComponentAligner.align(componentText, TextUtil.Alignment.RIGHT, width - 17 - 4);
        return ComponentUtils.background(width, line)
                .append(ComponentAligner.spacer(2))
                .append(icon)
                .append(alignedMessage)
                .append(ComponentAligner.spacer(2));
    }

    private static @NotNull String getArrow(Vec3 delta, float yRot) {
        double rotFromPlayer = Math.toDegrees(Math.atan2(delta.z, delta.x) + Math.PI);
        SegmentedAnglePrecision sap = new SegmentedAnglePrecision(3);
        int r = sap.fromDegrees((float) rotFromPlayer - yRot);
        return switch (r) {
            case 0 -> "←"; case 1 -> "↖"; case 2 -> "↑"; case 3 -> "↗";
            case 4 -> "→"; case 5 -> "↘"; case 6 -> "↓"; case 7 -> "↙";
            default -> "x";
        };
    }
}