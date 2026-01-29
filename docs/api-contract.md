## Conventions (MUST FOLLOW)
API uses camelCase; DB uses snake_case. Evidence date field in API is evidenceDate.
### Naming
- API JSON uses **camelCase** (e.g., evidenceDate, fieldKey).
- Database uses **snake_case** (e.g., evidence_date, field_key).

### Enums (exact values)
- role: `PLAYER | GAMEKEEPER`
- passport.status: `DRAFT | SUBMITTED | PUBLISHED`
- evidence.type: `CERTIFICATE | REPORT | PHOTO` (CW1)

### Score scale
- completenessScore and confidenceScore are **0.00–1.00** (DECIMAL), UI may display as percentage.
- Challenge constraints minCompleteness uses the same 0.00–1.00 scale.

### Date/Time format
- Date: `YYYY-MM-DD`
- Timestamp: ISO-8601 `YYYY-MM-DDTHH:mm:ssZ`


# API Contract v0.1 (CW1)

Base URL: `/api`  
Content-Type: `application/json`

## Common Response Shape

Success:
```json
{ "success": true, "data": {} }
```

Error:
```json
{ "success": false, "error": { "code": "STRING", "message": "STRING", "details": { } } }
```
## Auth POST /api/auth/login
Request:
```json
{ "username": "player1", "password": "password" }
```
Response:
```json
{ "success": true, "data": { "token": "demo-token", "role": "PLAYER", "userId": 1 } }
```

## Templates (GameKeeper) GET /api/templates?category={category}
Response:
```json
{
  "success": true,
  "data": {
    "templates": [
      {
        "templateId": 1,
        "category": "Battery",
        "requiredFields": ["name", "brand", "origin", "chemistry"],
        "optionalFields": ["recyclability", "warrantyMonths"],
        "ruleSetId": 1
      }
    ]
  }
}
```

## POST /api/templates (GameKeeper action)
Request:
```json
{
  "category": "Battery",
  "requiredFields": ["name", "brand", "origin", "chemistry"],
  "optionalFields": ["recyclability", "warrantyMonths"],
  "ruleSetId": 1
}
```
Response:
```json
{ "success": true, "data": { "templateId": 2 } }
```

## Products GET /api/products?category={category}
Response:
```json
{
  "success": true,
  "data": {
    "products": [
      {
        "productId": 1,
        "name": "EcoCell A1",
        "category": "Battery",
        "brand": "EcoBrand",
        "description": "Demo product",
        "passportVersion": 1
      }
    ]
  }
}
```

## Passports (Player) POST /api/passports Create draft passport from template.
Request:
```json
{ "productId": 1, "templateId": 1 }
```

Response:
```json
{ "success": true, "data": { "passportId": 10, "status": "DRAFT" } }
```

## PUT /api/passports/{passportId} Update passport fields (JSON object).
Request:
```json
{
  "fields": {
    "name": "EcoCell A1",
    "brand": "EcoBrand",
    "origin": "UK",
    "chemistry": "Li-ion",
    "recyclability": "High"
  }
}
```
Response:
```json
{ "success": true, "data": { "passportId": 10, "status": "DRAFT" } }
```

## GET /api/passports/{passportId}
Response:
```json
{
  "success": true,
  "data": {
    "passportId": 10,
    "productId": 1,
    "templateId": 1,
    "status": "DRAFT",
    "fields": { "name": "EcoCell A1", "brand": "EcoBrand" },
    "completenessScore": 0.8,
    "confidenceScore": 0.6
  }
}
```

## Evidence POST /api/passports/{passportId}/evidence
Request:
```json
{
  "type": "CERTIFICATE",
  "issuer": "ISO Body",
  "evidence_date": "2026-02-01",
  "summary": "ISO certificate for chemistry",
  "fileReference": "uploads/cert1.pdf",
  "fieldKey": "chemistry"
}
```
Response:
```json
{ "success": true, "data": { "evidenceId": 100 } }
```

## GET /api/passports/{passportId}/evidence
Response:
```json
{
  "success": true,
  "data": {
    "evidence": [
        {
        "evidenceId": 100,
        "type": "CERTIFICATE",
        "issuer": "ISO Body",
        "evidence_date": "2026-02-01",
        "summary": "ISO certificate for chemistry",
        "fileReference": "uploads/cert1.pdf",
        "fieldKey": "chemistry"
      }
    ]
  }
}
```
## Validation + Scoring POST /api/passports/{passportId}/validate
Response:
```json
{
  "success": true,
  "data": {
    "passportId": 10,
    "errors": [
      {
        "ruleId": 1,
        "severity": "ERROR",
        "message": "Required field 'origin' is missing",
        "fieldKey": "origin"
      }
    ],
    "warnings": [
      {
        "ruleId": 3,
        "severity": "WARN",
        "message": "Evidence missing for 'chemistry'",
        "fieldKey": "chemistry"
      }
    ],
    "completenessScore": 0.8,
    "confidenceScore": 0.6
  }
}
```

## Challenges (GameKeeper) POST /api/challenges (GameKeeper action)
Request:
```json
{
  "title": "Battery Compliance Sprint",
  "category": "Battery",
  "constraints": { "minCompleteness": 0.8, "requiredEvidenceTypes": ["CERTIFICATE"] },
  "scoringRules": { "base": 100, "bonusEvidence": 20 },
  "startDate": "2026-02-01",
  "endDate": "2026-03-01"
}
```
Response:
```json
{ "success": true, "data": { "challengeId": 200 } }
```

## GET /api/challenges?category={category}
Response:
```json
{
  "success": true,
  "data": {
    "challenges": [
      {
        "challengeId": 200,
        "title": "Battery Compliance Sprint",
        "category": "Battery",
        "startDate": "2026-02-01",
        "endDate": "2026-03-01"
      }
    ]
  }
}
```
## Submission + Leaderboard POST /api/challenges/{challengeId}/submit
Request:
```json
{ "passportId": 10 }
```
Response:
```json
{
  "success": true,
  "data": {
    "submissionId": 300,
    "score": 120,
    "outcome": "PASS",
    "feedback": [
      "Completeness meets threshold",
      "Required evidence types satisfied"
    ]
  }
}
```
## GET /api/leaderboard?challengeId={challengeId}
Response:
```json
{
  "success": true,
  "data": {
    "entries": [
      {
        "rank": 1,
        "username": "player1",
        "score": 120,
        "submissionId": 300,
        "timestamp": "2026-02-10T12:00:00Z"
      }
    ]
  }
}
```












