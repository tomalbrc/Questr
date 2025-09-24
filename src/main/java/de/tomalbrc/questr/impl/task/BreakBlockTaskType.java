package de.tomalbrc.questr.impl.task;

import de.tomalbrc.questr.QuestrMod;
import de.tomalbrc.questr.api.context.ContextMap;
import de.tomalbrc.questr.api.context.Keys;
import de.tomalbrc.questr.api.task.TaskEvent;
import de.tomalbrc.questr.api.task.TaskType;
import de.tomalbrc.questr.impl.util.ResourceSet;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.stream.Collectors;

public class BreakBlockTaskType implements TaskType {
    public BreakBlockTaskType() {}

    @Override
    public ResourceLocation id() {
        return ResourceLocation.withDefaultNamespace("break_block");
    }

    @Override
    public void registerEventListener() {
        PlayerBlockBreakEvents.AFTER.register((level, player, blockPos, blockState, blockEntity) -> {
            if (player instanceof ServerPlayer serverPlayer) {
                QuestrMod.EXECUTOR.execute(() -> {
                    var map = ContextMap.of(serverPlayer);
                    if (blockEntity != null)
                        map.put(Keys.BLOCK_ENTITY_TYPE, blockEntity.getType().builtInRegistryHolder().key().location());

                    map.put(Keys.BLOCK_TAG, ResourceSet.of(blockState.getBlockHolder().tags().map(x -> x.location()).collect(Collectors.toSet())));
                    map.put(Keys.BLOCK_STATE, blockState);
                    map.put(Keys.BLOCK, blockState.getBlock());
                    map.put(Keys.POSITION, blockPos);
                    map.put(Keys.DISTANCE, (float)blockPos.getCenter().distanceTo(serverPlayer.position()));
                    serverPlayer.queueQuestEvent(new TaskEvent(serverPlayer, id(), map));
                });
            }
        });
    }
}
