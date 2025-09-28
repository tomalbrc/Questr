package de.tomalbrc.questr.api.task;

import net.minecraft.resources.ResourceLocation;

public record TaskProgress(ResourceLocation id, int value, long startTime) {
}
