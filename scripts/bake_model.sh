#!/usr/bin/env bash
# Bake a glTF/GLB model into a transparent, looping animated WebP (+ optional VP9 WebM).
# Output goes next to the web app's public/models so components can <img> it.
#
# Usage:
#   scripts/bake_model.sh <model.gltf|glb> <name> [frames] [fps]
# Example:
#   scripts/bake_model.sh \
#     services/web/public/models/vietnamese___non_la/scene.gltf non-la 120 30
#
# Requires: blender, and img2webp (from the webp package). ffmpeg optional (WebM).

set -euo pipefail

MODEL="${1:?model path required}"
NAME="${2:?output name required}"
FRAMES="${3:-120}"
FPS="${4:-30}"

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
OUT_DIR="$ROOT/services/web/public/models"
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT

command -v blender >/dev/null || { echo "ERROR: blender not installed"; exit 1; }

echo "[bake] rendering $FRAMES transparent frames (Cycles CPU)..."
blender --background --python "$ROOT/scripts/bake_turntable.py" -- "$MODEL" "$TMP" "$FRAMES"

DELAY_MS=$((1000 / FPS))

echo "[bake] encoding animated WebP -> $OUT_DIR/$NAME.webp"
if command -v img2webp >/dev/null; then
  img2webp -loop 0 -d "$DELAY_MS" -q 75 "$TMP"/frame_*.png -o "$OUT_DIR/$NAME.webp"
elif command -v ffmpeg >/dev/null; then
  ffmpeg -y -framerate "$FPS" -i "$TMP/frame_%04d.png" \
    -c:v libwebp -lossless 0 -q:v 75 -loop 0 -pix_fmt yuva420p "$OUT_DIR/$NAME.webp"
else
  echo "ERROR: need img2webp (Arch: libwebp) or ffmpeg to encode WebP"; exit 1
fi

# Static poster (first frame) for the <img> while WebP loads / as alt.
cp "$TMP/frame_0001.png" "$OUT_DIR/$NAME.png"

# Optional smaller VP9-alpha WebM if ffmpeg is available.
if command -v ffmpeg >/dev/null; then
  echo "[bake] encoding VP9-alpha WebM -> $OUT_DIR/$NAME.webm"
  ffmpeg -y -framerate "$FPS" -i "$TMP/frame_%04d.png" \
    -c:v libvpx-vp9 -pix_fmt yuva420p -b:v 0 -crf 30 "$OUT_DIR/$NAME.webm"
fi

echo "[bake] done: $OUT_DIR/$NAME.webp (+ .png, .webm if encoded)"
