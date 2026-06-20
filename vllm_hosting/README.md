# vllm_hosting

Self-hosted **vLLM** OpenAI-compatible API for BeVietnam, served from GPU VM(s)
(Thundercompute, **L40 48 GB**) and exposed at **`https://api.iamphuckhang.dev`**
through the cloudflared **`vllm`** tunnel.

This is the **only** model provider for the AI service (Gemini retired). It hosts
**three** backends behind one nginx path-router, so a single hostname serves all
of them — chosen by URL path:

| Backend | Model | Public path | Used by |
|---|---|---|---|
| **text** | `Qwen2.5-14B-Instruct-FP8` | `https://api.iamphuckhang.dev/v1` | Trip Advisor explain, offline quest/post gen, Ask Huế |
| **vision** | `Qwen2.5-VL-7B-Instruct` | `https://api.iamphuckhang.dev/vision/v1` | Capture Judge (image vs task), Lens |
| **embed** | `BAAI/bge-m3` | `https://api.iamphuckhang.dev/embed/v1` | query embeddings (only if Ask Huế RAG is live; `EMBED_ENABLED=0` to skip) |

The **text path is unchanged** (`/v1`), so existing clients keep working.

## GPU layout

Default = **one L40 (48 GB)** hosts all three (FP8 text leaves KV headroom):

```
GPU0:  text  (FP8 ~14 GB, util 0.32) + vision (bf16 ~16 GB, util 0.40) + embed (~2.3 GB, util 0.10)
       → 0.82 of the GPU, ~16 GB free for KV cache. Fine for pilot traffic.
```

**Two L40s** — give vision its own card: set `VISION_GPU=1`, raise `TEXT_GPU_UTIL`
and `VISION_GPU_UTIL` toward `0.90`. (Optionally swap the text model to the
non-FP8 `Qwen/Qwen2.5-14B-Instruct`.)

Per-model GPU, port, and memory fraction are all in `.env`.

## Architecture

```
                 cloudflared (vllm tunnel)
                          │  api.iamphuckhang.dev
                          ▼
                 nginx router  (ROUTER_PORT 8000)
        ┌─────────────────┼──────────────────┐
        ▼                 ▼                   ▼
   text :8001        vision :8002        embed :8003
   (vllm serve)      (vllm serve)        (vllm serve --task embed)
```

Each backend binds to `127.0.0.1`; only the router port is tunnelled.

## Files

| File | Purpose |
|------|---------|
| `bootstrap.sh` | **After-clone entrypoint** — installs vLLM + qwen-vl-utils + cloudflared + nginx, applies L40 fixes, launches backends → router → tunnel. Holds no secrets |
| `serve_models.sh` | Launch the text + vision + embed backends from `.env`, wait for health |
| `serve_router.sh` | Generate `nginx.conf` from `.env` and run the path-router |
| `run_tunnel.sh` | Write cloudflared creds/config and run the `vllm` tunnel |
| `test_api.sh` | curl smoke test of all three backends (local or public) |
| `.env.example` | Config template (placeholders) |
| `.env` | Real config — **gitignored**, holds HF token + tunnel secret |

## Usage (fresh GPU VM, after cloning)

```bash
cd BeVietnam/vllm_hosting
cp .env.example .env     # fill in HF_TOKEN, CF_ACCOUNT_TAG, CF_TUNNEL_SECRET
bash bootstrap.sh
```

`bootstrap.sh`:
1. creates `.env` from the template on first run and stops for you to fill secrets,
2. creates `.venv`, installs `vllm==0.8.5` (+ `fastapi<0.137`, `transformers==4.51.3`, `qwen-vl-utils`),
3. applies L40 fixes (`ldconfig`, `libcuda.so` symlink, remove flashinfer/tvm-ffi),
4. installs cloudflared + nginx,
5. launches the backends (`logs/text.log`, `vision.log`, `embed.log`), waits for each `/health`,
6. starts the nginx router, then the tunnel.

First run downloads the models (~14 + 16 + 2 GB) to `~/.cache/huggingface` — watch
the logs. Ctrl+C only stops the tail; services keep running.

Install without launching:

```bash
bash bootstrap.sh --no-launch
bash serve_models.sh   # terminal 1
bash serve_router.sh   # terminal 2
bash run_tunnel.sh     # terminal 3
```

## Verify

```bash
bash test_api.sh local   # http://127.0.0.1:8000 (router)
bash test_api.sh         # https://api.iamphuckhang.dev
```

## Point the BeVietnam AI service at it

```python
from openai import OpenAI

# text (default — what services/ai/common/config.py already uses)
text = OpenAI(base_url="https://api.iamphuckhang.dev/v1", api_key="EMPTY")
text.chat.completions.create(model="qwen2.5-14b-instruct", messages=[...])

# vision (Capture Judge / Lens)
vision = OpenAI(base_url="https://api.iamphuckhang.dev/vision/v1", api_key="EMPTY")

# embeddings (Ask Huế RAG)
embed = OpenAI(base_url="https://api.iamphuckhang.dev/embed/v1", api_key="EMPTY")
```

(If `VLLM_API_KEY` is set, pass it instead of `"EMPTY"`.)

## Notes / prerequisites

- DNS route `api.iamphuckhang.dev → <tunnel-id>.cfargotunnel.com` must already
  exist in Cloudflare. This stack does not create it.
- Only one process can run the `vllm` tunnel at a time (shared credentials).
- On a shared GPU the per-model `*_GPU_UTIL` fractions **must sum under ~0.9** or
  the later backends OOM at startup. Defaults already do (0.82).
- Backends bind to `127.0.0.1`; the public entry point is the tunnel.
- Secrets live in `.env` (gitignored). Never commit real tokens or the tunnel secret.
</content>
