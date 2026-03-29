package com.dotartmod.client.screen;

import com.dotartmod.network.GenerateMapPacket;
import com.dotartmod.network.PlaceBlocksPacket;
import com.dotartmod.util.BlockPalette;
import com.dotartmod.util.DitheringEngine;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import com.mojang.datafixers.util.Pair;
import net.minecraft.util.Mth;
import net.minecraft.client.Minecraft;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DotArtScreen extends Screen {
    private EditBox pathEdit;
    private AbstractSliderButton sizeSlider;
    private int artSize = 32;
    private ResourceLocation[][] blockGrid;
    private int[][] colorPreview;

    private static final int MIN_SIZE = 16;
    private static final int MAX_SIZE = 128;

    private enum PlacementMode {
        WALL, FLOOR, MAP
    }

    private static PlacementMode placementMode = PlacementMode.WALL;

    private Button modeToggleButton;

    private boolean useBackgroundColor = true;
    private Button backgroundToggleButton;

    private int backgroundColor = 0xFF808080;
    private Button backgroundColorButton;

    public DotArtScreen() {
        super(Component.literal("ドットアート生成"));
    }

    @Override
    protected void init() {
        this.pathEdit = new EditBox(this.font, this.width / 2 - 100, 30, 200, 20, Component.literal("画像パス"));
        this.pathEdit.setMaxLength(512);
        this.addRenderableWidget(this.pathEdit);

        this.sizeSlider = new AbstractSliderButton(this.width / 2 - 100, 55, 200, 20,
                Component.literal("サイズ: " + artSize), (double) (artSize - MIN_SIZE) / (MAX_SIZE - MIN_SIZE)) {
            @Override
            protected void updateMessage() {
                this.setMessage(Component.literal("サイズ: " + artSize));
            }

            @Override
            protected void applyValue() {
                artSize = Mth.clamp((int) (this.value * (MAX_SIZE - MIN_SIZE) + MIN_SIZE), MIN_SIZE, MAX_SIZE);
            }
        };
        this.addRenderableWidget(this.sizeSlider);

        this.addRenderableWidget(Button.builder(Component.literal("読み込み & プレビュー"), button -> {
            System.out.println("[DotArt] Load button clicked");
            loadAndProcess(pathEdit.getValue());
        }).bounds(this.width / 2 - 100, 80, 200, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("設置開始"), button -> {
            System.out.println("[DotArt] Start placement clicked");
            System.out.println("[DotArt] blockGrid = " + (blockGrid != null));
            sendPlacementPackets();
        }).bounds(this.width / 2 - 100, 105, 200, 20).build());

        this.modeToggleButton = Button.builder(
                Component.literal("Mode: " + placementMode.name()),
                btn -> {
                    placementMode = switch (placementMode) {
                        case WALL -> PlacementMode.FLOOR;
                        case FLOOR -> PlacementMode.MAP;
                        case MAP -> PlacementMode.WALL;
                    };
                    btn.setMessage(Component.literal("Mode: " + placementMode.name()));
                })
                .pos(this.width / 2 - 100, 130)
                .size(100, 20)
                .build();
        this.addRenderableWidget(modeToggleButton);

        this.backgroundToggleButton = Button.builder(
                Component.literal("背景色: " + (useBackgroundColor ? "ON" : "OFF")),
                btn -> {
                    useBackgroundColor = !useBackgroundColor;
                    btn.setMessage(Component.literal("背景色: " + (useBackgroundColor ? "ON" : "OFF")));
                })
                .pos(this.width / 2 + 5, 130)
                .size(95, 20)
                .build();
        this.addRenderableWidget(backgroundToggleButton);

        this.backgroundColorButton = Button.builder(
                Component.literal("背景色選択"),
                btn -> {
                    if (backgroundColor == 0xFF808080) {
                        backgroundColor = 0xFF000000;
                    } else if (backgroundColor == 0xFF000000) {
                        backgroundColor = 0xFFFFFFFF;
                    } else {
                        backgroundColor = 0xFF808080;
                    }
                })
                .pos(this.width / 2 - 100, 155)
                .size(200, 20)
                .build();
        this.addRenderableWidget(backgroundColorButton);
    }

    private void loadAndProcess(String path) {
        try {
            File file = new File(path);
            if (!file.exists()) {
                System.out.println("[DotArt] File not found");
                return;
            }

            BufferedImage original = ImageIO.read(file);
            if (original == null) {
                System.out.println("[DotArt] Image load failed");
                return;
            }

            BufferedImage scaled = new BufferedImage(artSize, artSize, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = scaled.createGraphics();
            g.drawImage(original.getScaledInstance(artSize, artSize, Image.SCALE_SMOOTH), 0, 0, null);
            g.dispose();

            DitheringEngine.applyFloydSteinberg(scaled);

            this.blockGrid = new ResourceLocation[artSize][artSize];
            this.colorPreview = new int[artSize][artSize];

            for (int y = 0; y < artSize; y++) {
                for (int x = 0; x < artSize; x++) {
                    int rgb = scaled.getRGB(x, y);
                    if (((rgb >> 24) & 0xFF) >= 128) {
                        this.blockGrid[y][x] = BlockPalette.getNearestBlock(rgb);
                        this.colorPreview[y][x] = rgb;
                    } else {
                        this.blockGrid[y][x] = null;
                        this.colorPreview[y][x] = 0;
                    }
                }
            }

            System.out.println("[DotArt] Image processed successfully");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendPlacementPackets() {
        System.out.println("[DotArt] sendPlacementPackets called");
        System.out.println("[DotArt] placementMode = " + placementMode);

        if (placementMode == PlacementMode.MAP) {
            System.out.println("[DotArt] MAP mode branch entered");
            generateMapArt();
            return;
        }

        if (blockGrid == null) {
            System.out.println("[DotArt] blockGrid is null, abort");
            return;
        }

        List<Pair<net.minecraft.core.BlockPos, ResourceLocation>> allBlocks = new ArrayList<>();
        net.minecraft.core.BlockPos startPos = this.minecraft.player.blockPosition()
                .relative(this.minecraft.player.getDirection(), 5);

        for (int y = 0; y < artSize; y++) {
            for (int x = 0; x < artSize; x++) {
                net.minecraft.core.BlockPos pos;

                if (placementMode == PlacementMode.WALL) {
                    pos = startPos.offset(x, artSize - 1 - y, 0);
                } else {
                    pos = startPos.offset(x, 0, 0).relative(this.minecraft.player.getDirection(), y);
                }

                if (useBackgroundColor) {
                    allBlocks.add(Pair.of(pos, null));
                }

                if (blockGrid[y][x] != null) {
                    allBlocks.add(Pair.of(pos, blockGrid[y][x]));
                }
            }
        }

        if (!allBlocks.isEmpty()) {
            int batchSize = 200;
            for (int i = 0; i < allBlocks.size(); i += batchSize) {
                int end = Math.min(i + batchSize, allBlocks.size());
                List<Pair<net.minecraft.core.BlockPos, ResourceLocation>> subList = new ArrayList<>(
                        allBlocks.subList(i, end));

                if (Minecraft.getInstance().getConnection() != null) {
                    Minecraft.getInstance().getConnection()
                            .send(new PlaceBlocksPacket(subList, useBackgroundColor, backgroundColor));
                }
            }
            this.onClose();
        }
    }

    private void generateMapArt() {
        System.out.println("[DotArt] generateMapArt called");

        if (blockGrid == null) {
            System.out.println("[DotArt] generateMapArt aborted: blockGrid is null");
            return;
        }

        // ★ 修正：背景色ONのとき、背景色に最も近いブロックのマップカラーを事前に取得
        byte bgColorByte = 0;
        if (useBackgroundColor) {
            ResourceLocation bgBlock = BlockPalette.getNearestBlock(backgroundColor);
            if (bgBlock != null) {
                bgColorByte = getMapColorFromBlock(bgBlock);
                System.out.println("[DotArt] Background block: " + bgBlock + " → mapColor: " + bgColorByte);
            }
        }

        byte[] colors = new byte[128 * 128];

        for (int y = 0; y < 128; y++) {
            for (int x = 0; x < 128; x++) {
                int srcX = x * artSize / 128;
                int srcY = y * artSize / 128;
                ResourceLocation blockId = blockGrid[srcY][srcX];

                byte color;
                if (blockId != null) {
                    // 不透明ピクセル：ブロックのマップカラーを使用
                    color = getMapColorFromBlock(blockId);
                } else if (useBackgroundColor) {
                    // ★ 透明ピクセル＋背景色ON：背景色のマップカラーを使用
                    color = bgColorByte;
                } else {
                    // 透明ピクセル＋背景色OFF：透明（0）
                    color = 0;
                }

                colors[x + y * 128] = color;
            }
        }

        System.out.println("[DotArt] Sending GenerateMapPacket to server");

        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() != null) {
            mc.getConnection().send(new GenerateMapPacket(colors));
        }

        this.onClose();
    }

    private byte getMapColorFromBlock(ResourceLocation blockId) {
        var block = net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(blockId);
        if (block == null)
            return 0;
        try {
            int colorId = block.defaultMapColor().id;
            if (colorId == 0)
                return 0;
            return (byte) (colorId * 4 + 2); // シェード2＝通常の明るさ
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        if (colorPreview != null) {
            int startX = this.width / 2 - (artSize / 2);
            int startY = 160;
            int previewScale = Math.max(1, (this.width / MAX_SIZE) / 2);

            for (int y = 0; y < artSize; y++) {
                for (int x = 0; x < artSize; x++) {
                    if (useBackgroundColor) {
                        guiGraphics.fill(
                                startX + x * previewScale, startY + y * previewScale,
                                startX + (x + 1) * previewScale, startY + (y + 1) * previewScale,
                                backgroundColor);
                    }
                    if (colorPreview[y][x] != 0) {
                        guiGraphics.fill(
                                startX + x * previewScale, startY + y * previewScale,
                                startX + (x + 1) * previewScale, startY + (y + 1) * previewScale,
                                colorPreview[y][x]);
                    }
                }
            }

            guiGraphics.drawCenteredString(this.font, "プレビュー表示中", this.width / 2, startY + artSize + 5, 0x00FF00);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}