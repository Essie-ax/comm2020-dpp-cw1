-- 003_extend_challenge.sql
-- CW2 schema extensions: richer challenge definition and analytics indexes.
-- Run after 002_relax_submission_fk.sql.

USE comm2020_dpp;

-- Allow gamekeepers to add a human-readable description alongside the title.
ALTER TABLE challenge
    ADD COLUMN description TEXT NULL AFTER title;

-- New validation rules added in CW2 (brings total to 14 rules).
-- These mirror the checks implemented in ValidationService.java.
INSERT INTO validation_rule (rule_set_id, description, severity, implementation_reference) VALUES
(1, 'Weight must be a positive number',                          'ERROR', 'POSITIVE:weight'),
(1, 'Recyclable percentage must be between 0 and 100',          'ERROR', 'RANGE:recyclable_percentage:0:100'),
(1, 'Expiry date must be after manufacture date',               'ERROR', 'DATE_ORDER:manufacture_date:expiry_date'),
(1, 'Manufacture date must not be in the future',               'ERROR', 'DATE_NOT_FUTURE:manufacture_date'),
(1, 'Organic claim requires supporting evidence',               'WARN',  'ORGANIC_EVIDENCE'),
(1, 'Recyclable percentage requires recyclable flag to be true','WARN',  'FIELD_DEPENDENCY:recyclable_percentage:recyclable'),
(1, 'Battery category requires chemistry field',                'ERROR', 'CATEGORY_FIELD:Battery:chemistry'),
(1, 'PUBLISHED passport must have end_of_life instructions',   'WARN',  'STATUS_FIELD:PUBLISHED:end_of_life'),
(1, 'Origin must not be blank when brand is present',          'WARN',  'FIELD_PAIR:brand:origin');

-- Speed up analytics queries that aggregate by outcome or sort by score.
CREATE INDEX idx_submission_outcome ON submission(outcome);
CREATE INDEX idx_submission_score   ON submission(score);
