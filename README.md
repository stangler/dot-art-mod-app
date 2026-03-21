# 🎮 DotArt Mod
### Minecraft Java Edition 1.21.1 × NeoForge 21.1.172

画像ファイルを選ぶだけで、マイクラの世界に**自動でブロックドットアートを作成**できるMod！
**サバイバルモード・マルチサーバーにも対応。**

---

## 📋 必要なもの

| ツール | バージョン |
|--------|-----------|
| Java   | 21 以上   |
| Minecraft Java Edition | 1.21.1 |
| NeoForge | 21.1.172 |
| Docker + VS Code Dev Containers | 最新版 |

---

## 🚀 開発環境のセットアップ

### 1. VS Code でフォルダを開く

```bash
code dotart-mod/
```

VS Code が「Dev Container で開きますか？」と表示したら
**「Reopen in Container」** をクリック。

> 初回は Docker イメージのビルドと依存関係のダウンロードで **10〜20分** かかります。

### 2. ビルドする

```bash
# JAR を生成
./gradlew build
# → build/libs/dotartmod-1.0.0.jar

# Minecraft クライアントを起動（動作確認）
./gradlew runClient

# コンパイルのみ
./gradlew compileJava
```

---

## 📦 プレイヤー向けインストール

1. [NeoForge 21.1.172](https://neoforged.net/) をインストール
2. `dotartmod-1.0.0.jar` を `.minecraft/mods/` フォルダへ
3. NeoForge プロファイルで Minecraft を起動

---

## 🪄 遊び方

### クラフト

```
[ ][ ][金インゴット]
[ ][棒][  ]
[棒][  ][  ]
```

または `/give @s dotartmod:dot_art_wand` で入手。

### 使い方（3ステップ）

1. 杖を手に持って **右クリック**
2. 画像ファイルを選ぶ
   - **「📂 画像ファイルを選ぶ」** → OS のファイルダイアログ
   - **Dev Container / X11 なし環境** → 下のテキスト欄にパスを入力
     ```
     /home/mcdev/images/pikachu.png
     ```
3. サイズを選んで **「⚒ ブロックを設置！」** → 自動でブロックが置かれる！

### サバイバルモードでの注意

- 手持ちのブロックが消費されます
- 設置するブロック数分のアイテムをインベントリに入れておいてください
- クリエイティブモードでは消費されません

---

## 🎨 使用ブロック（約75種類）

| カテゴリ | 種類 | 特徴 |
|---------|-----|------|
| コンクリート | 16色 | 鮮やか・一番きれいに仕上がる |
| ウール | 16色 | 柔らかいトーン |
| テラコッタ | 17色 | 落ち着いたアースカラー |
| 自然・建築ブロック | 約20種 | 草・砂・石・氷など |
| 鉱石ブロック | 約10種 | 金・ダイヤ・エメラルドなど |

色変換は **加重ユークリッド距離**（`2R² + 4G² + 3B²`）を使用。
人間の視覚感度に合わせた重みで、より自然な仕上がりになります。

---

## ⚙️ 設計・仕様

### ネットワークアーキテクチャ

```
クライアント (DotArtScreen)
  ↓ 画像をリサイズ・ブロック変換
  ↓ BlockPos のリストを生成
  ↓ PlaceBlocksPacket を送信
サーバー (ServerPlaceHandler)
  ↓ プレイヤーの手持ちアイテムを確認
  ↓ サバイバルならアイテム消費
  ↓ level.setBlock() で設置
```

### 設置位置

- プレイヤーが向いている方向の **前3マス** から開始
- 左右に **中央揃え** で展開
- 下から上へ積み上げる（画像の下端 = 設置の最下段）

### サイズ選択

| サイズ | ブロック数 |
|--------|----------|
| 16×16  | 256      |
| 24×24  | 576      |
| 32×32  | 1,024    |
| 48×48  | 2,304    |
| 64×64  | 4,096    |

---

## 📁 ソースコード構成

```
dotart-mod/
├── .devcontainer/
│   ├── devcontainer.json    ← Dev Container 設定
│   ├── Dockerfile           ← eclipse-temurin:21-jdk-jammy + Gradle 8.8
│   └── post-create.sh       ← 初回セットアップスクリプト
│
├── src/main/java/com/dotartmod/
│   ├── DotArtMod.java               ← @Mod エントリポイント
│   ├── item/
│   │   ├── ModItems.java            ← DeferredRegister でアイテム登録
│   │   └── DotArtWandItem.java      ← 杖アイテム
│   ├── network/
│   │   ├── ModNetwork.java          ← @EventBusSubscriber でパケット登録
│   │   └── PlaceBlocksPacket.java   ← クライアント→サーバー 設置パケット
│   ├── server/
│   │   └── ServerPlaceHandler.java  ← サーバー側ブロック設置ロジック
│   ├── util/
│   │   └── BlockPalette.java        ← 約75種ブロックの RGB 色テーブル
│   └── client/screen/
│       └── DotArtScreen.java        ← GUI・画像変換・パケット送信
│
├── src/main/resources/
│   ├── META-INF/neoforge.mods.toml
│   ├── pack.mcmeta
│   ├── assets/dotartmod/
│   │   ├── lang/ja_jp.json
│   │   ├── lang/en_us.json
│   │   ├── models/item/dot_art_wand.json
│   │   └── textures/item/dot_art_wand.png
│   └── data/dotartmod/recipes/
│       └── dot_art_wand.json
│
├── build.gradle          ← net.neoforged.moddev 2.0.78
├── gradle.properties
├── settings.gradle
├── gradlew / gradlew.bat
└── README.md
```

---

## 🛠️ トラブルシューティング

**Q: `./gradlew build` でエラーが出る**
```bash
./gradlew --stop        # Gradle デーモンを停止
./gradlew build         # 再実行
```

**Q: ファイルダイアログが開かない（Dev Container）**
→ X11 がないため正常な挙動です。画面下のテキスト欄にファイルの**フルパス**を入力してください。

**Q: ブロックが置かれない（サバイバル）**
→ 手持ちに置きたいブロックを持っているか確認してください。

**Q: runClient が失敗する**
→ Dev Container 内では GUI を表示できません。ビルドした JAR をローカル環境の mods フォルダに入れて起動してください。

---

## 🔮 今後の予定（TODO）

- [ ] ブロック種別をパケットに含めてサーバー側で正確に配置
- [ ] Undo 機能（設置したブロックを元に戻す）
- [ ] 設置進捗バーの表示
- [ ] `/dotart <ファイルパス>` コマンド対応
- [ ] ディザリングオプション（色精度向上）

---

## 📄 ライセンス

MIT License — 自由に改造・再配布 OK！
