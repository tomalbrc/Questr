package de.tomalbrc.questr.impl.navigationbar;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public record NavigationBarConfig(ResourceLocation type, int width, @Nullable String text) {}
