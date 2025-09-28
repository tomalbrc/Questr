package de.tomalbrc.questr.api.quest;

import de.tomalbrc.questr.api.reward.Reward;
import de.tomalbrc.questr.api.task.Task;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class Quest {
    public ResourceLocation id;
    public ResourceLocation category;
    public String title;
    public String description;
    public QuestLifecycle lifecycle = new QuestLifecycle(true, true, true, 0);
    public QuestRequirement requirements = new QuestRequirement(null, null);
    public List<Task> tasks;
    public List<Reward> rewards;
}
