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

# Animated WebP only via img2webp — ffmpeg's libwebp writes frames without the
# ANIM/ANMF container, producing a non-animated (broken) WebP. The WebM below is
# the reliable transparent loop; WebP is an optional extra.
if command -v img2webp >/dev/null; then
  echo "[bake] encoding animated WebP -> $OUT_DIR/$NAME.webp"
  img2webp -loop 0 -d "$DELAY_MS" -q 75 "$TMP"/frame_*.png -o "$OUT_DIR/$NAME.webp"
else
  echo "[bake] skipping WebP (img2webp not found; Arch: 'libwebp'). Using WebM only."
fi

# Static poster (first frame) shown before the loop plays.
cp "$TMP/frame_0001.png" "$OUT_DIR/$NAME.png"

# Primary transparent loop: VP9-alpha WebM (the <video> source).
command -v ffmpeg >/dev/null || { echo "ERROR: ffmpeg required for the WebM loop"; exit 1; }
echo "[bake] encoding VP9-alpha WebM -> $OUT_DIR/$NAME.webm"
ffmpeg -y -framerate "$FPS" -i "$TMP/frame_%04d.png" \
  -c:v libvpx-vp9 -pix_fmt yuva420p -b:v 0 -crf 30 "$OUT_DIR/$NAME.webm"

echo "[bake] done: $OUT_DIR/$NAME.webm (+ .png poster, .webp if img2webp present)"
