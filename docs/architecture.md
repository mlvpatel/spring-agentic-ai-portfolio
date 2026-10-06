# Architecture

Archify 3.0.1 rendered [portfolio.architecture.html](architecture/portfolio.architecture.html) from public main `bb31e6d`. The typed source is [portfolio.architecture.json](architecture/portfolio.architecture.json).

A caller sends HTTP to ai-edge-gateway on port 8080. The gateway requires an API key, uses Redis for the rate limit when `REDIS_URL` is set, and forwards to the domain apps. Design RAG and paper-algorithm-lab use H2 until a Postgres compose profile is on. `PortfolioAiClient` calls ChatClient only when a ChatModel bean and an API key are both set. Compose has no Prometheus or Grafana service. TLS on port 8443 is the optional `tls` profile.
