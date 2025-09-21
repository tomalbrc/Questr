package de.tomalbrc.questr.api.condition;

import de.tomalbrc.questr.api.context.DataKey;
import de.tomalbrc.questr.api.context.TypedMap;

import java.util.List;
import java.util.Objects;

public final class Conditions {
    public record EqualsCondition(DataKey<?> key, Object expected) implements Condition {
        @Override
        public boolean test(TypedMap ctx) {
            Object v = ctx.get(key);
            if (v == null)
                return true;

            return Objects.equals(expected, v);
        }
    }

    public record NumericComparisonCondition(DataKey<Number> key, NumericOp op, double value) implements Condition {
        public enum NumericOp { GREATER_THAN, GREATER_THAN_OR_EQUAL, LESS_THAN, LESS_THAN_OR_EQUAL, EQUALS, NOT_EQUALS }

        @Override
        public boolean test(TypedMap ctx) {
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
        @Override public boolean test(TypedMap ctx) {
            for (Condition c : children) if (!c.test(ctx)) return false;
            return true;
        }
    }

    public static final class AnyCondition implements Condition {
        private final List<Condition> children;
        public AnyCondition(List<Condition> children) { this.children = children; }
        @Override public boolean test(TypedMap ctx) {
            for (Condition c : children) if (c.test(ctx)) return true;
            return false;
        }
    }

    public static final class NoneCondition implements Condition {
        private final List<Condition> children;
        public NoneCondition(List<Condition> children) { this.children = children; }
        @Override public boolean test(TypedMap ctx) {
            for (Condition c : children) if (c.test(ctx)) return false;
            return true;
        }
    }
}
