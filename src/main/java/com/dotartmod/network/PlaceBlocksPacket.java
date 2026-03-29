package com.dotartmod.network;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record PlaceBlocksPacket(
        List<Pair<BlockPos, ResourceLocation>> entries,
        boolean sendBackgroundBlocks,
        int backgroundColor) implements CustomPacketPayload {

    public static final Type<PlaceBlocksPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath("dotartmod", "place_blocks"));

    public static final StreamCodec<FriendlyByteBuf, PlaceBlocksPacket> STREAM_CODEC = StreamCodec.of(
            PlaceBlocksPacket::encode,
            PlaceBlocksPacket::decode);

    @Override
    public Type<PlaceBlocksPacket> type() {
        return TYPE;
    }

    private static void encode(FriendlyByteBuf buf, PlaceBlocksPacket packet) {
        // entries のサイズを書き込む
        buf.writeInt(packet.entries().size());

        for (Pair<BlockPos, ResourceLocation> entry : packet.entries()) {
            BlockPos pos = entry.getFirst();
            ResourceLocation blockId = entry.getSecond();

            // BlockPos が null の場合はスキップ
            if (pos == null) {
                continue;
            }

            // BlockPos を書き込む
            buf.writeBlockPos(pos);

            // ResourceLocation が null の場合は空文字を書き込む
            if (blockId == null) {
                buf.writeUtf("");
            } else {
                buf.writeUtf(blockId.toString());
            }
        }

        // sendBackgroundBlocks と backgroundColor を書き込む
        buf.writeBoolean(packet.sendBackgroundBlocks());
        buf.writeInt(packet.backgroundColor());
    }

    private static PlaceBlocksPacket decode(FriendlyByteBuf buf) {
        int size = buf.readInt();
        List<Pair<BlockPos, ResourceLocation>> entries = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            BlockPos pos = buf.readBlockPos();
            String blockIdStr = buf.readUtf();

            ResourceLocation blockId = null;
            if (!blockIdStr.isEmpty()) {
                blockId = ResourceLocation.parse(blockIdStr);
            }

            entries.add(Pair.of(pos, blockId));
        }

        boolean sendBackgroundBlocks = buf.readBoolean();
        int backgroundColor = buf.readInt();

        return new PlaceBlocksPacket(entries, sendBackgroundBlocks, backgroundColor);
    }
}