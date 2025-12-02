package de.tomalbrc.questr.mixin;

import de.tomalbrc.questr.QuestrMod;
import de.tomalbrc.questr.TrackedSpeed;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {
    @Unique private final TrackedSpeed qr$speed = new TrackedSpeed();

    @Shadow public ServerPlayer player;

    @Inject(method = "tick", at = @At("HEAD"))
    private void qr$onTick(CallbackInfo ci) {
        if (player.tickCount % 2 == 0)
            return;

        Vec3 newPos = player.position();

        if (qr$speed.getPos() != null) {
            double distSqr = !player.onGround() && player.getLastClientInput().jump() ? qr$speed.getPos().subtract(newPos).horizontalDistanceSqr() : qr$speed.getPos().distanceToSqr(newPos);

            double alpha = 0.25;
            var oldStep = qr$speed.getSpeedStep();
            var newStep = qr$speed.updateDelta(distSqr + alpha * (distSqr - qr$speed.getDelta()));
            if (oldStep != newStep)
                QuestrMod.runSpeedEffect(player, oldStep, newStep);
        }

        qr$speed.setPos(newPos);
    }
}
