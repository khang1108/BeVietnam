#!/usr/bin/env bash
# ──────────────────────────────────────────────────────────────────────────────
# All-in-one bootstrap for the BeVietnam vLLM host.
#
# Run this ONCE on a fresh GPU VM right after cloning the repo:
#
#     cd BeVietnam/vllm_hosting
#     cp .env.example .env          # then fill in the 3 secrets (see below)
#     bash bootstrap.sh
#
# It installs everything from cloudflared to vLLM and brings the API up at
# https://$CF_HOSTNAME:
#   1. Python venv + vLLM (+ fastapi pin + huggingface_hub)
#   2. L40 / Thundercompute environment fixes (libcuda symlink, flashinfer removal)
#   3. cloudflared
#   4. launches the vLLM server, waits for /health, then starts the tunnel
#
# SECRETS: this script contains NONE. All config + credentials are read from
# ./.env (gitignored). The cloudflared credential file is written at runtime
# from .env into ~/.cloudflared — never into the repo. Safe to commit.
#
# Flags:
#   --no-launch   install + configure only; don't start vLLM or the tunnel
# ──────────────────────────────────────────────────────────────────────────────
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT"
export PYTHONNOUSERSITE=1

LAUNCH=1
[[ "${1:-}" == "--no-launch" ]] && LAUNCH=0

# ── 0. config / .env ──────────────────────────────────────────────────────────
# Fresh clones have no .env (it is gitignored). Create it from the template and
# stop so the operator can fill in the secrets before anything is launched.
if [[ ! -f "$ROOT/.env" ]]; then
  cp "$ROOT/.env.example" "$ROOT/.env"
  cat >&2 <<'MSG'
[bootstrap] No .env found — created one from .env.example.
[bootstrap] Fill in these before re-running `bash bootstrap.sh`:
[bootstrap]     HF_TOKEN          (huggingface.co/settings/tokens)
[bootstrap]     CF_ACCOUNT_TAG    (Cloudflare account tag for the "vllm" tunnel)
[bootstrap]     CF_TUNNEL_SECRET  (the tunnel's credential secret)
[bootstrap] CF_TUNNEL_ID / CF_HOSTNAME already point at the BeVietnam tunnel.
MSG
  exit 1
fi

set -a
# shellcheck disable=SC1091
source "$ROOT/.env"
set +a

# Fail fast if the cloudflared credentials are still placeholders.
if [[ "${CF_TUNNEL_SECRET:-}" == xxxx* || -z "${CF_TUNNEL_SECRET:-}" \
      || "${CF_ACCOUNT_TAG:-}" == xxxx* || -z "${CF_ACCOUNT_TAG:-}" ]]; then
  echo "[bootstrap] ERROR: CF_ACCOUNT_TAG / CF_TUNNEL_SECRET not set in .env." >&2
  exit 1
fi

mkdir -p "$ROOT/logs"

# ── 1. venv ───────────────────────────────────────────────────────────────────
if command -v python3.12 >/dev/null 2>&1; then PYTHON=python3.12; else PYTHON=python3; fi
echo "[bootstrap] interpreter: $("$PYTHON" --version) ($PYTHON)"
if [[ ! -f "$ROOT/.venv/bin/activate" ]]; then
  [[ -e "$ROOT/.venv" ]] && { echo "[bootstrap] removing broken .venv"; rm -rf "$ROOT/.venv"; }
  echo "[bootstrap] creating venv: .venv"
  "$PYTHON" -m venv "$ROOT/.venv"
fi
# shellcheck disable=SC1091
source "$ROOT/.venv/bin/activate"
echo "[bootstrap] activated: $(python --version) at $(command -v python)"
python -m pip install --upgrade pip setuptools wheel

# ── 2. install vLLM ───────────────────────────────────────────────────────────
echo "[bootstrap] installing vllm==${VLLM_VERSION:-0.8.5}"
python -m pip install --no-cache-dir "vllm==${VLLM_VERSION:-0.8.5}"
# vLLM 0.8.5 breaks with fastapi>=0.137 (prometheus instrumentator routing).
echo "[bootstrap] pinning fastapi<0.137 for vLLM 0.8.5 compatibility"
python -m pip install --no-cache-dir "fastapi<0.137"
# vLLM 0.8.5 calls tokenizer.all_special_tokens_extended, removed in transformers
# >=4.53 → "Qwen2Tokenizer has no attribute all_special_tokens_extended". Pin back.
echo "[bootstrap] pinning transformers==4.51.3 for vLLM 0.8.5 compatibility"
python -m pip install --no-cache-dir "transformers==4.51.3"
python -m pip install --no-cache-dir "huggingface_hub[cli]"
# Qwen2.5-VL image preprocessing (vision backend).
echo "[bootstrap] installing qwen-vl-utils for the vision model"
python -m pip install --no-cache-dir "qwen-vl-utils"

# ── 3. L40 / Thundercompute environment fixes ─────────────────────────────────
echo "[bootstrap] running ldconfig"
sudo /sbin/ldconfig || true

echo "[bootstrap] ensuring libcuda.so symlink for Triton JIT"
LIBCUDA_TARGET=""
for candidate in \
    /usr/lib/x86_64-linux-gnu/libcuda.so.1 \
    /usr/local/cuda/lib64/libcuda.so.1 \
    /usr/local/cuda/lib64/stubs/libcuda.so; do
  [[ -f "$candidate" ]] && { LIBCUDA_TARGET="$candidate"; break; }
done
if [[ -n "$LIBCUDA_TARGET" ]]; then
  sudo ln -sf "$LIBCUDA_TARGET" /usr/lib/x86_64-linux-gnu/libcuda.so
  echo "[bootstrap] libcuda.so -> $LIBCUDA_TARGET"
else
  echo "[bootstrap] WARNING: libcuda.so not found; Triton JIT may fail on first inference"
fi

echo "[bootstrap] removing flashinfer / tvm-ffi (Torch ABI mismatch crashes vLLM)"
python -m pip uninstall -y \
  flashinfer flashinfer-python tvm-ffi apache-tvm-ffi torch-c-dlpack-ext || true

# ── 4. cloudflared ────────────────────────────────────────────────────────────
echo "[bootstrap] ensuring cloudflared is installed"
if ! command -v cloudflared >/dev/null 2>&1; then
  curl -fsSL https://pkg.cloudflare.com/cloudflare-main.gpg \
    | sudo tee /usr/share/keyrings/cloudflare-main.gpg >/dev/null
  echo "deb [signed-by=/usr/share/keyrings/cloudflare-main.gpg] https://pkg.cloudflare.com/cloudflared any main" \
    | sudo tee /etc/apt/sources.list.d/cloudflared.list >/dev/null
  sudo apt-get update
  sudo apt-get install -y cloudflared
else
  echo "[bootstrap] cloudflared present: $(cloudflared --version)"
fi

# ── 4b. nginx (model path-router) ─────────────────────────────────────────────
echo "[bootstrap] ensuring nginx is installed"
if ! command -v nginx >/dev/null 2>&1; then
  sudo apt-get update && sudo apt-get install -y nginx-light || sudo apt-get install -y nginx
else
  echo "[bootstrap] nginx present: $(nginx -v 2>&1)"
fi

if [[ "$LAUNCH" -eq 0 ]]; then
  echo "[bootstrap] --no-launch: install + config done. Start manually with:"
  echo "             bash serve_models.sh   # terminal 1 — text + vision + embed backends"
  echo "             bash serve_router.sh   # terminal 2 — nginx path-router"
  echo "             bash run_tunnel.sh     # terminal 3 — cloudflared"
  exit 0
fi

# ── 5. launch model backends (text + vision + embed), wait for health ─────────
echo "[bootstrap] launching vLLM backends (logs/text.log, vision.log, embed.log)"
bash "$ROOT/serve_models.sh"   # blocks until each enabled backend is healthy

# ── 6. launch nginx router (background) ───────────────────────────────────────
echo "[bootstrap] launching nginx router (logs/nginx-*.log)"
setsid bash "$ROOT/serve_router.sh" >> "$ROOT/logs/router.log" 2>&1 < /dev/null &
echo "[bootstrap] router started (PID $!)"
RP="${ROUTER_PORT:-8000}"
for i in $(seq 1 30); do
  curl -fsS "http://127.0.0.1:$RP/health" >/dev/null 2>&1 && { echo "[bootstrap] router healthy."; break; }
  sleep 2
done

# ── 7. launch cloudflared tunnel (background, detached) ────────────────────────
echo "[bootstrap] launching cloudflared tunnel (logs/cloudflared.log)"
setsid bash "$ROOT/run_tunnel.sh" >> "$ROOT/logs/cloudflared.log" 2>&1 < /dev/null &
echo "[bootstrap] cloudflared started (PID $!)"

touch "$ROOT/logs/cloudflared.log" "$ROOT/logs/router.log"
echo ""
echo "[bootstrap] done."
echo "[bootstrap]   text  : https://${CF_HOSTNAME:-api.iamphuckhang.dev}/v1/models"
echo "[bootstrap]   vision: https://${CF_HOSTNAME:-api.iamphuckhang.dev}/vision/v1/models"
echo "[bootstrap]   embed : https://${CF_HOSTNAME:-api.iamphuckhang.dev}/embed/v1/models"
echo "[bootstrap]   test  : bash test_api.sh"
echo "[bootstrap] === Tailing logs — Ctrl+C stops tailing; services keep running ==="
exec tail -f "$ROOT/logs"/*.log
