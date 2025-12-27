package de.tomalbrc.questr.impl.task;

import de.tomalbrc.questr.api.context.ContextMap;
import de.tomalbrc.questr.api.task.Task;
import de.tomalbrc.questr.api.task.TaskEvent;
import de.tomalbrc.questr.api.task.TaskType;
import net.minecraft.resources.Identifier;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public class OnTickTaskType implements TaskType {
    static Identifier ID = Identifier.withDefaultNamespace("on_tick");

    public OnTickTaskType() {}

    @Override
    public Identifier id() {
        return ID;
    }

    @Override
    public boolean isPolling() {
        return true;
    }

    @Override
    public TaskEvent poll(ServerGamePacketListenerImpl serverPlayer, Task task) {
        return new TaskEvent(serverPlayer.player, id(), ContextMap.of(serverPlayer.player));
    }
}
