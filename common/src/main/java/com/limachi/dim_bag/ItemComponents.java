package com.limachi.dim_bag;

import com.limachi.lim_lib.common.annotations.StaticInit;
import com.limachi.lim_lib.common.codec.Codecs;
import com.limachi.lim_lib.common.codec.StreamCodecs;

import dev.architectury.registry.registries.RegistrySupplier;

import net.minecraft.core.component.DataComponentType;

public class ItemComponents {
    public static RegistrySupplier<DataComponentType<Integer>> ROOM_ID;

    @StaticInit
    public static void registerComponent() {
        ROOM_ID = DimBag.MOD.registries.component("output", Codecs.INT, StreamCodecs.INT);
    }
}
