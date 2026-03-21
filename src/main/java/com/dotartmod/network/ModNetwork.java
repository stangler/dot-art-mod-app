package com.dotartmod.network;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import static com.dotartmod.DotArtMod.MOD_ID;

@EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModNetwork {

    /** DotArtMod コンストラクタから呼ぶ（@EventBusSubscriber で自動登録されるため引数不要） */
    public static void register(IEventBus modEventBus) {
        // @EventBusSubscriber アノテーションで onRegisterPayloads が自動登録される
    }

    @SubscribeEvent
    public static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        // ブロック設置パケット（クライアント → サーバー）
        registrar.playToServer(
                PlaceBlocksPacket.TYPE,
                PlaceBlocksPacket.STREAM_CODEC,
                (packet, ctx) -> packet.handle(ctx)
        );
    }
}
