package de.tomalbrc.questr.impl.navigationbar.component;

import de.tomalbrc.dialogutils.util.ComponentAligner;
import net.minecraft.network.chat.MutableComponent;

public class SpacerComponent implements NavBarComponent {
    @Override
    public MutableComponent build(Context context) {
        return (MutableComponent) ComponentAligner.spacer(context.config().width());
    }
}