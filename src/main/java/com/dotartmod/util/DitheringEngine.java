package com.dotartmod.util;

import java.awt.image.BufferedImage;

public class DitheringEngine {
    public static void applyFloydSteinberg(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int oldRgb = img.getRGB(x, y);
                if (((oldRgb >> 24) & 0xFF) < 128)
                    continue;

                int newRgb = BlockPalette.getNearestColor(oldRgb);
                img.setRGB(x, y, (0xFF << 24) | (newRgb & 0x00FFFFFF));

                int errR = ((oldRgb >> 16) & 0xFF) - ((newRgb >> 16) & 0xFF);
                int errG = ((oldRgb >> 8) & 0xFF) - ((newRgb >> 8) & 0xFF);
                int errB = (oldRgb & 0xFF) - (newRgb & 0xFF);

                distributeError(img, x + 1, y, errR, errG, errB, 7 / 16f);
                distributeError(img, x - 1, y + 1, errR, errG, errB, 3 / 16f);
                distributeError(img, x, y + 1, errR, errG, errB, 5 / 16f);
                distributeError(img, x + 1, y + 1, errR, errG, errB, 1 / 16f);
            }
        }
    }

    private static void distributeError(BufferedImage img, int x, int y, int errR, int errG, int errB, float factor) {
        if (x < 0 || x >= img.getWidth() || y < 0 || y >= img.getHeight())
            return;
        int rgb = img.getRGB(x, y);
        if (((rgb >> 24) & 0xFF) < 128)
            return;

        int r = Math.max(0, Math.min(255, ((rgb >> 16) & 0xFF) + (int) (errR * factor)));
        int g = Math.max(0, Math.min(255, ((rgb >> 8) & 0xFF) + (int) (errG * factor)));
        int b = Math.max(0, Math.min(255, (rgb & 0xFF) + (int) (errB * factor)));

        img.setRGB(x, y, (0xFF << 24) | (r << 16) | (g << 8) | b);
    }
}