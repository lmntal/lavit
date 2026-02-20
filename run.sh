#!/bin/sh
set -eu

cd "$(dirname "$0")"

# --- 画面サイズを自動取得（XWaylandがある前提: DISPLAY があればOK） ---
if [ -n "${DISPLAY:-}" ] && command -v xrandr >/dev/null 2>&1; then
  RES="$(xrandr 2>/dev/null | awk '/\*/{print $1; exit}')"
elif [ -n "${DISPLAY:-}" ] && command -v xdpyinfo >/dev/null 2>&1; then
  RES="$(xdpyinfo 2>/dev/null | awk '/dimensions:/{print $2; exit}')"
else
  RES="1920x1080"
fi

W="${RES%x*}"
H="${RES#*x}"

# --- ★追加：丸め誤差・装飾切替のための安全マージン ---
# 右端が見切れるのを避ける（まずは 16px 推奨。まだ切れるなら 32 に）
MARGIN_X="${MARGIN_X:-16}"
MARGIN_Y="${MARGIN_Y:-16}"

W=$((W - MARGIN_X))
H=$((H - MARGIN_Y))

# 下限（念のため）
[ "$W" -lt 800 ] && W=800
[ "$H" -lt 600 ] && H=600

# --- env.txt をテンプレから生成して WINDOW_* だけ上書き ---
TEMPLATE="env.startup.txt"
if [ -f "$TEMPLATE" ]; then
  cp "$TEMPLATE" env.txt
else
  : > env.txt
fi

grep -v '^WINDOW_' env.txt > env.txt.tmp || true
cat > env.txt <<EOF
$(cat env.txt.tmp)
WINDOW_STATE=normal
WINDOW_X=0
WINDOW_Y=0
WINDOW_WIDTH=$W
WINDOW_HEIGHT=$H
EOF
rm -f env.txt.tmp

# --- Wayland + Swing 対策 ---
JAVA_FLAGS="-Dsun.java2d.opengl=false"
# 分数スケーリング絡みが濃厚なら、環境変数で固定できるようにしておく
# 例: UISCALE=1 ./run.sh
if [ -n "${UISCALE:-}" ]; then
  JAVA_FLAGS="$JAVA_FLAGS -Dsun.java2d.uiScale=${UISCALE}"
fi

exec java $JAVA_FLAGS -jar LaViT.jar