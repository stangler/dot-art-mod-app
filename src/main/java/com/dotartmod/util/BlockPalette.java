package com.dotartmod.util;

import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class BlockPalette {

    public static class BlockColor {
        public final int r, g, b;
        public final ResourceLocation blockId;

        public BlockColor(int r, int g, int b, String blockId) {
            this.r = r;
            this.g = g;
            this.b = b;
            this.blockId = ResourceLocation.parse(blockId);
        }

        public ResourceLocation blockId() {
            return blockId;
        }
    }

    public static final List<BlockColor> PALETTE = new ArrayList<>();

    static {
        // ===== コンクリート16色 =====
        PALETTE.add(new BlockColor(255, 255, 255, "minecraft:white_concrete"));
        PALETTE.add(new BlockColor(0, 0, 0, "minecraft:black_concrete"));
        PALETTE.add(new BlockColor(255, 0, 0, "minecraft:red_concrete"));
        PALETTE.add(new BlockColor(0, 255, 0, "minecraft:lime_concrete"));
        PALETTE.add(new BlockColor(0, 0, 255, "minecraft:blue_concrete"));
        PALETTE.add(new BlockColor(255, 255, 0, "minecraft:yellow_concrete"));
        PALETTE.add(new BlockColor(255, 165, 0, "minecraft:orange_concrete"));
        PALETTE.add(new BlockColor(255, 192, 203, "minecraft:pink_concrete"));
        PALETTE.add(new BlockColor(128, 0, 128, "minecraft:purple_concrete"));
        PALETTE.add(new BlockColor(0, 255, 255, "minecraft:cyan_concrete"));
        PALETTE.add(new BlockColor(128, 128, 128, "minecraft:gray_concrete"));
        PALETTE.add(new BlockColor(192, 192, 192, "minecraft:light_gray_concrete"));
        PALETTE.add(new BlockColor(165, 42, 42, "minecraft:brown_concrete"));
        PALETTE.add(new BlockColor(0, 128, 0, "minecraft:green_concrete"));
        PALETTE.add(new BlockColor(0, 0, 128, "minecraft:blue_concrete"));
        PALETTE.add(new BlockColor(255, 0, 255, "minecraft:magenta_concrete"));
    }

    public static BlockColor findNearest(int r, int g, int b) {
        BlockColor best = null;
        double bestDist = Double.MAX_VALUE;

        for (BlockColor bc : PALETTE) {
            double dr = r - bc.r;
            double dg = g - bc.g;
            double db = b - bc.b;

            double dist = 2 * dr * dr + 4 * dg * dg + 3 * db * db;

            if (dist < bestDist) {
                bestDist = dist;
                best = bc;
            }
        }

        return best;
    }
}