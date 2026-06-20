#!/usr/bin/env bash
# Launch the vLLM backends (text + vision + optional embeddings) from .env.
# Each runs as its own process, pinned to a GPU, with a capped memory fraction
# so several fit on one L40. nginx (serve_router.sh) fronts them; cloudflared
# exposes only the router.
#
#   bash serve_models.sh            # start all enabled backends, wait for health
#   bash serve_models.sh --no-wait  # start and return immediately
#
# Logs: logs/text.log, logs/vision.log, logs/embed.log

set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT"

[[ -f "$ROOT/.env" ]] || { echo "[serve] ERROR: .env not found. Copy .env.example → .env." >&2; exit 1; }
set -a; source "$ROOT/.env"; set +a

[[ -f "$ROOT/.venv/bin/activate" ]] || { echo "[serve] ERROR: .venv missing. Run bootstrap.sh." >&2; exit 1; }
source "$ROOT/.venv/bin/activate"
export PYTHONNOUSERSITE=1
[[ -n "${HF_TOKEN:-}" ]] && export HF_TOKEN HUGGING_FACE_HUB_TOKEN="$HF_TOKEN"

mkdir -p "$ROOT/logs"
BIND="${BIND_HOST:-127.0.0.1}"
WAIT=1
[[ "${1:-}" == "--no-wait" ]] && WAIT=0

# launch_backend <name> <gpu> <model> <served> <port> <util> <maxlen> <dtype> [extra args...]
launch_backend() {
  local name=$1 gpu=$2 model=$3 served=$4 port=$5 util=$6 maxlen=$7 dtype=$8; shift 8
  local args=(
    "$model"
    --served-model-name "$served"
    --host "$BIND" --port "$port"
    --dtype "$dtype"
    --max-model-len "$maxlen"
    --gpu-memory-utilization "$util"
  )
  [[ "${VLLM_ENABLE_PREFIX_CACHING:-0}" == "1" ]] && args+=(--enable-prefix-caching)
  [[ -n "${VLLM_API_KEY:-}" ]] && args+=(--api-key "$VLLM_API_KEY")
  args+=("$@")

  echo "[serve] $name: GPU$gpu util=$util → $BIND:$port ($model)"
  CUDA_VISIBLE_DEVICES="$gpu" setsid vllm serve "${args[@]}" \
    >> "$ROOT/logs/$name.log" 2>&1 < /dev/null &
  echo "[serve]   $name PID $!  (logs/$name.log)"
}

wait_health() {
  local name=$1 port=$2
  echo "[serve] waiting for $name /health on $BIND:$port (model download can be slow)..."
  for i in $(seq 1 180); do
    curl -fsS "http://$BIND:$port/health" >/dev/null 2>&1 && { echo "[serve] $name healthy."; return 0; }
    [[ $((i % 12)) -eq 0 ]] && echo "[serve]   $name still loading (${i}0s)... tail logs/$name.log"
    sleep 10
  done
  echo "[serve] WARNING: $name not healthy after 30m — check logs/$name.log" >&2
}

# ── text ──────────────────────────────────────────────────────────────────────
launch_backend text "$TEXT_GPU" "$TEXT_MODEL" "$TEXT_SERVED_NAME" \
  "$TEXT_PORT" "$TEXT_GPU_UTIL" "$TEXT_MAX_LEN" "${TEXT_DTYPE:-auto}"

# ── vision (multimodal) ───────────────────────────────────────────────────────
launch_backend vision "$VISION_GPU" "$VISION_MODEL" "$VISION_SERVED_NAME" \
  "$VISION_PORT" "$VISION_GPU_UTIL" "$VISION_MAX_LEN" "${VISION_DTYPE:-bfloat16}" \
  --limit-mm-per-prompt image=2

# ── embeddings (optional) ─────────────────────────────────────────────────────
if [[ "${EMBED_ENABLED:-1}" == "1" ]]; then
  launch_backend embed "$EMBED_GPU" "$EMBED_MODEL" "$EMBED_SERVED_NAME" \
    "$EMBED_PORT" "$EMBED_GPU_UTIL" 512 float16 --task embed
fi

if [[ "$WAIT" -eq 1 ]]; then
  wait_health text "$TEXT_PORT"
  wait_health vision "$VISION_PORT"
  [[ "${EMBED_ENABLED:-1}" == "1" ]] && wait_health embed "$EMBED_PORT"
  echo "[serve] all backends up."
fi
