# FX Deals – SDET Assignment Reference

A complete, reproducible implementation for the assignment requirements using **Spring Boot**, **PostgreSQL**, **Docker Compose**, **JaCoCo (100% gate on core)**, **RestAssured**, **Testcontainers**, **K6**, and a **Postman** collection.

## Why this structure

- **Actual DB**: PostgreSQL with Flyway migrations.
- **Row validation & parsing**: implemented in `com.example.fxdeals.core.DealParser` and used by `DealImportService` to achieve partial success semantics.
- **Deduplication**: primary-key on `deal_id` and graceful handling of `DataIntegrityViolationException`.
- **No rollback for the batch**: each row is imported independently; invalid rows are reported, valid rows are saved, duplicates are skipped.
- **Coverage gate**: JaCoCo enforces **100% line & branch** coverage on the `com.example.fxdeals.core` package only (parsing, validation, deduplication, import flow). Build fails if target is not met.
- **API tests**: RestAssured integration test demonstrates non-duplication and partial success.
- **Performance**: K6 script supplied.
- **Reproducibility**: One-liners via `Makefile`.

## How to run

### Prerequisites
- Docker & Docker Compose
- Make (optional but recommended)
- Java 17 (for local build/tests)

### Run the stack
```bash
make up            # builds the app and starts Postgres + app
make logs          # tail application logs
make seed          # posts a small sample file to the API
make down          # stop and clean volumes
```

The API will be available at `http://localhost:8080`.

### Run tests & coverage gate
```bash
make build         # runs unit + integration tests; generates JaCoCo and enforces 100% gate on core
# or
make test          # only unit tests
```

Open coverage report: `target/site/jacoco/index.html`.

### API

**POST** `/api/deals/import` – Accepts a JSON array of rows:

```json
[
  {
    "dealId": "3f2fb581-8e20-4bb4-8f1e-39f7d4c9e501",
    "fromCurrency": "USD",
    "toCurrency": "EUR",
    "timestamp": "2025-01-01T00:00:00Z",
    "amount": 123.45
  }
]
```

**Response** (summary with partial success):

```json
{
  "total": 3,
  "accepted": 1,
  "duplicates": 1,
  "rejected": 1,
  "errors": [{"index":2,"code":"INVALID_DEAL_ID","message":"dealId must be UUID"}]
}
```

### Postman

Import `postman/FxDeals.postman_collection.json` and hit **Import Deals**.

### K6

With the stack running:

```bash
k6 run k6/import_deals.js
```

### Notes on exclusions for the 100% gate

- The gate targets `com.example.fxdeals.core` only. This is the production code directly tied to parsing, validation, deduplication, and import flow. 
- Bootstrap code (`FxDealsApplication`), Spring MVC controllers, persistence adapters, and generated proxies are excluded by package selection. This keeps the gate meaningful while avoiding penalizing framework glue.

## Requirement mapping

- **Accept & persist deal rows to DB** → `/api/deals/import` + `DealRepository` + Flyway migration.
- **Validate row structure** (missing fields, types, ISO codes, timestamp, amount) → `DealParser` unit-tested.
- **Do not import the same request twice** → primary-key on `deal_id` + duplicate handling in `DealImportService`.
- **No rollback allowed; partial success preserved** → per-row insert with error isolation.
- **Use actual DB (Postgres)** → Docker Compose + Testcontainers for tests.
- **Deployment + sample file (Docker Compose)** → `docker-compose.yml` + `sample/sample_deals.json`.
- **Maven project with proper logging & error handling** → see code.
- **Unit/Integration/Spring MVC tests** with coverage → see `src/test` and JaCoCo gate.
- **K6 performance test** → `k6/import_deals.js`.
- **Postman collection** → `postman/FxDeals.postman_collection.json`.
- **Makefile** → `make up`, `make build`, `make coverage` etc.

## Caveats / Next steps
- For simplicity, currency codes are validated only as 3 uppercase letters; you may replace with a full ISO-4217 registry check.
- Consider adding idempotency keys per request batch if you need exactly-once semantics across batches.
- Add structured logging and correlation IDs for production.
