package de.tomalbrc.questr.impl.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
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

        LiteralCommandNode<CommandSourceStack> cancelNode = Commands
                .literal("cancel")
                .build();

        LiteralCommandNode<CommandSourceStack> offerQuestNode = Commands
                .literal("offer")
                .build();

        LiteralCommandNode<CommandSourceStack> startNode = Commands
                .literal("start")
                .build();

        LiteralCommandNode<CommandSourceStack> forceStartNode = Commands
                .literal("force-start")
                .build();

        LiteralCommandNode<CommandSourceStack> dialogNode = Commands
                .literal("dialog")
                .build();



        var nodePlayer = Commands.argument("player", EntityArgument.player());
        var nodeText = Commands.argument("text", StringArgumentType.greedyString());
        var nodeIndex = Commands.argument("index", IntegerArgumentType.integer(0,1000));

        dialogNode.addChild(nodePlayer.then(nodeIndex.executes(NavigationBarCommand::executeSet)).build());

        hudNode.addChild(cancelNode);
        hudNode.addChild(startNode);
        hudNode.addChild(forceStartNode);
        hudNode.addChild(dialogNode);

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

            QuestrMod.DIALOG.add(player.connection, new MiniDialog(player, lines.stream().map(TextUtil::parse).toList(), maxWidth, genders.get(player.getRandom().nextInt(genders.size())), voices.get(player.getRandom().nextInt(voices.size())), ()->{}));

            return Command.SINGLE_SUCCESS;
        }
        return 0;
    }
}