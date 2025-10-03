package de.tomalbrc.questr.impl.navigationbar;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NavigationBarManager {
    private final Map<ServerGamePacketListenerImpl, NavigationBar> playerNavigationBars = new ConcurrentHashMap<>();

    public void add(ServerGamePacketListenerImpl player, BlockPos targetPos) {
        NavigationBar newData = new NavigationBar(player, targetPos, NavigationBarLayout.createDefaultLayout());
        newData.setVisible(true);
        newData.setActive(true);
        playerNavigationBars.put(player, newData);
    }

    public void setVisible(ServerPlayer player, boolean vis) {
        NavigationBar bar = this.playerNavigationBars.get(player);
        if (bar == null)
            return;

        bar.setVisible(vis);
    }

    public void remove(ServerGamePacketListenerImpl player) {
        var removed = playerNavigationBars.remove(player);
        if (removed != null)
            removed.setActive(false);
    }

    public void playerJoined(ServerGamePacketListenerImpl player, MinecraftServer server) {
        try {
            this.add(player, BlockPos.ZERO);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void playerLeft(ServerGamePacketListenerImpl player, MinecraftServer server) {
        remove(player);
    }

    public void tick(MinecraftServer server) {
        if (server.getTickCount() % 2 == 0) { // every .1 secs
            for (NavigationBar navigationBar: playerNavigationBars.values()) {
                navigationBar.update();

                if (server.getTickCount() % 40 == 0) {
                    navigationBar.sendParticleHint();
                }
            }
        }
    }
}