#!/usr/bin/env bash
# Smoke-test the three model backends, through the public tunnel or locally.
#   bash test_api.sh           # test https://$CF_HOSTNAME (nginx router)
#   bash test_api.sh local     # test http://127.0.0.1:$ROUTER_PORT

set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
set -a; source "$ROOT/.env"; set +a

if [[ "${1:-}" == "local" ]]; then
  BASE="http://127.0.0.1:${ROUTER_PORT:-8000}"
else
  BASE="https://${CF_HOSTNAME:-api.iamphuckhang.dev}"
fi

AUTH=()
[[ -n "${VLLM_API_KEY:-}" ]] && AUTH=(-H "Authorization: Bearer ${VLLM_API_KEY}")
JQ() { python3 -m json.tool 2>/dev/null || cat; }

# ── text ──────────────────────────────────────────────────────────────────────
echo "== TEXT: POST $BASE/v1/chat/completions =="
curl -fsS "${AUTH[@]}" -H "Content-Type: application/json" \
  "$BASE/v1/chat/completions" \
  -d "{
    \"model\": \"${TEXT_SERVED_NAME:-qwen2.5-14b-instruct}\",
    \"messages\": [{\"role\": \"user\", \"content\": \"Nói một câu ngắn về Kinh thành Huế.\"}],
    \"max_tokens\": 128, \"temperature\": 0.7
  }" | JQ || true

# ── vision ────────────────────────────────────────────────────────────────────
echo ""
echo "== VISION: GET $BASE/vision/v1/models =="
curl -fsS "${AUTH[@]}" "$BASE/vision/v1/models" | JQ || true
echo "   (full image test: POST $BASE/vision/v1/chat/completions with an image_url content part)"

# ── embeddings ────────────────────────────────────────────────────────────────
if [[ "${EMBED_ENABLED:-1}" == "1" ]]; then
  echo ""
  echo "== EMBED: POST $BASE/embed/v1/embeddings =="
  curl -fsS "${AUTH[@]}" -H "Content-Type: application/json" \
    "$BASE/embed/v1/embeddings" \
    -d "{
      \"model\": \"${EMBED_SERVED_NAME:-bge-m3}\",
      \"input\": \"Kinh thành Huế\"
    }" | python3 -c "import sys,json; d=json.load(sys.stdin); print('embedding dim =', len(d['data'][0]['embedding']))" || true
fi
