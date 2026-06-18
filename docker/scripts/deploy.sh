#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────────────────────
# Report System — Deploy script (mac / linux)
#
# Usage:
#   ./deploy.sh local up          Build + start all services (dev compose)
#   ./deploy.sh local down        Stop + remove containers (keeps volumes)
#   ./deploy.sh local logs [svc]  Tail logs (all or specific service)
#   ./deploy.sh local restart     Restart all services
#   ./deploy.sh local build       Build all images only (no start)
#   ./deploy.sh local status      Show container status
#   ./deploy.sh local clean       Stop + remove containers AND volumes
#
#   ./deploy.sh prod up           Build prod images + start (prod compose)
#   ./deploy.sh prod down         Stop prod
#   ./deploy.sh prod push [reg]   Build + tag + push to registry (default: docker.io)
#   ./deploy.sh prod status       Show prod container status
#
# Environment variables (or use .env file):
#   REGISTRY         e.g. docker.io/myorg, ghcr.io/myorg, 123.dkr.ecr.us-east-1.amazonaws.com/myorg
#   IMAGE_TAG        e.g. v1.0.0, latest, git-sha-abc123  (default: latest)
#   COMPOSE_FILE     override compose file path
# ─────────────────────────────────────────────────────────────────────────────

set -euo pipefail

# ─── Resolve paths ────────────────────────────────────────────────────────────
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
DOCKER_DIR="$PROJECT_ROOT/docker"

# ─── Defaults ────────────────────────────────────────────────────────────────
ENV_FILE="$DOCKER_DIR/.env"
COMPOSE_FILE="$DOCKER_DIR/docker-compose.yml"
COMPOSE_PROD_FILE="$DOCKER_DIR/docker-compose.prod.yml"
REGISTRY="${REGISTRY:-reportsystem}"
IMAGE_TAG="${IMAGE_TAG:-latest}"
SERVICES=(
  eureka gateway
  auth-service property-service restaurant-service inventory-service
  finance-service payment-service reporting-service
  nuxt-web
)

# ─── Colors ──────────────────────────────────────────────────────────────────
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'
BLUE='\033[0;34m'; CYAN='\033[0;36m'; NC='\033[0m'

log()    { echo -e "${BLUE}[$(date +%H:%M:%S)]${NC} $*"; }
ok()     { echo -e "${GREEN}[✓]${NC} $*"; }
warn()   { echo -e "${YELLOW}[!]${NC} $*"; }
fail()   { echo -e "${RED}[✗]${NC} $*" >&2; exit 1; }
header() { echo -e "\n${CYAN}═══ $* ═══${NC}\n"; }

# ─── Load .env if present ────────────────────────────────────────────────────
[[ -f "$ENV_FILE" ]] && { set -a; source "$ENV_FILE"; set +a; ok "Loaded $ENV_FILE"; }

# ─── Prerequisite checks ─────────────────────────────────────────────────────
check_docker() {
  command -v docker >/dev/null 2>&1 || fail "docker not installed — get it from https://docker.com"
  docker info >/dev/null 2>&1 || fail "docker daemon not running — start Docker Desktop or 'sudo systemctl start docker'"
  docker compose version >/dev/null 2>&1 || fail "'docker compose' plugin missing — run 'docker compose' once to install"
  ok "Docker $(docker --version | awk '{print $3}' | tr -d ',')"
}

check_maven() {
  if [[ "$1" == "build" || "$1" == "up" ]]; then
    command -v mvn >/dev/null 2>&1 || warn "mvn not in PATH — backend JARs must already exist in services/*/target/"
    if command -v mvn >/dev/null 2>&1; then
      ok "Maven $(mvn -v | head -1 | awk '{print $3}')"
    fi
  fi
}

# ─── Helpers ────────────────────────────────────────────────────────────────
maven_build() {
  header "Building backend JARs"
  cd "$PROJECT_ROOT"
  if command -v mvn >/dev/null 2>&1; then
    mvn clean package -DskipTests -q
    ok "Maven build complete"
  else
    warn "Skipping Maven build (mvn not found) — assuming JARs are present in services/*/target/"
    find services -name "*-service-*.jar" -not -path "*/target/original-*" | head -1 >/dev/null \
      || fail "No JARs found in services/*/target/ and mvn is not available"
  fi
}

compose_cmd() {
  local file="$1"; shift
  # docker compose uses the dirname of -f as the project directory for
  # relative paths in the compose file. We must pass --project-directory
  # explicitly so build contexts (e.g. ./frontend/...) resolve from
  # PROJECT_ROOT, not from the compose file's directory.
  docker compose --project-directory "$PROJECT_ROOT" -f "$file" "$@"
}

# ─── Local mode ─────────────────────────────────────────────────────────────
local_up() {
  header "LOCAL: Build + start"
  check_docker
  maven_build
  compose_cmd "$COMPOSE_FILE" up -d --build
  wait_for_healthy
  bash "$SCRIPT_DIR/keycloak-ensure-admin.sh"
  local_status
}

local_down() {
  header "LOCAL: Stop"
  check_docker
  compose_cmd "$COMPOSE_FILE" down
  ok "Stopped"
}

local_logs() {
  check_docker
  if [[ -n "${1:-}" ]]; then
    compose_cmd "$COMPOSE_FILE" logs -f "$1"
  else
    compose_cmd "$COMPOSE_FILE" logs -f
  fi
}

local_restart() {
  header "LOCAL: Restart"
  check_docker
  compose_cmd "$COMPOSE_FILE" restart
  wait_for_healthy
  local_status
}

local_build() {
  header "LOCAL: Build images only"
  check_docker
  maven_build
  compose_cmd "$COMPOSE_FILE" build
  ok "Images built"
}

local_status() {
  header "LOCAL: Status"
  check_docker
  compose_cmd "$COMPOSE_FILE" ps
  echo ""
  log "Service URLs:"
  echo "  • Eureka       →  http://localhost:8761"
  echo "  • API Gateway  →  http://localhost:8080"
  echo "  • Nuxt Web     →  http://localhost:3000"
  echo "  • MinIO UI     →  http://localhost:9001  (minioadmin / minioadmin)"
  echo "  • Zipkin       →  http://localhost:9411"
}

local_clean() {
  header "LOCAL: Clean (containers + volumes + images)"
  check_docker
  warn "This will DELETE all data volumes. Continue? [y/N]"
  read -r confirm
  [[ "$confirm" =~ ^[Yy]$ ]] || { echo "Aborted."; return; }
  compose_cmd "$COMPOSE_FILE" down -v --rmi local
  ok "Cleaned"
}

wait_for_healthy() {
  log "Waiting for services to become healthy (max 180s)..."
  local elapsed=0
  local healthy_count=0
  local target_count
  target_count=$(compose_cmd "$COMPOSE_FILE" config --services 2>/dev/null | wc -l | tr -d ' ')

  while [[ $elapsed -lt 180 ]]; do
    healthy_count=$(compose_cmd "$COMPOSE_FILE" ps --format json 2>/dev/null \
      | grep -c '"Health":"healthy"' 2>/dev/null || echo 0)
    if [[ "$healthy_count" -ge "$target_count" ]]; then
      ok "All $target_count services healthy"
      return 0
    fi
    sleep 5
    elapsed=$((elapsed + 5))
    echo -n "."
  done
  warn "Timeout: $healthy_count/$target_count services healthy after 180s"
  warn "Run '$0 local logs' to inspect"
}

# ─── Prod mode ──────────────────────────────────────────────────────────────
prod_up() {
  header "PROD: Build + start (image tag: $IMAGE_TAG)"
  check_docker
  maven_build
  compose_cmd "$COMPOSE_PROD_FILE" up -d
  prod_status
}

prod_down() {
  header "PROD: Stop"
  check_docker
  compose_cmd "$COMPOSE_PROD_FILE" down
  ok "Stopped"
}

prod_push() {
  header "PROD: Build + tag + push to $REGISTRY"
  check_docker
  maven_build
  local push_reg="${1:-$REGISTRY}"
  [[ -z "$push_reg" || "$push_reg" == "reportsystem" ]] \
    && fail "Set REGISTRY env var or pass registry as argument, e.g. ./deploy.sh prod push ghcr.io/myorg"

  cd "$PROJECT_ROOT"
  for svc in "${SERVICES[@]}"; do
    local dockerfile
    case "$svc" in
      eureka)         dockerfile="infrastructure/eureka/Dockerfile" ;;
      gateway)        dockerfile="infrastructure/gateway/Dockerfile" ;;
      nuxt-web)       dockerfile="frontend/report-system-web/Dockerfile" ;;
      *)              dockerfile="services/$svc/Dockerfile" ;;
    esac
    local img="$push_reg/$svc:$IMAGE_TAG"
    log "Building $img"
    docker build -t "$img" -f "$dockerfile" "$PROJECT_ROOT"
  done

  log "Pushing images..."
  for svc in "${SERVICES[@]}"; do
    docker push "$push_reg/$svc:$IMAGE_TAG"
  done
  ok "All images pushed to $push_reg"
  echo ""
  echo "On the target host, set REGISTRY=$push_reg and IMAGE_TAG=$IMAGE_TAG, then run:"
  echo "  REGISTRY=$push_reg IMAGE_TAG=$IMAGE_TAG ./deploy.sh prod up"
}

prod_status() {
  header "PROD: Status"
  check_docker
  compose_cmd "$COMPOSE_PROD_FILE" ps
  echo ""
  log "Image registry: ${REGISTRY:-reportsystem}"
  log "Image tag:      $IMAGE_TAG"
}

# ─── Help ──────────────────────────────────────────────────────────────────
usage() {
  sed -n '2,23p' "${BASH_SOURCE[0]}" | sed 's/^# \{0,1\}//'
}

# ─── Main ──────────────────────────────────────────────────────────────────
[[ $# -lt 2 ]] && { usage; exit 1; }

MODE=$1; shift
ACTION=$1; shift

case "$MODE" in
  local)
    case "$ACTION" in
      up)      local_up ;;
      down)    local_down ;;
      logs)    local_logs "${1:-}" ;;
      restart) local_restart ;;
      build)   local_build ;;
      status)  local_status ;;
      clean)   local_clean ;;
      *)       fail "Unknown local action: $ACTION  (try: up|down|logs|restart|build|status|clean)" ;;
    esac
    ;;
  prod)
    case "$ACTION" in
      up)      prod_up ;;
      down)    prod_down ;;
      push)    prod_push "${1:-}" ;;
      status)  prod_status ;;
      *)       fail "Unknown prod action: $ACTION  (try: up|down|push|status)" ;;
    esac
    ;;
  help|--help|-h|"")
    usage
    ;;
  *)
    fail "Unknown mode: $MODE  (try: local|prod)"
    ;;
esac
