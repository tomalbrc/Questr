package de.tomalbrc.questr.impl.navigationbar;

import de.tomalbrc.dialogutils.util.ComponentAligner;
import de.tomalbrc.questr.impl.navigationbar.component.NavigationBarComponent;
import de.tomalbrc.questr.impl.navigationbar.component.type.NavigationBarComponentTypes;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.BossEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class NavigationBar {
    private final @NotNull ServerGamePacketListenerImpl player;
    private final @NotNull BlockPos targetPos;
    private final @NotNull BossEvent bossEvent;
    private final NavigationBarLayout layout;

    private boolean active;
    private boolean visible;

    private List<NavigationBarComponent> components1;
    private List<NavigationBarComponent> components2;

    public NavigationBar(@NotNull ServerGamePacketListenerImpl player, @NotNull BlockPos targetPos, @NotNull NavigationBarLayout layout) {
        this.player = player;
        this.targetPos = targetPos;
        this.layout = layout;
        this.active = true;
        this.visible = true;
        this.bossEvent = new ServerBossEvent(Component.empty(), BossEvent.BossBarColor.YELLOW, BossEvent.BossBarOverlay.PROGRESS);
        this.bossEvent.setProgress(0);

        reload();
    }

    public void reload() {
        this.components1 = preBuildLine(layout.getLine1(), 1);
        this.components2 = preBuildLine(layout.getLine2(), 2);
    }

    public Component message(ServerPlayer player) {
        if (player == null || player.isRemoved()) {
            return null;
        }

        MutableComponent line1 = buildLine(components1, player);
        MutableComponent line2 = buildLine(components2, player);
        int line1Width = ComponentAligner.getWidth(line1);
        int line2Width = ComponentAligner.getWidth(line2);
        return line1.append(ComponentAligner.spacer(-line1Width)).append(line2).append(ComponentAligner.spacer(line1Width-line2Width));
    }

    private List<NavigationBarComponent> preBuildLine(List<NavigationBarConfig> configs, int line) {
        List<NavigationBarComponent> list = new ObjectArrayList<>();
        for (NavigationBarConfig config : configs) {
            list.add(NavigationBarComponentTypes.get(config.type()).orElseThrow().create(config, line));
        }
        return list;
    }

    private MutableComponent buildLine(List<NavigationBarComponent> configs, ServerPlayer player) {
        MutableComponent lineComponent = Component.empty();
        for (NavigationBarComponent component : configs) {
            lineComponent.append(buildComponentFromConfig(component, player));
        }
        return lineComponent;
    }

    private MutableComponent buildComponentFromConfig(NavigationBarComponent component, ServerPlayer player) {
        return component.getText(player);
    }

    public @NotNull BlockPos getTargetPos() {
        return targetPos;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
        if (active) this.player.send(ClientboundBossEventPacket.createAddPacket(this.bossEvent));
        else this.player.send(ClientboundBossEventPacket.createRemovePacket(this.bossEvent.getId()));
    }

    public void update() {
        if (player.player.isRemoved()) return;
        if (this.isVisible() && !this.isActive()) this.setActive(true);
        if (!this.isVisible() && this.isActive()) this.setActive(false);
        if (active) {
            var msg = this.message(player.player);
            if (msg != null) {
                bossEvent.setName(msg);
                var packet = ClientboundBossEventPacket.createUpdateNamePacket(bossEvent);
                player.player.connection.send(packet);
            }
        }
    }

    public void sendParticleHint() {
        if (player.player.getRandom().nextBoolean()) {
            for (int i = 0; i < 3; i++) {
                var dir = targetPos.getCenter().subtract(player.player.position()).normalize().add(player.player.getRandom().nextFloat() * 0.25, player.player.getRandom().nextFloat() * 0.25, player.player.getRandom().nextFloat() * 0.25);
                var pos = player.player.position().add(0, player.player.getEyeHeight() / 2, 0).add(dir.scale(2));
                player.send(new ClientboundLevelParticlesPacket(ParticleTypes.SOUL_FIRE_FLAME, false, false, pos.x(), pos.y(), pos.z(), (float) dir.x(), (float) dir.y(), (float) dir.z(), 0.1f, 0));
            }
        }
    }
}