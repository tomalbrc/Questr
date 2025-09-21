package de.tomalbrc.questr.api.context;

import net.minecraft.resources.ResourceLocation;

public class DataKey<T> {
    private final ResourceLocation id;
    private final Class<T> type;

    private DataKey(ResourceLocation id, Class<T> type) {
        this.id = id;
        this.type = type;
    }

    public static <T> DataKey<T> of(String string, Class<T> type) {
        var key = new DataKey<>(ResourceLocation.withDefaultNamespace(string), type);
        Keys.BY_ID.put(string, key);
        return key;
    }

    public ResourceLocation id() {
        return id;
    }

    public Class<T> getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Key<" + id + " : " + type.getSimpleName() + ">";
    }
}
