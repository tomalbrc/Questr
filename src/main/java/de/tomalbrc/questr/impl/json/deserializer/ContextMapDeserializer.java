package de.tomalbrc.questr.impl.json.deserializer;

import com.google.gson.*;
import de.tomalbrc.questr.api.context.ContextMap;
import de.tomalbrc.questr.api.context.DataKey;
import de.tomalbrc.questr.api.context.Keys;

import java.lang.reflect.Type;

public final class ContextMapDeserializer implements JsonDeserializer<ContextMap> {
    @Override
    public ContextMap deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx)
            throws JsonParseException {
        if (!json.isJsonObject()) {
            throw new JsonParseException("ContextMap must be a JSON object: " + json);
        }

        JsonObject obj = json.getAsJsonObject();
        ContextMap map = new ContextMap();

        for (var entry : obj.entrySet()) {
            String keyName = entry.getKey();
            DataKey<?> key = Keys.BY_ID.get(keyName);

            if (key == null) {
                continue;
            }

            JsonElement val = entry.getValue();
            Object deserialized = ctx.deserialize(val, key.getType());
            if (deserialized != null) {
                putUnchecked(map, key, deserialized);
            }
        }

        return map;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void putUnchecked(ContextMap map, DataKey key, Object value) {
        map.put(key, value);
    }
}
