# Spec orchestrator design

P4 — Brief → Spec Kit-shaped artifacts → routed worker plan (offline).

## Goal

Persist a run from a product brief with constitution/spec/tasks/routing artifacts.

## Stack pin

Boot 4.1.1 + Spring AI 2.0.1.

## Endpoints

POST /api/v1/runs · GET /api/v1/runs/{id} · GET /actuator/health
