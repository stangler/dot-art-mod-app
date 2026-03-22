package com.dotartmod.network;

import com.dotartmod.DotArtMod;
import com.dotartmod.server.ServerPlaceHandler;
import com.mojang.datafixers.util.Pair;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record PlaceBlocksPacket(List<Pair<BlockPos, ResourceLocation>> entries)
        implements CustomPacketPayload {

    public static final Type<PlaceBlocksPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(DotArtMod.MOD_ID, "place_blocks"));

    public static final StreamCodec<ByteBuf, PlaceBlocksPacket> STREAM_CODEC = new StreamCodec<>() {

        @Override
        public PlaceBlocksPacket decode(ByteBuf buf) {

            int size = buf.readInt();
            List<Pair<BlockPos, ResourceLocation>> list = new ArrayList<>();

            for (int i = 0; i < size; i++) {
                BlockPos pos = BlockPos.STREAM_CODEC.decode(buf);
                ResourceLocation id = ResourceLocation.STREAM_CODEC.decode(buf);
                list.add(Pair.of(pos, id));
            }

            return new PlaceBlocksPacket(list);
        }

        @Override
        public void encode(ByteBuf buf, PlaceBlocksPacket packet) {

            buf.writeInt(packet.entries.size());

            for (Pair<BlockPos, ResourceLocation> entry : packet.entries) {
                BlockPos.STREAM_CODEC.encode(buf, entry.getFirst());
                ResourceLocation.STREAM_CODEC.encode(buf, entry.getSecond());
            }
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> ServerPlaceHandler.handlePlaceBlocks(this, context));
    }
}