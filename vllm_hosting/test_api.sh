#!/usr/bin/env bash
# Smoke-test the vLLM endpoint, locally and through the public tunnel.
#   bash test_api.sh           # test https://$CF_HOSTNAME
#   bash test_api.sh local     # test http://127.0.0.1:$VLLM_PORT

set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
set -a; source "$ROOT/.env"; set +a

if [[ "${1:-}" == "local" ]]; then
  BASE="http://127.0.0.1:${VLLM_PORT:-8000}"
else
  BASE="https://${CF_HOSTNAME:-api.iamphuckhang.dev}"
fi

AUTH=()
[[ -n "${VLLM_API_KEY:-}" ]] && AUTH=(-H "Authorization: Bearer ${VLLM_API_KEY}")

echo "== GET $BASE/v1/models =="
curl -fsS "${AUTH[@]}" "$BASE/v1/models" | python3 -m json.tool || true

echo ""
echo "== POST $BASE/v1/chat/completions =="
curl -fsS "${AUTH[@]}" -H "Content-Type: application/json" \
  "$BASE/v1/chat/completions" \
  -d "{
    \"model\": \"${VLLM_SERVED_MODEL_NAME:-qwen2.5-14b-instruct}\",
    \"messages\": [{\"role\": \"user\", \"content\": \"Nói một câu ngắn về Kinh thành Huế.\"}],
    \"max_tokens\": 128,
    \"temperature\": 0.7
  }" | python3 -m json.tool || true
