package com.dotartmod.server;

import com.dotartmod.DotArtMod;
import com.dotartmod.network.PlaceBlocksPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

/**
 * サーバー側でのブロック設置処理。
 *
 * クライアントから PlaceBlocksPacket を受け取り、
 * プレイヤーの権限・インベントリを確認した上でブロックを設置する。
 *
 * ＜サバイバルモードの挙動＞
 *   - 手持ちのアイテムを消費する
 *   - クリエイティブモードでは消費しない
 */
public class ServerPlaceHandler {

    public static void handlePlaceBlocks(PlaceBlocksPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            ServerLevel level  = player.serverLevel();
            List<BlockPos> positions = packet.positions();

            int placed = 0;
            for (BlockPos pos : positions) {
                // すでにブロックがある場所はスキップ
                if (!level.getBlockState(pos).canBeReplaced()) continue;

                // 手持ちのアイテムを確認
                ItemStack heldItem = player.getMainHandItem();
                if (!(heldItem.getItem() instanceof BlockItem blockItem)) continue;

                // ブロックを設置
                BlockState state = blockItem.getBlock().defaultBlockState();
                level.setBlock(pos, state, 3);

                // サバイバルモードではアイテムを消費
                if (!player.isCreative()) {
                    heldItem.shrink(1);
                    if (heldItem.isEmpty()) break; // インベントリが空になったら終了
                }
                placed++;
            }

            DotArtMod.LOGGER.info("[DotArt] {} ブロックを設置しました (player: {})",
                    placed, player.getName().getString());
        });
    }
}
