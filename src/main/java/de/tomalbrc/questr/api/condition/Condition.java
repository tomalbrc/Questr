package de.tomalbrc.questr.api.condition;

import de.tomalbrc.questr.api.context.ContextMap;

@FunctionalInterface
public interface Condition {
    boolean test(ContextMap ctx);
}
