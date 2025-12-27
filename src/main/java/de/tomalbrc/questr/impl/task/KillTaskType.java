package de.tomalbrc.questr.impl.task;

import de.tomalbrc.questr.QuestrMod;
import de.tomalbrc.questr.api.context.ContextMap;
import de.tomalbrc.questr.api.context.Keys;
import de.tomalbrc.questr.api.task.TaskEvent;
import de.tomalbrc.questr.api.task.TaskType;
import de.tomalbrc.questr.impl.util.ResourceSet;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;

import java.util.stream.Collectors;

public class KillTaskType implements TaskType {
    public KillTaskType() {}

    @Override
    public Identifier id() {
        return Identifier.withDefaultNamespace("kill");
    }

    @Override
    public void registerEventListener() {
        ServerLivingEntityEvents.AFTER_DEATH.register((livingEntity, damageSource) -> {
            if (damageSource.getEntity() != null && damageSource.getEntity() instanceof ServerPlayer serverPlayer) {
                QuestrMod.EXECUTOR.execute(() -> {
                    var map = ContextMap.of(serverPlayer);
                    map.put(Keys.ENTITY_TAG, ResourceSet.of(livingEntity.getType().builtInRegistryHolder().tags().map(TagKey::location).collect(Collectors.toSet())));
                    map.put(Keys.ENTITY_TYPE, livingEntity.getType().builtInRegistryHolder().key().identifier());
                    map.put(Keys.POSITION, livingEntity.getOnPos());
                    map.put(Keys.DISTANCE, livingEntity.distanceTo(serverPlayer));
                    serverPlayer.connection.queueQuestEvent(new TaskEvent(serverPlayer, id(), map));
                });
            }
        });
    }
}
