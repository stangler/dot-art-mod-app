#!/bin/bash
# ──────────────────────────────────────────────────────────────
# post-create.sh  -  コンテナ初回作成後の自動セットアップ
# ──────────────────────────────────────────────────────────────
set -e

echo ""
echo "╔══════════════════════════════════════════════════════════╗"
echo "║       DotArt Mod - DevContainer Setup                   ║"
echo "╚══════════════════════════════════════════════════════════╝"
echo ""

# ── 1. Gradle Wrapper の生成 ───────────────────────────────
echo ">>> [1/4] Gradle Wrapper のセットアップ..."

if [ ! -f "./gradlew" ]; then
    echo "    gradlew が見つかりません。自動生成します..."
    gradle wrapper --gradle-version 8.8 --distribution-type bin
    echo "    gradlew を生成しました ✓"
else
    echo "    gradlew は既に存在します ✓"
fi

chmod +x ./gradlew 2>/dev/null || true

# ── 2. 依存関係のダウンロード ──────────────────────────────
echo ""
echo ">>> [2/4] NeoForge 依存関係のダウンロード (初回は数分かかります)..."
./gradlew dependencies --no-daemon --quiet 2>&1 | tail -5 || true
echo "    依存関係のダウンロード完了 ✓"

# ── 3. コンパイル確認 ─────────────────────────────────────
echo ""
echo ">>> [3/4] コンパイル確認..."
./gradlew --no-daemon compileJava --quiet 2>&1 | tail -5 || {
    echo "    ⚠ コンパイルエラーがあります。VS Code でソースを確認してください。"
}
echo "    コンパイル確認完了 ✓"

# ── 4. Git 初期化 ─────────────────────────────────────────
echo ""
echo ">>> [4/4] Git の初期設定..."
if [ ! -d ".git" ]; then
    git init
    git add .
    git commit -m "feat: Initial commit - DotArt Mod"
    echo "    Git リポジトリを初期化しました ✓"
else
    echo "    Git リポジトリは既に存在します ✓"
fi

# ── 完了メッセージ ────────────────────────────────────────
echo ""
echo "╔══════════════════════════════════════════════════════════╗"
echo "║     セットアップ完了！                                    ║"
echo "╠══════════════════════════════════════════════════════════╣"
echo "║                                                          ║"
echo "║  よく使うコマンド:                                        ║"
echo "║    ./gradlew runClient   → クライアント起動              ║"
echo "║    ./gradlew runServer   → サーバー起動                  ║"
echo "║    ./gradlew build       → JAR ビルド                    ║"
echo "║    ./gradlew compileJava → コンパイルのみ                 ║"
echo "║                                                          ║"
echo "║  生成される JAR:                                          ║"
echo "║    build/libs/dotartmod-1.0.0.jar                        ║"
echo "║                                                          ║"
echo "╚══════════════════════════════════════════════════════════╝"
echo ""
