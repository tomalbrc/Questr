package de.tomalbrc.questr.injection;

import de.tomalbrc.questr.api.quest.Quest;
import de.tomalbrc.questr.api.quest.QuestEvent;
import de.tomalbrc.questr.api.quest.QuestProgress;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;

public interface PlayerQuestExtension {
    default void startQuest(Quest quest) {
    }

    default boolean hasQuest(Quest quest) {
        return false;
    }

    default QuestProgress cancelQuest(ResourceLocation id) {
        return null;
    }

    default Collection<QuestProgress> getActiveQuests() {
        return null;
    }

    default void queueQuestEvent(QuestEvent event) {
    }

    default void tickQuests() {
    }
}
