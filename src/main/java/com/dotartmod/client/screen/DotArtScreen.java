package com.dotartmod.client.screen;

import com.dotartmod.client.ClientPlaceQueue;
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

    // サイズ上限: 16〜128
    private static final int MIN_SIZE = 16;
    private static final int MAX_SIZE = 128;

    // 設置方向モード
    private enum PlacementMode {
        WALL, FLOOR
    }

    private PlacementMode placementMode = PlacementMode.WALL;

    // モード切り替えボタン
    private Button modeToggleButton;

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
            loadAndProcess(pathEdit.getValue());
        }).bounds(this.width / 2 - 100, 80, 200, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("設置開始"), button -> {
            if (blockGrid != null)
                sendPlacementPackets();
        }).bounds(this.width / 2 - 100, 105, 200, 20).build());

        // モード切り替えボタン
        this.modeToggleButton = Button.builder(
                Component.literal("Mode: " + placementMode.name()),
                btn -> {
                    placementMode = (placementMode == PlacementMode.WALL)
                            ? PlacementMode.FLOOR
                            : PlacementMode.WALL;
                    btn.setMessage(Component.literal("Mode: " + placementMode.name()));
                })
                .pos(this.width / 2 - 100, 130)
                .size(100, 20)
                .build();
        this.addRenderableWidget(modeToggleButton);
    }

    private void loadAndProcess(String path) {
        try {
            File file = new File(path);
            if (!file.exists())
                return;
            BufferedImage original = ImageIO.read(file);
            if (original == null)
                return;

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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendPlacementPackets() {
        List<Pair<net.minecraft.core.BlockPos, ResourceLocation>> allBlocks = new ArrayList<>();
        net.minecraft.core.BlockPos startPos = this.minecraft.player.blockPosition()
                .relative(this.minecraft.player.getDirection(), 5);

        // 視線方向（HorizontalDirection）
        net.minecraft.core.Direction lookDir = this.minecraft.player.getDirection();

        for (int y = 0; y < artSize; y++) {
            for (int x = 0; x < artSize; x++) {
                if (blockGrid[y][x] != null) {
                    net.minecraft.core.BlockPos pos;
                    if (placementMode == PlacementMode.WALL) {
                        // 壁モード（既存の動作）
                        // x → 右方向、y → 上方向（高さ）
                        pos = startPos.offset(x, artSize - 1 - y, 0);
                    } else {
                        // 床モード（新規）
                        // x → 右方向、y → 前方向（視線方向）
                        pos = startPos
                                .offset(x, 0, 0) // 右方向
                                .relative(lookDir, y); // 前方向
                    }
                    allBlocks.add(Pair.of(pos, blockGrid[y][x]));
                }
            }
        }

        if (!allBlocks.isEmpty()) {
            // パケット分割送信：200個ずつ小分けにする
            int batchSize = 200;
            for (int i = 0; i < allBlocks.size(); i += batchSize) {
                int end = Math.min(i + batchSize, allBlocks.size());
                List<Pair<net.minecraft.core.BlockPos, ResourceLocation>> subList = new ArrayList<>(
                        allBlocks.subList(i, end));

                // サーバー接続を通じてパケットを送信
                if (Minecraft.getInstance().getConnection() != null) {
                    Minecraft.getInstance().getConnection().send(new PlaceBlocksPacket(subList));
                }
            }
            this.onClose();
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        if (colorPreview != null) {
            int startX = this.width / 2 - (artSize / 2);
            int startY = 160; // モードボタンの下にプレビューを表示
            // プレビューのスケール調整（128×128でも画面内に収める）
            int previewScale = Math.max(1, (this.width / MAX_SIZE) / 2);
            for (int y = 0; y < artSize; y++) {
                for (int x = 0; x < artSize; x++) {
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
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}