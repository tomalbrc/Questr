package de.tomalbrc.questr.api.context;

import de.tomalbrc.questr.impl.util.ResourceSet;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.stream.Collectors;

public class ContextMap {
    protected final Map<DataKey<?>, Object> values = new Reference2ObjectOpenHashMap<>();

    public ContextMap copy() {
        ContextMap map = new ContextMap();
        map.values.putAll(values);
        return map;
    }

    public static ContextMap of(ServerPlayer player) {
        ContextMap contextMap = new ContextMap();
        contextMap.put(Keys.PLAYER, player.getScoreboardName());
        contextMap.put(Keys.IS_RAINING, player.level().isRaining());
        contextMap.put(Keys.IS_THUNDERING, player.level().isThundering());
        contextMap.put(Keys.DIMENSION, player.level().dimension().location());
        contextMap.put(Keys.PLAYER_POSITION, player.getOnPos());
        contextMap.put(Keys.ITEM_STACK, player.getWeaponItem());
        contextMap.put(Keys.ITEM, player.getWeaponItem().getItem());
        contextMap.put(Keys.ITEM_TAG, ResourceSet.of(player.getWeaponItem().getItem().builtInRegistryHolder().tags().map(TagKey::location).collect(Collectors.toSet())));
        return contextMap;
    }

    public <T> void put(DataKey<T> key, T value) {
        values.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> @Nullable T get(DataKey<T> key) {
        return (T) values.get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> @Nullable T remove(DataKey<T> key) {
        return (T) values.remove(key);
    }

    public boolean has(DataKey<?> key) {
        return values.containsKey(key);
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }
}
