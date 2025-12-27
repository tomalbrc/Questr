package de.tomalbrc.questr.api.quest;

import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class QuestCategories {
    private static final Map<Identifier, QuestCategory> categories = new ConcurrentHashMap<>();

    public static void register(QuestCategory quest) {
        categories.put(quest.id(), quest);
    }

    public static QuestCategory get(Identifier id) {
        return categories.get(id);
    }

    public static boolean has(Identifier id) {
        return categories.containsKey(id);
    }
}
