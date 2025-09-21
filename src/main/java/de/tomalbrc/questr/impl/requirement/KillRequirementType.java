package de.tomalbrc.questr.impl.requirement;

import de.tomalbrc.questr.api.context.Keys;
import de.tomalbrc.questr.api.context.TypedMap;
import de.tomalbrc.questr.api.quest.QuestEvent;
import de.tomalbrc.questr.api.requirement.Requirement;
import de.tomalbrc.questr.api.requirement.RequirementType;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class KillRequirementType implements RequirementType {
    public KillRequirementType() {}

    @Override
    public ResourceLocation id() {
        return ResourceLocation.withDefaultNamespace("kill");
    }

    @Override
    public void registerEventListener() {
        ServerLivingEntityEvents.AFTER_DEATH.register((livingEntity, damageSource) -> {
            if (damageSource.getEntity() != null && damageSource.getEntity() instanceof ServerPlayer serverPlayer) {
                var map = TypedMap.of(serverPlayer);
                map.put(Keys.ENTITY_TYPE, livingEntity.getType().builtInRegistryHolder().key().location());
                map.put(Keys.POSITION, livingEntity.getOnPos());
                map.put(Keys.DISTANCE, livingEntity.distanceTo(serverPlayer));
                map.put(Keys.ITEM_STACK, serverPlayer.getWeaponItem());
                map.put(Keys.ITEM, serverPlayer.getWeaponItem().getItem());
                serverPlayer.queueQuestEvent(new QuestEvent(serverPlayer, id(), map));
            }
        });
    }
}
