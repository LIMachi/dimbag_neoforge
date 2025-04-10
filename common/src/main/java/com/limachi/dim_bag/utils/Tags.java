package com.limachi.dim_bag.utils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.function.Supplier;

public class Tags {
    public static CompoundTag getOrCreateCompound(CompoundTag self, String key, Supplier<CompoundTag> build) {
        if (!self.contains(key, Tag.TAG_COMPOUND))
            self.put(key, build.get());
        return self.getCompound(key);
    }

    public static ListTag getOrCreateList(CompoundTag self, String key, Supplier<ListTag> build) {
        if (!self.contains(key, Tag.TAG_LIST))
            self.put(key, build.get());
        return (ListTag)self.get(key);
    }
}
