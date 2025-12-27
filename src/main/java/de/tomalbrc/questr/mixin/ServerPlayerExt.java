package de.tomalbrc.questr.mixin;

import de.tomalbrc.dialogutils.util.TextUtil;
import de.tomalbrc.questr.api.quest.Quest;
import de.tomalbrc.questr.api.quest.QuestProgress;
import de.tomalbrc.questr.api.task.Task;
import de.tomalbrc.questr.api.task.TaskEvent;
import de.tomalbrc.questr.api.task.TaskType;
import de.tomalbrc.questr.api.task.TaskTypes;
import de.tomalbrc.questr.impl.storage.ProgressList;
import de.tomalbrc.questr.injection.PlayerQuestExtension;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.Connection;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerPlayerExt implements PlayerQuestExtension {
    @Shadow public ServerPlayer player;

    @Unique
    private Map<Identifier, QuestProgress> quest$quests;
    @Unique
    private List<TaskEvent> quest$events;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void q$onInit(MinecraftServer minecraftServer, Connection connection, ServerPlayer serverPlayer, CommonListenerCookie commonListenerCookie, CallbackInfo ci) {
        quest$quests = Collections.synchronizedMap(new Object2ReferenceOpenHashMap<>());
        quest$events = Collections.synchronizedList(new ObjectArrayList<>());
    }

    @Override
    public boolean startQuest(Quest quest) {
        if (!quest$quests.containsKey(quest.id) && quest.requirements.fulfillsRequirements(player.connection)) {
            quest$quests.put(quest.id, new QuestProgress(quest.id));
            player.sendSystemMessage(TextUtil.parse("Quest started: " + quest.title));
            quest$quests.put(quest.id, new QuestProgress(quest.id));
            return true;
        }

        return false;
    }

    @Override
    public Collection<Identifier> getCompletedQuests() {
        return List.of();
    }

    @Override
    public void queueQuestEvent(TaskEvent event) {
        quest$events.add(event);
    }

    @Override
    public void tickQuests() {
        for (QuestProgress questProgress : ProgressList.getProgress(player.getUUID())) {
            if (!questProgress.isActive())
                continue;

            // polling task to event
            for (Task task : questProgress.quest().tasks) {
                TaskType taskType = TaskTypes.get(task.type());
                if (taskType.isPolling()) {
                    var pollEvent = taskType.poll((ServerGamePacketListenerImpl)(Object) this, task);
                    if (pollEvent != null) {
                        quest$events.add(pollEvent);
                    }
                }
            }

            for (TaskEvent event : quest$events) {
                var taskType = TaskTypes.get(event.taskType());
                for (Task task : questProgress.quest().tasks) {
                    var sameType = task.type().equals(taskType.id());
                    if (sameType && questProgress.isActive()) {
                        if (taskType.meetsFailConditions(event, task)) {
                            questProgress.cancel((ServerGamePacketListenerImpl)(Object) this);
                        }
                        else if (questProgress.isActive() && taskType.meetsConditions(event, task)) {
                             questProgress.incrementTaskProgress(task.id(), event, 1);

                             if (questProgress.isCompleted()) {
                                 questProgress.quest().rewards.forEach(reward -> reward.apply(player));
                             }
                        }


                    }
                }
            }
        }

        quest$events.clear();
        quest$quests.entrySet().removeIf(x -> !x.getValue().isActive());
    }
}
