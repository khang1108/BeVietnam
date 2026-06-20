#!/usr/bin/env bash
# Unified VM runner for BeVietnam.
#
# Starts/stops the production-style local services on a VM:
#   - AI Core   -> default http://0.0.0.0:8001
#   - Backend   -> default http://0.0.0.0:8000
# Optional:
#   - vLLM      -> existing vllm_hosting/serve_vllm.sh
#   - tunnel    -> existing vllm_hosting/run_tunnel.sh
#
# Usage:
#   bash scripts/vm_start.sh start
#   bash scripts/vm_start.sh status
#   bash scripts/vm_start.sh stop
#   bash scripts/vm_start.sh restart

set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV_FILE="${ENV_FILE:-$ROOT/.env}"
LOG_DIR="${LOG_DIR:-$ROOT/logs}"
PID_DIR="${PID_DIR:-$ROOT/.run}"

BACKEND_VENV="${BACKEND_VENV:-$ROOT/services/backend/venv}"
AI_VENV="${AI_VENV:-$ROOT/services/ai/venv}"

BACKEND_HOST="${BACKEND_HOST:-0.0.0.0}"
BACKEND_PORT="${BACKEND_PORT:-8000}"
BACKEND_WORKERS="${BACKEND_WORKERS:-1}"

AI_HOST="${AI_HOST:-0.0.0.0}"
AI_PORT="${AI_PORT:-8001}"
AI_WORKERS="${AI_WORKERS:-1}"

RUN_MIGRATIONS="${RUN_MIGRATIONS:-1}"
INSTALL_DEPS="${INSTALL_DEPS:-auto}"
AUTO_APT_INSTALL="${AUTO_APT_INSTALL:-1}"
AUTO_POSTGRES="${AUTO_POSTGRES:-1}"
POSTGRES_DB="${POSTGRES_DB:-bevietnam}"
POSTGRES_USER="${POSTGRES_USER:-bevietnam}"
POSTGRES_PASSWORD="${POSTGRES_PASSWORD:-}"
POSTGRES_HOST="${POSTGRES_HOST:-127.0.0.1}"
POSTGRES_PORT="${POSTGRES_PORT:-5432}"
START_VLLM="${START_VLLM:-0}"
START_VLLM_TUNNEL="${START_VLLM_TUNNEL:-0}"

mkdir -p "$LOG_DIR" "$PID_DIR"

usage() {
  cat <<EOF
Unified VM runner for BeVietnam.

Commands:
  bootstrap    create venvs and install Backend/AI dependencies only
  start        prepare everything, then start AI Core and Backend
  stop         stop managed services
  restart      stop then start
  status       show managed process status
  logs         tail Backend and AI Core logs
  validate-env validate .env parsing without starting services
  help         show this message

Environment knobs:
  ENV_FILE=/path/to/.env              default: $ROOT/.env
  BACKEND_HOST=0.0.0.0                BACKEND_PORT=8000
  AI_HOST=0.0.0.0                     AI_PORT=8001
  BACKEND_WORKERS=1                   AI_WORKERS=1
  RUN_MIGRATIONS=1                    run Alembic before backend start
  INSTALL_DEPS=auto                   auto | 1 | 0
  AUTO_APT_INSTALL=1                  install python3-venv/python3-pip on Ubuntu if missing
  AUTO_POSTGRES=1                     install/start local PostgreSQL if DATABASE_URL is missing
  POSTGRES_DB=bevietnam               local DB name when AUTO_POSTGRES=1
  POSTGRES_USER=bevietnam             local DB user when AUTO_POSTGRES=1
  POSTGRES_PASSWORD=                  generated if empty
  START_VLLM=1                        also start vllm_hosting/serve_vllm.sh
  START_VLLM_TUNNEL=1                 also start vllm_hosting/run_tunnel.sh

Recommended VM start:
  bash scripts/vm_start.sh start

With local vLLM on same VM:
  Set vllm_hosting/.env VLLM_PORT to a port that does not conflict with BACKEND_PORT.
  START_VLLM=1 START_VLLM_TUNNEL=1 bash scripts/vm_start.sh start
EOF
}

log() {
  printf '[vm] %s\n' "$*"
}

die() {
  printf '[vm] ERROR: %s\n' "$*" >&2
  exit 1
}

command_exists() {
  command -v "$1" >/dev/null 2>&1
}

trim() {
  local value="$1"

  value="${value#"${value%%[![:space:]]*}"}"
  value="${value%"${value##*[![:space:]]}"}"
  printf '%s' "$value"
}

load_dotenv_file() {
  local line
  local key
  local value
  local line_number=0

  while IFS= read -r line || [[ -n "$line" ]]; do
    line_number=$((line_number + 1))
    line="$(trim "$line")"

    [[ -z "$line" || "$line" == \#* ]] && continue

    if [[ "$line" != *=* ]]; then
      log "Ignoring invalid .env line $line_number: $line"
      continue
    fi

    key="$(trim "${line%%=*}")"
    value="$(trim "${line#*=}")"

    if [[ ! "$key" =~ ^[A-Za-z_][A-Za-z0-9_]*$ ]]; then
      log "Ignoring invalid .env key on line $line_number: $key"
      continue
    fi

    if [[ "$value" == \"*\" && "$value" == *\" ]]; then
      value="${value:1:${#value}-2}"
    elif [[ "$value" == \'*\' && "$value" == *\' ]]; then
      value="${value:1:${#value}-2}"
    fi

    export "$key=$value"
  done < "$ENV_FILE"
}

has_real_database_url() {
  [[ -n "${DATABASE_URL:-}" ]] \
    && [[ "${DATABASE_URL:-}" != *"..."* ]] \
    && [[ "${DATABASE_URL:-}" != *"change-me"* ]]
}

database_url_is_local() {
  [[ "${DATABASE_URL:-}" == *"@127.0.0.1"* ]] \
    || [[ "${DATABASE_URL:-}" == *"@localhost"* ]]
}

load_env() {
  if [[ ! -f "$ENV_FILE" ]]; then
    if [[ -f "$ROOT/.env.example" ]]; then
      cp "$ROOT/.env.example" "$ENV_FILE"
      die "Created $ENV_FILE from .env.example. Fill secrets/URLs, then rerun."
    fi
    die "$ENV_FILE not found"
  fi

  load_dotenv_file

  export PYTHONPATH="$ROOT"
  export AI_CORE_BASE_URL="${AI_CORE_BASE_URL:-http://127.0.0.1:$AI_PORT}"
  export AI_CORE_USE_MOCK="${AI_CORE_USE_MOCK:-false}"
  export AI_CORE_TIMEOUT="${AI_CORE_TIMEOUT:-300}"
  export LLM_PROVIDER="${LLM_PROVIDER:-vllm}"
}

ensure_python_tooling() {
  command_exists python3 || die "python3 is required but was not found"

  if python3 -m venv --help >/dev/null 2>&1; then
    return
  fi

  if [[ "$AUTO_APT_INSTALL" == "1" ]] && command_exists apt-get && command_exists sudo; then
    log "Installing Python venv tooling with apt"
    sudo apt-get update
    sudo apt-get install -y python3-venv python3-pip
  fi

  python3 -m venv --help >/dev/null 2>&1 || die "python3 venv support is missing. Install python3-venv, then rerun."
}

generate_password() {
  if command_exists openssl; then
    openssl rand -hex 18
  else
    python3 - <<'PY'
import secrets

print(secrets.token_hex(18))
PY
  fi
}

validate_pg_name() {
  local label="$1"
  local value="$2"

  [[ "$value" =~ ^[A-Za-z_][A-Za-z0-9_]*$ ]] || die "$label must match ^[A-Za-z_][A-Za-z0-9_]*$; got: $value"
}

install_postgres_packages() {
  if command_exists psql && command_exists pg_ctlcluster && command_exists pg_lsclusters; then
    return
  fi

  if [[ "$AUTO_APT_INSTALL" == "1" ]] && command_exists apt-get && command_exists sudo; then
    log "Installing PostgreSQL with apt"
    sudo apt-get update
    sudo apt-get install -y postgresql postgresql-contrib
  fi

  command_exists psql || die "psql is missing. Install postgresql, then rerun."
  command_exists pg_ctlcluster || die "pg_ctlcluster is missing. Install postgresql-common, then rerun."
  command_exists pg_lsclusters || die "pg_lsclusters is missing. Install postgresql-common, then rerun."
}

postgres_version() {
  if [[ -d /etc/postgresql ]]; then
    find /etc/postgresql -mindepth 1 -maxdepth 1 -type d -printf '%f\n' | sort -V | tail -n 1
  fi
}

ensure_postgres_cluster() {
  local version
  local clusters

  clusters="$(pg_lsclusters --no-header 2>/dev/null || true)"
  if [[ -z "$clusters" ]]; then
    version="$(postgres_version)"
    [[ -n "$version" ]] || die "Could not find an installed PostgreSQL version under /etc/postgresql"
    log "Creating PostgreSQL cluster $version/main"
    sudo pg_createcluster "$version" main --start
    return
  fi

  while read -r version cluster _port status _rest; do
    [[ -n "${version:-}" ]] || continue
    if [[ "$status" != "online" ]]; then
      log "Starting PostgreSQL cluster $version/$cluster"
      sudo pg_ctlcluster "$version" "$cluster" start
    fi
  done <<< "$clusters"
}

wait_for_postgres() {
  log "Waiting for local PostgreSQL"
  for _ in {1..30}; do
    if sudo -u postgres psql -tAc "select 1" >/dev/null 2>&1; then
      log "PostgreSQL is ready"
      return
    fi
    sleep 1
  done

  if command_exists service; then
    sudo service postgresql start >/dev/null 2>&1 || true
    for _ in {1..15}; do
      if sudo -u postgres psql -tAc "select 1" >/dev/null 2>&1; then
        log "PostgreSQL is ready"
        return
      fi
      sleep 1
    done
  fi

  die "PostgreSQL did not start. Run pg_lsclusters for details."
}

write_env_values() {
  local db_url="$1"
  local db_password="$2"

  python3 - "$ENV_FILE" "$db_url" "$POSTGRES_DB" "$POSTGRES_USER" "$db_password" <<'PY'
import sys
from pathlib import Path

path = Path(sys.argv[1])
updates = {
    "DATABASE_URL": sys.argv[2],
    "POSTGRES_DB": sys.argv[3],
    "POSTGRES_USER": sys.argv[4],
    "POSTGRES_PASSWORD": sys.argv[5],
}

lines = path.read_text().splitlines() if path.exists() else []
seen = set()
next_lines = []

for line in lines:
    key = line.split("=", 1)[0] if "=" in line and not line.lstrip().startswith("#") else None
    if key in updates:
        next_lines.append(f"{key}={updates[key]}")
        seen.add(key)
    else:
        next_lines.append(line)

if any(key not in seen for key in updates):
    if next_lines and next_lines[-1] != "":
        next_lines.append("")
    next_lines.append("# Local PostgreSQL generated by scripts/vm_start.sh")
    for key, value in updates.items():
        if key not in seen:
            next_lines.append(f"{key}={value}")

path.write_text("\n".join(next_lines) + "\n")
PY
}

ensure_local_postgres() {
  local sql_password
  local db_url

  if [[ "$AUTO_POSTGRES" != "1" ]]; then
    log "Skipping local PostgreSQL setup (AUTO_POSTGRES=$AUTO_POSTGRES)"
    return
  fi

  if has_real_database_url; then
    if database_url_is_local; then
      install_postgres_packages
      ensure_postgres_cluster
      wait_for_postgres
    fi
    log "DATABASE_URL is already configured"
    return
  fi

  validate_pg_name "POSTGRES_DB" "$POSTGRES_DB"
  validate_pg_name "POSTGRES_USER" "$POSTGRES_USER"

  install_postgres_packages
  ensure_postgres_cluster
  wait_for_postgres

  if [[ -z "$POSTGRES_PASSWORD" || "$POSTGRES_PASSWORD" == "change-me" ]]; then
    POSTGRES_PASSWORD="$(generate_password)"
  fi

  sql_password="${POSTGRES_PASSWORD//\'/\'\'}"

  log "Creating/updating PostgreSQL role and database"
  sudo -u postgres psql -v ON_ERROR_STOP=1 <<SQL
DO \$\$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = '$POSTGRES_USER') THEN
    CREATE ROLE "$POSTGRES_USER" LOGIN PASSWORD '$sql_password';
  ELSE
    ALTER ROLE "$POSTGRES_USER" WITH LOGIN PASSWORD '$sql_password';
  END IF;
END
\$\$;
SELECT 'CREATE DATABASE "$POSTGRES_DB" OWNER "$POSTGRES_USER"'
WHERE NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = '$POSTGRES_DB') \gexec
ALTER DATABASE "$POSTGRES_DB" OWNER TO "$POSTGRES_USER";
GRANT ALL PRIVILEGES ON DATABASE "$POSTGRES_DB" TO "$POSTGRES_USER";
SQL

  db_url="$(python3 - "$POSTGRES_USER" "$POSTGRES_PASSWORD" "$POSTGRES_HOST" "$POSTGRES_PORT" "$POSTGRES_DB" <<'PY'
import sys
from urllib.parse import quote

user, password, host, port, db = sys.argv[1:]
print(f"postgresql+asyncpg://{quote(user, safe='')}:{quote(password, safe='')}@{host}:{port}/{quote(db, safe='')}")
PY
)"

  export DATABASE_URL="$db_url"
  write_env_values "$db_url" "$POSTGRES_PASSWORD"
  log "DATABASE_URL written to $ENV_FILE"
}

python_bin() {
  local venv="$1"
  printf '%s/bin/python' "$venv"
}

requirements_hash() {
  local req="$1"

  if command_exists sha256sum; then
    sha256sum "$req" | awk '{print $1}'
  else
    python3 - "$req" <<'PY'
import hashlib
import sys
from pathlib import Path

print(hashlib.sha256(Path(sys.argv[1]).read_bytes()).hexdigest())
PY
  fi
}

should_install_deps() {
  local venv="$1"
  local req="$2"
  local marker="$venv/.deps-installed"
  local hash_file="$venv/.requirements.sha256"
  local current_hash

  [[ "$INSTALL_DEPS" == "1" ]] && return 0
  [[ "$INSTALL_DEPS" == "0" ]] && return 1
  [[ -f "$marker" ]] || return 0
  [[ "$req" -nt "$marker" ]] && return 0

  current_hash="$(requirements_hash "$req")"
  [[ -f "$hash_file" ]] || return 0
  [[ "$(cat "$hash_file")" == "$current_hash" ]] || return 0
  return 1
}

ensure_venv() {
  local name="$1"
  local venv="$2"
  local req="$3"
  local py
  local current_hash

  [[ -f "$req" ]] || die "$name requirements file not found: $req"
  ensure_python_tooling
  py="$(python_bin "$venv")"

  if [[ ! -x "$py" ]]; then
    log "Creating $name virtualenv at $venv"
    python3 -m venv "$venv"
  fi

  if should_install_deps "$venv" "$req"; then
    log "Installing $name dependencies"
    "$py" -m pip install --upgrade pip
    "$py" -m pip install -r "$req"
    current_hash="$(requirements_hash "$req")"
    printf '%s\n' "$current_hash" > "$venv/.requirements.sha256"
    date -u +%Y-%m-%dT%H:%M:%SZ > "$venv/.deps-installed"
  else
    log "$name dependencies are up to date"
  fi
}

pid_file() {
  printf '%s/%s.pid' "$PID_DIR" "$1"
}

is_running() {
  local pidfile="$1"
  [[ -f "$pidfile" ]] && kill -0 "$(cat "$pidfile")" 2>/dev/null
}

start_process() {
  local name="$1"
  local pidfile
  local logfile
  shift

  pidfile="$(pid_file "$name")"
  logfile="$LOG_DIR/$name.log"

  if is_running "$pidfile"; then
    log "$name already running (pid $(cat "$pidfile"))"
    return
  fi

  rm -f "$pidfile"
  log "Starting $name -> $logfile"
  (
    cd "$ROOT"
    exec "$@"
  ) >> "$logfile" 2>&1 &
  echo "$!" > "$pidfile"
  sleep 1

  if ! is_running "$pidfile"; then
    tail -n 80 "$logfile" >&2 || true
    die "$name failed to start"
  fi
}

stop_process() {
  local name="$1"
  local pidfile
  pidfile="$(pid_file "$name")"

  if ! is_running "$pidfile"; then
    rm -f "$pidfile"
    log "$name is not running"
    return
  fi

  log "Stopping $name (pid $(cat "$pidfile"))"
  kill "$(cat "$pidfile")" 2>/dev/null || true
  for _ in {1..20}; do
    if ! is_running "$pidfile"; then
      rm -f "$pidfile"
      return
    fi
    sleep 0.5
  done

  log "$name did not stop gracefully; killing"
  kill -9 "$(cat "$pidfile")" 2>/dev/null || true
  rm -f "$pidfile"
}

health_check() {
  local name="$1"
  local url="$2"
  local attempts="${3:-30}"

  log "Waiting for $name health: $url"
  for _ in $(seq 1 "$attempts"); do
    if python3 - "$url" <<'PY' >/dev/null 2>&1
import sys
from urllib.request import urlopen

with urlopen(sys.argv[1], timeout=2) as response:
    if response.status < 500:
        sys.exit(0)
sys.exit(1)
PY
    then
      log "$name is ready"
      return
    fi
    sleep 2
  done

  die "$name did not become healthy at $url"
}

read_vllm_env_value() {
  local key="$1"
  local file="$ROOT/vllm_hosting/.env"
  [[ -f "$file" ]] || return 0
  awk -F= -v k="$key" '$1 == k {print $2}' "$file" | tail -n 1
}

guard_vllm_port() {
  local vllm_host
  local vllm_port

  vllm_host="$(read_vllm_env_value VLLM_HOST)"
  vllm_port="$(read_vllm_env_value VLLM_PORT)"
  vllm_host="${vllm_host:-127.0.0.1}"
  vllm_port="${vllm_port:-8000}"

  if [[ "$START_VLLM" == "1" && "$vllm_port" == "$BACKEND_PORT" ]]; then
    die "vLLM and Backend both want port $BACKEND_PORT. Set VLLM_PORT=8010 in vllm_hosting/.env or BACKEND_PORT=8080 before starting."
  fi

  export VLLM_BASE_URL="${VLLM_BASE_URL:-http://$vllm_host:$vllm_port/v1}"
}

bootstrap() {
  load_env
  ensure_venv "backend" "$BACKEND_VENV" "$ROOT/services/backend/requirements.txt"
  ensure_venv "ai" "$AI_VENV" "$ROOT/services/ai/requirements.txt"

  if [[ "$START_VLLM" == "1" ]]; then
    [[ -f "$ROOT/vllm_hosting/.env" ]] || die "vllm_hosting/.env missing. Copy vllm_hosting/.env.example and fill secrets."
    log "Bootstrapping vLLM hosting stack"
    (cd "$ROOT/vllm_hosting" && bash bootstrap.sh --no-launch)
  fi
}

run_migrations() {
  if [[ "$RUN_MIGRATIONS" != "1" ]]; then
    log "Skipping migrations (RUN_MIGRATIONS=$RUN_MIGRATIONS)"
    return
  fi

  if [[ -z "${DATABASE_URL:-}" ]]; then
    log "Skipping migrations because DATABASE_URL is not set"
    return
  fi

  log "Running Alembic migrations"
  "$(python_bin "$BACKEND_VENV")" -m alembic -c "$ROOT/database/alembic.ini" upgrade head
}

start_all() {
  load_env
  ensure_local_postgres
  guard_vllm_port
  ensure_venv "backend" "$BACKEND_VENV" "$ROOT/services/backend/requirements.txt"
  ensure_venv "ai" "$AI_VENV" "$ROOT/services/ai/requirements.txt"

  if [[ "$START_VLLM" == "1" ]]; then
    [[ -f "$ROOT/vllm_hosting/.env" ]] || die "vllm_hosting/.env missing. Copy vllm_hosting/.env.example and fill secrets."
    start_process "vllm" bash "$ROOT/vllm_hosting/serve_vllm.sh"
    health_check "vLLM" "${VLLM_BASE_URL%/v1}/health" 180

    if [[ "$START_VLLM_TUNNEL" == "1" ]]; then
      start_process "vllm-tunnel" bash "$ROOT/vllm_hosting/run_tunnel.sh"
    fi
  fi

  start_process "ai-core" "$(python_bin "$AI_VENV")" -m uvicorn services.ai.main:app \
    --host "$AI_HOST" --port "$AI_PORT" --workers "$AI_WORKERS"
  health_check "AI Core" "http://127.0.0.1:$AI_PORT/health" 45

  run_migrations

  start_process "backend" "$(python_bin "$BACKEND_VENV")" -m uvicorn services.backend.app.main:app \
    --host "$BACKEND_HOST" --port "$BACKEND_PORT" --workers "$BACKEND_WORKERS"
  health_check "Backend" "http://127.0.0.1:$BACKEND_PORT/docs" 45

  status
  log "Backend: http://$BACKEND_HOST:$BACKEND_PORT/docs"
  log "AI Core: http://$AI_HOST:$AI_PORT/health"
}

stop_all() {
  stop_process "backend"
  stop_process "ai-core"
  stop_process "vllm-tunnel"
  stop_process "vllm"
}

status_one() {
  local name="$1"
  local pidfile
  pidfile="$(pid_file "$name")"

  if is_running "$pidfile"; then
    printf '%-12s running pid=%s log=%s/%s.log\n' "$name" "$(cat "$pidfile")" "$LOG_DIR" "$name"
  else
    printf '%-12s stopped\n' "$name"
  fi
}

status() {
  status_one "vllm"
  status_one "vllm-tunnel"
  status_one "ai-core"
  status_one "backend"
}

validate_env() {
  load_env
  log "Environment loaded from $ENV_FILE"
}

case "${1:-start}" in
  bootstrap)
    INSTALL_DEPS=1 bootstrap
    ;;
  start)
    start_all
    ;;
  stop)
    stop_all
    ;;
  restart)
    stop_all
    start_all
    ;;
  status)
    status
    ;;
  logs)
    tail -n 120 -f "$LOG_DIR"/backend.log "$LOG_DIR"/ai-core.log
    ;;
  validate-env)
    validate_env
    ;;
  help|-h|--help)
    usage
    ;;
  *)
    usage
    die "Unknown command: ${1:-}"
    ;;
esac
