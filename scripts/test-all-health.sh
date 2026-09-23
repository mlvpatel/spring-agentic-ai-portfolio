#!/usr/bin/env bash
# ==============================================================================
# Enterprise Agentic AI Portfolio — Health Check Verification Script
# Compose topology: agents are not published on the host. Probe via gateway :8080.
# Requires GATEWAY_API_KEY (same value as compose GATEWAY_API_KEY).
# ==============================================================================

set -e

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

GATEWAY_URL="${GATEWAY_URL:-http://localhost:8080}"
API_KEY="${GATEWAY_API_KEY:-${GATEWAY_SECURITY_APIKEY:-}}"

echo "================================================================="
echo "  Enterprise Agentic AI Portfolio — Gateway Ingress Health Check"
echo "================================================================="

if [ -z "${API_KEY}" ]; then
  echo -e "${YELLOW}GATEWAY_API_KEY is unset — agent routes via gateway will fail auth.${NC}"
  echo "Export GATEWAY_API_KEY to match infra/compose/docker-compose.yml before re-running."
fi

# name | display | URL (gateway paths). Module 01 uses /api/v1/lazy-dev/** rewrite.
SERVICES=(
  "Gateway actuator|8080|${GATEWAY_URL}/actuator/health|"
  "Module 01 (Lazy Dev)|via gw|${GATEWAY_URL}/api/v1/lazy-dev/health|auth"
  "Module 02 (UI/UX)|via gw|${GATEWAY_URL}/api/v1/design-system/health|auth"
  "Module 03 (Quality Gate)|via gw|${GATEWAY_URL}/api/v1/quality-gate/health|auth"
  "Module 04 (SDD)|via gw|${GATEWAY_URL}/api/v1/sdd/health|auth"
  "Module 05 (Meta Observer)|via gw|${GATEWAY_URL}/api/v1/meta-observer/health|auth"
  "Module 06 (Marketplace)|via gw|${GATEWAY_URL}/api/v1/marketplace/health|auth"
  "Module 07 (Prototyping)|via gw|${GATEWAY_URL}/api/v1/prototype/health|auth"
  "Module 08 (Paper2Code)|via gw|${GATEWAY_URL}/api/v1/paper2code/health|auth"
  "Module 09 (Architect)|via gw|${GATEWAY_URL}/api/v1/architect/health|auth"
)

for svc in "${SERVICES[@]}"; do
  IFS='|' read -r NAME PORT URL NEED_AUTH <<< "$svc"

  printf "Testing %-28s (%s) ... " "$NAME" "$PORT"
  CURL_ARGS=(-s -f -o /dev/null --connect-timeout 3)
  if [ "$NEED_AUTH" = "auth" ]; then
    if [ -z "${API_KEY}" ]; then
      echo -e "${RED}SKIP (no API key)${NC}"
      continue
    fi
    CURL_ARGS+=(-H "X-API-Key: ${API_KEY}")
  fi

  if curl "${CURL_ARGS[@]}" "$URL"; then
    echo -e "${GREEN}UP${NC}"
  else
    echo -e "${RED}DOWN${NC}"
  fi
done

echo "================================================================="
echo "Host publishes (compose): gateway :8080, prometheus :9090, grafana :3000."
echo "Agent containers stay on agentic-network only (internal 8081–8089)."
echo "================================================================="
