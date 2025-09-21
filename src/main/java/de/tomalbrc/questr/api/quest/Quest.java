package de.tomalbrc.questr.api.quest;

import de.tomalbrc.questr.api.requirement.Requirement;
import de.tomalbrc.questr.api.reward.Reward;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class Quest {
    public ResourceLocation id;
    public ResourceLocation category;
    public String title;
    public String description;
    public QuestLifecycle lifecycle;
    public List<Requirement> requirements;
    public List<Reward> rewards;
    public boolean needsSelection = true;
    public String permission;
}
