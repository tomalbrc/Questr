package de.tomalbrc.questr.impl.navigationbar.component;

import de.tomalbrc.dialogutils.util.ComponentAligner;
import de.tomalbrc.dialogutils.util.TextUtil;
import de.tomalbrc.questr.QuestrMod;
import de.tomalbrc.questr.impl.util.SmallCapsConverter;
import de.tomalbrc.questr.impl.util.Util;
import net.fabricmc.loader.impl.util.StringUtil;
import net.minecraft.network.chat.MutableComponent;

public class WorldComponent implements NavBarComponent {
    @Override
    public MutableComponent build(Context context) {
        String worldName = context.player().level().dimension().location().getPath()
                .replace("the_", "")
                .replace("_", " ");
        
        String iconStr = "\uE100"; // Overworld
        if (worldName.toLowerCase().contains("end")) iconStr = "\uE101";
        else if (worldName.toLowerCase().contains("nether")) iconStr = "\uE102";

        var iconComponent = ComponentUtils.icon(iconStr, context.lineNumber());
        int width = context.config().width();
        var text = ComponentAligner.align(
                Util.whiteWithFont(SmallCapsConverter.toSmallCaps(StringUtil.capitalize(worldName)), context.lineNumber() == 1 ? QuestrMod.NAV_FONT:QuestrMod.NAV_FONT2),
                TextUtil.Alignment.CENTER,
                width - ComponentAligner.getWidth(iconComponent) - 4
        );

        return ComponentUtils.background(width, context.lineNumber())
                .append(ComponentAligner.spacer(2))
                .append(iconComponent)
                .append(ComponentAligner.spacer(2))
                .append(text);
    }
}