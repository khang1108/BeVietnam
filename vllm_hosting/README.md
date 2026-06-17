# vllm_hosting

Self-hosted **vLLM** OpenAI-compatible API for BeVietnam, served from a GPU VM
(Thundercompute, **L40 48 GB**) and exposed at **`https://api.iamphuckhang.dev`**
through the existing cloudflared **`vllm`** tunnel.

This exists to replace the quota-blocked Gemini provider (the free-tier key is
`429 RESOURCE_EXHAUSTED`) for LLM work like book ingestion and cultural
explanation generation.

## What it serves

- Model: **`Qwen/Qwen2.5-14B-Instruct`** (`bfloat16`, fits the L40 with KV-cache
  headroom; strong Vietnamese + JSON). Change via `VLLM_MODEL` in `.env`.
- OpenAI-compatible routes: `/v1/models`, `/v1/chat/completions`, `/v1/completions`.
- Endpoint auth: **open** by default. Set `VLLM_API_KEY` in `.env` to require
  `Authorization: Bearer <key>`.

## Files

| File | Purpose |
|------|---------|
| `bootstrap.sh` | **All-in-one, after-clone entrypoint** — installs vLLM + cloudflared, applies L40 fixes, launches both. Committed; holds no secrets |
| `serve_vllm.sh` | Launch the vLLM OpenAI server from `.env` (restart without reinstalling) |
| `run_tunnel.sh` | Write cloudflared creds/config and run the `vllm` tunnel (restart tunnel only) |
| `test_api.sh` | curl smoke test (local or public) |
| `.env.example` | Config template (placeholders) |
| `.env` | Real config — **gitignored**, holds HF token + tunnel secret |

## Usage (fresh GPU VM, after cloning the repo)

```bash
cd BeVietnam/vllm_hosting
cp .env.example .env     # then fill in HF_TOKEN, CF_ACCOUNT_TAG, CF_TUNNEL_SECRET
bash bootstrap.sh
```

`bootstrap.sh` will:
1. on first run, if `.env` is missing it creates one from `.env.example` and stops
   so you can fill in the secrets (it never embeds any),
2. create `.venv`, install `vllm==0.8.5` (+ `fastapi<0.137`),
3. apply the L40 fixes (`ldconfig`, `libcuda.so` symlink for Triton JIT, remove
   flashinfer/tvm-ffi),
4. install cloudflared,
5. start vLLM (background, `logs/vllm.log`), wait for `/health`,
6. write the cloudflared credential from `.env` into `~/.cloudflared` and start the
   tunnel (background, `logs/cloudflared.log`).

First run downloads the model (~28 GB) to `~/.cache/huggingface` — watch
`logs/vllm.log`. Ctrl+C only stops the log tail; the services keep running.

Install/configure without launching:

```bash
bash bootstrap.sh --no-launch
bash serve_vllm.sh   # terminal 1
bash run_tunnel.sh   # terminal 2
```

## Verify

```bash
bash test_api.sh local   # http://127.0.0.1:8000
bash test_api.sh         # https://api.iamphuckhang.dev
```

## Point the BeVietnam AI service at it

vLLM is OpenAI-compatible, so any OpenAI client works:

```python
from openai import OpenAI
client = OpenAI(base_url="https://api.iamphuckhang.dev/v1", api_key="EMPTY")
resp = client.chat.completions.create(
    model="qwen2.5-14b-instruct",
    messages=[{"role": "user", "content": "..."}],
)
```

(If `VLLM_API_KEY` is set, pass it as `api_key` instead of `"EMPTY"`.)

## Notes / prerequisites

- The DNS route `api.iamphuckhang.dev → <tunnel-id>.cfargotunnel.com` must already
  exist in the Cloudflare dashboard. This stack does not create it.
- Only one process can run the `vllm` tunnel at a time — don't run this and the
  EXACT stack's tunnel simultaneously (they share the same tunnel credentials).
- vLLM binds to `127.0.0.1` only; the public entry point is the tunnel.
- Secrets live in `.env` (gitignored). Never commit real tokens or the tunnel
  secret.
