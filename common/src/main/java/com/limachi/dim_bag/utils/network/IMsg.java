package com.limachi.dim_bag.utils.network;

import com.limachi.dim_bag.utils.ModBase;

import dev.architectury.networking.NetworkManager;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

@FunctionalInterface
public interface IMsg<T extends IMsg<T>> extends CustomPacketPayload {
    void run(NetworkManager.PacketContext ctx);

    default Boolean upstream() { return null; }

    @Override
    default Type<T> type() { return ModBase.registries.getMessageType(this); }
}