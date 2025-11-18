# FX Deal Import – Plain Language Guide

This note explains the assignment, the shape of the request & response objects, and the algorithm the project uses to process FX deal rows. It is written for someone who understands basic programming ideas (loops, validations, databases) but is not familiar with Java or Spring Boot.

## 1. What the assignment asks for (from `SDET_Assignment.pdf`)

- Accept a batch of FX deal rows, validate every field, and store good rows in a real database (PostgreSQL in this project).
- Never import the same deal twice; duplicates must be detected per `dealId`.
- Do not roll back the whole batch when one row is bad. Each row is handled independently and the system must report which rows passed or failed.
- Ship reproducible tooling: Docker Compose to bring up the app + DB, Make targets, tests with full coverage of the parsing/validation/dedup logic (JaCoCo gate at 100%), a Postman collection, and a K6 performance script.

## 2. High-level architecture

```
Client (Postman, k6, etc.)
        │ sends JSON array
        ▼
REST Controller (`src/main/java/com/example/fxdeals/web/DealsController.java`)
        │ turns HTTP body into DealRequest objects
        ▼
Core Service (`core/DealImportService`)
        │ orchestrates validation + persistence row by row
        ▼
Parser (`core/DealParser`)
        │ converts request row → domain object, reports validation errors
        ▼
Repository (`repository/DealRepository`)
        │ wraps Spring Data JPA and talks to PostgreSQL table `deals`
        ▼
Database (PostgreSQL via Docker Compose / Testcontainers)
```

Every layer has a single responsibility, which keeps the code testable:

- Web layer only cares about HTTP requests/responses.
- Core layer (parser + import service + `ImportResponse`) implements assignment rules.
- Repository/DB layer persists `Deal` entities without knowing anything about HTTP.

## 3. Why the request looks the way it does

The POST endpoint `/api/deals/import` receives a JSON array because the assignment wants *batch* import. Each element mirrors the `DealRequest` class (`src/main/java/com/example/fxdeals/core/DealRequest.java`) with five fields the client already knows:

| Field | Why it exists |
| --- | --- |
| `dealId` | Acts as a unique identifier. We can spot duplicates simply by reusing the same value and letting the DB’s primary key reject it. |
| `fromCurrency` / `toCurrency` | Three-letter ISO-4217 codes tell us what pair was traded. Keeping both prevents ambiguity (e.g., USD→EUR vs. EUR→USD). |
| `timestamp` | The moment the deal happened. Stored as an ISO-8601 string so any language can produce it without locale issues. |
| `amount` | Positive number representing deal amount in the ordering currency. |

Keeping the request object this small helps clients compose JSON without learning Java. All validations happen server-side, so even if a client accidentally sends lowercase currency codes or an invalid UUID, the server will respond with clear errors instead of crashing.

## 4. Why the response is a summary object

Instead of returning the entire stored dataset, the API responds with a single `ImportResponse` (`core/ImportResponse.java`). It contains:

- `total`, `accepted`, `duplicates`, `rejected`: counters that prove the “no rollback, partial success” rule is respected.
- `errors`: a list of `ErrorRow` entries (`core/ErrorRow.java`) documenting which row index failed and why (e.g., `INVALID_TIMESTAMP`).

This format lets basic clients (Postman scripts, shell pipelines, QA automation) decide what to do next without parsing Java stack traces. It also maps directly onto the assignment’s acceptance criteria: you can tell which rows stuck, which were skipped due to duplication, and which were invalid.

## 5. The import algorithm in plain English

1. **Receive the batch** – the controller collects the JSON array into a `List<DealRequest>` and hands it to `DealImportService.importBatch`.
2. **Prepare counters** – the service starts counters for accepted, duplicate, and rejected rows, plus a builder for the eventual `ImportResponse`.
3. **Loop row by row**:
   - **Parse & validate** (`DealParser.parse`)  
     - Check `dealId` exists and is a UUID.  
     - Ensure both currency codes exist and are three uppercase letters.  
     - Parse the timestamp as an ISO-8601 instant.  
     - Verify `amount` exists and is positive.  
     - Convert the clean data into a `Deal` domain object if all checks pass.  
     - If any check fails, record an `ErrorRow` with the row’s position, an error code, and a human-readable message; increment the “rejected” counter and move to the next row.
   - **Persist** (`DealImportService.importOne`)  
     - Save the `Deal` through `DealRepository`.  
     - If the DB throws a `DataIntegrityViolationException`, it means the primary key (`deal_id`) already existed, so the row counts as a duplicate but does not stop the batch.  
     - Any other runtime failure marks the row as rejected with a generic persistence error.
4. **Return summary** – after the loop, the service fills in the totals and returns the built `ImportResponse`, which the controller returns as JSON.

Because each row runs inside its own transaction (`Propagation.REQUIRES_NEW`), a failing row never rolls back a previously accepted row—exactly what the assignment requires.

## 6. How specific classes support the algorithm

- `DealsController` (`src/main/java/com/example/fxdeals/web/DealsController.java`): thin HTTP adapter; it does not contain business rules, so it stays easy to test with Spring MVC tests.
- `DealImportService` (`src/main/java/com/example/fxdeals/core/DealImportService.java`): orchestrator that owns the counters, the loop, and the partial-success logic.
- `DealParser` (`src/main/java/com/example/fxdeals/core/DealParser.java`): validation engine; by isolating parsing here we can give it 100% unit-test coverage independent of HTTP or DB concerns.
- `ImportResponse` & `ErrorRow` (`src/main/java/com/example/fxdeals/core`): plain objects that describe the outcome of the batch in a client-friendly way.
- `Deal` + `DealRepository` (`src/main/java/com/example/fxdeals/domain` + `repository`): JPA entity mapped to the `deals` table. PostgreSQL enforces uniqueness on `deal_id`, which is how duplicates are detected without writing custom SQL.
- `application.yml` (`src/main/resources/application.yml`) + `docker-compose.yml`: configure the app to talk to Postgres locally or inside Docker, keeping environments consistent.

## 7. How this satisfies the PDF requirements

- **Validation & structure** – handled entirely in `DealParser`.
- **Deduplication & no rollback** – PostgreSQL primary key + per-row transactions in `DealImportService`.
- **Actual DB & deployment** – Docker Compose spins up both the database and the app; Flyway migrations create the schema automatically.
- **Testing coverage** – the Maven build wires JaCoCo to enforce 100% coverage for the `core` package; RestAssured + Testcontainers provide API/integration tests; K6 and Postman artifacts live under `k6/` and `postman/`.
- **Documentation** – README plus this guide explain how the pieces fit together.

## 8. Tips if you are not a Java developer

- Think of each Java class as the equivalent of a module or component in your preferred language.
- Spring’s annotations (e.g., `@RestController`, `@Service`, `@Entity`) mainly tell the framework how to wire dependencies; they do not change the underlying algorithm.
- If you want to experiment, you can run `make up` to start the whole stack and `make build` to run the full test + coverage suite without writing any Java tooling yourself.

With these pieces, you can map every assignment requirement to a concrete, testable part of the codebase, and reason about the request/response flow without needing to dive into JVM internals.
