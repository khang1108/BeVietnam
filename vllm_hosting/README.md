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
| `setup.sh` | Install vLLM + cloudflared, apply L40 env fixes, launch both services |
| `serve_vllm.sh` | Launch the vLLM OpenAI server from `.env` |
| `run_tunnel.sh` | Write cloudflared creds/config and run the `vllm` tunnel |
| `test_api.sh` | curl smoke test (local or public) |
| `.env.example` | Config template (placeholders) |
| `.env` | Real config — **gitignored**, holds HF token + tunnel secret |

## Usage (on the GPU VM)

```bash
cd vllm_hosting
# .env already contains the model, HF token, and the "vllm" tunnel credentials.
bash setup.sh
```

`setup.sh` will:
1. create `.venv`, install `vllm==0.8.5` (+ `fastapi<0.137`),
2. apply the L40 fixes (`ldconfig`, `libcuda.so` symlink for Triton JIT, remove
   flashinfer/tvm-ffi),
3. install cloudflared,
4. start vLLM (background, `logs/vllm.log`), wait for `/health`,
5. start the cloudflared tunnel (background, `logs/cloudflared.log`).

First run downloads the model (~28 GB) to `~/.cache/huggingface` — watch
`logs/vllm.log`. Ctrl+C only stops the log tail; the services keep running.

Install/configure without launching:

```bash
bash setup.sh --no-launch
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
