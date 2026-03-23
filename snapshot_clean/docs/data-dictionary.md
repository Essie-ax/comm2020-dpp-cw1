Naming rule: table names follow the dictionary exactly (users, product, template, passport, evidence, challenge, submission, validation_rule).
Do NOT create plural variants (e.g., products/templates).

# Data Dictionary v0.1 (CW1)

DB: MySQL 8+, charset utf8mb4.

## users
- user_id (PK, BIGINT, auto increment)
- username (VARCHAR, unique)
- password_hash (VARCHAR)
- role (ENUM: PLAYER | GAMEKEEPER)
- created_at (TIMESTAMP)

## product
- product_id (PK, BIGINT, auto increment)
- name (VARCHAR(120))
- category (VARCHAR(80))
- brand (VARCHAR(120))
- description (TEXT)
- passport_version (INT) -- latest published passport version for this product (CW1 demo)


## template
- template_id (PK, BIGINT, auto increment)
- category (VARCHAR(80))
- required_fields (JSON) array of field keys
- optional_fields (JSON) array of field keys
- rule_set_id (BIGINT)
- created_by (FK -> users.user_id)
- created_at (TIMESTAMP)


## validation_rule
- rule_id (PK, BIGINT, auto increment)
- rule_set_id (BIGINT)
- description (VARCHAR(255))
- severity (ENUM: ERROR | WARN)
- implementation_reference (VARCHAR(120)) e.g. REQ_FIELD:origin
- created_at (TIMESTAMP)

## passport
- passport_id (PK, BIGINT, auto increment)
- product_id (FK -> product.product_id)
- template_id (FK -> template.template_id)
- fields (JSON) key -> value
- completeness_score (DECIMAL(4,2))  -- 0.00..1.00
- confidence_score (DECIMAL(4,2))    -- 0.00..1.00
- status (ENUM: DRAFT | SUBMITTED | PUBLISHED)
- created_by (FK -> users.user_id)
- updated_at (TIMESTAMP)


## evidence
- evidence_id (PK, BIGINT, auto increment)
- passport_id (FK -> passport.passport_id)
- type (ENUM: CERTIFICATE | REPORT | PHOTO)
- issuer (VARCHAR(120))
- evidence_date (DATE)
- summary (TEXT)
- file_reference (VARCHAR(255))
- field_key (VARCHAR(80)) which passport field it supports
- created_at (TIMESTAMP)


## challenge
- challenge_id (PK, BIGINT, auto increment)
- title (VARCHAR(120))
- category (VARCHAR(80))
- constraints (JSON) e.g. {"minCompleteness":0.8,"requiredEvidenceTypes":["CERTIFICATE"]}
- scoring_rules (JSON) e.g. {"base":100,"bonusEvidence":20}
- start_date (DATE)
- end_date (DATE)
- created_by (FK -> users.user_id)
- created_at (TIMESTAMP)

## submission
- submission_id (PK, BIGINT, auto increment)
- challenge_id (FK -> challenge.challenge_id)
- passport_id (FK -> passport.passport_id)
- submitted_by (FK -> users.user_id)
- submitted_at (TIMESTAMP)
- score (INT)
- outcome (ENUM: PASS | FAIL)







