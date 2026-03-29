package com.dotartmod.server;

import com.dotartmod.network.PlaceBlocksPacket;
import com.dotartmod.util.BackgroundPalette;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ServerPlaceHandler {

    public static void handlePlaceBlocks(PlaceBlocksPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            // Player を ServerPlayer にキャスト
            ServerPlayer player = (ServerPlayer) context.player();
            if (player == null)
                return;

            Level level = player.level();
            if (!level.isLoaded(player.blockPosition()))
                return;

            // プレイヤーの足元の Y 座標を基準にする
            int baseY = player.blockPosition().getY();

            for (Pair<BlockPos, ResourceLocation> entry : packet.entries()) {
                BlockPos pos = entry.getFirst();
                ResourceLocation blockId = entry.getSecond();

                // 背景ブロックを送信しない場合は null ブロックをスキップ
                if (!packet.sendBackgroundBlocks() && blockId == null) {
                    continue;
                }

                // 背景ブロックの場合は backgroundColor からブロックを決定
                if (blockId == null && packet.sendBackgroundBlocks()) {
                    blockId = BackgroundPalette.getBackgroundBlock(packet.backgroundColor());
                }

                // ブロックが null の場合はスキップ（通常はここには来ないはず）
                if (blockId == null) {
                    continue;
                }

                // Y 座標をプレイヤーの足元に固定
                BlockPos targetPos = new BlockPos(pos.getX(), baseY, pos.getZ());

                // ブロックの設置ロジック
                Block block = BuiltInRegistries.BLOCK.get(blockId);
                if (block == null) {
                    // ブロックが登録されていない場合はスキップ
                    continue;
                }

                BlockState state = block.defaultBlockState();
                if (level.setBlock(targetPos, state, Block.UPDATE_ALL)) {
                    // ブロック設置成功
                }
            }
        });
    }
}