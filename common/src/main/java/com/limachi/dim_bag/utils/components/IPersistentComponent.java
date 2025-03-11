package com.limachi.dim_bag.utils.components;

import com.mojang.serialization.Codec;

public interface IPersistentComponent<T extends IPersistentComponent<T>> extends IComponent<T> {
    Codec<T> codec();
}
