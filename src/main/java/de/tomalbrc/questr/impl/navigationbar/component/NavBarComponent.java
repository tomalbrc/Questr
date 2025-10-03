package de.tomalbrc.questr.impl.navigationbar.component;

import de.tomalbrc.questr.impl.navigationbar.NavigationBar;
import de.tomalbrc.questr.impl.navigationbar.NavigationBarConfig;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

@FunctionalInterface
public interface NavBarComponent {
    MutableComponent build(Context context);

    record Context(
        ServerPlayer player,
        NavigationBarConfig config,
        NavigationBar bar,
        int lineNumber
    ) {}
}