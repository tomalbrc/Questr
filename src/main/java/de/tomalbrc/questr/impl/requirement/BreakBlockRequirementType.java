package de.tomalbrc.questr.impl.requirement;

import de.tomalbrc.questr.api.context.Keys;
import de.tomalbrc.questr.api.context.TypedMap;
import de.tomalbrc.questr.api.quest.QuestEvent;
import de.tomalbrc.questr.api.requirement.Requirement;
import de.tomalbrc.questr.api.requirement.RequirementType;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class BreakBlockRequirementType implements RequirementType {
    public BreakBlockRequirementType() {}

    @Override
    public ResourceLocation id() {
        return ResourceLocation.withDefaultNamespace("kill");
    }

    @Override
    public void registerEventListener() {
        PlayerBlockBreakEvents.AFTER.register((level, player, blockPos, blockState, blockEntity) -> {
            if (player instanceof ServerPlayer serverPlayer) {
                var map = TypedMap.of(serverPlayer);
                if (blockEntity != null)
                    map.put(Keys.BLOCK_ENTITY_TYPE, blockEntity.getType().builtInRegistryHolder().key().location());

                map.put(Keys.POSITION, blockPos);
                map.put(Keys.DISTANCE, (float)blockPos.getCenter().distanceTo(serverPlayer.position()));
                serverPlayer.queueQuestEvent(new QuestEvent(serverPlayer, id(), map));
            }
        });
    }
}
