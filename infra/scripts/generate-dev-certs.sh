#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
CERT_DIR="$ROOT/certs"
mkdir -p "$CERT_DIR"
openssl req -x509 -nodes -newkey rsa:2048 \
  -keyout "$CERT_DIR/key.pem" \
  -out "$CERT_DIR/cert.pem" \
  -days 365 \
  -subj "/CN=localhost/O=portfolio-dev"
chmod 600 "$CERT_DIR/key.pem"
echo "Wrote $CERT_DIR/cert.pem and key.pem"
