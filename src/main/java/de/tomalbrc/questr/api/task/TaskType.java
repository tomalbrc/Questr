package de.tomalbrc.questr.api.task;

import de.tomalbrc.questr.api.context.ContextMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.jetbrains.annotations.Nullable;

public interface TaskType {
    ResourceLocation id();

    default void registerEventListener() {}

    default boolean meetsConditions(TaskEvent event, Task task) {
        ContextMap context = event.data();
        context = task.addArgumentContext(context);
        return task.conditions() == null || task.conditions().test(context);
    }

    default boolean meetsFailConditions(TaskEvent event, Task task) {
        ContextMap context = event.data();
        context = task.addArgumentContext(context);
        return task.failConditions() != null && task.failConditions().test(context);
    }

    default boolean isPolling() {
        return false;
    }

    default @Nullable TaskEvent poll(ServerGamePacketListenerImpl serverPlayer, Task task) {
        return null;
    }
}
