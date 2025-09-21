package de.tomalbrc.questr.api.requirement;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class RequirementTypes {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<ResourceLocation, RequirementType> REGISTRY = new ConcurrentHashMap<>();

    private RequirementTypes() {}

    public static void register(RequirementType requirementType) {
        var type = requirementType.id();
        if (REGISTRY.containsKey(type)) {
            LOGGER.warn("Replacing existing requirement: {}", type);
        }
        REGISTRY.put(type, requirementType);

        requirementType.registerEventListener();
    }

    public static RequirementType get(ResourceLocation type) {
        return REGISTRY.get(type);
    }
}