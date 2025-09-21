package de.tomalbrc.questr.impl.util;

import com.google.gson.*;
import de.tomalbrc.questr.api.condition.Condition;
import de.tomalbrc.questr.api.condition.Conditions;
import de.tomalbrc.questr.api.context.DataKey;
import de.tomalbrc.questr.api.context.Keys;

import java.lang.reflect.Type;
import java.util.*;

public final class ConditionDeserializer implements JsonDeserializer<Condition> {
    @Override
    public Condition deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx)
            throws JsonParseException {

        if (json.isJsonObject()) {
            JsonObject obj = json.getAsJsonObject();

            // combinators
            if (obj.has("all")) {
                return new Conditions.AllCondition(deserializeList(obj.getAsJsonArray("all"), ctx));
            }
            if (obj.has("any")) {
                return new Conditions.AnyCondition(deserializeList(obj.getAsJsonArray("any"), ctx));
            }
            if (obj.has("none")) {
                return new Conditions.NoneCondition(deserializeList(obj.getAsJsonArray("none"), ctx));
            }

            List<Condition> ents = new ArrayList<>();
            for (var entry : obj.entrySet()) {
                String keyName = entry.getKey();
                JsonElement val = entry.getValue();

                if (keyName.equalsIgnoreCase("all")) {
                    ents.add(new Conditions.AllCondition(deserializeList(obj.getAsJsonArray("all"), ctx)));
                    continue;
                }
                if (keyName.equalsIgnoreCase("any")) {
                    ents.add(new Conditions.AnyCondition(deserializeList(obj.getAsJsonArray("any"), ctx)));
                    continue;
                }
                if (keyName.equalsIgnoreCase("none")) {
                    ents.add(new Conditions.NoneCondition(deserializeList(obj.getAsJsonArray("none"), ctx)));
                    continue;
                }

                if (val.isJsonObject()) {
                    JsonObject candidate = val.getAsJsonObject();

                    // value with operator { "value": 5, "operation": "gt" }
                    if (candidate.has("value") && candidate.has("operation")) {
                        DataKey<?> key = Keys.BY_ID.get(keyName);
                        var valueObj = ctx.deserialize(candidate.get("value"), key.getType());
                        String op = candidate.get("operation").getAsString();
                        ents.add(buildForOperator(key, op, valueObj));
                        continue;
                    }

                    // nested
                    ents.add(deserialize(val, Condition.class, ctx));
                } else if (val.isJsonArray()) {
                    ents.add(deserialize(val, Condition.class, ctx));
                } else {
                    // obj equality: { "entity": "minecraft:zombie" }
                    DataKey<?> key = Keys.BY_ID.get(keyName);
                    Object primitive = ctx.deserialize(val, key.getType());
                    ents.add(new Conditions.EqualsCondition(key, primitive));
                }
            }

            return new Conditions.AllCondition(ents);
        }

        if (json.isJsonArray()) {
            return new Conditions.AllCondition(deserializeList(json.getAsJsonArray(), ctx));
        }

        throw new JsonParseException("Unsupported condition structure: " + json);
    }

    private static List<Condition> deserializeList(JsonArray arr, JsonDeserializationContext ctx) {
        return arr.asList().stream()
                .map(e -> (Condition)ctx.deserialize(e, Condition.class))
                .toList();
    }

    private static Condition buildForOperator(DataKey<?> key, String op, Object valueObj) {
        String norm = op.trim().toLowerCase(Locale.ROOT);

        if (Number.class.isAssignableFrom(key.getType())) {
            double val = ((Number) valueObj).doubleValue();
            Conditions.NumericComparisonCondition.NumericOp nop = switch (norm) {
                case "greater_than", "gt", ">" -> Conditions.NumericComparisonCondition.NumericOp.GREATER_THAN;
                case "greater_than_or_equal", "gte", ">=" -> Conditions.NumericComparisonCondition.NumericOp.GREATER_THAN_OR_EQUAL;
                case "less_than", "lt", "<" -> Conditions.NumericComparisonCondition.NumericOp.LESS_THAN;
                case "less_than_or_equal", "lte", "<=" -> Conditions.NumericComparisonCondition.NumericOp.LESS_THAN_OR_EQUAL;
                case "equals", "eq", "=" -> Conditions.NumericComparisonCondition.NumericOp.EQUALS;
                case "not_equals", "neq", "!=" -> Conditions.NumericComparisonCondition.NumericOp.NOT_EQUALS;
                default -> throw new IllegalArgumentException("Unknown operation: " + op);
            };
            @SuppressWarnings("unchecked")
            DataKey<Number> numKey = (DataKey<Number>) key;
            return new Conditions.NumericComparisonCondition(numKey, nop, val);
        } else {
            return new Conditions.EqualsCondition(key, valueObj);
        }
    }
}
