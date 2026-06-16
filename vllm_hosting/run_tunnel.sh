#!/usr/bin/env bash
# Write cloudflared credentials/config from .env and run the "vllm" tunnel,
# exposing the local vLLM server at https://$CF_HOSTNAME.
#
#   bash run_tunnel.sh
#
# Idempotent: safe to re-run. Requires cloudflared installed (setup.sh does it).

set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

if [[ ! -f "$ROOT/.env" ]]; then
  echo "[tunnel] ERROR: .env not found." >&2
  exit 1
fi
set -a
# shellcheck disable=SC1091
source "$ROOT/.env"
set +a

: "${CF_TUNNEL_ID:?CF_TUNNEL_ID missing in .env}"
: "${CF_ACCOUNT_TAG:?CF_ACCOUNT_TAG missing in .env}"
: "${CF_TUNNEL_SECRET:?CF_TUNNEL_SECRET missing in .env}"
: "${CF_HOSTNAME:?CF_HOSTNAME missing in .env}"

CF_DIR="$HOME/.cloudflared"
CRED="$CF_DIR/$CF_TUNNEL_ID.json"
CONF="$CF_DIR/config.yml"
mkdir -p "$CF_DIR"

echo "[tunnel] writing credentials: $CRED"
cat > "$CRED" <<JSON
{
    "AccountTag": "$CF_ACCOUNT_TAG",
    "TunnelSecret": "$CF_TUNNEL_SECRET",
    "TunnelID": "$CF_TUNNEL_ID",
    "Endpoint": ""
}
JSON
chmod 600 "$CRED"

echo "[tunnel] writing config: $CONF (→ http://127.0.0.1:${VLLM_PORT:-8000})"
cat > "$CONF" <<YAML
# cloudflared tunnel "vllm" — managed by vllm_hosting/run_tunnel.sh
tunnel: $CF_TUNNEL_ID
credentials-file: $CRED

ingress:
  - hostname: $CF_HOSTNAME
    service: http://127.0.0.1:${VLLM_PORT:-8000}
  - service: http_status:404
YAML

echo "[tunnel] starting cloudflared (tunnel $CF_TUNNEL_ID → $CF_HOSTNAME)"
exec cloudflared tunnel --config "$CONF" run "$CF_TUNNEL_ID"
