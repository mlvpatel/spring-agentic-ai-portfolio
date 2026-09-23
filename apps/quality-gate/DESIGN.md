# Engineering quality gate design

P3 — CI-facing hybrid gate: deterministic static checks first; optional LLM explanation later.

## Goal

Run deterministic SAST-style rules on source and return structured PASS/FAIL with findings.

## In scope

- Boot 3.3.3 parent; AI BOM 1.0.0-M2 unused by default
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

Boot 3.3.3 (AI 2.0 later).

## Endpoints

| Method | Path | Auth |
|---|---|---|
| GET | /actuator/health | no |
| POST | /api/v1/gate | yes |
