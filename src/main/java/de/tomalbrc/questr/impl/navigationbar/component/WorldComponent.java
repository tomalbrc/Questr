package de.tomalbrc.questr.impl.navigationbar.component;

import de.tomalbrc.dialogutils.util.ComponentAligner;
import de.tomalbrc.dialogutils.util.TextUtil;
import de.tomalbrc.questr.QuestrMod;
import de.tomalbrc.questr.impl.navigationbar.NavigationBarConfig;
import de.tomalbrc.questr.impl.util.SmallCapsConverter;
import de.tomalbrc.questr.impl.util.Util;
import net.fabricmc.loader.impl.util.StringUtil;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class WorldComponent implements NavigationBarComponent {
    final NavigationBarConfig config;
    final int line;

    ResourceKey<Level> lastDim;
    MutableComponent cached;

    public WorldComponent(NavigationBarConfig config, int line) {
        this.config = config;
        this.line = line;
    }

    @Override
    public MutableComponent getText(ServerPlayer serverPlayer) {
        String worldName = serverPlayer.level().dimension().location().getPath()
                .replace("the_", "")
                .replace("_", " ");
        
        String iconStr = "\uE100"; // Overworld
        if (worldName.toLowerCase().contains("end")) iconStr = "\uE101";
        else if (worldName.toLowerCase().contains("nether")) iconStr = "\uE102";

        if (serverPlayer.level().dimension() == lastDim && cached != null) {
            return cached;
        }

        lastDim = serverPlayer.level().dimension();

        var iconComponent = ComponentUtils.icon(iconStr, line);
        int width = config.width();
        var text = ComponentAligner.align(
                Util.whiteWithFont(SmallCapsConverter.toSmallCaps(StringUtil.capitalize(worldName)), line == 1 ? QuestrMod.NAV_FONT:QuestrMod.NAV_FONT2),
                TextUtil.Alignment.CENTER,
                width - ComponentAligner.getWidth(iconComponent) - 4
        );

        cached = ComponentUtils.background(width, line)
                .append(ComponentAligner.spacer(2))
                .append(iconComponent)
                .append(ComponentAligner.spacer(2))
                .append(text);

        return cached;
    }
}