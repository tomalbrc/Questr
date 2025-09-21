package de.tomalbrc.questr.impl.navigationbar;

import de.tomalbrc.dialogutils.util.TextAligner;
import eu.pb4.placeholders.api.TextParserUtils;
import net.fabricmc.loader.impl.util.StringUtil;
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
import java.util.UUID;

public class NavigationBar {
    private static final String BACKGROUND_EDGE = "\uE100\uE200";
    private static final String BACKGROUND = "\uE101\uE200";
    private static final String NEGATIVE_SPACE = "\uE200";
    private static final String NEGATIVE_SPACE10 = "\uE200";
    private static final ResourceLocation NAV_FONT = ResourceLocation.fromNamespaceAndPath("questr", "nav");
    private static final ResourceLocation FONT_BOXY = ResourceLocation.fromNamespaceAndPath("questr", "boxy");
    private static final Style NAV_STYLE_WHITE = Style.EMPTY.withFont(NAV_FONT).withShadowColor(0).withColor(0xFFFFFF);
    private static final Style NAV_STYLE_BLACK = Style.EMPTY.withFont(NAV_FONT).withShadowColor(0).withColor(0);

    private static final Style STYLE_WHITE_BOXY = Style.EMPTY.withFont(FONT_BOXY).withShadowColor(0).withColor(0xFFFFFF);
    private static final Style STYLE_WHITE_VANILLA = Style.EMPTY.withFont(Style.DEFAULT_FONT).withShadowColor(0).withColor(0xFFFFFF);

    private final MinecraftServer server;
    private final @Nullable String message;
    private final @NotNull UUID playerUUID;

    private final @NotNull BlockPos targetPos;
    private final @NotNull BossEvent bossEvent;

    private boolean active;

    private boolean visible;
    private Vec3 oldPos = Vec3.ZERO;
    private float oldRot = 0;

    public NavigationBar(@NotNull MinecraftServer server, @NotNull UUID playerUUID, @Nullable String message, @NotNull BlockPos targetPos) {
        this.message = message;
        this.server = server;
        this.playerUUID = playerUUID;
        this.targetPos = targetPos;
        this.active = false;
        this.visible = false;

        this.bossEvent = new ServerBossEvent(Component.empty(), BossEvent.BossBarColor.YELLOW, BossEvent.BossBarOverlay.PROGRESS);
        this.bossEvent.setProgress(0);
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setActive(boolean active) {
        this.active = active;

        ServerPlayer player = server.getPlayerList().getPlayer(this.playerUUID);
        if (player != null) {
            if (active) {
                player.connection.send(ClientboundBossEventPacket.createAddPacket(this.bossEvent));
            } else {
                player.connection.send(ClientboundBossEventPacket.createRemovePacket(this.bossEvent.getId()));
            }
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
        return TextParserUtils.formatText(String.format("<font:questr:nav>%s</font>", "\uE202".repeat(s))).copy();
    }

    public MutableComponent world(String name) {
        int width = 70;
        var timeText = TextAligner.alignLine("",
                String.format("<white><font:questr:nav>%s</font></white>", name),
                "",
                width
        );
        return background(width).append(TextParserUtils.formatText(timeText));
    }

    public MutableComponent clock(long time) {
        int width = 50;
        var timeText = TextAligner.alignLine("",
                String.format("<white><font:questr:nav>%s</font></white>", ticksToClock(time)),
                "",
                width
        );
        return background(width).append(TextParserUtils.formatText(timeText));
    }

    public MutableComponent background(int width) {
        return Component.literal(BACKGROUND_EDGE + BACKGROUND.repeat(width-2) + BACKGROUND_EDGE + NEGATIVE_SPACE.repeat(width)).withStyle(NAV_STYLE_BLACK);
    }

    public MutableComponent backgroundNoShift(int width) {
        return Component.literal(BACKGROUND_EDGE + BACKGROUND.repeat(width-2) + BACKGROUND_EDGE).withStyle(NAV_STYLE_BLACK);
    }

    public Component message(ServerPlayer player) {
        int width = 120;

        if (player != null && !player.isRemoved()) {
            Vec3 delta = this.targetPos.getCenter().subtract(player.position());

            double distance = this.targetPos.getCenter().subtract(player.position()).horizontalDistance();

            float yRot = player.getYHeadRot() + 180;
            String arrow = getArrow(delta, yRot);

            var icon = Component.literal(arrow).withStyle(NAV_STYLE_WHITE);
            var str = String.format("%3.0f BLOCKS", distance);
            if (distance > 999) str = String.format("FAR AWAY  ", distance);
            var padding = width-(str.length()*10) - 17;//16+1=arrow
            var paddingText = Component.literal("\uE200".repeat(padding)).withStyle(STYLE_WHITE_BOXY);
            var worldName = player.level().dimension().location().getPath()
                    .replace("the_", "")
                    .replace("_", " ");
            return background(width).append(icon).append(Component.literal(str).withStyle(STYLE_WHITE_BOXY)).append(paddingText).append(space(1)).append(world(StringUtil.capitalize(worldName))).append(space(1)).append(clock(player.level().dayTime()));
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
        ServerPlayer player = server.getPlayerList().getPlayer(this.playerUUID);
        if (player == null || player.isRemoved()) {
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
            if (true|| (player.position().distanceTo(oldPos) > 0.1 || Math.abs(player.yHeadRot-oldRot) > 1)) {
                var msg = this.message(player);
                if (msg != null) {
                    var p = new ClientboundBossEventPacket(bossEvent.getId(), new ClientboundBossEventPacket.UpdateNameOperation(msg));
                    player.connection.send(p);

                    this.oldPos = player.position();
                    this.oldRot = player.yHeadRot;
                }
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

    public @NotNull UUID getPlayerUUID() {
        return playerUUID;
    }

    public @NotNull BlockPos getTargetPos() {
        return targetPos;
    }

    public void trail() {
        ServerPlayer player = server.getPlayerList().getPlayer(this.playerUUID);
        if (player != null && player.getRandom().nextBoolean()) {
            for (int i = 0; i < 3; i++) {
                var dir = targetPos.getCenter().subtract(player.position()).normalize().add(player.getRandom().nextFloat()*0.25, player.getRandom().nextFloat()*0.25, player.getRandom().nextFloat()*0.25);
                var pos = player.position().add(0, player.getEyeHeight()/2, 0).add(dir.scale(2));
                player.connection.send(new ClientboundLevelParticlesPacket(ParticleTypes.SOUL_FIRE_FLAME, false, false, pos.x(), pos.y(), pos.z(), (float) dir.x(), (float) dir.y(), (float) dir.z(), 0.1f, 0));
            }
        }
    }
}