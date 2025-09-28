package de.tomalbrc.questr.impl;

import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import xyz.nucleoid.stimuli.Stimuli;
import xyz.nucleoid.stimuli.event.EventResult;
import xyz.nucleoid.stimuli.event.player.PlayerC2SPacketEvent;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DialogManager {
    private final Map<ServerGamePacketListenerImpl, MiniDialog> runningDialogs = new ConcurrentHashMap<>();

    public DialogManager() {
        Stimuli.global().listen(PlayerC2SPacketEvent.EVENT, (player, packet) -> {
            if (packet instanceof ServerboundSwingPacket && runningDialogs.containsKey(player.connection)) {
                var dialog = runningDialogs.get(player.connection);
                if (dialog != null) {
                    if (!dialog.textFinished()) {
                        dialog.skip();
                    } else if (!dialog.isClosed()) {
                        dialog.close();
                    }
                }
                return EventResult.DENY;
            }

            return EventResult.PASS;
        });
    }

    public void add(ServerGamePacketListenerImpl player, MiniDialog dialog) {
        runningDialogs.put(player, dialog);
    }

    public void remove(ServerGamePacketListenerImpl serverPlayer) {
        runningDialogs.remove(serverPlayer);
    }

    public void tick(MinecraftServer server) {
        for (Iterator<Map.Entry<ServerGamePacketListenerImpl, MiniDialog>> it = runningDialogs.entrySet().iterator(); it.hasNext();) {
            var entry = it.next();
            var dialog = entry.getValue();

            if (dialog.isClosed()) {
                it.remove();
            } else {
                dialog.tick(server);
            }
        }
    }

    public void playerLeft(ServerGamePacketListenerImpl player) {
        remove(player);
    }
}
