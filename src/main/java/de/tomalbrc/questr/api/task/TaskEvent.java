package de.tomalbrc.questr.api.task;

import de.tomalbrc.questr.api.context.ContextMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public record TaskEvent(ServerPlayer player, ResourceLocation taskType, ContextMap data) {
}
