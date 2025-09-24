package de.tomalbrc.questr.impl.navigationbar;

import de.tomalbrc.dialogutils.util.ComponentAligner;
import de.tomalbrc.dialogutils.util.TextUtil;
import de.tomalbrc.questr.QuestrMod;
import de.tomalbrc.questr.impl.util.SmallCapsConverter;
import net.fabricmc.loader.impl.util.StringUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.SegmentedAnglePrecision;
import net.minecraft.world.BossEvent;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class NavigationBar {
    private static final String BACKGROUND_EDGE = "\uE100\uE200";
    private static final String BACKGROUND = "\uE101\uE200";
    private static final String NEGATIVE_SPACE = "\uE200";
    private static final ResourceLocation NAV_FONT = ResourceLocation.fromNamespaceAndPath("questr", "nav");
    private static final Style NAV_STYLE_WHITE = Style.EMPTY.withFont(NAV_FONT).withShadowColor(0).withColor(0xFFFFFF);
    private static final Style NAV_STYLE_BLACK = Style.EMPTY.withFont(NAV_FONT).withShadowColor(0).withColor(0);

    private final MinecraftServer server;
    private final @Nullable String message;
    private final @NotNull ServerPlayer player;

    private final @NotNull BlockPos targetPos;
    private final @NotNull BossEvent bossEvent;

    private boolean active;
    private boolean visible;

    public NavigationBar(@NotNull ServerPlayer player, @Nullable String message, @NotNull BlockPos targetPos) {
        this.message = message;
        this.player = player;
        this.server = player.getServer();
        this.targetPos = targetPos;
        this.active = true;
        this.visible = true;

        this.bossEvent = new ServerBossEvent(Component.empty(), BossEvent.BossBarColor.YELLOW, BossEvent.BossBarOverlay.PROGRESS);
        this.bossEvent.setProgress(0);
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setActive(boolean active) {
        this.active = active;

        if (active) {
            this.player.connection.send(ClientboundBossEventPacket.createAddPacket(this.bossEvent));
        } else {
            this.player.connection.send(ClientboundBossEventPacket.createRemovePacket(this.bossEvent.getId()));
        }
    }

    public static String ticksToClock(long timeTicks) {
        long ticks = timeTicks % 24000;
        if (ticks < 0) ticks += 24000;

        double fractionOfDay = ticks / 24000.0;

        double hours24 = fractionOfDay * 24.0;
        // but add offset so 0 ticks = 6:00
        double realHours = (hours24 + 6.0) % 24.0;

        int hour = (int) realHours;
        int minute = (int) ((realHours - hour) * 60);

        String ampm = (hour < 12) ? "AM" : "PM";
        int displayHour = hour % 12;
        if (displayHour == 0) displayHour = 12;

        // zero-pad minutes
        String minuteStr = String.format(Locale.ROOT, "%02d", minute);

        return displayHour + ":" + minuteStr + " " + ampm;
    }

    public MutableComponent space(int s) {
        return TextUtil.parse(String.format("<font:questr:nav>%s</font>", "\uE202".repeat(s))).copy();
    }

    public MutableComponent world(String name) {
        String iconStr = "\uE100";
        if (name.toLowerCase().contains("end")) {
            iconStr = "\uE101";
        }
        else if (name.toLowerCase().contains("nether")) {
            iconStr = "\uE102";
        }

        var iconComponent = icon(iconStr);
        int iconWidth = ComponentAligner.getWidth(iconComponent);

        int width = 65;
        var text = ComponentAligner.align(TextUtil.parse(String.format("<white><font:%s>%s</font></white>", QuestrMod.NAV_FONT, SmallCapsConverter.toSmallCaps(name))), TextUtil.Alignment.CENTER, width - iconWidth - 4);
        return background(width)
                .append(ComponentAligner.spacer(2))
                .append(iconComponent)
                .append(ComponentAligner.spacer(2))
                .append(text);
    }

    public static MutableComponent icon(String icon) {
        return Component.literal(icon).withStyle(Style.EMPTY.withFont(QuestrMod.ICON_FONT_NAV).withColor(ChatFormatting.WHITE));
    }

    public static MutableComponent clock(long time) {
        var iconComponent = icon("\uE103");
        int iconWidth = ComponentAligner.getWidth(iconComponent);

        int width = 60;
        var timeText = ComponentAligner.align(TextUtil.parse(String.format("<white><font:questr:nav>%s</font></white>", ticksToClock(time))), TextUtil.Alignment.CENTER, width-iconWidth-4);
        return background(width)
                .append(ComponentAligner.spacer(2))
                .append(iconComponent)
                .append(ComponentAligner.spacer(2))
                .append(timeText);
    }

    public static MutableComponent background(int width) {
        return Component.literal(BACKGROUND_EDGE + BACKGROUND.repeat(width-2) + BACKGROUND_EDGE + NEGATIVE_SPACE.repeat(width)).withStyle(NAV_STYLE_BLACK);
    }

    public static MutableComponent navigation(String arrow, String message) {
        int width = 120;

        var icon = Component.literal(arrow).withStyle(NAV_STYLE_WHITE);
        //var iconWidth = ComponentAligner.getWidth(icon); // not usable with mapcanvas-api but #6

        var component = TextUtil.parse(String.format("<white><font:%s>%s</font></white>", NAV_FONT, message));
        var alignedMessage = ComponentAligner.align(component, TextUtil.Alignment.RIGHT, width-17-4); // 17 for icon + 4 spacer px

        return background(width)
                .append(ComponentAligner.spacer(2))
                .append(icon)
                .append(alignedMessage)
                .append(ComponentAligner.spacer(2));
    }

    public Component message(ServerPlayer player) {
        int width = 120;

        if (player != null && !player.isRemoved()) {
            Vec3 delta = this.targetPos.getCenter().subtract(player.position());

            double distance = this.targetPos.getCenter().subtract(player.position()).horizontalDistance();

            float yRot = player.getYHeadRot() + 180;
            String arrow = getArrow(delta, yRot);

            String str;
            if (distance > 99999)
                str = "Far Away";
            else
                str = String.format("%4.0f Blocks Away", distance);

            var worldName = player.level().dimension().location().getPath()
                    .replace("the_", "")
                    .replace("_", " ");

            return navigation(arrow, str)
                    .append(space(1))
                    .append(world(StringUtil.capitalize(worldName)))
                    .append(space(1))
                    .append(clock(player.level().dayTime()));
        }

        return null;
    }

    private static @NotNull String getArrow(Vec3 delta, float yRot) {
        double rotFromPlayer = Math.toDegrees(Math.atan2(delta.z, delta.x) + Math.PI);

        SegmentedAnglePrecision sap = new SegmentedAnglePrecision(3);
        int r = sap.fromDegrees((float) rotFromPlayer - yRot);

        return switch (r) {
            case 0 -> "←";
            case 1 -> "↖";
            case 2 -> "↑";
            case 3 -> "↗";
            case 4 -> "→";
            case 5 -> "↘";
            case 6 -> "↓";
            case 7 -> "↙";
            default -> "x";
        };
    }

    public void update() {
        if (player.isRemoved()) {
            return;
        }

        if (this.isVisible()) {
            if (!this.isActive()) {
                this.setActive(true);
            }
        } else if (this.isActive()) {
            this.setActive(false);
        }

        if (active) {
            var msg = this.message(player);
            if (msg != null) {
                var p = new ClientboundBossEventPacket(bossEvent.getId(), new ClientboundBossEventPacket.UpdateNameOperation(msg));
                player.connection.send(p);
            }
        }
    }

    public boolean isActive() {
        return active;
    }

    public boolean isVisible() {
        return visible;
    }

    public @Nullable String getMessage() {
        return message;
    }

    public @NotNull BlockPos getTargetPos() {
        return targetPos;
    }

    public void sendParticleHint() {
        if (player.getRandom().nextBoolean()) {
            for (int i = 0; i < 3; i++) {
                var dir = targetPos.getCenter().subtract(player.position()).normalize().add(player.getRandom().nextFloat()*0.25, player.getRandom().nextFloat()*0.25, player.getRandom().nextFloat()*0.25);
                var pos = player.position().add(0, player.getEyeHeight()/2, 0).add(dir.scale(2));
                player.connection.send(new ClientboundLevelParticlesPacket(ParticleTypes.SOUL_FIRE_FLAME, false, false, pos.x(), pos.y(), pos.z(), (float) dir.x(), (float) dir.y(), (float) dir.z(), 0.1f, 0));
            }
        }
    }
}