package de.tomalbrc.questr.api.condition;

import de.tomalbrc.questr.api.context.ContextMap;
import de.tomalbrc.questr.api.context.DataKey;
import de.tomalbrc.questr.api.context.Keys;
import de.tomalbrc.questr.impl.util.SetLike;
import net.minecraft.core.BlockPos;

import java.util.List;
import java.util.Objects;

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

            NumericOp fromString(String norm) {
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

    public record ProximityCondition(BlockPos position, double distance) implements Condition {
        @Override
        public boolean test(ContextMap ctx) {
            BlockPos playerPosition = ctx.get(Keys.PLAYER_POSITION);
            if (playerPosition == null) {
                return true;
            }

            return playerPosition.distSqr(position) <= distance * distance;
        }
    }
}
