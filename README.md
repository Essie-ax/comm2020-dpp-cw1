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

## Contracts
- API contract: `docs/api-contract.md`
- Data dictionary: `docs/data-dictionary.md`
- Contract change process: `docs/contract-change-process.md`

## Notes
- Do not change DB schema/API shapes without following the contract change process.
- Do not commit secrets (use environment variables).
