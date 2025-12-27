package de.tomalbrc.questr.api.quest;

import net.minecraft.resources.Identifier;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Quests {
    private static final Map<Identifier, Quest> quests = new ConcurrentHashMap<>();

    public static void register(Quest quest) {
        quests.put(quest.id, quest);
    }

    public static Quest get(Identifier id) {
        return quests.get(id);
    }

    public static Collection<Quest> all() {
        return quests.values();
    }

    public static boolean has(Identifier id) {
        return quests.containsKey(id);
    }
}