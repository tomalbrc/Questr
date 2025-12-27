package de.tomalbrc.questr.api.quest;

import net.minecraft.resources.Identifier;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import java.util.List;

public record QuestRequirement(
        String permission,
        List<Identifier> completedQuests
) {
    public boolean fulfillsRequirements(ServerGamePacketListenerImpl serverPlayer) {
        ////if (permission == null && (completedQuests == null || completedQuests.isEmpty()))
        ////    return true;

        // if (permission != null && !player.hasPerm(permission)) {return false}

        var completed = serverPlayer.getCompletedQuests();
        if (completedQuests != null) {
            for (Identifier completedQuest : completedQuests) {
                if (!completed.contains(completedQuest))
                    return false;
            }
        }

        return true;
    }
}
