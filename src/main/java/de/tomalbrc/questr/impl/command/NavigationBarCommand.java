package de.tomalbrc.questr.impl.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import de.tomalbrc.dialogutils.util.TextUtil;
import de.tomalbrc.questr.QuestrMod;
import de.tomalbrc.questr.impl.MiniDialog;
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
            ServerPlayer player = EntityArgument.getPlayer(context, "player");
            AtomicInteger c = new AtomicInteger();
            var lines = List.of(
                    "I think, therefore I am.",
                    "Time is an illusion. Lunchtime ",
                    "<b>doubly</b> so."
            );
            int maxWidth = 150;

            var genders = List.of("male", "female");
            var voices = List.of("voice_1", "voice_2", "voice_3", "voice_4");

            QuestrMod.DIALOG.add(player, new MiniDialog(player, lines.stream().map(TextUtil::parse).toList(), maxWidth, genders.get(player.getRandom().nextInt(genders.size())), voices.get(player.getRandom().nextInt(voices.size())), ()->{}));

            return Command.SINGLE_SUCCESS;
        }
        return 0;
    }
}