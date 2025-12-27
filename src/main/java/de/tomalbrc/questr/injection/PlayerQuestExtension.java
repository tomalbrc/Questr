package de.tomalbrc.questr.injection;

import de.tomalbrc.questr.api.quest.Quest;
import de.tomalbrc.questr.api.task.TaskEvent;
import net.minecraft.resources.Identifier;

import java.util.Collection;

public interface PlayerQuestExtension {
    default boolean startQuest(Quest quest) {
        return false;
    }

    default Collection<Identifier> getCompletedQuests() {
        return null;
    }

    default void queueQuestEvent(TaskEvent event) {
    }

    default void tickQuests() {
    }
}
