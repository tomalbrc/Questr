package de.tomalbrc.questr.api.quest;

import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class QuestCategories {
    private static final Map<ResourceLocation, QuestCategory> categories = new ConcurrentHashMap<>();

    public static void register(QuestCategory quest) {
        categories.put(quest.id(), quest);
    }

    public static QuestCategory get(ResourceLocation id) {
        return categories.get(id);
    }

    public static boolean has(ResourceLocation id) {
        return categories.containsKey(id);
    }
}
