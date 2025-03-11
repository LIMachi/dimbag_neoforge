package com.limachi.dim_bag.neoforge;

import com.limachi.dim_bag.neoforge.utils.CheckDistVisitor;
import com.limachi.dim_bag.utils.ModBase;
import com.limachi.dim_bag.utils.reflect.AnnotationExtractor;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;

import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@Mod("dim_bag")
public final class MainNeo {
    public MainNeo() { ModBase.init(new AnnotationExtractor(ModBase.class, CheckDistVisitor::skipInvalidEnv)); }

    @EventBusSubscriber(value = Dist.CLIENT, modid = "dim_bag", bus = EventBusSubscriber.Bus.MOD)
    public static class Client {
        @SubscribeEvent
        public static void init(RenderLevelStageEvent.RegisterStageEvent event) { ModBase.ClientModBase.init(); }
    }


}
