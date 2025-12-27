package de.tomalbrc.questr.impl.navigationbar;

import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public record NavigationBarConfig(Identifier type, int width, @Nullable String text) {}
