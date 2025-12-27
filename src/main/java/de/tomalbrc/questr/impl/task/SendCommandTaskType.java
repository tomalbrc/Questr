package de.tomalbrc.questr.impl.task;

import de.tomalbrc.questr.QuestrMod;
import de.tomalbrc.questr.api.context.ContextMap;
import de.tomalbrc.questr.api.context.Keys;
import de.tomalbrc.questr.api.task.TaskEvent;
import de.tomalbrc.questr.api.task.TaskType;
import net.minecraft.resources.Identifier;
import xyz.nucleoid.stimuli.Stimuli;
import xyz.nucleoid.stimuli.event.EventResult;
import xyz.nucleoid.stimuli.event.player.PlayerCommandEvent;

public class SendCommandTaskType implements TaskType {
    public SendCommandTaskType() {}

    @Override
    public Identifier id() {
        return Identifier.withDefaultNamespace("send_command");
    }

    @Override
    public void registerEventListener() {
        Stimuli.global().listen(PlayerCommandEvent.EVENT, (serverPlayer, msg) -> {
            QuestrMod.EXECUTOR.execute(() -> {
                var map = ContextMap.of(serverPlayer);
                map.put(Keys.MESSAGE, msg);
                map.put(Keys.MESSAGE_LENGTH, msg.length());
                serverPlayer.connection.queueQuestEvent(new TaskEvent(serverPlayer, id(), map));
            });

            return EventResult.PASS;
        });
    }
}
