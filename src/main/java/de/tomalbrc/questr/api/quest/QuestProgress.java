
package de.tomalbrc.questr.api.quest;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.tomalbrc.dialogutils.util.TextUtil;
import de.tomalbrc.questr.QuestrMod;
import de.tomalbrc.questr.api.task.Task;
import de.tomalbrc.questr.api.task.TaskEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class QuestProgress {
    private final Identifier quest;
    private final Map<Identifier, Integer> taskProgress; // taskId -> count
    private boolean isCompleted;
    private boolean isCancelled;

    private long cooldownEndsOn;

    public static final Codec<QuestProgress> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Identifier.CODEC.fieldOf("quest").forGetter(QuestProgress::questId),
                    Codec.unboundedMap(Identifier.CODEC, Codec.INT).fieldOf("task_progress").forGetter(QuestProgress::getTaskProgress),
                    Codec.BOOL.fieldOf("is_completed").forGetter(QuestProgress::isCompleted),
                    Codec.BOOL.fieldOf("is_cancelled").forGetter(QuestProgress::isCancelled),
                    Codec.LONG.fieldOf("cooldown_ends_on").forGetter(QuestProgress::getCooldownEndsOn)
            ).apply(instance, QuestProgress::new)
    );

    public QuestProgress(Identifier quest) {
        this.quest = quest;
        this.taskProgress = new ConcurrentHashMap<>();
        this.isCompleted = false;
        this.cooldownEndsOn = 0;
    }

    public QuestProgress(Identifier quest,
                         Map<Identifier, Integer> taskProgress,
                         boolean isCompleted,
                         boolean isCancelled,
                         long cooldownEndsOn) {
        this.quest = quest;
        this.taskProgress = new ConcurrentHashMap<>();
        if (taskProgress != null) this.taskProgress.putAll(taskProgress);
        this.isCompleted = isCompleted;
        this.isCancelled = isCancelled;
        this.cooldownEndsOn = cooldownEndsOn;
    }

    public Identifier questId() {
        return this.quest;
    }

    public Map<Identifier, Integer> getTaskProgress() {
        return this.taskProgress;
    }

    public boolean isCompleted() {
        return this.isCompleted;
    }

    public long getCooldownEndsOn() {
        return this.cooldownEndsOn;
    }

    public boolean incrementTaskProgress(Identifier taskId, TaskEvent event, int amount) {
        taskProgress.compute(taskId, (k, current) -> (current == null ? 0 : current) + amount);
        return checkAndCompleteQuest(event.player());
    }

    public int getProgress(Task task) {
        return taskProgress.getOrDefault(task.id(), 0);
    }

    public boolean isCompleted(Task task) {
        return taskProgress.getOrDefault(task.id(), 0) >= task.target();
    }

    private boolean checkAndCompleteQuest(ServerPlayer serverPlayer) {
        if (isCompleted || isCancelled) return false;

        Quest quest = quest();
        for (Task task : quest.tasks) {
            if (taskProgress.getOrDefault(task.id(), 0) < task.target()) {
                return false;
            }
        }

        // all targets hit / conditions are met
        if (quest.lifecycle != null) {
            this.isCompleted = quest.lifecycle.automaticCompletion();

            if (quest.lifecycle.repeatable()) {
                this.cooldownEndsOn = System.currentTimeMillis() + (quest.lifecycle.cooldownSeconds() * 1000L);
            }
        } else {
            this.isCompleted = true;
        }

        if (isCompleted && QuestrMod.config.announceQuestCompletion) {
            serverPlayer.sendSystemMessage(TextUtil.parse(String.format(QuestrMod.config.messages.completedQuestAnnouncement, serverPlayer.getScoreboardName(), quest.title)));
        }

        return isCompleted;
    }

    public boolean isActive() {
        return !isCompleted && !isCancelled();
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    public void reset() {
        this.isCompleted = false;
        this.isCancelled = false;
        this.taskProgress.clear();
        this.cooldownEndsOn = 0;
    }

    public Quest quest() {
        return Quests.get(quest);
    }

    public QuestProgress cancel(ServerGamePacketListenerImpl serverPlayer) {
        serverPlayer.player.sendSystemMessage(Component.literal(String.format(QuestrMod.config.messages.cancelledQuest, quest.toString())));
        this.isCancelled = false;

        return this;
    }
}
