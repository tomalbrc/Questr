package de.tomalbrc.questr.impl.task;

import de.tomalbrc.questr.api.context.ContextMap;
import de.tomalbrc.questr.api.task.Task;
import de.tomalbrc.questr.api.task.TaskEvent;
import de.tomalbrc.questr.api.task.TaskType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class OnTickTaskType implements TaskType {
    static ResourceLocation ID = ResourceLocation.withDefaultNamespace("on_tick");

    public OnTickTaskType() {}

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public boolean isPolling() {
        return true;
    }

    @Override
    public TaskEvent poll(ServerPlayer serverPlayer, Task task) {
        return new TaskEvent(serverPlayer, id(), ContextMap.of(serverPlayer));
    }
}
