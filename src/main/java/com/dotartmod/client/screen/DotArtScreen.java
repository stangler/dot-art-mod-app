package com.dotartmod.client.screen;

import com.dotartmod.client.ClientPlaceQueue;
import com.dotartmod.network.PlaceBlocksPacket;
import com.dotartmod.util.BlockPalette;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DotArtScreen extends Screen {

    private static final int PACKET_CHUNK_SIZE = 512;

    private EditBox pathInput;
    private Button loadBtn;
    private Button placeBtn;
    private SizeSlider sizeSlider;

    private BlockPalette.BlockColor[][] blockGrid;
    private int artSize = 32;

    private String status = "画像パスを入力してください";

    /** 段階送信用キュー */
    private ClientPlaceQueue placeQueue;

    public DotArtScreen() {
        super(Component.literal("DotArt Generator"));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;

        pathInput = new EditBox(this.font, centerX - 150, 40, 300, 20, Component.literal("path"));
        pathInput.setMaxLength(512);
        this.addRenderableWidget(pathInput);

        loadBtn = Button.builder(Component.literal("画像読み込み"), b -> loadImage())
                .pos(centerX - 150, 70)
                .size(140, 20)
                .build();
        this.addRenderableWidget(loadBtn);

        placeBtn = Button.builder(Component.literal("設置"), b -> sendPlacePacket())
                .pos(centerX + 10, 70)
                .size(140, 20)
                .build();
        placeBtn.active = false;
        this.addRenderableWidget(placeBtn);

        sizeSlider = new SizeSlider(centerX - 150, 100, 300, 20);
        this.addRenderableWidget(sizeSlider);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g, mouseX, mouseY, partialTick);

        g.drawCenteredString(this.font, "DotArt Generator", this.width / 2, 10, 0xFFFFFF);
        g.drawString(this.font, status, 20, this.height - 30, 0xAAAAAA);

        super.render(g, mouseX, mouseY, partialTick);

        // プレビュー描画
        if (blockGrid != null) {
            int previewSize = 150;
            int px = this.width - previewSize - 20;
            int py = 40;

            int size = blockGrid.length;
            int pixel = previewSize / size;

            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    BlockPalette.BlockColor bc = blockGrid[y][x];
                    int color = (bc.r << 16) | (bc.g << 8) | bc.b;

                    g.fill(px + x * pixel, py + y * pixel,
                            px + (x + 1) * pixel, py + (y + 1) * pixel,
                            0xFF000000 | color);
                }
            }
        }

        // 段階送信処理
        if (placeQueue != null) {
            placeQueue.tick();

            if (placeQueue.isFinished()) {
                placeQueue = null;
                placeBtn.active = true;
                status = "設置完了";
            }
        }
    }

    private void loadImage() {
        try {
            File file = new File(pathInput.getValue());
            if (!file.exists()) {
                status = "ファイルが存在しません";
                return;
            }

            BufferedImage img = ImageIO.read(file);
            if (img == null) {
                status = "画像読み込み失敗";
                return;
            }

            int size = artSize;
            blockGrid = new BlockPalette.BlockColor[size][size];

            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    int px = x * img.getWidth() / size;
                    int py = y * img.getHeight() / size;
                    int rgb = img.getRGB(px, py);

                    blockGrid[y][x] = BlockPalette.findNearest(
                            (rgb >> 16) & 0xFF,
                            (rgb >> 8) & 0xFF,
                            rgb & 0xFF);
                }
            }

            status = "変換完了！ " + size + "x" + size;
            placeBtn.active = true;

        } catch (Exception e) {
            status = "エラー: " + e.getMessage();
        }
    }

    private void sendPlacePacket() {
        if (blockGrid == null)
            return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null)
            return;

        int size = blockGrid.length;
        BlockPos base = mc.player.blockPosition()
                .relative(mc.player.getDirection(), 3)
                .relative(mc.player.getDirection().getClockWise(), -(size / 2));

        List<Pair<BlockPos, ResourceLocation>> entries = new ArrayList<>();

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                entries.add(Pair.of(
                        base.relative(mc.player.getDirection().getClockWise(), x).above(y),
                        blockGrid[size - 1 - y][x].blockId()));
            }
        }

        List<PlaceBlocksPacket> packets = new ArrayList<>();
        for (int i = 0; i < entries.size(); i += PACKET_CHUNK_SIZE) {
            packets.add(new PlaceBlocksPacket(
                    entries.subList(i, Math.min(i + PACKET_CHUNK_SIZE, entries.size()))));
        }

        placeQueue = new ClientPlaceQueue();
        placeQueue.enqueue(packets);

        placeBtn.active = false;
        status = "設置中... (" + entries.size() + "ブロック)";
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private class SizeSlider extends AbstractSliderButton {
        public SizeSlider(int x, int y, int w, int h) {
            super(x, y, w, h, Component.literal("Size: 32"), 0.33);
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            artSize = 16 + (int) (value * 48);
            setMessage(Component.literal("Size: " + artSize));
        }

        @Override
        protected void applyValue() {
        }
    }
}
