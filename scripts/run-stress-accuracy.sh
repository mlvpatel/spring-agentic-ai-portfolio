#!/usr/bin/env bash
# Laptop Docker stress + accuracy against ai-edge-gateway :8080.
# Loads API key from infra/compose/.env (never prints it).
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
COMPOSE_DIR="$ROOT/infra/compose"
ENV_FILE="$COMPOSE_DIR/.env"
RAW="${STRESS_RAW_DIR:-/tmp/stress-portfolio}"
mkdir -p "$RAW"

set -a
# shellcheck disable=SC1090
source "$ENV_FILE"
set +a

API_KEY="${GATEWAY_SECURITY_APIKEY:?missing GATEWAY_SECURITY_APIKEY}"
GW="http://localhost:8080"
N_CONCUR="${STRESS_CONCURRENCY:-50}"

percentile() {
  local p="$1"; shift
  local -a arr=("$@")
  local n=${#arr[@]}
  if (( n == 0 )); then echo 0; return; fi
  local idx
  idx=$(python3 -c "import math; print(max(0,min($n-1, math.ceil($p/100.0*$n)-1)))")
  printf '%s\n' "${arr[@]}" | sort -n | sed -n "$((idx+1))p"
}

retarget() {
  local url="$1"
  python3 - "$ENV_FILE" "$url" <<'PY'
import sys
from pathlib import Path
p = Path(sys.argv[1]); url = sys.argv[2]
lines = p.read_text().splitlines()
out=[]; found=False
for line in lines:
    if line.startswith("GATEWAY_DOWNSTREAM_URL="):
        out.append(f"GATEWAY_DOWNSTREAM_URL={url}"); found=True
    else:
        out.append(line)
if not found:
    out.append(f"GATEWAY_DOWNSTREAM_URL={url}")
p.write_text("\n".join(out)+"\n")
PY
  # Compose prefers process env over --env-file; keep them in sync.
  export GATEWAY_DOWNSTREAM_URL="$url"
  (cd "$COMPOSE_DIR" && docker compose --env-file .env up -d --force-recreate --no-deps ai-edge-gateway >/dev/null)
  local i code
  for i in $(seq 1 60); do
    code=$(curl -sS -o /dev/null -w '%{http_code}' "$GW/actuator/health" 2>/dev/null || true)
    if [[ "$code" == "200" ]]; then
      sleep 1
      code=$(curl -sS -o /dev/null -w '%{http_code}' "$GW/actuator/health" 2>/dev/null || true)
      if [[ "$code" == "200" ]]; then return 0; fi
    fi
    sleep 0.5
  done
  echo "gateway not healthy after retarget" >&2
  return 1
}

auth_checks() {
  local method="$1" path="$2" body="$3"
  local c1 c2 c3
  c1=$(curl -sS -o /dev/null -w '%{http_code}' -X "$method" "$GW$path" -H 'Content-Type: application/json' -d "$body" || true)
  c2=$(curl -sS -o /dev/null -w '%{http_code}' -X "$method" "$GW$path" -H 'Content-Type: application/json' -H 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.x.y' -d "$body" || true)
  c3=$(curl -sS -o /dev/null -w '%{http_code}' -X "$method" "$GW$path" -H 'Content-Type: application/json' -H 'Authorization: Bearer valid-test-token' -d "$body" || true)
  echo "$c1/$c2/$c3"
}

one_req() {
  local method="$1" path="$2" body="$3" outfile="$4"
  curl -sS -o "$outfile.body" -w '%{http_code} %{time_total}' \
    -X "$method" "$GW$path" \
    -H "Content-Type: application/json" \
    -H "X-API-Key: $API_KEY" \
    -d "$body" > "$outfile.meta" || echo "000 0" > "$outfile.meta"
}

stress_concur() {
  local method="$1" path="$2" body="$3" tag="$4"
  local dir="$RAW/$tag"
  mkdir -p "$dir"
  rm -f "$dir"/r*.meta "$dir"/r*.body
  one_req "$method" "$path" "$body" "$dir/warm"
  seq 1 "$N_CONCUR" | xargs -P "$N_CONCUR" -I{} bash -c '
    method="$1"; path="$2"; body="$3"; dir="$4"; i="$5"; gw="$6"; key="$7"
    curl -sS -o "$dir/r$i.body" -w "%{http_code} %{time_total}" \
      -X "$method" "$gw$path" \
      -H "Content-Type: application/json" \
      -H "X-API-Key: $key" \
      -d "$body" > "$dir/r$i.meta" 2>/dev/null || echo "000 0" > "$dir/r$i.meta"
  ' _ "$method" "$path" "$body" "$dir" {} "$GW" "$API_KEY"

  local ok=0 err=0
  local -a times=()
  local -a codes=()
  local f code t ms hist
  for f in "$dir"/r*.meta; do
    [[ -f "$f" ]] || continue
    code=$(awk '{print $1}' "$f")
    t=$(awk '{print $2}' "$f")
    codes+=("$code")
    ms=$(python3 -c "print(int(round(float('${t:-0}')*1000)))")
    times+=("$ms")
    if [[ "$code" =~ ^2 ]]; then ok=$((ok+1)); else err=$((err+1)); fi
  done
  local p50 p95
  p50=$(percentile 50 "${times[@]}")
  p95=$(percentile 95 "${times[@]}")
  hist=$(printf '%s\n' "${codes[@]}" | sort | uniq -c | tr '\n' ';' | sed 's/;$//')
  echo "ok=$ok err=$err p50=${p50}ms p95=${p95}ms codes=[$hist]"
}

check_acc() {
  local code="$1" body="$2" py="$3"
  ACC_CODE="$code" ACC_BODY="$body" python3 -c "$py" || echo "FAIL:acc-exception"
}

run_app() {
  local name="$1" down="$2" setup="$3" method="$4" path="$5" body="$6" acc_py="$7"
  echo "=== $name ===" >&2
  retarget "$down"
  if [[ -n "$setup" ]]; then
    eval "$setup" >/dev/null || true
  fi
  local adir="$RAW/${name}-acc"
  mkdir -p "$adir"
  one_req "$method" "$path" "$body" "$adir/a"
  local acc_code acc_t
  # Avoid `read <file` inside pipelines (stdin conflict under set -e)
  acc_code=$(awk '{print $1}' "$adir/a.meta" 2>/dev/null || echo 000)
  acc_t=$(awk '{print $2}' "$adir/a.meta" 2>/dev/null || echo 0)
  local acc_body
  acc_body=$(cat "$adir/a.body" 2>/dev/null || true)
  local acc_pass
  acc_pass=$(check_acc "$acc_code" "$acc_body" "$acc_py")
  local auth
  auth=$(auth_checks "$method" "$path" "$body")
  local stress
  stress=$(stress_concur "$method" "$path" "$body" "$name")
  local snippet
  snippet=$(echo "$acc_body" | head -c 160 | tr '\t\n' '  ')
  local line
  line=$(printf '%s\t%s\t%s\t%s\t%s\t%s' "$name" "$acc_pass" "auth=$auth" "$stress" "http=$acc_code" "$snippet")
  printf '%s\n' "$line" | tee -a "$RESULTS"
}

ACC_Y='import os,json
c=os.environ["ACC_CODE"]; b=os.environ["ACC_BODY"]
try: j=json.loads(b)
except Exception: print("FAIL:yagni-badjson"); raise SystemExit
ok=c=="200" and j.get("withinBounds") is True and j.get("mode")=="offline" and "cyclomaticComplexity" in j
print("PASS" if ok else "FAIL:yagni")'
ACC_QG_PASS='import os,json
c=os.environ["ACC_CODE"]
try: j=json.loads(os.environ["ACC_BODY"])
except Exception: print("FAIL:qg-badjson"); raise SystemExit
print("PASS" if c=="200" and j.get("verdict")=="PASS" else "FAIL:qg")'
ACC_QG_FAIL='import os,json
c=os.environ["ACC_CODE"]
try: j=json.loads(os.environ["ACC_BODY"])
except Exception: print("FAIL:qg-secret-badjson"); raise SystemExit
print("PASS" if c=="200" and j.get("verdict")=="FAIL" else "FAIL:qg-secret")'
ACC_RAG_REFUSE='import os,json
c=os.environ["ACC_CODE"]
try: j=json.loads(os.environ["ACC_BODY"])
except Exception: print("FAIL:refuse-badjson"); raise SystemExit
print("PASS" if c=="200" and j.get("refused") is True else "FAIL:refuse")'
ACC_RAG_OK='import os,json
c=os.environ["ACC_CODE"]
try: j=json.loads(os.environ["ACC_BODY"])
except Exception: print("FAIL:rag-badjson"); raise SystemExit
print("PASS" if c=="200" and j.get("refused") is False else "FAIL:rag")'
ACC_SPEC='import os,json
c=os.environ["ACC_CODE"]
try: j=json.loads(os.environ["ACC_BODY"])
except Exception: print("FAIL:spec-badjson"); raise SystemExit
print("PASS" if c=="200" and j.get("status")=="COMPLETED" else "FAIL:spec")'
ACC_OBS='import os,json
c=os.environ["ACC_CODE"]
try: j=json.loads(os.environ["ACC_BODY"])
except Exception: print("FAIL:obs-badjson"); raise SystemExit
print("PASS" if c=="200" and j.get("mode")=="offline" and "failures" in j else "FAIL:obs")'
ACC_MCP='import os,json
c=os.environ["ACC_CODE"]
try: j=json.loads(os.environ["ACC_BODY"])
except Exception: print("FAIL:mcp-badjson"); raise SystemExit
print("PASS" if c=="200" and ("result" in j or j.get("mode") or "echo" in str(j).lower()) else "FAIL:mcp")'
ACC_APP='import os,json
c=os.environ["ACC_CODE"]
try: j=json.loads(os.environ["ACC_BODY"])
except Exception: print("FAIL:app-badjson"); raise SystemExit
print("PASS" if c=="200" and j.get("blueprint")=="crud-api" and "files" in j else "FAIL:app")'
ACC_PAPER='import os,json
c=os.environ["ACC_CODE"]
try: j=json.loads(os.environ["ACC_BODY"])
except Exception: print("FAIL:paper-badjson"); raise SystemExit
print("PASS" if c=="200" and "java" in j and "citations" in j else "FAIL:paper")'
ACC_ARCH='import os,json
c=os.environ["ACC_CODE"]
try: j=json.loads(os.environ["ACC_BODY"])
except Exception: print("FAIL:arch-badjson"); raise SystemExit
print("PASS" if c=="200" and "c4Mermaid" in j else "FAIL:arch")'
ACC_TRIAGE='import os,json
c=os.environ["ACC_CODE"]
try: j=json.loads(os.environ["ACC_BODY"])
except Exception: print("FAIL:triage-badjson"); raise SystemExit
print("PASS" if c=="200" and j.get("severity")=="high" else "FAIL:triage")'
ACC_KOTLIN='import os,json
c=os.environ["ACC_CODE"]
try: j=json.loads(os.environ["ACC_BODY"])
except Exception: print("FAIL:kotlin-badjson"); raise SystemExit
print("PASS" if c=="200" and j.get("hits") else "FAIL:kotlin")'
ACC_KOTLIN_EMPTY='import os,json
c=os.environ["ACC_CODE"]; b=os.environ["ACC_BODY"]
try: j=json.loads(b)
except Exception: print(f"FAIL:empty-corpus:{c}"); raise SystemExit
ok = c=="200" and j.get("refused") is True
print("PASS" if ok else f"FAIL:empty-corpus:{c}")'
ACC_HARNESS='import os,json
c=os.environ["ACC_CODE"]
try: j=json.loads(os.environ["ACC_BODY"])
except Exception: print("FAIL:harness-badjson"); raise SystemExit
print("PASS" if c=="200" and j.get("verdict")=="PASS" else "FAIL:harness")'
ACC_SEC='import os,json
c=os.environ["ACC_CODE"]
try: j=json.loads(os.environ["ACC_BODY"])
except Exception: print("FAIL:sec-badjson"); raise SystemExit
print("PASS" if c=="200" and j.get("status")=="clean" else "FAIL:sec")'

RESULTS="$RAW/results.tsv"
: > "$RESULTS"

run_app "yagni-copilot" "http://yagni-copilot:8081" "" "POST" "/api/v1/patch" \
  '{"changeRequest":"rename method","sourceCode":"public class Demo { public int add(int a, int b) { return a + b; } }"}' \
  "$ACC_Y"

run_app "design-rag-studio" "http://design-rag-studio:8082" \
  'curl -sS -o /dev/null -X POST "$GW/svc/design-rag-studio/api/v1/ingest" -H "Content-Type: application/json" -H "X-API-Key: $API_KEY" -d "{\"csv\":\"token,value,notes\\ncolor.primary,#0B1F33,brand\\nspace.md,16px,rhythm\"}"' \
  "POST" "/svc/design-rag-studio/api/v1/generate" '{"stack":"react","intent":"primary"}' \
  "$ACC_RAG_OK"

(cd "$COMPOSE_DIR" && docker compose --env-file .env up -d --force-recreate --no-deps design-rag-studio >/dev/null)
sleep 4
run_app "design-rag-empty" "http://design-rag-studio:8082" "" "POST" "/svc/design-rag-studio/api/v1/generate" \
  '{"stack":"vue","intent":"primary"}' \
  "$ACC_RAG_REFUSE"

run_app "quality-gate" "http://quality-gate:8083" "" "POST" "/api/v1/gate" \
  '{"source":"public class Ok { int x = 1; }"}' \
  "$ACC_QG_PASS"

run_app "quality-gate-secret" "http://quality-gate:8083" "" "POST" "/api/v1/gate" \
  '{"source":"String password = \"secret\";"}' \
  "$ACC_QG_FAIL"

run_app "spec-orchestrator" "http://spec-orchestrator:8084" "" "POST" "/api/v1/runs" \
  '{"brief":"Add refund API"}' \
  "$ACC_SPEC"

run_app "agent-observability" "http://agent-observability:8085" \
  'curl -sS -o /dev/null -X POST "$GW/api/v1/trajectories" -H "Content-Type: application/json" -H "X-API-Key: $API_KEY" -d "{\"prompt\":\"call search\",\"tool\":\"search\",\"outcome\":\"error timeout\"}"' \
  "POST" "/api/v1/analyze" '{}' \
  "$ACC_OBS"

run_app "mcp-broker" "http://mcp-broker:8086" \
  'curl -sS -o /dev/null -X POST "$GW/api/v1/tools" -H "Content-Type: application/json" -H "X-API-Key: $API_KEY" -d "{\"name\":\"echo\",\"scope\":\"read\",\"schema\":{\"type\":\"object\"}}"' \
  "POST" "/api/v1/invoke" '{"name":"echo","args":{"q":"hi"}}' \
  "$ACC_MCP"

run_app "app-factory" "http://app-factory:8087" "" "POST" "/svc/app-factory/api/v1/generate" \
  '{"blueprint":"crud-api","appName":"demo-api"}' \
  "$ACC_APP"

run_app "paper-algorithm-lab" "http://paper-algorithm-lab:8088" \
  'curl -sS -o /dev/null -X POST "$GW/api/v1/papers" -H "Content-Type: application/json" -H "X-API-Key: $API_KEY" -d "{\"title\":\"Sort survey\",\"text\":\"Quicksort average n log n\"}"' \
  "POST" "/api/v1/synthesize" '{"topic":"sort"}' \
  "$ACC_PAPER"

run_app "system-architect" "http://system-architect:8089" "" "POST" "/api/v1/architect" \
  '{"requirements":"Build checkout API for EU retail"}' \
  "$ACC_ARCH"

run_app "ai-edge-gateway" "http://yagni-copilot:8081" "" "POST" "/api/v1/patch" \
  '{"changeRequest":"rename method","sourceCode":"public class Demo { public int add(int a, int b) { return a + b; } }"}' \
  "$ACC_Y"

run_app "multimodal-support-desk" "http://multimodal-support-desk:8091" "" "POST" "/api/v1/triage" \
  '{"ticket":"Payment outage in checkout","imageMeta":"png 800x600"}' \
  "$ACC_TRIAGE"

(cd "$COMPOSE_DIR" && docker compose --env-file .env up -d --force-recreate --no-deps kotlin-rag-microservice >/dev/null)
sleep 4
run_app "kotlin-rag-empty" "http://kotlin-rag-microservice:8092" "" "POST" "/api/v1/query" \
  '{"q":"vector"}' \
  "$ACC_KOTLIN_EMPTY"

run_app "kotlin-rag-microservice" "http://kotlin-rag-microservice:8092" \
  'curl -sS -o /dev/null -X POST "$GW/svc/kotlin-rag-microservice/api/v1/ingest" -H "Content-Type: application/json" -H "X-API-Key: $API_KEY" -d "{\"title\":\"RAG notes\",\"text\":\"vector retrieval with citations\"}"' \
  "POST" "/api/v1/query" '{"q":"vector"}' \
  "$ACC_KOTLIN"

run_app "ai-validated-integration-harness" "http://ai-validated-integration-harness:8093" "" "POST" "/api/v1/validate" \
  '{"contract":"status=UP","observed":"status=UP ok"}' \
  "$ACC_HARNESS"

run_app "security-review-assistant" "http://security-review-assistant:8094" "" "POST" "/api/v1/review" \
  '{"scope":"auth-review","artifact":"login uses api key auth header"}' \
  "$ACC_SEC"

retarget "http://yagni-copilot:8081"
echo "DONE results=$RESULTS" >&2
