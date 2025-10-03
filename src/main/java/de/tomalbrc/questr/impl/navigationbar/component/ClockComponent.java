package de.tomalbrc.questr.impl.navigationbar.component;

import de.tomalbrc.dialogutils.util.ComponentAligner;
import de.tomalbrc.dialogutils.util.TextUtil;
import de.tomalbrc.questr.QuestrMod;
import de.tomalbrc.questr.impl.util.Util;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;

import java.util.Locale;

public class ClockComponent implements NavBarComponent {
    private static final String SUN = "\uE104", RAIN = "\uE105", THUNDER = "\uE106", NIGHT = "\uE107";

    @Override
    public MutableComponent build(Context context) {
        ServerLevel level = context.player().level();
        long time = level.dayTime();

        String iconStr = SUN;
        if (!level.isBrightOutside()) iconStr = NIGHT;
        if (level.isRaining()) iconStr = RAIN;
        if (level.isThundering()) iconStr = THUNDER;

        var iconComponent = ComponentUtils.icon(iconStr, 1);
        int width = context.config().width();
        var timeText = ComponentAligner.align(
                Util.whiteWithFont(ticksToClock(time), context.lineNumber() == 1 ? QuestrMod.NAV_FONT : QuestrMod.NAV_FONT2),
                TextUtil.Alignment.CENTER, width - ComponentAligner.getWidth(iconComponent) - 4
        );

        return ComponentUtils.background(width, context.lineNumber())
                .append(ComponentAligner.spacer(2))
                .append(iconComponent)
                .append(ComponentAligner.spacer(2))
                .append(timeText);
    }

    private String ticksToClock(long timeTicks) {
        long ticks = timeTicks % 24000;
        if (ticks < 0) ticks += 24000;

        double fractionOfDay = ticks / 24000.0;
        double realHours = (fractionOfDay * 24.0 + 6.0) % 24.0;
        int hour = (int) realHours;
        int minute = (int) ((realHours - hour) * 60);
        String ampm = (hour < 12) ? "AM" : "PM";
        int displayHour = hour % 12;
        if (displayHour == 0) displayHour = 12;
        String minuteStr = String.format(Locale.ROOT, "%02d", minute);
        return displayHour + ":" + minuteStr + " " + ampm;
    }
}