package com.dotartmod.server;

import com.dotartmod.DotArtMod;
import com.dotartmod.network.PlaceBlocksPacket;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayDeque;
import java.util.Queue;

public class ServerPlaceHandler {

    public static void handlePlaceBlocks(PlaceBlocksPacket packet, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player))
            return;

        Queue<Pair<BlockPos, ResourceLocation>> queue = new ArrayDeque<>(packet.entries());

        ServerPlaceExecutor.enqueue(player.serverLevel(), queue);

        DotArtMod.LOGGER.info("[DotArt] queued {} blocks", queue.size());
    }

    static void place(ServerLevel level, BlockPos pos, ResourceLocation blockId) {
        if (!level.getBlockState(pos).canBeReplaced())
            return;

        Block block = BuiltInRegistries.BLOCK.get(blockId);
        if (block == null)
            return;

        BlockState state = block.defaultBlockState();
        level.setBlock(pos, state, 3);
    }
}
