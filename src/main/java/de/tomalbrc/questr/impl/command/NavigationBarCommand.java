package de.tomalbrc.questr.impl.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import de.tomalbrc.questr.QuestrMod;
import de.tomalbrc.questr.impl.util.Animalese;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class NavigationBarCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> hudNode = Commands
                .literal("questr")
                .build();

        LiteralCommandNode<CommandSourceStack> removeNode = Commands
                .literal("remove")
                .build();


        LiteralCommandNode<CommandSourceStack> setNode = Commands
                .literal("set")
                .build();

        var nodePlayerSet = Commands.argument("player", EntityArgument.player());
        var activateIndex = Commands.argument("index", IntegerArgumentType.integer(0,1000));

        setNode.addChild(nodePlayerSet.then(activateIndex.executes(NavigationBarCommand::executeSet)).build());

        hudNode.addChild(removeNode);
        hudNode.addChild(setNode);

        dispatcher.getRoot().addChild(hudNode);
    }

    private static int executeSet(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if (context.getSource() != null && context.getSource().getPlayer() != null) {
// Player, counter, and lines list remain the same
            ServerPlayer player = EntityArgument.getPlayer(context, "player");
            AtomicInteger c = new AtomicInteger();
            var lines = List.of(
                    "The quick brown fox jumps over the lazy dog.",
                    "A journey of a thousand miles begins with a single step.",
                    "To be or not to be, that is the question.",
                    "All that glitters is not gold.",
                    "The only thing we have to fear is fear itself.",
                    "Ask not what your country can do for you, ask what you can do for your country.",
                    "Elementary, my dear Watson.",
                    "I have a dream that one day this nation will rise up.",
                    "It was the best of times, it was the worst of times.",
                    "The best-laid schemes of mice and men often go awry.",
                    "I think, therefore I am.",
                    "This is one small step for a man, one giant leap for mankind.",
                    "In the beginning, the universe was created. This has made a lot of people very angry and been widely regarded as a bad move.",
                    "Time is an illusion. Lunchtime doubly so.",
                    "So long, and thanks for all the fish.",
                    "Don't panic."
            );

            ServerTickEvents.END_SERVER_TICK.register(minecraftServer -> {
                if (minecraftServer.getTickCount() % 2 != 0) {
                    return;
                }

                int globalCharIndex = c.get();
                int charsInPreviousLines = 0;

                for (String line : lines) {
                    if (globalCharIndex < charsInPreviousLines + line.length()) {
                        int charIndexInLine = globalCharIndex - charsInPreviousLines;

                        char characterToPlay = line.charAt(charIndexInLine);
                        Animalese.playLetter(player, characterToPlay, 1f, player.getRandom().nextIntBetweenInclusive(80, 100)/ 100f, "female", "voice_3");

                        c.incrementAndGet();

                        return;
                    }

                    charsInPreviousLines += line.length();
                }
            });

            QuestrMod.NAVIGATION.setVisible(player, true);

            return Command.SINGLE_SUCCESS;
        }
        return 0;
    }
}