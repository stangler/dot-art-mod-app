package com.dotartmod.server;

import com.dotartmod.network.PlaceBlocksPacket;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public class ServerPlaceQueue {

    public static void process(ServerLevel level, List<Pair<BlockPos, ResourceLocation>> queue,
            IPayloadContext context) {
        // PlaceBlocksPacket を構築して ServerPlaceHandler.handlePlaceBlocks に渡す
        PlaceBlocksPacket packet = new PlaceBlocksPacket(
                queue,
                false, // sendBackgroundBlocks（必要に応じて変更）
                0 // backgroundColor（必要に応じて変更）
        );
        ServerPlaceHandler.handlePlaceBlocks(packet, context);
    }
}