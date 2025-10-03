package de.tomalbrc.questr.impl.navigationbar;

import de.tomalbrc.questr.impl.navigationbar.component.*;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class NavBarComponentRegistry {
    private static final Map<ResourceLocation, NavBarComponent> COMPONENTS = new HashMap<>();

    public static void register(String type, NavBarComponent component) {
        ResourceLocation id = ResourceLocation.withDefaultNamespace(type);
        if (COMPONENTS.containsKey(id)) {
            System.out.println("WARN: Overwriting existing nav bar component registration for type: " + type);
        }
        COMPONENTS.put(id, component);
    }

    public static Optional<NavBarComponent> get(ResourceLocation type) {
        return Optional.ofNullable(COMPONENTS.get(type));
    }

    static {
        register("clock", new ClockComponent());
        register("custom_text", new CustomTextComponent());
        register("mspt", new MsptComponent());
        register("navigation", new NavigationComponent());
        register("spacer", new SpacerComponent());
        register("tps", new TpsComponent());
        register("world", new WorldComponent());
    }
}