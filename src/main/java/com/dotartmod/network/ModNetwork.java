package com.dotartmod.network;

import com.dotartmod.server.ServerPlaceHandler;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class ModNetwork {

    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1.0.0");

        // PlaceBlocksPacket の登録
        registrar.playToServer(
                PlaceBlocksPacket.TYPE,
                PlaceBlocksPacket.STREAM_CODEC,
                (packet, ctx) -> ServerPlaceHandler.handlePlaceBlocks(packet, ctx));

        // GenerateMapPacket の登録 ← 追加
        registrar.playToServer(
                GenerateMapPacket.TYPE,
                GenerateMapPacket.CODEC,
                GenerateMapPacket::handle);
    }
}