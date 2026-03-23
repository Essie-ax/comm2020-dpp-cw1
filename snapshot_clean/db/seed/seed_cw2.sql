-- seed_cw2.sql
-- CW2 seed data: extended products, templates, passports, evidence, challenges and submissions.
-- Run after seed.sql and seed_demo.sql.

USE comm2020_dpp;

-- ─── Products (ids 3-7, matching CW2 in-memory test data) ───────────────────

INSERT INTO product (name, category, brand, description, passport_version) VALUES
('Organic Cotton Tee',   'Textiles',     'EcoWear',   'Certified organic cotton t-shirt',           1),
('Glow Serum X',         'Cosmetics',    'PureSkin',  'Anti-aging face serum',                      1),
('QuickCharge 500',      'Battery',      'VoltMax',   'Fast-charge USB-C power bank',               1),
('SmartTracker Pro',     'Electronics',  'TechNova',  'Bluetooth fitness and location tracker',      1),
('Classic Denim Jacket', 'Textiles',     'DenimCo',   'Recycled-fibre denim jacket',                1);

-- ─── Templates (ids 3-4 for new categories) ─────────────────────────────────

INSERT INTO template (category, required_fields, optional_fields, rule_set_id, created_by) VALUES
('Textiles',
 JSON_ARRAY('name', 'brand', 'origin', 'weight'),
 JSON_ARRAY('organic', 'recyclable', 'recyclable_percentage', 'end_of_life'),
 1, 2),
('Cosmetics',
 JSON_ARRAY('name', 'brand', 'weight'),
 JSON_ARRAY('organic', 'manufacture_date', 'expiry_date'),
 1, 2);

-- ─── Passports (ids 4-8, aligned with in-memory seed) ───────────────────────

-- Passport 4: Organic Cotton Tee — PUBLISHED, good quality
INSERT INTO passport (product_id, template_id, fields, completeness_score, confidence_score, status, created_by)
VALUES (3, 3,
 JSON_OBJECT(
   'name',             'Organic Cotton Tee',
   'brand',            'EcoWear',
   'category',         'Textiles',
   'origin',           'India',
   'weight',           0.3,
   'organic',          true,
   'recyclable',       false,
   'manufacture_date', '2025-08-10'
 ),
 0.7500, 0.6000, 'PUBLISHED', 1);

-- Passport 5: Glow Serum X — DRAFT, broken values (weight negative, dates swapped)
INSERT INTO passport (product_id, template_id, fields, completeness_score, confidence_score, status, created_by)
VALUES (4, 4,
 JSON_OBJECT(
   'name',             'Glow Serum X',
   'brand',            'PureSkin',
   'category',         'Cosmetics',
   'weight',           -5.0,
   'manufacture_date', '2028-01-01',
   'expiry_date',      '2027-06-01'
 ),
 0.4000, 0.0000, 'DRAFT', 1);

-- Passport 6: QuickCharge 500 — DRAFT, recyclable_percentage out of range
INSERT INTO passport (product_id, template_id, fields, completeness_score, confidence_score, status, created_by)
VALUES (5, 1,
 JSON_OBJECT(
   'name',                  'QuickCharge 500',
   'brand',                 'VoltMax',
   'category',              'Battery',
   'origin',                'China',
   'weight',                0.15,
   'recyclable',            true,
   'recyclable_percentage', 120,
   'end_of_life',           'Recycle at local centre',
   'manufacture_date',      '2025-09-01'
 ),
 0.6000, 0.4000, 'DRAFT', 1);

-- Passport 7: SmartTracker Pro — PUBLISHED, solid electronics data
INSERT INTO passport (product_id, template_id, fields, completeness_score, confidence_score, status, created_by)
VALUES (6, 2,
 JSON_OBJECT(
   'name',                  'SmartTracker Pro',
   'brand',                 'TechNova',
   'category',              'Electronics',
   'origin',                'South Korea',
   'weight',                0.08,
   'recyclable',            true,
   'recyclable_percentage', 65,
   'end_of_life',           'Return to manufacturer',
   'manufacture_date',      '2025-11-20'
 ),
 0.7000, 0.4000, 'PUBLISHED', 1);

-- Passport 8: Classic Denim Jacket — PUBLISHED, high completeness
INSERT INTO passport (product_id, template_id, fields, completeness_score, confidence_score, status, created_by)
VALUES (7, 3,
 JSON_OBJECT(
   'name',                  'Classic Denim Jacket',
   'brand',                 'DenimCo',
   'category',              'Textiles',
   'origin',                'Portugal',
   'weight',                0.85,
   'organic',               false,
   'recyclable',            true,
   'recyclable_percentage', 55,
   'end_of_life',           'Return to store for recycling',
   'manufacture_date',      '2025-10-05'
 ),
 0.8800, 0.7000, 'PUBLISHED', 1);

-- ─── Evidence ────────────────────────────────────────────────────────────────

-- Organic Cotton Tee: organic certificate
INSERT INTO evidence (passport_id, type, issuer, evidence_date, summary, file_reference, field_key) VALUES
(4, 'CERTIFICATE', 'GOTS Cert Body',  '2025-07-01', 'Global Organic Textile Standard certificate', 'uploads/gots_cert.pdf',   'organic'),
(4, 'AUDIT',       'Bureau Veritas',  '2025-08-01', 'Supply chain sustainability audit',           'uploads/audit_bv.pdf',    'origin');

-- QuickCharge 500: battery safety certificate
INSERT INTO evidence (passport_id, type, issuer, evidence_date, summary, file_reference, field_key) VALUES
(6, 'CERTIFICATE', 'UL Laboratories', '2025-08-15', 'UL battery safety certification',             'uploads/ul_battery.pdf',  'chemistry'),
(6, 'TEST_REPORT', 'SGS Lab',         '2025-09-10', 'Overcharge and temperature test report',      'uploads/sgs_charge.pdf',  'chemistry');

-- SmartTracker Pro: electronics compliance
INSERT INTO evidence (passport_id, type, issuer, evidence_date, summary, file_reference, field_key) VALUES
(7, 'CERTIFICATE', 'TUV Rheinland',   '2025-10-01', 'CE marking and RoHS compliance',             'uploads/ce_rohs.pdf',     'origin'),
(7, 'TEST_REPORT', 'Intertek',        '2025-11-01', 'EMC and radio frequency test report',        'uploads/emc_test.pdf',    'name');

-- ─── Challenges (CW2 enhanced format with requiredFields and bonusAllFields) ──

-- Update the existing Battery challenge to include the CW2 enhanced constraints.
UPDATE challenge
SET
  description = 'Submit a battery passport that meets EU regulatory completeness requirements.',
  constraints  = JSON_OBJECT(
    'minCompleteness',       0.8,
    'requiredFields',        JSON_ARRAY('name', 'brand', 'origin', 'chemistry'),
    'requiredEvidenceTypes', JSON_ARRAY('CERTIFICATE')
  ),
  scoring_rules = JSON_OBJECT(
    'base',          100,
    'bonusEvidence', 20,
    'bonusAllFields', 10
  )
WHERE challenge_id = 1;

-- New challenge: Textiles transparency
INSERT INTO challenge (title, description, category, constraints, scoring_rules, start_date, end_date, created_by)
VALUES (
  'Textile Transparency Challenge',
  'Demonstrate full supply-chain transparency for any textile product.',
  'Textiles',
  JSON_OBJECT(
    'minCompleteness',       0.7,
    'requiredFields',        JSON_ARRAY('name', 'brand', 'origin', 'weight'),
    'requiredEvidenceTypes', JSON_ARRAY('CERTIFICATE', 'AUDIT')
  ),
  JSON_OBJECT(
    'base',          100,
    'bonusEvidence', 15,
    'bonusAllFields', 10
  ),
  '2026-02-01', '2026-04-01', 2
);

-- New challenge: Electronics sustainability
INSERT INTO challenge (title, description, category, constraints, scoring_rules, start_date, end_date, created_by)
VALUES (
  'Electronics Sustainability Sprint',
  'Verify recyclability and end-of-life planning for electronics.',
  'Electronics',
  JSON_OBJECT(
    'minCompleteness',       0.75,
    'requiredFields',        JSON_ARRAY('name', 'brand', 'origin', 'recyclable'),
    'requiredEvidenceTypes', JSON_ARRAY('CERTIFICATE', 'TEST_REPORT')
  ),
  JSON_OBJECT(
    'base',          100,
    'bonusEvidence', 20,
    'bonusAllFields', 5
  ),
  '2026-03-01', '2026-05-01', 2
);

-- ─── Sample submissions (for analytics dashboard demo) ───────────────────────

-- player1 submits passport 1 (good battery) to challenge 1 → PASS
INSERT INTO submission (challenge_id, passport_id, submitted_by, score, outcome) VALUES
(1, 1, 1, 82, 'PASS');

-- player1 submits passport 6 (QuickCharge, incomplete) to challenge 1 → FAIL
INSERT INTO submission (challenge_id, passport_id, submitted_by, score, outcome) VALUES
(1, 6, 1, 41, 'FAIL');

-- player1 submits passport 4 (Organic Tee) to textile challenge → PASS
INSERT INTO submission (challenge_id, passport_id, submitted_by, score, outcome) VALUES
(2, 4, 1, 76, 'PASS');

-- player1 submits passport 7 (SmartTracker) to electronics challenge → PASS
INSERT INTO submission (challenge_id, passport_id, submitted_by, score, outcome) VALUES
(3, 7, 1, 68, 'PASS');
