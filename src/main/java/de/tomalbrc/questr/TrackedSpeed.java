package de.tomalbrc.questr;

import net.minecraft.world.phys.Vec3;

public class TrackedSpeed {
    double delta;
    double oldDelta;
    int speedStep;
    Vec3 pos;

    public TrackedSpeed() {
    }

    public void setPos(Vec3 p) {
        this.pos = p;
    }

    public Vec3 getPos() {
        return pos;
    }

    public int updateDelta(double newDelta) {
        this.oldDelta = this.delta;
        this.delta = newDelta;

        this.speedStep = Math.clamp((int)(newDelta*20), 0, 6);
        this.speedStep = 13 - this.speedStep;

        return speedStep;
    }

    public int getSpeedStep() {
        return this.speedStep;
    }

    public double getDelta() {
        return this.delta;
    }

    public double getOldDelta() {
        return this.oldDelta;
    }
}
