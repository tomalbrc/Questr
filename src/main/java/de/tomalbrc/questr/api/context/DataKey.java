package de.tomalbrc.questr.api.context;

import net.minecraft.resources.Identifier;

public class DataKey<T> {
    private final Identifier id;
    private final Class<T> type;

    private DataKey(Identifier id, Class<T> type) {
        this.id = id;
        this.type = type;
    }

    public static <T> DataKey<T> of(String string, Class<T> type) {
        var key = new DataKey<>(Identifier.withDefaultNamespace(string), type);
        Keys.BY_ID.put(string, key);
        return key;
    }

    public Identifier id() {
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
