package de.tomalbrc.questr.api.task;

import de.tomalbrc.questr.QuestrMod;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class TaskTypes {
    private static final Map<ResourceLocation, TaskType> REGISTRY = new ConcurrentHashMap<>();

    private TaskTypes() {}

    public static void register(TaskType taskType) {
        var type = taskType.id();
        if (REGISTRY.containsKey(type)) {
            QuestrMod.LOGGER.warn("Replacing existing task: {}", type);
        }
        REGISTRY.put(type, taskType);

        taskType.registerEventListener();
    }

    public static TaskType get(ResourceLocation type) {
        return REGISTRY.get(type);
    }
}