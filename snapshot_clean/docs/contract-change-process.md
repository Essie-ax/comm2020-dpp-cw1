# Contract Change Process (DB + API)

## Golden Rules
1) DB schema is the source of truth (`db/migrations/`)
2) API shapes are the source of truth (`docs/api-contract.md`)
3) Any schema/API change must go through PR + at least 1 review.

## When you need a new field / endpoint
1) Create a GitHub Issue describing what/why (CW1 flow impacted).
2) Update contracts first:
   - Update `docs/api-contract.md` (if API changes)
   - Add a new migration file (do NOT edit old migrations once merged)
   - Update `db/seed/seed.sql` if demo data needs it
3) Implement on a feature branch.
4) Open PR, link the issue, get at least one approval.
5) Merge into `main` (or `develop` if you later introduce it).
