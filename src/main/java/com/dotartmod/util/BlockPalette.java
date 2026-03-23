package com.dotartmod.util;

import net.minecraft.resources.ResourceLocation;
import java.util.HashMap;
import java.util.Map;

public class BlockPalette {
    public static final Map<Integer, ResourceLocation> BLOCK_COLORS = new HashMap<>();

    static {
        // Wool
        BLOCK_COLORS.put(0xFFFFFFFF, ResourceLocation.parse("minecraft:white_wool"));
        BLOCK_COLORS.put(0xFFD97B33, ResourceLocation.parse("minecraft:orange_wool"));
        BLOCK_COLORS.put(0xFFB350BC, ResourceLocation.parse("minecraft:magenta_wool"));
        BLOCK_COLORS.put(0xFF6689D3, ResourceLocation.parse("minecraft:light_blue_wool"));
        BLOCK_COLORS.put(0xFFE6E644, ResourceLocation.parse("minecraft:yellow_wool"));
        BLOCK_COLORS.put(0xFF7FCC19, ResourceLocation.parse("minecraft:lime_wool"));
        BLOCK_COLORS.put(0xFFF27FA5, ResourceLocation.parse("minecraft:pink_wool"));
        BLOCK_COLORS.put(0xFF4C4C4C, ResourceLocation.parse("minecraft:gray_wool"));
        BLOCK_COLORS.put(0xFF999999, ResourceLocation.parse("minecraft:light_gray_wool"));
        BLOCK_COLORS.put(0xFF4C7F99, ResourceLocation.parse("minecraft:cyan_wool"));
        BLOCK_COLORS.put(0xFF7F3FB2, ResourceLocation.parse("minecraft:purple_wool"));
        BLOCK_COLORS.put(0xFF334CB2, ResourceLocation.parse("minecraft:blue_wool"));
        BLOCK_COLORS.put(0xFF664C33, ResourceLocation.parse("minecraft:brown_wool"));
        BLOCK_COLORS.put(0xFF667F33, ResourceLocation.parse("minecraft:green_wool"));
        BLOCK_COLORS.put(0xFF993333, ResourceLocation.parse("minecraft:red_wool"));
        BLOCK_COLORS.put(0xFF191919, ResourceLocation.parse("minecraft:black_wool"));

        // Concrete
        BLOCK_COLORS.put(0xFFCFD5D6, ResourceLocation.parse("minecraft:white_concrete"));
        BLOCK_COLORS.put(0xFFE06101, ResourceLocation.parse("minecraft:orange_concrete"));
        BLOCK_COLORS.put(0xFFA9309F, ResourceLocation.parse("minecraft:magenta_concrete"));
        BLOCK_COLORS.put(0xFF2489C7, ResourceLocation.parse("minecraft:light_blue_concrete"));
        BLOCK_COLORS.put(0xFFF1AF15, ResourceLocation.parse("minecraft:yellow_concrete"));
        BLOCK_COLORS.put(0xFF5EA818, ResourceLocation.parse("minecraft:lime_concrete"));
        BLOCK_COLORS.put(0xFFD5658E, ResourceLocation.parse("minecraft:pink_concrete"));
        BLOCK_COLORS.put(0xFF373A3E, ResourceLocation.parse("minecraft:gray_concrete"));
        BLOCK_COLORS.put(0xFF7D7D73, ResourceLocation.parse("minecraft:light_gray_concrete"));
        BLOCK_COLORS.put(0xFF157788, ResourceLocation.parse("minecraft:cyan_concrete"));
        BLOCK_COLORS.put(0xFF641F9C, ResourceLocation.parse("minecraft:purple_concrete"));
        BLOCK_COLORS.put(0xFF2D2F8F, ResourceLocation.parse("minecraft:blue_concrete"));
        BLOCK_COLORS.put(0xFF603B1F, ResourceLocation.parse("minecraft:brown_concrete"));
        BLOCK_COLORS.put(0xFF495B24, ResourceLocation.parse("minecraft:green_concrete"));
        BLOCK_COLORS.put(0xFF8E2121, ResourceLocation.parse("minecraft:red_concrete"));
        BLOCK_COLORS.put(0xFF080A0F, ResourceLocation.parse("minecraft:black_concrete"));
    }

    public static ResourceLocation getNearestBlock(int color) {
        ResourceLocation nearest = null;
        double minDistance = Double.MAX_VALUE;
        for (Map.Entry<Integer, ResourceLocation> entry : BLOCK_COLORS.entrySet()) {
            double dist = getColorDistance(color, entry.getKey());
            if (dist < minDistance) {
                minDistance = dist;
                nearest = entry.getValue();
            }
        }
        return nearest;
    }

    public static int getNearestColor(int color) {
        int nearestColor = 0xFFFFFFFF;
        double minDistance = Double.MAX_VALUE;
        for (int paletteColor : BLOCK_COLORS.keySet()) {
            double dist = getColorDistance(color, paletteColor);
            if (dist < minDistance) {
                minDistance = dist;
                nearestColor = paletteColor;
            }
        }
        return 0xFF000000 | (nearestColor & 0x00FFFFFF);
    }

    private static double getColorDistance(int c1, int c2) {
        int r1 = (c1 >> 16) & 0xFF;
        int g1 = (c1 >> 8) & 0xFF;
        int b1 = c1 & 0xFF;
        int r2 = (c2 >> 16) & 0xFF;
        int g2 = (c2 >> 8) & 0xFF;
        int b2 = c2 & 0xFF;
        return Math.sqrt(Math.pow(r1 - r2, 2) + Math.pow(g1 - g2, 2) + Math.pow(b1 - b2, 2));
    }
}