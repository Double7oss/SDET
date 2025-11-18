SHELL := /bin/bash

# Use local Maven if available, otherwise fall back to a Dockerized Maven image.
MVN := $(shell command -v mvn 2>/dev/null)
ifeq ($(strip $(MVN)),)
MVN := docker run --rm -v $(PWD):/workspace -w /workspace maven:3.9.9-eclipse-temurin-21 mvn
endif

.PHONY: build up down logs test coverage seed clean

build:
	$(MVN) -DskipTests=false clean verify

up:
	docker compose up -d --build

down:
	docker compose down -v

logs:
	docker compose logs -f app

test:
	$(MVN) -DskipTests=false test

coverage: build
	@echo "Open target/site/jacoco/index.html for the report."

seed:
	@echo "Seeding sample deals via curl..."
	@curl -s -X POST http://localhost:8080/api/deals/import \
	  -H 'Content-Type: application/json' \
	  --data-binary @sample/sample_deals.json | jq .

clean:
	rm -rf target
