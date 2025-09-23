package de.tomalbrc.questr.impl.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import de.tomalbrc.questr.QuestrMod;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.server.level.ServerPlayer;

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

    private static int executeAddMessage(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if (context.getSource() != null && context.getSource().getPlayer() != null) {
            ServerPlayer player = EntityArgument.getPlayer(context, "player");
            String message = StringArgumentType.getString(context, "message");

            QuestrMod.NAVIGATION.add(player, message, BlockPosArgument.getBlockPos(context, "location"));

            return Command.SINGLE_SUCCESS;
        }
        return 0;
    }

    private static int executeRemove(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if (context.getSource() != null && context.getSource().getPlayer() != null) {
            ServerPlayer player = EntityArgument.getPlayer(context, "player");

            QuestrMod.NAVIGATION.remove(player);

            return Command.SINGLE_SUCCESS;
        }
        return 0;
    }

    private static int executeSet(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if (context.getSource() != null && context.getSource().getPlayer() != null) {
            ServerPlayer player = EntityArgument.getPlayer(context, "player");

            QuestrMod.NAVIGATION.setVisible(player, true);

            return Command.SINGLE_SUCCESS;
        }
        return 0;
    }
}