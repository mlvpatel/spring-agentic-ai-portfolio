#!/usr/bin/env bash
# ==============================================================================
# Enterprise Agentic AI Portfolio - Mutual TLS (mTLS) Certificate Generator
# Generates Root CA, Server KeyStore, Client KeyStore, and TrustStore (PKCS12).
#
# PKCS#12 material is NEVER committed. Generate locally (or in CI) and point
# the gateway mtls profile at the output directory.
#
# Usage:
#   ./scripts/generate-mtls-certs.sh [OUTPUT_DIR] [PASSWORD]
#   ./scripts/generate-mtls-certs.sh --install              # gen + copy into gateway ssl/
#   ./scripts/generate-mtls-certs.sh --rotate-today         # same-day local rotation (validity=1)
#   VALIDITY_DAYS=7 ./scripts/generate-mtls-certs.sh       # custom validity
#
# Env:
#   VALIDITY_DAYS  Certificate validity in days (default 730; --rotate-today forces 1)
#   SSL_PASSWORD   Keystore password (default changeit; overridden by arg 2)
# ==============================================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
GATEWAY_DIR="${REPO_ROOT}/archive/legacy-portfolio/10-spring-ai-cloud-gateway"

INSTALL=0
ROTATE_TODAY=0
POSITIONAL=()

while [[ $# -gt 0 ]]; do
  case "$1" in
    --install)
      INSTALL=1
      shift
      ;;
    --rotate-today|--same-day)
      ROTATE_TODAY=1
      shift
      ;;
    -h|--help)
      sed -n '2,20p' "$0"
      exit 0
      ;;
    *)
      POSITIONAL+=("$1")
      shift
      ;;
  esac
done

OUTPUT_DIR="${POSITIONAL[0]:-${REPO_ROOT}/certs/mtls}"
PASSWORD="${POSITIONAL[1]:-${SSL_PASSWORD:-changeit}}"
if [[ "${ROTATE_TODAY}" -eq 1 ]]; then
  VALIDITY_DAYS="1"
else
  VALIDITY_DAYS="${VALIDITY_DAYS:-730}"
fi

# Resolve keytool (prefer JAVA_HOME / PATH; avoid hardcoding Homebrew Cellar versions)
if [ -n "${JAVA_HOME:-}" ] && [ -x "${JAVA_HOME}/bin/keytool" ]; then
  KEYTOOL="${JAVA_HOME}/bin/keytool"
elif command -v keytool &> /dev/null; then
  KEYTOOL="$(command -v keytool)"
elif [ -x "/opt/homebrew/opt/openjdk/bin/keytool" ]; then
  KEYTOOL="/opt/homebrew/opt/openjdk/bin/keytool"
else
  echo "ERROR: keytool not found. Set JAVA_HOME or install a JDK." >&2
  exit 1
fi

echo "Generating mTLS certificates in: ${OUTPUT_DIR}"
echo "  keytool=${KEYTOOL}"
echo "  validityDays=${VALIDITY_DAYS}"
mkdir -p "${OUTPUT_DIR}"

# 1. Generate Root CA
echo "  [1/4] Generating Certificate Authority (Root CA)..."
rm -f "${OUTPUT_DIR}/ca.p12" "${OUTPUT_DIR}/ca.crt"
"${KEYTOOL}" -genkeypair -v \
  -alias root-ca \
  -keyalg RSA -keysize 4096 \
  -sigalg SHA256withRSA \
  -dname "CN=AgenticAI-Root-CA,OU=Security,O=EnterprisePortfolio,L=SanFrancisco,ST=California,C=US" \
  -validity "${VALIDITY_DAYS}" \
  -keystore "${OUTPUT_DIR}/ca.p12" \
  -storetype PKCS12 \
  -storepass "${PASSWORD}" \
  -keypass "${PASSWORD}" \
  -ext BasicConstraints:critical=ca:true

"${KEYTOOL}" -exportcert \
  -alias root-ca \
  -keystore "${OUTPUT_DIR}/ca.p12" \
  -storetype PKCS12 \
  -storepass "${PASSWORD}" \
  -rfc \
  -file "${OUTPUT_DIR}/ca.crt"

# 2. Generate Server KeyStore (Gateway & Microservices)
echo "  [2/4] Generating Server KeyStore..."
rm -f "${OUTPUT_DIR}/server-keystore.p12" "${OUTPUT_DIR}/server.csr" "${OUTPUT_DIR}/server.crt"
"${KEYTOOL}" -genkeypair -v \
  -alias gateway-server \
  -keyalg RSA -keysize 2048 \
  -sigalg SHA256withRSA \
  -dname "CN=gateway,OU=Security,O=EnterprisePortfolio,L=SanFrancisco,ST=California,C=US" \
  -validity "${VALIDITY_DAYS}" \
  -keystore "${OUTPUT_DIR}/server-keystore.p12" \
  -storetype PKCS12 \
  -storepass "${PASSWORD}" \
  -keypass "${PASSWORD}" \
  -ext SAN=dns:localhost,dns:gateway,dns:host.docker.internal,ip:127.0.0.1

"${KEYTOOL}" -certreq \
  -alias gateway-server \
  -keystore "${OUTPUT_DIR}/server-keystore.p12" \
  -storetype PKCS12 \
  -storepass "${PASSWORD}" \
  -file "${OUTPUT_DIR}/server.csr"

"${KEYTOOL}" -gencert \
  -alias root-ca \
  -keystore "${OUTPUT_DIR}/ca.p12" \
  -storetype PKCS12 \
  -storepass "${PASSWORD}" \
  -infile "${OUTPUT_DIR}/server.csr" \
  -outfile "${OUTPUT_DIR}/server.crt" \
  -validity "${VALIDITY_DAYS}" \
  -ext SAN=dns:localhost,dns:gateway,dns:host.docker.internal,ip:127.0.0.1 \
  -rfc

"${KEYTOOL}" -importcert -noprompt \
  -alias root-ca \
  -keystore "${OUTPUT_DIR}/server-keystore.p12" \
  -storetype PKCS12 \
  -storepass "${PASSWORD}" \
  -file "${OUTPUT_DIR}/ca.crt"

"${KEYTOOL}" -importcert -noprompt \
  -alias gateway-server \
  -keystore "${OUTPUT_DIR}/server-keystore.p12" \
  -storetype PKCS12 \
  -storepass "${PASSWORD}" \
  -file "${OUTPUT_DIR}/server.crt"

# 3. Generate Client KeyStore (Inter-Service mTLS Client)
echo "  [3/4] Generating Client KeyStore..."
rm -f "${OUTPUT_DIR}/client-keystore.p12" "${OUTPUT_DIR}/client.csr" "${OUTPUT_DIR}/client.crt"
"${KEYTOOL}" -genkeypair -v \
  -alias gateway-client \
  -keyalg RSA -keysize 2048 \
  -sigalg SHA256withRSA \
  -dname "CN=gateway-client,OU=Security,O=EnterprisePortfolio,L=SanFrancisco,ST=California,C=US" \
  -validity "${VALIDITY_DAYS}" \
  -keystore "${OUTPUT_DIR}/client-keystore.p12" \
  -storetype PKCS12 \
  -storepass "${PASSWORD}" \
  -keypass "${PASSWORD}"

"${KEYTOOL}" -certreq \
  -alias gateway-client \
  -keystore "${OUTPUT_DIR}/client-keystore.p12" \
  -storetype PKCS12 \
  -storepass "${PASSWORD}" \
  -file "${OUTPUT_DIR}/client.csr"

"${KEYTOOL}" -gencert \
  -alias root-ca \
  -keystore "${OUTPUT_DIR}/ca.p12" \
  -storetype PKCS12 \
  -storepass "${PASSWORD}" \
  -infile "${OUTPUT_DIR}/client.csr" \
  -outfile "${OUTPUT_DIR}/client.crt" \
  -validity "${VALIDITY_DAYS}" \
  -rfc

"${KEYTOOL}" -importcert -noprompt \
  -alias root-ca \
  -keystore "${OUTPUT_DIR}/client-keystore.p12" \
  -storetype PKCS12 \
  -storepass "${PASSWORD}" \
  -file "${OUTPUT_DIR}/ca.crt"

"${KEYTOOL}" -importcert -noprompt \
  -alias gateway-client \
  -keystore "${OUTPUT_DIR}/client-keystore.p12" \
  -storetype PKCS12 \
  -storepass "${PASSWORD}" \
  -file "${OUTPUT_DIR}/client.crt"

# 4. Generate TrustStore containing Root CA
echo "  [4/4] Generating TrustStore..."
rm -f "${OUTPUT_DIR}/truststore.p12"
"${KEYTOOL}" -importcert -noprompt \
  -alias root-ca \
  -keystore "${OUTPUT_DIR}/truststore.p12" \
  -storetype PKCS12 \
  -storepass "${PASSWORD}" \
  -file "${OUTPUT_DIR}/ca.crt"

install_into_gateway() {
  local dest_main="${GATEWAY_DIR}/src/main/resources/ssl"
  local dest_test="${GATEWAY_DIR}/src/test/resources/ssl"
  mkdir -p "${dest_main}" "${dest_test}"
  for f in ca.p12 ca.crt \
           server-keystore.p12 server.crt server.csr \
           client-keystore.p12 client.crt client.csr \
           truststore.p12; do
    cp -f "${OUTPUT_DIR}/${f}" "${dest_main}/${f}"
    cp -f "${OUTPUT_DIR}/${f}" "${dest_test}/${f}"
  done
  echo "Installed copies into:"
  echo "  ${dest_main}"
  echo "  ${dest_test}"
  echo "(These paths are gitignored — do not commit.)"
}

if [[ "${INSTALL}" -eq 1 ]]; then
  install_into_gateway
fi

echo "Successfully generated mTLS artifacts in: ${OUTPUT_DIR}"
echo "  - Server KeyStore: ${OUTPUT_DIR}/server-keystore.p12 (alias: gateway-server)"
echo "  - Client KeyStore: ${OUTPUT_DIR}/client-keystore.p12 (alias: gateway-client)"
echo "  - Common TrustStore: ${OUTPUT_DIR}/truststore.p12 (alias: root-ca)"
echo ""
echo "Local / same-day rotation:"
echo "  ./scripts/generate-mtls-certs.sh --rotate-today --install"
echo "Default (long-lived local) + install for classpath mtls profile:"
echo "  ./scripts/generate-mtls-certs.sh --install"
echo "Or point Spring at the output dir (no install):"
echo "  See GATEWAY_MTLS_* overrides in application-mtls.yml"
