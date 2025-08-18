package com.limachi.dim_bag.fabric;

import com.limachi.lim_lib.common.annotations.Loader;
import com.limachi.lim_lib.common.modCreation.Loaders;

import com.limachi.lim_lib.fabric.FabricEntryPoint;
import com.limachi.lim_lib.fabric.annotations.FabricMod;

@Loader(Loaders.Fabric)
@FabricMod("dim_bag")
public final class MainFabric extends FabricEntryPoint {
    @Override
    protected String commonRootPackage() {
        return "com.limachi.dim_bag";
    }
}
