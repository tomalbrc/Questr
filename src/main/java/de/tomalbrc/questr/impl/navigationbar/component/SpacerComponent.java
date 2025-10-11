package de.tomalbrc.questr.impl.navigationbar.component;

import de.tomalbrc.dialogutils.util.ComponentAligner;
import de.tomalbrc.questr.impl.navigationbar.NavigationBarConfig;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

public class SpacerComponent implements NavigationBarComponent {
    final NavigationBarConfig config;
    final MutableComponent text;

    public SpacerComponent(NavigationBarConfig config, int line) {
        this.config = config;
        this.text = (MutableComponent) ComponentAligner.spacer(config.width());
    }

    @Override
    public MutableComponent getText(ServerPlayer serverPlayer) {
        return text;
    }
}