package de.tomalbrc.questr.impl.storage;

import de.tomalbrc.questr.api.quest.QuestProgress;
import net.minecraft.resources.ResourceLocation;
import org.ladysnake.cca.api.v3.component.Component;

import java.util.Map;

public interface PlayerQuestComponent extends Component {
    Map<ResourceLocation, QuestProgress> getAllProgress();
    QuestProgress getProgress(ResourceLocation questId);
    void setProgress(ResourceLocation questId, QuestProgress progress);
    void removeProgress(ResourceLocation questId);
}