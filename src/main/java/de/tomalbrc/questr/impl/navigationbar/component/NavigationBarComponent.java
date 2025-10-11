package de.tomalbrc.questr.impl.navigationbar.component;

import de.tomalbrc.questr.impl.navigationbar.NavigationBar;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

@FunctionalInterface
public interface NavigationBarComponent {
    MutableComponent getText(ServerPlayer serverPlayer);

    record Context(
        ServerPlayer player,
        NavigationBar bar,
        int lineNumber
    ) {}
}