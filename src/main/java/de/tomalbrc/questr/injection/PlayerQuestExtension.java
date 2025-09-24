package de.tomalbrc.questr.injection;

import de.tomalbrc.questr.api.quest.Quest;
import de.tomalbrc.questr.api.quest.QuestProgress;
import de.tomalbrc.questr.api.task.TaskEvent;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;

public interface PlayerQuestExtension {
    default boolean startQuest(Quest quest) {
        return false;
    }

    default boolean hasQuest(Quest quest) {
        return false;
    }

    default QuestProgress cancelQuest(ResourceLocation id) {
        return null;
    }

    default void addQuestProgress(QuestProgress progress) {}

    default Collection<QuestProgress> getActiveQuests() {
        return null;
    }

    default Collection<ResourceLocation> getCompletedQuests() {
        return null;
    }

    default void queueQuestEvent(TaskEvent event) {
    }

    default void tickQuests() {
    }
}
