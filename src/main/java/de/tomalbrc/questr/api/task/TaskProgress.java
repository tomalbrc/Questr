package de.tomalbrc.questr.api.task;

import net.minecraft.resources.Identifier;

public record TaskProgress(Identifier id, int value, long startTime) {
}
