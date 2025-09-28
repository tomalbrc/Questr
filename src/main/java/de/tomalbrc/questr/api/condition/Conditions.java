package de.tomalbrc.questr.api.condition;

import de.tomalbrc.questr.api.context.ContextMap;
import de.tomalbrc.questr.api.context.DataKey;
import de.tomalbrc.questr.impl.util.SetLike;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public final class Conditions {
    public record EqualsCondition(DataKey<?> key, Object expected) implements Condition {
        @Override
        public boolean test(ContextMap ctx) {
            Object v = ctx.get(key);
            if (v == null)
                return true;

            return Objects.equals(expected, v);
        }
    }

    public record NumericComparisonCondition(DataKey<Number> key, NumericOp op, double value) implements Condition {
        public enum NumericOp {
            GREATER_THAN,
            GREATER_THAN_OR_EQUAL,
            LESS_THAN,
            LESS_THAN_OR_EQUAL,
            EQUALS,
            NOT_EQUALS;

            public static NumericOp fromString(String norm) {
                return switch (norm) {
                    case "greater_than", "gt", ">" -> Conditions.NumericComparisonCondition.NumericOp.GREATER_THAN;
                    case "greater_than_or_equal", "gte", ">=" -> Conditions.NumericComparisonCondition.NumericOp.GREATER_THAN_OR_EQUAL;
                    case "less_than", "lt", "<" -> Conditions.NumericComparisonCondition.NumericOp.LESS_THAN;
                    case "less_than_or_equal", "lte", "<=" -> Conditions.NumericComparisonCondition.NumericOp.LESS_THAN_OR_EQUAL;
                    case "equals", "eq", "=" -> Conditions.NumericComparisonCondition.NumericOp.EQUALS;
                    case "not_equals", "neq", "!=" -> Conditions.NumericComparisonCondition.NumericOp.NOT_EQUALS;
                    default -> throw new IllegalArgumentException("Unknown operation: " + norm);
                };
            }
        }

        @Override
        public boolean test(ContextMap ctx) {
            Number n = ctx.get(key);
            if (n == null) return true;
            double d = n.doubleValue();
            return switch (op) {
                case GREATER_THAN -> d > value;
                case GREATER_THAN_OR_EQUAL -> d >= value;
                case LESS_THAN -> d < value;
                case LESS_THAN_OR_EQUAL -> d <= value;
                case EQUALS -> d == value;
                case NOT_EQUALS -> d != value;
            };
        }
    }

    public static final class AllCondition implements Condition {
        private final List<Condition> children;
        public AllCondition(List<Condition> children) { this.children = children; }
        @Override public boolean test(ContextMap ctx) {
            for (Condition c : children) if (!c.test(ctx)) return false;
            return true;
        }
    }

    public static final class AnyCondition implements Condition {
        private final List<Condition> children;
        public AnyCondition(List<Condition> children) { this.children = children; }
        @Override public boolean test(ContextMap ctx) {
            for (Condition c : children) if (c.test(ctx)) return true;
            return false;
        }
    }

    public static final class NoneCondition implements Condition {
        private final List<Condition> children;
        public NoneCondition(List<Condition> children) { this.children = children; }
        @Override public boolean test(ContextMap ctx) {
            for (Condition c : children) if (c.test(ctx)) return false;
            return true;
        }
    }

    public record ContainsCondition(DataKey<? extends SetLike<?>> key, SetLike<?> expected) implements Condition {
        @Override
        public boolean test(ContextMap ctx) {
            SetLike<?> runtime = ctx.get(key);
            if (runtime == null) return true;

            if (expected == null) return true;

            for (Object elem : expected) {
                if (elem == null) continue;
                @SuppressWarnings("unchecked")
                SetLike<Object> rs = (SetLike<Object>) runtime;
                if (rs.contains(elem)) return true;
            }

            return false;
        }
    }

    public record StringComparisonCondition(DataKey<String> key, StringOp op, String value) implements Condition {
        public enum StringOp {
            STARTS_WITH,
            ENDS_WITH,
            CONTAINS,
            EQUALS,
            NOT_EQUALS,
            MATCHES; // regex match

            public static StringOp fromString(String norm) {
                return switch (norm) {
                    case "starts_with", "startswith", "sw", "^" -> STARTS_WITH;
                    case "ends_with", "endswith", "ew", "$" -> ENDS_WITH;
                    case "contains", "in", "has", "like" -> CONTAINS;
                    case "equals", "eq", "=", "==" -> EQUALS;
                    case "not_equals", "neq", "!=" -> NOT_EQUALS;
                    case "matches", "match", "regex", "rx" -> MATCHES;
                    default -> throw new IllegalArgumentException("Unknown string operation: " + norm);
                };
            }
        }

        @Override
        public boolean test(ContextMap ctx) {
            String runtime = ctx.get(key);
            if (runtime == null) return true;
            if (value == null) return true;

            return switch (op) {
                case STARTS_WITH -> runtime.startsWith(value);
                case ENDS_WITH -> runtime.endsWith(value);
                case CONTAINS -> runtime.contains(value);
                case EQUALS -> Objects.equals(runtime, value);
                case NOT_EQUALS -> !Objects.equals(runtime, value);
                case MATCHES -> Pattern.matches(value, runtime);
            };
        }
    }
}
