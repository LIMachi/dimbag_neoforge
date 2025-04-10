package com.limachi.dim_bag.utils.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

@Environment(EnvType.CLIENT)
public class ClientUtils {
    public static Level getLevel(ResourceLocation location) {
        var cl = Minecraft.getInstance().level;
        if (cl == null)
            return null;
        return cl.dimension().location().equals(location) ? cl : null;
    }
}
