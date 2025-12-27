package de.tomalbrc.questr.impl.storage;

import de.tomalbrc.questr.api.quest.QuestProgress;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ProgressList {
    public static Map<UUID, Map<Identifier, QuestProgress>> PROGRESS = new ConcurrentHashMap<>();

    public static Collection<QuestProgress> getProgress(UUID uuid) {
        var p = PROGRESS.get(uuid);
        return p != null ? p.values() : Collections.EMPTY_LIST;
    }

    public static void add(UUID uuid, QuestProgress questProgress) {
        PROGRESS.computeIfAbsent(uuid, x -> new Object2ObjectOpenHashMap<>()).put(questProgress.questId(), questProgress);
    }

    public static boolean has(UUID uuid, Identifier questId) {
        return PROGRESS.computeIfAbsent(uuid, x -> new Object2ObjectOpenHashMap<>()).containsKey(questId);
    }

    public static void remove(UUID uuid, QuestProgress questProgress, ServerPlayer player) {
        var map = PROGRESS.computeIfAbsent(uuid, x -> new Object2ObjectOpenHashMap<>());
        map.remove(questProgress.questId());
        if (map.isEmpty())
            PROGRESS.remove(uuid);

        questProgress.cancel(player.connection);
    }

    public static void remove(UUID uuid) {
        PROGRESS.remove(uuid);
    }
}
