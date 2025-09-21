package de.tomalbrc.questr.api.quest;

import de.tomalbrc.questr.api.context.TypedMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public record QuestEvent(ServerPlayer player, ResourceLocation requirementType, TypedMap data) {
}
