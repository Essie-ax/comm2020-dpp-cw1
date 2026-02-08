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

## Run with MySQL locally
Set environment variables and run the app. If `DB_URL` is not provided the app
will fall back to the in-memory template DAO so tests and CI continue to pass.

Example env and commands:

```
export DB_URL="jdbc:mysql://localhost:3306/comm2020?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
export DB_USER="your_db_user"
export DB_PASS="your_db_pass"
mvn clean package
java -cp target/comm2020-dpp-cw1-0.1.0.jar uk.ac.comm2020.WebApp
```

On Windows PowerShell use `$env:DB_URL=...` etc.

## Contracts
- API contract: `docs/api-contract.md`
- Data dictionary: `docs/data-dictionary.md`
- Contract change process: `docs/contract-change-process.md`

## Notes
- Do not change DB schema/API shapes without following the contract change process.
- Do not commit secrets (use environment variables).
