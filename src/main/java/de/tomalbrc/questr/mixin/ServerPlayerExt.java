package de.tomalbrc.questr.mixin;

import de.tomalbrc.questr.api.requirement.Requirement;
import de.tomalbrc.questr.api.requirement.RequirementTypes;
import de.tomalbrc.questr.api.quest.Quest;
import de.tomalbrc.questr.api.quest.QuestEvent;
import de.tomalbrc.questr.api.quest.QuestProgress;
import de.tomalbrc.questr.injection.PlayerQuestExtension;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Mixin(ServerPlayer.class)
public class ServerPlayerExt implements PlayerQuestExtension {
    Map<ResourceLocation, QuestProgress> quest$quests = Collections.synchronizedMap(new Object2ReferenceOpenHashMap<>());
    private static final List<QuestEvent> quest$events = Collections.synchronizedList(new ObjectArrayList<>());

    @Override
    public void startQuest(Quest quest) {
        if (!quest$quests.containsKey(quest.id)) {
            quest$quests.put(quest.id, new QuestProgress(quest.id));
            ServerPlayer.class.cast(this).sendSystemMessage(Component.literal("Quest started: " + quest.title));
        }
        quest$quests.put(quest.id, new QuestProgress(quest.id));
    }

    @Override
    public boolean hasQuest(Quest quest) {
        return quest$quests.containsValue(quest);
    }

    @Override
    public QuestProgress cancelQuest(ResourceLocation id) {
        return quest$quests.remove(id).cancel(ServerPlayer.class.cast(this));
    }

    @Override
    public Collection<QuestProgress> getActiveQuests() {
        return quest$quests.values();
    }

    @Override
    public void queueQuestEvent(QuestEvent event) {
        quest$events.add(event);
    }

    @Override
    public void tickQuests() {
        for (QuestProgress questProgress : getActiveQuests()) {
            if (questProgress.isQuestCompleted() || questProgress.isCancelled())
                continue;

            for (QuestEvent event : quest$events) {
                var requirementType = RequirementTypes.get(event.requirementType());
                for (Requirement requirement : questProgress.quest().requirements) {
                    var sameType = requirement.getType().equals(requirementType.id());
                    if (sameType && requirementType.meetsConditions(event, requirement)) {
                        questProgress.incrementRequirement(requirement.getId(), event, 1);
                    }
                }
            }
        }
    }
}
