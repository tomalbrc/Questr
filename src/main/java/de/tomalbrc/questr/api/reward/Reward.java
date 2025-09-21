package de.tomalbrc.questr.api.reward;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface Reward {
    void apply(ServerPlayer player);

    record XpReward(int amount) implements Reward {
        @Override
        public void apply(ServerPlayer player) {
            player.giveExperiencePoints(amount);
        }
    }

    record ItemReward(ResourceLocation item, int count) implements Reward {
        @Override
        public void apply(ServerPlayer player) {
            var itemInstance = BuiltInRegistries.ITEM.get(item);
            if (itemInstance.isPresent()) {
                player.addItem(new ItemStack(itemInstance.orElseThrow(), count));
            }
        }
    }

    record CommandReward(List<String> commands) implements Reward {
        @Override
        public void apply(ServerPlayer player) {
            var css = player.createCommandSourceStack().withPosition(player.position()).withRotation(player.getRotationVector()).withLevel(player.level()).withSuppressedOutput();
            for (String command : commands) {
                player.getServer().getCommands().performPrefixedCommand(css, command.replace("%player%", player.getScoreboardName()));
            }
        }
    }
}