package de.tomalbrc.questr.impl.storage;

import de.tomalbrc.questr.QuestrMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;

public class QuestrComponents implements EntityComponentInitializer {
    public static final ComponentKey<PlayerQuestComponentImpl> QUESTS =
            ComponentRegistry.getOrCreate(
                    ResourceLocation.fromNamespaceAndPath(QuestrMod.MODID, "progress"),
                    PlayerQuestComponentImpl.class
            );

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerFor(ServerPlayer.class, QUESTS, PlayerQuestComponentImpl::new);
    }
}