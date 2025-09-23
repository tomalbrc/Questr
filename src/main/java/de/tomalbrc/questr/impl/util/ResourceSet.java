package de.tomalbrc.questr.impl.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

public final class ResourceSet implements SetLike<ResourceLocation> {
    private final Set<ResourceLocation> ids;

    public ResourceSet(Set<ResourceLocation> ids) {
        this.ids = Set.copyOf(Objects.requireNonNull(ids));
    }

    public static ResourceSet of(ResourceLocation id) {
        return new ResourceSet(Set.of(Objects.requireNonNull(id)));
    }

    public static ResourceSet of(Set<ResourceLocation> many) {
        return new ResourceSet(many);
    }

    @Override
    public boolean contains(ResourceLocation element) {
        return element != null && ids.contains(element);
    }

    @Override
    public @NotNull Iterator<ResourceLocation> iterator() {
        return ids.iterator();
    }

    @Override
    public String toString() {
        return "ResourceSet" + ids;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ids);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResourceSet rs)) return false;
        return Objects.equals(this.ids, rs.ids);
    }

    public static final class Deserializer implements JsonDeserializer<ResourceSet> {
        @Override
        public ResourceSet deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx) throws JsonParseException {
            if (json == null || json.isJsonNull()) {
                throw new JsonParseException("Id can not be empty");
            }

            if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
                String s = json.getAsString();
                if (s.isBlank()) throw new JsonParseException("Id can not be empty");
                return ResourceSet.of(ResourceLocation.parse(s));
            }

            throw new JsonParseException("Invalid id");
        }
    }
}