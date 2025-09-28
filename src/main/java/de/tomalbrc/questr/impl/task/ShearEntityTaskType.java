package de.tomalbrc.questr.impl.task;

import de.tomalbrc.questr.QuestrMod;
import de.tomalbrc.questr.api.context.ContextMap;
import de.tomalbrc.questr.api.context.Keys;
import de.tomalbrc.questr.api.task.TaskEvent;
import de.tomalbrc.questr.api.task.TaskType;
import de.tomalbrc.questr.impl.util.ResourceSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import xyz.nucleoid.stimuli.Stimuli;
import xyz.nucleoid.stimuli.event.EventResult;
import xyz.nucleoid.stimuli.event.entity.EntityShearEvent;

import java.util.stream.Collectors;

public class ShearEntityTaskType implements TaskType {
    public ShearEntityTaskType() {}

    @Override
    public ResourceLocation id() {
        return ResourceLocation.withDefaultNamespace("shear_entity");
    }

    @Override
    public void registerEventListener() {
        Stimuli.global().listen(EntityShearEvent.EVENT, (livingEntity, serverPlayer, hand, pos) -> {
            if (serverPlayer != null) QuestrMod.EXECUTOR.execute(() -> {
                var map = ContextMap.of(serverPlayer);
                map.put(Keys.ENTITY_TAG, ResourceSet.of(livingEntity.getType().builtInRegistryHolder().tags().map(TagKey::location).collect(Collectors.toSet())));
                map.put(Keys.ENTITY_TYPE, livingEntity.getType().builtInRegistryHolder().key().location());
                map.put(Keys.POSITION, livingEntity.getOnPos());
                map.put(Keys.DISTANCE, livingEntity.distanceTo(serverPlayer));
                serverPlayer.connection.queueQuestEvent(new TaskEvent(serverPlayer, id(), map));
            });

            return EventResult.PASS;
        });
    }
}
