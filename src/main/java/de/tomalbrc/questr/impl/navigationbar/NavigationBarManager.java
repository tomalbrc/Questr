package de.tomalbrc.questr.impl.navigationbar;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NavigationBarManager {
    private final Map<ServerPlayer, NavigationBar> playerNavigationBars = new ConcurrentHashMap<>();

    public void add(ServerPlayer player, String message, BlockPos targetPos) {
        NavigationBar newData = new NavigationBar(player, message, targetPos);
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

    public void remove(ServerPlayer player) {
        var removed = playerNavigationBars.remove(player);
        if (removed != null)
            removed.setActive(false);
    }

    public void playerJoined(ServerPlayer player, MinecraftServer server) {
        try {
            this.add(player, "", BlockPos.ZERO);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void playerLeft(ServerPlayer player, MinecraftServer server) {
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