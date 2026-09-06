# MiniMart

A small online store (Spring Boot + Postgres + Thymeleaf) built as a demo application to
showcase API, UI, integration, performance and end-to-end testing.

## Architecture

```
minimart/
  app/                 Spring Boot app — modular monolith (catalog / orders / users packages)
  tests/
    api/               RestAssured + JUnit5 — HTTP contract tests against the running app
    integration/        Spring Boot Test + Testcontainers — real Postgres, no HTTP/browser
    ui-playwright/       Playwright (JS) — UI behaviour tests, Page Object Model
    ui-cypress/          Cypress (JS) — the same UI coverage in a different tool, for comparison
    e2e/                 Playwright (JS) — a handful of full golden-path user journeys,
                          verified across the UI *and* the API afterwards
    performance/         JMeter — load / stress / soak test plans
  docker-compose.yml    Postgres for local development
```

`catalog` and `orders` are split into distinct packages deliberately, so a later phase can
extract them into separate services (for a distributed-tracing/observability demo) without
restructuring the domain.

## Running the app locally

```bash
docker compose up -d          # Postgres on :5432
cd app
mvn clean package             # produces target/minimart-app-boot.jar (executable) and
                               # target/minimart-app.jar (plain jar, used as a library by
                               # tests/integration)
java -jar target/minimart-app-boot.jar
```

The app is then at `http://localhost:8080` (UI: `/products`; API: `/api/products`, `/api/orders`).

## Running the test suites

All suites assume the app is running at `http://localhost:8080` unless noted otherwise.

| Suite | Command | Notes |
|---|---|---|
| API (RestAssured) | `mvn -f tests/api/pom.xml test` | Needs the app running |
| Integration (Testcontainers) | `mvn -f tests/integration/pom.xml test` | Needs Docker; starts its own Postgres, app does **not** need to be running |
| UI — Playwright | `cd tests/ui-playwright && npm install && npx playwright install && npx playwright test` | Needs the app running |
| UI — Cypress | `cd tests/ui-cypress && npm install && npx cypress run` | Needs the app running |
| E2E golden paths | `cd tests/e2e && npm install && npx playwright test` | Needs the app running |
| Performance (JMeter) | see `tests/performance/README.md` | Needs the app running |

## Why five different testing layers

- **API tests** verify backend contracts fast, without a browser.
- **Integration tests** verify real DB/transaction behaviour (constraints, rollbacks) that
  mocks would hide.
- **UI tests** (Playwright and Cypress, both showcased) verify the actual rendered app works
  for a user — two different tools/architectures against the same app, for comparison.
- **E2E tests** are a thin top layer: only the critical golden paths, checked end-to-end
  through the UI and then cross-verified against the API/DB — not a place for exhaustive
  edge cases (those live in the API suite).
- **Performance tests** (load / stress / soak) characterize how the same critical flows
  behave under expected traffic, beyond-capacity traffic, and sustained traffic.

This test suite is considered complete for this phase of the project — later phases
(observability, search, data engineering) add new app capabilities with their own tests,
without modifying this one.

## Roadmap

1. **Phase 1 (this phase)** — core app + the five testing layers above.
2. **Phase 2 — Search**: OpenSearch indexing the catalog, a real search endpoint.
3. **Phase 3 — Observability**: split `catalog`/`orders` into separate services,
   instrument with OpenTelemetry → Prometheus/Grafana/Tempo.
4. **Phase 4 — Data Engineering**: order events → Kafka → batch/stream ETL into an
   analytics schema, SQL aggregations.
