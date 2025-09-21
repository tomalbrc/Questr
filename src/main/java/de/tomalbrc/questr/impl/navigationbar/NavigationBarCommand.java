package de.tomalbrc.questr.impl.navigationbar;

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

        LiteralCommandNode<CommandSourceStack> addNode = Commands
                .literal("add")
                .build();

        LiteralCommandNode<CommandSourceStack> removeNode = Commands
                .literal("remove")
                .build();

        LiteralCommandNode<CommandSourceStack> listNode = Commands
                .literal("list")
                .build();

        LiteralCommandNode<CommandSourceStack> setNode = Commands
                .literal("set")
                .build();

        var nodePlayerAdd = Commands.argument("player", EntityArgument.player());
        var nodePlayerRemove = Commands.argument("player", EntityArgument.player());
        var nodePlayerList = Commands.argument("player", EntityArgument.player());
        var nodePlayerSet = Commands.argument("player", EntityArgument.player());

        var removeIndex = Commands.argument("index", IntegerArgumentType.integer(0,1000));
        var activateIndex = Commands.argument("index", IntegerArgumentType.integer(0,1000));

        var locationArg = Commands.argument("location", BlockPosArgument.blockPos());
        var msg = Commands.argument("message", StringArgumentType.string());

        addNode.addChild(nodePlayerAdd.then(locationArg.executes(NavigationBarCommand::executeAddDistance).then(msg.executes(NavigationBarCommand::executeAddMessage))).build());
        removeNode.addChild(nodePlayerRemove.executes(NavigationBarCommand::executeRemove).then(removeIndex.executes(NavigationBarCommand::executeRemoveIndexed)).build());
        listNode.addChild(nodePlayerList.executes(NavigationBarCommand::executeList).build());
        setNode.addChild(nodePlayerSet.then(activateIndex.executes(NavigationBarCommand::executeSet)).build());

        hudNode.addChild(addNode);
        hudNode.addChild(removeNode);
        hudNode.addChild(listNode);
        hudNode.addChild(setNode);

        dispatcher.getRoot().addChild(hudNode);
    }

    private static int executeAddMessage(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if (context.getSource() != null && context.getSource().getPlayer() != null) {
            ServerPlayer player = EntityArgument.getPlayer(context, "player");
            String message = StringArgumentType.getString(context, "message");

            QuestrMod.NAVIGATION.add(context.getSource().getServer(), player.getUUID(), message, BlockPosArgument.getBlockPos(context, "location"));

            return Command.SINGLE_SUCCESS;
        }
        return 0;
    }

    private static int executeAddDistance(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if (context.getSource() != null && context.getSource().getPlayer() != null) {
            ServerPlayer player = EntityArgument.getPlayer(context, "player");
            QuestrMod.NAVIGATION.add(context.getSource().getServer(), player.getUUID(), null, BlockPosArgument.getBlockPos(context, "location"));

            return Command.SINGLE_SUCCESS;
        }
        return 0;
    }

    private static int executeRemove(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if (context.getSource() != null && context.getSource().getPlayer() != null) {
            ServerPlayer player = EntityArgument.getPlayer(context, "player");

            QuestrMod.NAVIGATION.removeOldestNavigationBar(player.getUUID());

            return Command.SINGLE_SUCCESS;
        }
        return 0;
    }

    private static int executeRemoveIndexed(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if (context.getSource() != null && context.getSource().getPlayer() != null) {
            ServerPlayer player = EntityArgument.getPlayer(context, "player");
            int index = IntegerArgumentType.getInteger(context, "index");

            QuestrMod.NAVIGATION.remove(player.getUUID(), index);

            return Command.SINGLE_SUCCESS;
        }
        return 0;
    }

    private static int executeSet(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if (context.getSource() != null && context.getSource().getPlayer() != null) {
            ServerPlayer player = EntityArgument.getPlayer(context, "player");
            int index = IntegerArgumentType.getInteger(context, "index");

            QuestrMod.NAVIGATION.setVisible(player.getUUID(), index);

            return Command.SINGLE_SUCCESS;
        }
        return 0;
    }

    private static int executeList(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if (context.getSource() != null && context.getSource().getPlayer() != null) {
            ServerPlayer player = EntityArgument.getPlayer(context, "player");

            context.getSource().sendSuccess(() -> QuestrMod.NAVIGATION.list(player.getUUID()), true);

            return Command.SINGLE_SUCCESS;
        }
        return 0;
    }
}