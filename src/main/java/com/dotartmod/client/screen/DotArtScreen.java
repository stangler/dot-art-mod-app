package com.dotartmod.client.screen;

import com.dotartmod.network.PlaceBlocksPacket;
import com.dotartmod.util.BlockPalette;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@OnlyIn(Dist.CLIENT)
public class DotArtScreen extends Screen {

    // ── 状態 ─────────────────────────────────────────────
    private BufferedImage               sourceImage  = null;
    private BlockPalette.BlockColor[][] blockGrid    = null;
    private int[][]                     previewRgb   = null;
    private int                         artSize      = 32;
    private String                      status       = "§e画像ファイルを選んでね！";
    private boolean                     processing   = false;
    private boolean                     readyToPlace = false;

    // ── 定数 ─────────────────────────────────────────────
    private static final int   PREVIEW_SIZE = 128;
    private static final int[] SIZES        = {16, 24, 32, 48, 64};

    // ── UI ウィジェット ───────────────────────────────────
    private Button    openFileBtn;
    private Button    placeBtn;
    private SizeSlider sizeSlider;
    private EditBox   pathBox;        // Dev Container 環境向けテキスト入力

    public DotArtScreen() {
        super(Component.literal("ドットアートメーカー"));
    }

    // =========================================================
    //  初期化
    // =========================================================
    @Override
    protected void init() {
        int lx = 20;
        int ly = this.height / 2 - 80;

        // ① ファイルダイアログで開く
        openFileBtn = Button.builder(
            Component.literal("📂 画像ファイルを選ぶ"),
            btn -> tryOpenFileDialog()
        ).bounds(lx, ly, 160, 20).build();
        addRenderableWidget(openFileBtn);

        // ② ファイルパス直接入力（Dev Container / X11 なし環境向け）
        pathBox = new EditBox(this.font, lx, ly + 25, 160, 18,
                Component.literal("ファイルパス"));
        pathBox.setHint(Component.literal("§8/path/to/image.png"));
        pathBox.setMaxLength(512);
        addRenderableWidget(pathBox);

        Button loadPathBtn = Button.builder(
            Component.literal("読み込む"),
            btn -> loadFromPath()
        ).bounds(lx, ly + 47, 160, 18).build();
        addRenderableWidget(loadPathBtn);

        // ③ サイズスライダー
        sizeSlider = new SizeSlider(lx, ly + 73, 160, 20);
        addRenderableWidget(sizeSlider);

        // ④ 設置ボタン
        placeBtn = Button.builder(
            Component.literal("⚒ ブロックを設置！"),
            btn -> sendPlacePacket()
        ).bounds(lx, ly + 100, 160, 20).build();
        placeBtn.active = false;
        addRenderableWidget(placeBtn);

        // ⑤ とじる
        addRenderableWidget(Button.builder(
            Component.literal("✕ とじる"),
            btn -> onClose()
        ).bounds(lx, ly + 126, 160, 20).build());
    }

    // =========================================================
    //  ファイル選択
    // =========================================================

    /** Swing JFileChooser を試みる。X11 がなければステータスに案内を表示 */
    private void tryOpenFileDialog() {
        CompletableFuture.runAsync(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                JFileChooser fc = new JFileChooser();
                fc.setDialogTitle("ドットアートにする画像を選んでね！");
                fc.setFileFilter(new FileNameExtensionFilter(
                    "画像ファイル (png, jpg, bmp, gif)", "png", "jpg", "jpeg", "bmp", "gif"));
                fc.setPreferredSize(new Dimension(600, 420));

                JFrame frame = new JFrame();
                frame.setAlwaysOnTop(true);
                frame.setVisible(true);
                frame.setLocationRelativeTo(null);
                int result = fc.showOpenDialog(frame);
                frame.dispose();

                if (result == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    Minecraft.getInstance().execute(() -> loadImage(file));
                }
            } catch (Exception e) {
                // X11 なし環境ではここに落ちる
                Minecraft.getInstance().execute(() ->
                    status = "§c画面を開けません。下のテキスト欄にパスを入力してね"
                );
            }
        });
    }

    /** テキスト欄のパスから読み込む（Dev Container 環境向け） */
    private void loadFromPath() {
        String path = pathBox.getValue().trim();
        if (path.isEmpty()) {
            status = "§cパスを入力してください";
            return;
        }
        File file = new File(path);
        if (!file.exists() || !file.isFile()) {
            status = "§cファイルが見つかりません: " + path;
            return;
        }
        loadImage(file);
    }

    // =========================================================
    //  画像読み込み
    // =========================================================
    private void loadImage(File file) {
        processing   = true;
        readyToPlace = false;
        placeBtn.active = false;
        previewRgb   = null;
        blockGrid    = null;
        status = "§e画像を読み込み中...";

        CompletableFuture.runAsync(() -> {
            try {
                BufferedImage img = ImageIO.read(file);
                if (img == null) throw new IllegalArgumentException("読み込めない画像形式です");

                // アルファを白で合成
                BufferedImage rgb = new BufferedImage(img.getWidth(), img.getHeight(),
                                                      BufferedImage.TYPE_INT_RGB);
                Graphics2D g2 = rgb.createGraphics();
                g2.setColor(Color.WHITE);
                g2.fillRect(0, 0, rgb.getWidth(), rgb.getHeight());
                g2.drawImage(img, 0, 0, null);
                g2.dispose();

                sourceImage = rgb;
                Minecraft.getInstance().execute(() -> {
                    status = "§a読み込み完了！変換中...";
                    convertImage();
                });
            } catch (Exception e) {
                Minecraft.getInstance().execute(() -> {
                    status = "§cエラー: " + e.getMessage();
                    processing = false;
                });
            }
        });
    }

    // =========================================================
    //  画像 → ブロックグリッド変換
    // =========================================================
    private void convertImage() {
        if (sourceImage == null) return;
        processing = true;
        status = "§e変換中... しばらく待ってね ✨";

        CompletableFuture.runAsync(() -> {
            final int size = artSize;

            // バイリニアリサイズ
            BufferedImage scaled = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = scaled.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                               RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(sourceImage, 0, 0, size, size, null);
            g.dispose();

            // ブロックマッチング
            BlockPalette.BlockColor[][] grid = new BlockPalette.BlockColor[size][size];
            int[][] rgb = new int[size][size];
            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    int c  = scaled.getRGB(x, y);
                    int r  = (c >> 16) & 0xFF;
                    int gv = (c >>  8) & 0xFF;
                    int b  = c & 0xFF;
                    BlockPalette.BlockColor bc = BlockPalette.nearest(r, gv, b);
                    grid[y][x] = bc;
                    rgb[y][x]  = (bc.r << 16) | (bc.g << 8) | bc.b;
                }
            }

            Minecraft.getInstance().execute(() -> {
                blockGrid    = grid;
                previewRgb   = rgb;
                processing   = false;
                readyToPlace = true;
                placeBtn.active = true;

                Set<String> kinds = new HashSet<>();
                for (int y = 0; y < size; y++)
                    for (int x = 0; x < size; x++)
                        kinds.add(grid[y][x].jaName);

                status = "§a変換完了！§r " + kinds.size()
                       + "種類 / " + (size*size) + "ブロック。§e「設置！」を押してね";
            });
        });
    }

    // =========================================================
    //  ブロック設置パケット送信
    // =========================================================
    private void sendPlacePacket() {
        if (blockGrid == null) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        int size = artSize;

        // プレイヤーの向きから設置開始位置を計算
        net.minecraft.core.BlockPos playerPos = mc.player.blockPosition();
        net.minecraft.core.Direction facing   = mc.player.getDirection();
        net.minecraft.core.Direction right     = facing.getClockWise();

        // プレイヤーの前3マス・中央揃え
        BlockPos origin = playerPos
                .relative(facing, 3)
                .relative(right, -(size / 2));

        List<BlockPos> positions = new ArrayList<>(size * size);
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                // blockGrid は左上が [0][0]、設置は下から上（y=0 が最下段）
                BlockPalette.BlockColor bc = blockGrid[size - 1 - y][x];
                BlockPos pos = origin.relative(right, x).above(y);

                // サーバー側でブロック種別を判定するため、ここでは座標のみ送る
                // ※ブロック種別はサーバー側のブロックパレットで変換が必要
                // → 今回は「各座標に対して手持ちブロックを置く」実装
                //   将来は (pos, blockId) のペアをパケットに含める拡張が望ましい
                positions.add(pos);
            }
        }

        // パケット送信（クライアント → サーバー）
        PacketDistributor.sendToServer(new PlaceBlocksPacket(positions));

        status = "§a§l送信完了！§r サーバーで " + positions.size() + "ブロックを設置中...";
        placeBtn.active = false;

        // 2 秒後に画面を閉じる
        CompletableFuture.runAsync(() -> {
            try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
            Minecraft.getInstance().execute(this::onClose);
        });
    }

    // =========================================================
    //  描画
    // =========================================================
    @Override
    public void render(GuiGraphics gg, int mouseX, int mouseY, float delta) {
        renderBackground(gg, mouseX, mouseY, delta);

        int cx = this.width / 2;

        // タイトル
        gg.drawCenteredString(font,
            Component.literal("§6§l🎮 ドットアートメーカー"), cx, 10, 0xFFFFFF);

        // サイズ
        gg.drawString(font,
            Component.literal("§7サイズ: §e" + artSize + "×" + artSize), 20, this.height/2 - 3, 0xFFFFFF);

        // ステータスメッセージ
        gg.drawString(font,
            Component.literal(status), 20, this.height/2 + 58, 0xFFFFFF);

        // ── プレビューエリア（中央）─────────────────────
        int px = 200;
        int py = this.height / 2 - PREVIEW_SIZE / 2;

        if (processing) {
            gg.fill(px, py, px + PREVIEW_SIZE, py + PREVIEW_SIZE, 0xFF111111);
            gg.drawCenteredString(font, Component.literal("§e変換中..."),
                px + PREVIEW_SIZE/2, py + PREVIEW_SIZE/2 - 4, 0xFFFFFF);

        } else if (previewRgb != null) {
            int pxSize = Math.max(1, PREVIEW_SIZE / artSize);
            for (int y = 0; y < artSize; y++) {
                for (int x = 0; x < artSize; x++) {
                    gg.fill(px + x*pxSize, py + y*pxSize,
                            px + x*pxSize + pxSize, py + y*pxSize + pxSize,
                            previewRgb[y][x] | 0xFF000000);
                }
            }
            gg.renderOutline(px-1, py-1, artSize*pxSize+2, artSize*pxSize+2, 0xFF888888);
            gg.drawString(font, Component.literal("§7プレビュー"), px, py-12, 0xFFFFFF);

            if (readyToPlace && blockGrid != null) {
                renderMaterialList(gg, px + artSize*pxSize + 10, py);
            }

        } else {
            gg.fill(px, py, px + PREVIEW_SIZE, py + PREVIEW_SIZE, 0xFF1A1A1A);
            gg.renderOutline(px, py, PREVIEW_SIZE, PREVIEW_SIZE, 0xFF444444);
            gg.drawCenteredString(font, Component.literal("§7ここにプレビューが出るよ"),
                px + PREVIEW_SIZE/2, py + PREVIEW_SIZE/2 - 4, 0xFFFFFF);
        }

        super.render(gg, mouseX, mouseY, delta);
    }

    /** ブロック素材リストを右側に描画 */
    private void renderMaterialList(GuiGraphics gg, int lx, int ly) {
        Map<String, int[]> map = new LinkedHashMap<>();
        for (int y = 0; y < artSize; y++) {
            for (int x = 0; x < artSize; x++) {
                BlockPalette.BlockColor bc = blockGrid[y][x];
                map.computeIfAbsent(bc.jaName, k -> new int[]{ bc.r, bc.g, bc.b, 0 })[3]++;
            }
        }
        List<Map.Entry<String, int[]>> sorted = new ArrayList<>(map.entrySet());
        sorted.sort((a, b) -> b.getValue()[3] - a.getValue()[3]);

        gg.drawString(font, Component.literal("§e必要なブロック:"), lx, ly, 0xFFFFFF);
        int shown = 0;
        for (Map.Entry<String, int[]> e : sorted) {
            if (shown >= 15) {
                gg.drawString(font,
                    Component.literal("§7... 他" + (sorted.size()-15) + "種類"),
                    lx, ly + 12 + shown*9, 0xFFFFFF);
                break;
            }
            int[] v = e.getValue();
            gg.fill(lx, ly+12+shown*9, lx+7, ly+19+shown*9,
                    0xFF000000 | (v[0]<<16) | (v[1]<<8) | v[2]);
            gg.drawString(font,
                Component.literal("§f" + e.getKey() + " §7×" + v[3]),
                lx+9, ly+12+shown*9, 0xFFFFFF);
            shown++;
        }
    }

    @Override public boolean shouldCloseOnEsc() { return true; }
    @Override public boolean isPauseScreen()    { return false; }

    // =========================================================
    //  サイズスライダー
    // =========================================================
    private class SizeSlider extends AbstractSliderButton {
        SizeSlider(int x, int y, int w, int h) {
            super(x, y, w, h, Component.literal("サイズ: 32×32"), 0.4);
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            int idx = Mth.clamp((int) Math.round(value * (SIZES.length-1)), 0, SIZES.length-1);
            artSize = SIZES[idx];
            setMessage(Component.literal("§rサイズ: §e" + artSize + "×" + artSize));
        }

        @Override
        protected void applyValue() {
            if (sourceImage != null && !processing) {
                status = "§eサイズ変更中...";
                convertImage();
            }
        }
    }
}
