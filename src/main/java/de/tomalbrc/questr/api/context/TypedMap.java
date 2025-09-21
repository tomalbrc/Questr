package de.tomalbrc.questr.api.context;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class TypedMap {
    protected final Map<DataKey<?>, Object> values = new Reference2ObjectOpenHashMap<>();

    public static TypedMap of(ServerPlayer player) {
        TypedMap typedMap = new TypedMap();
        typedMap.put(Keys.IS_RAINING, player.level().isRaining());
        typedMap.put(Keys.IS_THUNDERING, player.level().isThundering());
        typedMap.put(Keys.DIMENSION, player.level().dimension().location());
        typedMap.put(Keys.PLAYER_POSITION, player.getOnPos());
        typedMap.put(Keys.ITEM_STACK, player.getWeaponItem());
        typedMap.put(Keys.ITEM, player.getWeaponItem().getItem());
        return typedMap;
    }

    public <T> void put(DataKey<T> key, T value) {
        values.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> @Nullable T get(DataKey<T> key) {
        return (T) values.get(key);
    }

    public boolean has(DataKey<?> key) {
        return values.containsKey(key);
    }

    public boolean keysAndValuesMatch(TypedMap other) {
        for (Map.Entry<DataKey<?>, Object> entry : this.values.entrySet()) {
            DataKey<?> key = entry.getKey();
            Object value = entry.getValue();

            if (!other.has(key)) {
                return false; // key missing in other
            }

            Object otherValue = other.get(key);
            if (value == null) {
                if (otherValue != null) {
                    return false; // null mismatch
                }
            } else {
                if (!value.equals(otherValue)) {
                    return false; // value mismatch
                }
            }
        }
        return true;
    }

}
