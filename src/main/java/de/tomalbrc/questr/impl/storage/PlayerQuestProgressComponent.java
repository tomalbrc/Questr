package de.tomalbrc.questr.impl.storage;

import com.mojang.serialization.Codec;
import de.tomalbrc.questr.api.quest.QuestProgress;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.ladysnake.cca.api.v3.component.ComponentV3;

public class PlayerQuestProgressComponent implements ComponentV3 {
    private final ServerPlayer player;

    public PlayerQuestProgressComponent(ServerPlayer player) {
        this.player = player;
    }

    @Override
    public void readData(ValueInput tag) {
        tag.read("quests", Codec.list(QuestProgress.CODEC)).ifPresent(x -> {
            for (QuestProgress progress : x) {
                player.addQuestProgress(progress);
            }
        });
    }

    @Override
    public void writeData(ValueOutput tag) {
        tag.store("quests", Codec.list(QuestProgress.CODEC), player.getActiveQuests().stream().toList());
    }
}
