package de.tomalbrc.questr.impl.navigationbar;

import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class NavigationBarLayout {
    private final List<NavigationBarConfig> line1 = new ArrayList<>();
    private final List<NavigationBarConfig> line2 = new ArrayList<>();

    public List<NavigationBarConfig> getLine1() {
        return line1;
    }

    public List<NavigationBarConfig> getLine2() {
        return line2;
    }

    public static NavigationBarLayout createDefaultLayout() {
        NavigationBarLayout layout = new NavigationBarLayout();

        layout.getLine1().add(new NavigationBarConfig(ResourceLocation.withDefaultNamespace("custom_text"), 80, "Server-Name"));
        layout.getLine1().add(new NavigationBarConfig(ResourceLocation.withDefaultNamespace("spacer"), 1, null));
        layout.getLine1().add(new NavigationBarConfig(ResourceLocation.withDefaultNamespace("navigation"), 110, null));
        layout.getLine1().add(new NavigationBarConfig(ResourceLocation.withDefaultNamespace("spacer"), 1, null));
        layout.getLine1().add(new NavigationBarConfig(ResourceLocation.withDefaultNamespace("world"), 80, null));
        layout.getLine1().add(new NavigationBarConfig(ResourceLocation.withDefaultNamespace("spacer"), 1, null));
        layout.getLine1().add(new NavigationBarConfig(ResourceLocation.withDefaultNamespace("clock"), 60, null));
        layout.getLine1().add(new NavigationBarConfig(ResourceLocation.withDefaultNamespace("spacer"), 1, null));
        layout.getLine1().add(new NavigationBarConfig(ResourceLocation.withDefaultNamespace("tps"), 60, null));

        int w = 0;
        for (NavigationBarConfig config : layout.getLine1()) {
            w += config.width();
        }

        layout.getLine2().add(new NavigationBarConfig(ResourceLocation.withDefaultNamespace("navigation"), 110, "Hello, world!"));
        layout.getLine2().add(new NavigationBarConfig(ResourceLocation.withDefaultNamespace("spacer"), w-110-60, null));
        layout.getLine2().add(new NavigationBarConfig(ResourceLocation.withDefaultNamespace("mspt"), 60, null));

        return layout;
    }
}