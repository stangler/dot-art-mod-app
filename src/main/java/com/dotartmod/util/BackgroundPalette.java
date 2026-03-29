package com.dotartmod.util;

import net.minecraft.resources.ResourceLocation;

/**
 * 背景色と対応するブロックを管理するクラス
 */
public class BackgroundPalette {

    /**
     * 背景色（ARGB）から対応するブロックの ResourceLocation を返す
     *
     * @param backgroundColor ARGB形式の色（例: 0xFF808080）
     * @return 対応するブロックの ResourceLocation
     */
    public static ResourceLocation getBackgroundBlock(int backgroundColor) {
        // 例: グレー → 灰色羊毛、黒 → 黒色羊毛、白 → 白色羊毛
        if (backgroundColor == 0xFF000000) {
            return ResourceLocation.withDefaultNamespace("black_wool");
        } else if (backgroundColor == 0xFFFFFFFF) {
            return ResourceLocation.withDefaultNamespace("white_wool");
        } else {
            // デフォルトは灰色羊毛
            return ResourceLocation.withDefaultNamespace("gray_wool");
        }
    }
}