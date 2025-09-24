package de.tomalbrc.questr.api.task;

import de.tomalbrc.questr.api.condition.Condition;
import de.tomalbrc.questr.api.context.ContextMap;
import net.minecraft.resources.ResourceLocation;

public record Task(ResourceLocation id, ResourceLocation type, String description, Condition conditions, ContextMap arguments, int target, boolean showProgress) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Task that = (Task) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
