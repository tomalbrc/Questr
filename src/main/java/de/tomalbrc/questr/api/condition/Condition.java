package de.tomalbrc.questr.api.condition;

import de.tomalbrc.questr.api.context.TypedMap;

@FunctionalInterface
public interface Condition {
    boolean test(TypedMap ctx);
}
