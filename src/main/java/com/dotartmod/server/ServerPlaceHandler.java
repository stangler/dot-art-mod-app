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

public class ServerPlaceHandler {

    public static void handlePlaceBlocks(PlaceBlocksPacket packet, IPayloadContext context) {

        if (!(context.player() instanceof ServerPlayer player))
            return;

        ServerLevel level = player.serverLevel();

        int placed = 0;

        for (Pair<BlockPos, ResourceLocation> entry : packet.entries()) {

            BlockPos pos = entry.getFirst();
            ResourceLocation blockId = entry.getSecond();

            if (!level.getBlockState(pos).canBeReplaced())
                continue;

            Block block = BuiltInRegistries.BLOCK.get(blockId);
            if (block == null)
                continue;

            BlockState state = block.defaultBlockState();
            level.setBlock(pos, state, 3);

            placed++;
        }

        DotArtMod.LOGGER.info("[DotArt] {} blocks placed by {}",
                placed, player.getName().getString());
    }
}