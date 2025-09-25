package de.tomalbrc.questr.api.task;

import de.tomalbrc.questr.api.context.ContextMap;
import de.tomalbrc.questr.api.context.DataKey;
import de.tomalbrc.questr.api.context.Keys;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
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

    default @Nullable TaskEvent poll(ServerPlayer serverPlayer, Task task) {
        return null;
    }
}
