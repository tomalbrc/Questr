package de.tomalbrc.questr.api.requirement;

import de.tomalbrc.questr.api.condition.Condition;
import net.minecraft.resources.ResourceLocation;

public final class Requirement {
    final ResourceLocation id;
    final ResourceLocation type;
    final Condition conditions;
    final int target;

    public Requirement(ResourceLocation id, ResourceLocation type, Condition conditions, int target) {
        this.id = id;
        this.type = type;
        this.conditions = conditions;
        this.target = target;
    }

    public ResourceLocation getId() {
        return id;
    }

    public ResourceLocation getType() {
        return type;
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

        Requirement that = (Requirement) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Requirement{" +
                "id=" + id +
                ", conditions=" + conditions +
                ", target=" + target +
                '}';
    }
}
