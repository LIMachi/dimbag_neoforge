package com.limachi.dim_bag.neoforge;

import com.limachi.lim_lib.common.annotations.Loader;
import com.limachi.lim_lib.common.modCreation.Loaders;

import com.limachi.lim_lib.neoforge.NeoEntryPoint;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Loader(Loaders.NeoForge)
@Mod("dim_bag")
public final class MainNeo extends NeoEntryPoint {
    public MainNeo(IEventBus modBus, Dist dist) {
        super(modBus, dist);
    }

    @Override
    protected String commonRootPackage() {
        return "com.limachi.dim_bag";
    }
}
