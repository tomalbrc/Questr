package de.tomalbrc.questr.api.quest;

import de.tomalbrc.questr.QuestrMod;
import de.tomalbrc.questr.api.task.Task;
import de.tomalbrc.questr.api.task.TaskEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class QuestProgress {
    private final ResourceLocation quest;
    private final Map<ResourceLocation, Integer> taskProgress; // taskId -> count
    private boolean isCompleted;
    private boolean isCancelled;

    private long cooldownEndsAt;

    public QuestProgress(ResourceLocation quest) {
        this.quest = quest;
        this.taskProgress = new ConcurrentHashMap<>();
        this.isCompleted = false;
        this.cooldownEndsAt = 0;
    }

    public boolean incrementTaskProgress(ResourceLocation taskId, TaskEvent event, int amount) {
        taskProgress.compute(taskId, (k, current) -> (current == null ? 0 : current) + amount);
        return checkAndCompleteQuest(event.player());
    }

    public int getProgress(Task task) {
        return taskProgress.getOrDefault(task.getId(), 0);
    }

    private boolean checkAndCompleteQuest(ServerPlayer serverPlayer) {
        if (isCompleted || isCancelled) return false;

        var quest = quest();
        for (Task task : quest.tasks) {
            if (taskProgress.getOrDefault(task.getId(), 0) < task.getTarget()) {
                return false;
            }
        }

        // requirements are met
        this.isCompleted = true;
        if (quest.lifecycle != null && quest.lifecycle.repeatable()) {
            this.cooldownEndsAt = System.currentTimeMillis() + (quest.lifecycle.cooldownSeconds() * 1000L);
        }

        quest.rewards.forEach(reward -> reward.apply(serverPlayer));

        if (QuestrMod.config.announceQuestCompletion && serverPlayer.getServer() != null) {
            serverPlayer.getServer().sendSystemMessage(Component.literal(String.format(QuestrMod.config.messages.completedQuestAnnouncement, serverPlayer.getScoreboardName(), quest.toString())));
        }

        return true;
    }

    public boolean isActive() {
        return !isQuestCompleted() && !isCancelled();
    }

    public boolean isQuestCompleted() {
        var quest = quest();
        if (quest.lifecycle != null && !quest.lifecycle.repeatable()) {
            return isCompleted;
        }

        if (isCompleted && System.currentTimeMillis() >= cooldownEndsAt) {
            reset();
            return false; // ready to be started again
        }

        return isCompleted;
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    public void reset() {
        this.isCompleted = false;
        this.isCancelled = false;
        this.taskProgress.clear();
        this.cooldownEndsAt = 0;
    }

    public Quest quest() {
        return Quests.get(quest);
    }

    public QuestProgress cancel(ServerPlayer serverPlayer) {
        serverPlayer.sendSystemMessage(Component.literal(String.format(QuestrMod.config.messages.cancelledQuest, quest.toString())));
        this.isCancelled = false;

        return this;
    }
}