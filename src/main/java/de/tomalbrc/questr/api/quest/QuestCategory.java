package de.tomalbrc.questr.api.quest;

import net.minecraft.resources.ResourceLocation;

public record QuestCategory(
        ResourceLocation id,
        String title,
        long periodSeconds,         // 0 = no periodic limit
        QuestLifecycle lifecycle    // default lifecycle for quests in this category
) {}
