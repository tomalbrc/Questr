package de.tomalbrc.questr.impl.navigationbar.component.type;

import de.tomalbrc.questr.QuestrMod;
import de.tomalbrc.questr.impl.navigationbar.component.*;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class NavigationBarComponentTypes {
    private static final Map<ResourceLocation, NavigationBarComponentType<?>> COMPONENTS = new HashMap<>();

    public static NavigationBarComponentType<ClockComponent> CLOCK = register("clock", ClockComponent::new);
    public static NavigationBarComponentType<CustomTextComponent> CUSTOM_TEXT = register("custom_text", CustomTextComponent::new);
    public static NavigationBarComponentType<MsptComponent> MSPT = register("mspt", MsptComponent::new);
    public static NavigationBarComponentType<NavigationComponent> NAVIGATOR = register("navigator", NavigationComponent::new);
    public static NavigationBarComponentType<SpacerComponent> SPACER = register("spacer", SpacerComponent::new);
    public static NavigationBarComponentType<TpsComponent> TPS = register("tps", TpsComponent::new);
    public static NavigationBarComponentType<WorldComponent> WORLD = register("world", WorldComponent::new);

    public static <T extends NavigationBarComponent> NavigationBarComponentType<T> register(String path, NavigationBarComponentType.Creator<T> creator) {
        ResourceLocation id = ResourceLocation.withDefaultNamespace(path);
        if (COMPONENTS.containsKey(id)) {
            QuestrMod.LOGGER.warn("Overwriting existing nav bar component registration for id: {}", id);
        }

        var type = new NavigationBarComponentType<>(creator);
        COMPONENTS.put(id, type);

        return type;
    }

    public static Optional<NavigationBarComponentType<?>> get(ResourceLocation type) {
        return Optional.ofNullable(COMPONENTS.get(type));
    }
}