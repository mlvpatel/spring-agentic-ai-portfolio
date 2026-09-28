# Engineering quality gate design

P3 — CI-facing hybrid gate: deterministic static checks first; optional LLM explanation later.

## Goal

Run deterministic SAST-style rules on source and return structured PASS/FAIL with findings.

## In scope

- Boot 4.1.1 parent; Spring AI 2.0.1 (offline default when no API key)
- POST /api/v1/gate
- API key auth; offline mode

## Out of scope

- Live SpotBugs CLI, GitHub Actions connector, live LLM
- Boot 4 / AI 2.0

## Acceptance criteria

1. Tests green on reactor
2. HIGH finding → FAIL; clean code → PASS
3. Auth bypass rejected; health clean

## Stack pin

Boot 4.1.1 + Spring AI 2.0.1.

## Endpoints

| Method | Path | Auth |
|---|---|---|
| GET | /actuator/health | no |
| POST | /api/v1/gate | yes |
