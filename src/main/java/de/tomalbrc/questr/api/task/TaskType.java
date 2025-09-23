package de.tomalbrc.questr.api.task;

import net.minecraft.resources.ResourceLocation;

public interface TaskType {
    ResourceLocation id();
    void registerEventListener();

    default boolean meetsConditions(TaskEvent event, Task task) {
        return task.getConditions() == null || task.getConditions().test(event.data());
    }
}
