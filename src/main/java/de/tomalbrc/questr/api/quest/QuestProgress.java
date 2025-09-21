package de.tomalbrc.questr.api.quest;

import de.tomalbrc.questr.QuestrMod;
import de.tomalbrc.questr.api.requirement.Requirement;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class QuestProgress {
    private final ResourceLocation quest;
    private final Map<ResourceLocation, Integer> requirementProgress; // requirementId -> count
    private boolean isCompleted;
    private boolean isCancelled;

    private long cooldownEndsAt;

    public QuestProgress(ResourceLocation quest) {
        this.quest = quest;
        this.requirementProgress = new ConcurrentHashMap<>();
        for (Requirement requirement : quest().requirements) {
            this.requirementProgress.put(requirement.getId(), 0);
        }
        this.isCompleted = false;
        this.cooldownEndsAt = 0;
    }

    public boolean incrementRequirement(ResourceLocation requirementId, QuestEvent event, int amount) {
        if (isCancelled || isQuestCompleted() || !requirementProgress.containsKey(requirementId)) {
            return false;
        }

        requirementProgress.compute(requirementId, (k, current) -> current + amount);

        return checkAndCompleteQuest(event.player());
    }

    private boolean checkAndCompleteQuest(ServerPlayer serverPlayer) {
        if (isCompleted || isCancelled) return false;

        for (Requirement req : quest().requirements) {
            if (requirementProgress.getOrDefault(req.getId(), 0) < req.getTarget()) {
                return false;
            }
        }

        // requirements are met
        this.isCompleted = true;
        if (quest().lifecycle.repeatable()) {
            this.cooldownEndsAt = System.currentTimeMillis() + (quest().lifecycle.cooldownSeconds() * 1000L);
        }

        quest().rewards.forEach(reward -> reward.apply(serverPlayer));

        if (QuestrMod.config.announceQuestCompletion && serverPlayer.getServer() != null) {
            serverPlayer.getServer().sendSystemMessage(Component.literal(String.format(QuestrMod.config.messages.completedQuestAnnouncement, serverPlayer.getScoreboardName(), quest.toString())));
        }

        return true;
    }

    public boolean isQuestCompleted() {
        if (!quest().lifecycle.repeatable()) {
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
        this.requirementProgress.clear();
        this.cooldownEndsAt = 0;
    }

    public Quest quest() {
        return new Quest();
    }

    public QuestProgress cancel(ServerPlayer serverPlayer) {
        serverPlayer.sendSystemMessage(Component.literal(String.format(QuestrMod.config.messages.cancelledQuest, quest.toString())));
        this.isCancelled = false;

        return this;
    }
}