package de.tomalbrc.questr.api.quest;

import net.minecraft.resources.Identifier;

public record QuestCategory(
        Identifier id,
        String title,
        long periodSeconds,         // 0 = no periodic limit
        QuestLifecycle lifecycle    // default lifecycle for quests in this category
) {}
