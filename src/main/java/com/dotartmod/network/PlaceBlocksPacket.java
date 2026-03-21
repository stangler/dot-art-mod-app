package com.dotartmod.network;

import com.dotartmod.DotArtMod;
import com.dotartmod.server.ServerPlaceHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

/**
 * クライアント → サーバー：ブロック設置パケット
 *
 * クライアントが変換した BlockPos のリストをサーバーに送り、
 * サーバー側でパーミッションチェック・実際の設置を行う。
 * これによりサバイバルモード・マルチサーバーでも動作する。
 */
public record PlaceBlocksPacket(List<BlockPos> positions) implements CustomPacketPayload {

    public static final Type<PlaceBlocksPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(DotArtMod.MOD_ID, "place_blocks")
    );

    public static final StreamCodec<ByteBuf, PlaceBlocksPacket> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list()),
                    PlaceBlocksPacket::positions,
                    PlaceBlocksPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> ServerPlaceHandler.handlePlaceBlocks(this, context));
    }
}
