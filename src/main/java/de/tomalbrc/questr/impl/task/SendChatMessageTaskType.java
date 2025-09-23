package de.tomalbrc.questr.impl.task;

import de.tomalbrc.questr.QuestrMod;
import de.tomalbrc.questr.api.context.ContextMap;
import de.tomalbrc.questr.api.context.Keys;
import de.tomalbrc.questr.api.task.TaskEvent;
import de.tomalbrc.questr.api.task.TaskType;
import net.minecraft.resources.ResourceLocation;
import xyz.nucleoid.stimuli.Stimuli;
import xyz.nucleoid.stimuli.event.EventResult;
import xyz.nucleoid.stimuli.event.player.PlayerChatEvent;

public class SendChatMessageTaskType implements TaskType {
    public SendChatMessageTaskType() {}

    @Override
    public ResourceLocation id() {
        return ResourceLocation.withDefaultNamespace("send_chat_message");
    }

    @Override
    public void registerEventListener() {
        Stimuli.global().listen(PlayerChatEvent.EVENT, (serverPlayer, message, bound) -> {
            QuestrMod.EXECUTOR.execute(() -> {
                var map = ContextMap.of(serverPlayer);
                var msg = message.signedContent();
                map.put(Keys.MESSAGE, msg);
                map.put(Keys.MESSAGE_LENGTH, msg.length());
                serverPlayer.queueQuestEvent(new TaskEvent(serverPlayer, id(), map));
            });

            return EventResult.PASS;
        });
    }
}
