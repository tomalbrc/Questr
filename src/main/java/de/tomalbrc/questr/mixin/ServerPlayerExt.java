package de.tomalbrc.questr.mixin;

import de.tomalbrc.dialogutils.util.TextUtil;
import de.tomalbrc.questr.api.quest.Quest;
import de.tomalbrc.questr.api.quest.QuestProgress;
import de.tomalbrc.questr.api.task.Task;
import de.tomalbrc.questr.api.task.TaskEvent;
import de.tomalbrc.questr.api.task.TaskType;
import de.tomalbrc.questr.api.task.TaskTypes;
import de.tomalbrc.questr.injection.PlayerQuestExtension;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@SuppressWarnings("all")
@Mixin(ServerPlayer.class)
public class ServerPlayerExt implements PlayerQuestExtension {
    @Shadow @Final private MinecraftServer server;
    Map<ResourceLocation, QuestProgress> quest$quests = Collections.synchronizedMap(new Object2ReferenceOpenHashMap<>());
    private static final List<TaskEvent> quest$events = Collections.synchronizedList(new ObjectArrayList<>());

    @Override
    public boolean startQuest(Quest quest) {
        if (!quest$quests.containsKey(quest.id) && quest.requirements.fulfillsRequirements((ServerPlayer)(Object) this)) {
            quest$quests.put(quest.id, new QuestProgress(quest.id));
            ((ServerPlayer)(Object) this).sendSystemMessage(TextUtil.parse("Quest started: " + quest.title));
            quest$quests.put(quest.id, new QuestProgress(quest.id));
            return true;
        }

        return false;
    }

    @Override
    public boolean hasQuest(Quest quest) {
        return quest$quests.containsValue(quest);
    }

    @Override
    public QuestProgress cancelQuest(ResourceLocation id) {
        return quest$quests.remove(id).cancel((ServerPlayer)(Object) this);
    }

    @Override
    public Collection<QuestProgress> getActiveQuests() {
        return quest$quests.values();
    }

    @Override
    public Collection<ResourceLocation> getCompletedQuests() {
        return List.of();
    }

    @Override
    public void queueQuestEvent(TaskEvent event) {
        quest$events.add(event);
    }

    @Override
    public void tickQuests() {
        for (QuestProgress questProgress : getActiveQuests()) {
            if (!questProgress.isActive())
                continue;

            // polling task to event
            for (Task task : questProgress.quest().tasks) {
                TaskType taskType = TaskTypes.get(task.type());
                if (taskType.isPolling()) {
                    var pollEvent = taskType.poll((ServerPlayer)(Object) this, task);
                    if (pollEvent != null) {
                        quest$events.add(pollEvent);
                    }
                }
            }

            for (TaskEvent event : quest$events) {
                var taskType = TaskTypes.get(event.taskType());
                for (Task task : questProgress.quest().tasks) {
                    var sameType = task.type().equals(taskType.id());
                    if (sameType) {
                        if (taskType.meetsFailConditions(event, task)) {
                            questProgress.cancel((ServerPlayer)(Object) this);
                        }
                        if (taskType.meetsConditions(event, task)) {
                            questProgress.incrementTaskProgress(task.id(), event, 1);
                        }
                    }
                }
            }
        }
        quest$events.clear();
        quest$quests.entrySet().removeIf(x -> !x.getValue().isActive());
    }

    @Override
    public void addQuestProgress(QuestProgress progress) {
        quest$quests.put(progress.getQuestId(), progress);
    }
}
