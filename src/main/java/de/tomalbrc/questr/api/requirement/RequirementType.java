package de.tomalbrc.questr.api.requirement;

import de.tomalbrc.questr.api.quest.QuestEvent;
import net.minecraft.resources.ResourceLocation;

public interface RequirementType {
    ResourceLocation id();
    void registerEventListener();

    default boolean meetsConditions(QuestEvent event, Requirement requirement) {
        return requirement.getConditions().test(event.data());
    }
}
