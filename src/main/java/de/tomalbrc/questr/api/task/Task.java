package de.tomalbrc.questr.api.task;

import de.tomalbrc.questr.api.condition.Condition;
import de.tomalbrc.questr.api.context.ContextMap;
import de.tomalbrc.questr.api.context.Keys;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public record Task(
        Identifier id,
        Identifier type,
        String description,
        Condition conditions,
        Condition failConditions,
        ContextMap arguments,
        int target,
        boolean showProgress
) {
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

    public ContextMap addArgumentContext(ContextMap context) {
        ContextMap args = this.arguments();

        if (args != null && !args.isEmpty()) {
            context = context.copy();

            var trackPos = args.get(Keys.TRACK_POSITION);
            var playerPos = context.get(Keys.PLAYER_POSITION);

            if (trackPos != null && playerPos != null) {
                var rad = context.get(Keys.TRACK_RADIUS);
                if (rad == null) rad = 0;

                context.put(Keys.DISTANCE_TRACKING, Mth.sqrt((float) playerPos.distSqr(trackPos))-rad);
            }
        }

        return context;
    }
}
