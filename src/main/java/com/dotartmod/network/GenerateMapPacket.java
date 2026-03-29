package com.dotartmod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record GenerateMapPacket(byte[] colors) implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("dotartmod", "generate_map");
    public static final Type<GenerateMapPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, GenerateMapPacket> CODEC = StreamCodec.of(
            (buf, pkt) -> buf.writeByteArray(pkt.colors()),
            buf -> new GenerateMapPacket(buf.readByteArray()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(GenerateMapPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();
            ServerLevel level = player.serverLevel();

            System.out.println("[DotArt][Server] GenerateMapPacket received");

            ItemStack mapStack = net.minecraft.world.item.MapItem.create(
                    level,
                    player.getBlockX(),
                    player.getBlockZ(),
                    (byte) 0,
                    false,
                    false);

            MapItemSavedData mapData = net.minecraft.world.item.MapItem.getSavedData(mapStack, level);
            if (mapData == null) {
                System.out.println("[DotArt][Server] ERROR: mapData is null!");
                return;
            }

            // ★ リフレクションでlockedをtrueに設定
            try {
                var field = MapItemSavedData.class.getDeclaredField("locked");
                field.setAccessible(true);
                field.set(mapData, true);
                System.out.println("[DotArt][Server] locked = true (via reflection)");
            } catch (Exception e) {
                System.out.println("[DotArt][Server] Could not lock map: " + e.getMessage());
            }

            byte[] colors = packet.colors();
            System.arraycopy(colors, 0, mapData.colors, 0, Math.min(colors.length, mapData.colors.length));
            mapData.setDirty(true);

            System.out.println("[DotArt][Server] colors written successfully");

            if (!player.getInventory().add(mapStack)) {
                player.drop(mapStack, false);
                System.out.println("[DotArt][Server] Inventory full, dropped to world");
            } else {
                System.out.println("[DotArt][Server] Map added to inventory successfully!");
            }
        });
    }
}