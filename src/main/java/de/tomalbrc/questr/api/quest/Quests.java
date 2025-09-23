package de.tomalbrc.questr.api.quest;

import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Quests {
    private static final Map<ResourceLocation, Quest> quests = new ConcurrentHashMap<>();

    public static void register(Quest quest) {
        quests.put(quest.id, quest);
    }

    public static Quest get(ResourceLocation id) {
        return quests.get(id);
    }

    public static Collection<Quest> all() {
        return quests.values();
    }

    public static boolean has(ResourceLocation id) {
        return quests.containsKey(id);
    }
}