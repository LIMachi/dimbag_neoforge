package com.limachi.dim_bag.mixin;

import com.limachi.dim_bag.entities.BagEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.vehicle.Boat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(Ravager.class)
public abstract class RavagerMixin {
    @Shadow @Nullable public abstract LivingEntity getControllingPassenger();

    @Unique
    private final Ravager mixin$this = (Ravager)(Object)this;

    @Inject(method = "updateControlFlags()V", at = @At(value = "TAIL"))
    protected void bagsControlEntitiesOnlyWhenParasiteIsActive(CallbackInfo ci) {
        if (getControllingPassenger() instanceof BagEntity bag && !bag.isParasiting()) {
            mixin$this.goalSelector.setControlFlag(Goal.Flag.MOVE, true);
            mixin$this.goalSelector.setControlFlag(Goal.Flag.JUMP, !(mixin$this.getVehicle() instanceof Boat));
            mixin$this.goalSelector.setControlFlag(Goal.Flag.LOOK, true);
            mixin$this.goalSelector.setControlFlag(Goal.Flag.TARGET, true);
        }
    }
}
