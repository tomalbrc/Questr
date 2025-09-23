package de.tomalbrc.questr.api.task;

import de.tomalbrc.questr.api.condition.Condition;
import de.tomalbrc.questr.api.context.ContextMap;
import net.minecraft.resources.ResourceLocation;

public final class Task {
    final ResourceLocation id;
    final ResourceLocation type;
    final String description;
    final Condition conditions;
    int target = 1;

    public Task(ResourceLocation id, ResourceLocation type, String description, Condition conditions, int target) {
        this.id = id;
        this.type = type;
        this.description = description;
        this.conditions = conditions;
        this.target = Math.max(target, 1);
    }

    public ResourceLocation getId() {
        return id;
    }

    public ResourceLocation getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public Condition getConditions() {
        return conditions;
    }

    public int getTarget() {
        return target;
    }

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

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", conditions=" + conditions +
                ", target=" + target +
                '}';
    }
}
