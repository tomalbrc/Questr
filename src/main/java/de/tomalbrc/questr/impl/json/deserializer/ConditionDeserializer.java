package de.tomalbrc.questr.impl.json.deserializer;

import com.google.gson.*;
import de.tomalbrc.questr.api.condition.Condition;
import de.tomalbrc.questr.api.condition.Conditions;
import de.tomalbrc.questr.api.context.DataKey;
import de.tomalbrc.questr.api.context.Keys;
import de.tomalbrc.questr.impl.util.SetLike;

import java.lang.reflect.Type;
import java.util.*;
import java.util.function.BiFunction;

public final class ConditionDeserializer implements JsonDeserializer<Condition> {
    private static final Map<String, BiFunction<JsonElement, JsonDeserializationContext, Condition>> SPECIALS =
            new HashMap<>();

    static {
        SPECIALS.put("all", (json, ctx) ->
                new Conditions.AllCondition(deserializeList(json.getAsJsonArray(), ctx)));
        SPECIALS.put("any", (json, ctx) ->
                new Conditions.AnyCondition(deserializeList(json.getAsJsonArray(), ctx)));
        SPECIALS.put("none", (json, ctx) ->
                new Conditions.NoneCondition(deserializeList(json.getAsJsonArray(), ctx)));
    }

    @Override
    public Condition deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx)
            throws JsonParseException {

        if (json.isJsonArray()) {
            return new Conditions.AllCondition(deserializeList(json.getAsJsonArray(), ctx));
        }

        if (!json.isJsonObject()) {
            throw new JsonParseException("Unsupported condition structure: " + json);
        }

        JsonObject obj = json.getAsJsonObject();
        List<Condition> ents = new ArrayList<>();

        for (var entry : obj.entrySet()) {
            String key = entry.getKey();
            JsonElement val = entry.getValue();

            BiFunction<JsonElement, JsonDeserializationContext, Condition> handler = SPECIALS.get(key.toLowerCase(Locale.ROOT));
            if (handler != null) {
                ents.add(handler.apply(val, ctx));
                continue;
            }

            // DataKey lookup
            DataKey<?> dataKey = Keys.BY_ID.get(key);
            if (dataKey == null) {
                continue;
            }

            if (val.isJsonObject()) {
                JsonObject candidate = val.getAsJsonObject();
                if (candidate.has("value") && candidate.has("operation")) {
                    Object valueObj = ctx.deserialize(candidate.get("value"), dataKey.getType());
                    String op = candidate.get("operation").getAsString();
                    ents.add(buildForOperator(dataKey, op, valueObj));
                    continue;
                }
                ents.add(deserialize(candidate, Condition.class, ctx));
            }
            else if (SetLike.class.isAssignableFrom(dataKey.getType())) {
                Object setLike = ctx.deserialize(val, dataKey.getType());
                @SuppressWarnings("unchecked")
                DataKey<? extends SetLike<?>> dk = (DataKey<? extends SetLike<?>>) dataKey;
                ents.add(new Conditions.ContainsCondition(dk, (SetLike<?>) setLike));
            }
            else {
                Object primitive = ctx.deserialize(val, dataKey.getType());
                ents.add(new Conditions.EqualsCondition(dataKey, primitive));
            }
        }

        return new Conditions.AllCondition(ents);
    }

    private static List<Condition> deserializeList(JsonArray arr, JsonDeserializationContext ctx) {
        List<Condition> out = new ArrayList<>(arr.size());
        for (JsonElement e : arr) {
            out.add(ctx.deserialize(e, Condition.class));
        }
        return out;
    }

    private static Condition buildForOperator(DataKey<?> key, String op, Object valueObj) {
        String norm = op.trim().toLowerCase(Locale.ROOT);
        if (Number.class.isAssignableFrom(key.getType())) {
            double val = ((Number) valueObj).doubleValue();
            var nop = Conditions.NumericComparisonCondition.NumericOp.fromString(norm);
            @SuppressWarnings("unchecked")
            DataKey<Number> numKey = (DataKey<Number>) key;
            return new Conditions.NumericComparisonCondition(numKey, nop, val);
        }

        if (String.class.isAssignableFrom(key.getType())) {
            String val = (String) valueObj;
            var sop = Conditions.StringComparisonCondition.StringOp.fromString(norm);
            @SuppressWarnings("unchecked")
            DataKey<String> strKey = (DataKey<String>) key;
            return new Conditions.StringComparisonCondition(strKey, sop, val);
        }

        return new Conditions.EqualsCondition(key, valueObj);
    }
}
