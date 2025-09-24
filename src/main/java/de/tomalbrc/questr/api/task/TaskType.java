package de.tomalbrc.questr.api.task;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public interface TaskType {
    ResourceLocation id();

    default void registerEventListener() {}

    default boolean meetsConditions(TaskEvent event, Task task) {
        return task.conditions() == null || task.conditions().test(event.data());
    }

    default boolean isPolling() {
        return false;
    }

    default @Nullable TaskEvent poll(ServerPlayer serverPlayer, Task task) {
        return null;
    }
}
