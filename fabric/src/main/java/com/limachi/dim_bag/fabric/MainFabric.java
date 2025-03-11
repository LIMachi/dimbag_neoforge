package com.limachi.dim_bag.fabric;

import com.limachi.dim_bag.fabric.utils.CheckEnvironmentVisitor;
import com.limachi.dim_bag.utils.ModBase;
import com.limachi.dim_bag.utils.reflect.AnnotationExtractor;

import net.fabricmc.api.ModInitializer;

public final class MainFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ModBase.init(new AnnotationExtractor(ModBase.class, CheckEnvironmentVisitor::skipInvalidEnv));
    }
}
