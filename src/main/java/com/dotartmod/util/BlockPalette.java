package com.dotartmod.util;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;

/**
 * マイクラブロックと RGB 色のマッピング。
 * 加重ユークリッド距離で最近傍ブロックを探す。
 * ( 重み: 2R² + 4G² + 3B² — 人間の視覚感度に対応 )
 */
public class BlockPalette {

    public static final class BlockColor {
        public final Block  block;
        public final String jaName;
        public final int    r, g, b;

        public BlockColor(Block block, String jaName, int r, int g, int b) {
            this.block  = block;
            this.jaName = jaName;
            this.r = r; this.g = g; this.b = b;
        }
    }

    private static final List<BlockColor> PALETTE = new ArrayList<>();

    static {
        // ── コンクリート 16色 ──────────────────────────
        add(Blocks.WHITE_CONCRETE,        "白コンクリート",        207, 213, 214);
        add(Blocks.LIGHT_GRAY_CONCRETE,   "薄灰コンクリート",      125, 125, 115);
        add(Blocks.GRAY_CONCRETE,         "灰コンクリート",         54,  57,  61);
        add(Blocks.BLACK_CONCRETE,        "黒コンクリート",          8,  10,  15);
        add(Blocks.BROWN_CONCRETE,        "茶コンクリート",         96,  59,  31);
        add(Blocks.RED_CONCRETE,          "赤コンクリート",        142,  32,  32);
        add(Blocks.ORANGE_CONCRETE,       "橙コンクリート",        224,  97,   0);
        add(Blocks.YELLOW_CONCRETE,       "黄コンクリート",        243, 175,  21);
        add(Blocks.LIME_CONCRETE,         "黄緑コンクリート",       94, 168,  24);
        add(Blocks.GREEN_CONCRETE,        "緑コンクリート",         73,  91,  36);
        add(Blocks.CYAN_CONCRETE,         "水色コンクリート",       21, 120, 136);
        add(Blocks.LIGHT_BLUE_CONCRETE,   "空色コンクリート",       35, 137, 187);
        add(Blocks.BLUE_CONCRETE,         "青コンクリート",         44,  46, 143);
        add(Blocks.PURPLE_CONCRETE,       "紫コンクリート",        100,  31, 156);
        add(Blocks.MAGENTA_CONCRETE,      "マゼンタコンクリート",  169,  48, 159);
        add(Blocks.PINK_CONCRETE,         "ピンクコンクリート",    213, 101, 142);

        // ── ウール 16色 ────────────────────────────────
        add(Blocks.WHITE_WOOL,            "白ウール",              233, 236, 236);
        add(Blocks.LIGHT_GRAY_WOOL,       "薄灰ウール",            142, 142, 134);
        add(Blocks.GRAY_WOOL,             "灰ウール",               62,  68,  71);
        add(Blocks.BLACK_WOOL,            "黒ウール",               20,  21,  25);
        add(Blocks.BROWN_WOOL,            "茶ウール",              114,  71,  40);
        add(Blocks.RED_WOOL,              "赤ウール",              161,  39,  34);
        add(Blocks.ORANGE_WOOL,           "橙ウール",              240, 118,  19);
        add(Blocks.YELLOW_WOOL,           "黄ウール",              248, 198,  39);
        add(Blocks.LIME_WOOL,             "黄緑ウール",            112, 185,  25);
        add(Blocks.GREEN_WOOL,            "緑ウール",               84, 109,  27);
        add(Blocks.CYAN_WOOL,             "水色ウール",             21, 137, 145);
        add(Blocks.LIGHT_BLUE_WOOL,       "空色ウール",             58, 175, 217);
        add(Blocks.BLUE_WOOL,             "青ウール",               53,  57, 157);
        add(Blocks.PURPLE_WOOL,           "紫ウール",              121,  42, 172);
        add(Blocks.MAGENTA_WOOL,          "マゼンタウール",        189,  68, 179);
        add(Blocks.PINK_WOOL,             "ピンクウール",          237, 141, 172);

        // ── テラコッタ 17色 ────────────────────────────
        add(Blocks.TERRACOTTA,            "テラコッタ",            152,  94,  67);
        add(Blocks.WHITE_TERRACOTTA,      "白テラコッタ",          209, 178, 161);
        add(Blocks.RED_TERRACOTTA,        "赤テラコッタ",          143,  61,  47);
        add(Blocks.ORANGE_TERRACOTTA,     "橙テラコッタ",          162,  84,  38);
        add(Blocks.YELLOW_TERRACOTTA,     "黄テラコッタ",          186, 133,  36);
        add(Blocks.GREEN_TERRACOTTA,      "緑テラコッタ",           76,  83,  42);
        add(Blocks.BLUE_TERRACOTTA,       "青テラコッタ",           74,  60,  91);
        add(Blocks.BROWN_TERRACOTTA,      "茶テラコッタ",          102,  66,  44);
        add(Blocks.CYAN_TERRACOTTA,       "水テラコッタ",           86,  91,  91);
        add(Blocks.PURPLE_TERRACOTTA,     "紫テラコッタ",          118,  70,  86);
        add(Blocks.MAGENTA_TERRACOTTA,    "マゼンタテラコッタ",    149,  88, 108);
        add(Blocks.PINK_TERRACOTTA,       "ピンクテラコッタ",      161,  78,  78);
        add(Blocks.LIGHT_BLUE_TERRACOTTA, "空色テラコッタ",        113, 108, 137);
        add(Blocks.LIME_TERRACOTTA,       "黄緑テラコッタ",        103, 117,  52);
        add(Blocks.LIGHT_GRAY_TERRACOTTA, "薄灰テラコッタ",        135, 106,  97);
        add(Blocks.GRAY_TERRACOTTA,       "灰テラコッタ",           57,  42,  35);
        add(Blocks.BLACK_TERRACOTTA,      "黒テラコッタ",           37,  22,  16);

        // ── 自然・建築ブロック ─────────────────────────
        add(Blocks.GRASS_BLOCK,           "草ブロック",             93, 156,  42);
        add(Blocks.DIRT,                  "土",                    134,  96,  67);
        add(Blocks.SAND,                  "砂",                    219, 207, 163);
        add(Blocks.SANDSTONE,             "砂岩",                  220, 199, 134);
        add(Blocks.RED_SAND,              "赤い砂",                174,  97,  31);
        add(Blocks.STONE,                 "石",                    125, 125, 125);
        add(Blocks.COBBLESTONE,           "丸石",                  115, 115, 115);
        add(Blocks.DEEPSLATE,             "深石板岩",               76,  77,  87);
        add(Blocks.CALCITE,               "方解石",                221, 221, 215);
        add(Blocks.TUFF,                  "凝灰岩",                108, 108,  95);
        add(Blocks.SNOW_BLOCK,            "雪ブロック",            242, 243, 243);
        add(Blocks.ICE,                   "氷",                    145, 183, 228);
        add(Blocks.PACKED_ICE,            "青氷",                  138, 176, 228);
        add(Blocks.NETHERRACK,            "ネザーラック",           97,  36,  36);
        add(Blocks.NETHER_BRICKS,         "ネザーレンガ",          114,  63,  55);
        add(Blocks.GLOWSTONE,             "グロウストーン",        217, 174, 107);
        add(Blocks.END_STONE,             "エンドストーン",        219, 219, 172);
        add(Blocks.OBSIDIAN,              "黒曜石",                 21,  17,  26);
        add(Blocks.OAK_LOG,               "オーク原木",             92,  73,  47);
        add(Blocks.OAK_PLANKS,            "オーク板",              162, 131,  78);
        add(Blocks.DARK_OAK_PLANKS,       "ダークオーク板",         78,  55,  30);
        add(Blocks.BIRCH_PLANKS,          "シラカバ板",            196, 179, 123);
        add(Blocks.PUMPKIN,               "カボチャ",              197, 103,  17);
        add(Blocks.MELON,                 "スイカ",                113, 151,  44);
        add(Blocks.HAY_BLOCK,             "干し草",                172, 153,  26);
        add(Blocks.SPONGE,                "スポンジ",              182, 182,  55);
        add(Blocks.MUD,                   "泥",                     60,  57,  55);
        add(Blocks.PACKED_MUD,            "固めた泥",              105,  83,  64);
        add(Blocks.MUD_BRICKS,            "泥レンガ",              138, 103,  82);

        // ── 鉱石ブロック ───────────────────────────────
        add(Blocks.GOLD_BLOCK,            "金ブロック",            249, 236,  79);
        add(Blocks.IRON_BLOCK,            "鉄ブロック",            220, 220, 220);
        add(Blocks.EMERALD_BLOCK,         "エメラルドブロック",     42, 179,  93);
        add(Blocks.DIAMOND_BLOCK,         "ダイヤブロック",         93, 224, 228);
        add(Blocks.LAPIS_BLOCK,           "ラピスブロック",         30,  58, 146);
        add(Blocks.REDSTONE_BLOCK,        "レッドストーンブロック",163,  18,   9);
        add(Blocks.COPPER_BLOCK,          "銅ブロック",            183, 103,  72);
        add(Blocks.AMETHYST_BLOCK,        "アメジストブロック",    100,  72, 155);
        add(Blocks.NETHERITE_BLOCK,       "ネザライトブロック",     68,  58,  61);
        add(Blocks.COAL_BLOCK,            "石炭ブロック",           25,  25,  25);
    }

    private static void add(Block block, String jaName, int r, int g, int b) {
        PALETTE.add(new BlockColor(block, jaName, r, g, b));
    }

    /** RGB に最も近いブロックを返す（加重ユークリッド距離） */
    public static BlockColor nearest(int r, int g, int b) {
        BlockColor best = PALETTE.get(0);
        long bestDist = Long.MAX_VALUE;
        for (BlockColor bc : PALETTE) {
            long dr = r - bc.r, dg = g - bc.g, db = b - bc.b;
            long d  = 2*dr*dr + 4*dg*dg + 3*db*db;
            if (d < bestDist) { bestDist = d; best = bc; }
        }
        return best;
    }

    public static List<BlockColor> getPalette() { return PALETTE; }
}
