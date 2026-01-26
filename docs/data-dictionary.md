# Data Dictionary v0.1 (CW1)

DB: MySQL 8+, charset utf8mb4.

## users
- user_id (PK, BIGINT, auto increment)
- username (VARCHAR, unique)
- password_hash (VARCHAR)
- role (ENUM: PLAYER | GAMEKEEPER)
- created_at (TIMESTAMP)

## product
- product_id (PK)
- name
- category
- brand
- description
- passport_version (INT)

## template
- template_id (PK)
- category
- required_fields (JSON array of field keys)
- optional_fields (JSON array of field keys)
- rule_set_id (INT)
- created_by (FK -> users.user_id)
- created_at

## validation_rule
- rule_id (PK)
- rule_set_id (INT)
- description (VARCHAR)
- severity (ENUM: ERROR | WARN)
- implementation_reference (VARCHAR) e.g. REQ_FIELD:origin
- created_at

## passport
- passport_id (PK)
- product_id (FK -> product.product_id)
- template_id (FK -> template.template_id)
- fields (JSON object key -> value)
- completeness_score (DECIMAL)
- confidence_score (DECIMAL)
- status (ENUM: DRAFT | SUBMITTED | PUBLISHED)
- created_by (FK -> users.user_id)
- updated_at

## evidence
- evidence_id (PK)
- passport_id (FK -> passport.passport_id)
- type (VARCHAR) e.g. CERTIFICATE | REPORT | PHOTO
- issuer (VARCHAR)
- evidence_date (DATE)
- summary (TEXT)
- file_reference (VARCHAR)
- field_key (VARCHAR) which passport field it supports
- created_at

## challenge
- challenge_id (PK)
- title (VARCHAR)
- category (VARCHAR)
- constraints (JSON) e.g. {"minCompleteness":0.8,"requiredEvidenceTypes":["CERTIFICATE"]}
- scoring_rules (JSON) e.g. {"base":100,"bonusEvidence":20}
- start_date (DATE)
- end_date (DATE)
- created_by (FK -> users.user_id)
- created_at

## submission
- submission_id (PK)
- challenge_id (FK -> challenge.challenge_id)
- passport_id (FK -> passport.passport_id)
- submitted_by (FK -> users.user_id)
- submitted_at (TIMESTAMP)
- score (INT)
- outcome (ENUM: PASS | FAIL)
