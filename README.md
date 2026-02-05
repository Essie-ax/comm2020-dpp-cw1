# COMM2020 CW1 Prototype (v0.1.0)

This repo contains the CW1 vertical slice for the Digital Product Passport Builder League.

## CW1 Vertical Slice (Must Demo)
1) Create passport (draft) from template
2) Run validation and compute scores
3) Attach evidence
4) Submit passport to one challenge
5) Leaderboard updates
6) Demo at least one GameKeeper action (create template or create challenge)

## Tech Stack
- Java (backend)
- MySQL (database)

## Local Setup (High level)
1) Create MySQL database and user (see `.env.example`)
2) Run migration: `db/migrations/001_init.sql`
3) Run seed: `db/seed/seed.sql`
4) Start backend
5) Open the app

## API Demo (Module B)
1) Copy `.env.example` to `.env` and set DB credentials.
2) Run migration: `mysql -u <user> -p < db/migrations/001_init.sql`
3) Run seed: `mysql -u <user> -p comm2020_dpp < db/seed/seed.sql`
4) Start backend: `mvn -q -DskipTests package && java -cp target/comm2020-dpp-cw1-0.1.0.jar uk.ac.comm2020.App`

Example curls:
```
curl "http://localhost:8080/api/templates?category=Battery"
curl "http://localhost:8080/api/products?category=Battery"
curl -X POST "http://localhost:8080/api/passports" -H "Content-Type: application/json" -d "{ \"productId\": 1, \"templateId\": 1 }"
curl -X PUT "http://localhost:8080/api/passports/1" -H "Content-Type: application/json" -d "{ \"fields\": { \"name\": \"EcoCell A1\", \"brand\": \"EcoBrand\", \"origin\": \"UK\", \"chemistry\": \"Li-ion\" } }"
curl "http://localhost:8080/api/passports/1"
```

## Module B UI Demo
- Authoring page: `http://localhost:8080/authoring.html`
- Consumer page: `http://localhost:8080/consumer.html?id=1`
- Flow: load category -> create passport -> fill fields -> save -> open consumer view with the new passportId

## Contracts
- API contract: `docs/api-contract.md`
- Data dictionary: `docs/data-dictionary.md`
- Contract change process: `docs/contract-change-process.md`

## Notes
- Do not change DB schema/API shapes without following the contract change process.
- Do not commit secrets (use environment variables).
