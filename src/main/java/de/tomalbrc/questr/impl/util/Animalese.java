package de.tomalbrc.questr.impl.util;

import de.tomalbrc.questr.QuestrMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

import java.util.HashMap;
import java.util.Map;

public class Animalese {
    private static final Map<Character, ResourceLocation> letterSounds = new HashMap<>();

    static {
        loadLetterSounds();
    }

    public static void loadLetterSounds() {
        letterSounds.clear();
        for (char c = 'a'; c <= 'z'; c++) {
            ResourceLocation rl = ResourceLocation.fromNamespaceAndPath(QuestrMod.MODID, String.valueOf(Character.toLowerCase(c)));
            letterSounds.put(c, rl);
        }

        letterSounds.put('?', ResourceLocation.fromNamespaceAndPath(QuestrMod.MODID, "deska"));
        letterSounds.put('!', ResourceLocation.fromNamespaceAndPath(QuestrMod.MODID, "gwah"));
    }

    public static void playLetter(ServerPlayer player, char c, float volume, float pitch, String gender, String voiceType) {
        if (!letterSounds.containsKey(c)) {
            return;
        }

        ResourceLocation rl = letterSounds.get(c).withPrefix(voiceType + ".").withPrefix(gender + ".");
        SoundEvent sound = SoundEvent.createVariableRangeEvent(rl);

        player.level().playSound(
                null,
                player.blockPosition(),
                sound,
                SoundSource.VOICE,
                volume,
                pitch
        );
    }
}
