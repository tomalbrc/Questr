package de.tomalbrc.questr.impl.util;

import de.tomalbrc.questr.QuestrMod;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.Identifier;

public class ShaderFx {
    public static FontDescription FONT = new FontDescription.Resource(Identifier.fromNamespaceAndPath(QuestrMod.MODID, "fx"));

    public static String VIGNETTE = "2";
}
