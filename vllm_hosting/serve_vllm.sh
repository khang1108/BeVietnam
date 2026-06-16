#!/usr/bin/env bash
# Launch the vLLM OpenAI-compatible API server from .env config.
# Run standalone (after setup.sh has built the venv) or via setup.sh.
#
#   bash serve_vllm.sh
#
# Serves: http://$VLLM_HOST:$VLLM_PORT/v1/{chat/completions,completions,models}

set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT"

# --- load config ----------------------------------------------------------
if [[ ! -f "$ROOT/.env" ]]; then
  echo "[serve] ERROR: .env not found. Copy .env.example → .env first." >&2
  exit 1
fi
set -a
# shellcheck disable=SC1091
source "$ROOT/.env"
set +a

# --- activate venv --------------------------------------------------------
if [[ -f "$ROOT/.venv/bin/activate" ]]; then
  # shellcheck disable=SC1091
  source "$ROOT/.venv/bin/activate"
else
  echo "[serve] ERROR: .venv missing. Run setup.sh first." >&2
  exit 1
fi

export PYTHONNOUSERSITE=1
[[ -n "${HF_TOKEN:-}" ]] && export HF_TOKEN HUGGING_FACE_HUB_TOKEN="$HF_TOKEN"

# --- build args -----------------------------------------------------------
args=(
  "$VLLM_MODEL"
  --served-model-name "${VLLM_SERVED_MODEL_NAME:-$VLLM_MODEL}"
  --host "${VLLM_HOST:-127.0.0.1}"
  --port "${VLLM_PORT:-8000}"
  --dtype "${VLLM_DTYPE:-bfloat16}"
  --max-model-len "${VLLM_MAX_MODEL_LEN:-16384}"
  --gpu-memory-utilization "${VLLM_GPU_MEMORY_UTILIZATION:-0.92}"
)
[[ "${VLLM_ENABLE_PREFIX_CACHING:-0}" == "1" ]] && args+=(--enable-prefix-caching)
# Only enforce auth when a key is set; empty key = open endpoint.
[[ -n "${VLLM_API_KEY:-}" ]] && args+=(--api-key "$VLLM_API_KEY")

echo "[serve] vllm serve ${VLLM_MODEL} on ${VLLM_HOST:-127.0.0.1}:${VLLM_PORT:-8000}"
echo "[serve] auth: $([[ -n "${VLLM_API_KEY:-}" ]] && echo 'API key required' || echo 'OPEN')"
exec vllm serve "${args[@]}"
