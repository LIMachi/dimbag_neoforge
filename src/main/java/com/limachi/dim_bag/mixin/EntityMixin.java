package com.limachi.dim_bag.mixin;

import com.limachi.dim_bag.DimBag;
import com.limachi.dim_bag.save_datas.BagsData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.gameevent.DynamicGameEventListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BiConsumer;

@Mixin(Entity.class)
public abstract class EntityMixin {
    //only called when an entity is added/removed from world and when crossing a section of world
    //might still need to improve the bag query if this impact the performances too much
    //an option would be to manually register/unregister the trackers when the holder ticker detects a change of entity
    @Inject(method = "updateDynamicGameEventListener", at = @At("TAIL"))
    public void anyEntityWithBagCanListen(BiConsumer<DynamicGameEventListener<?>, ServerLevel> consumer, CallbackInfo ci) {
        BagsData.runOnBag(DimBag.getBagAccess((Entity)(Object)this, 0, true, false, false, true), b->b.updateEventTracker(consumer));
    }
}
