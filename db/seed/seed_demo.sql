-- seed_demo.sql
-- Extra demo data for CW1 presentation.
-- Run AFTER seed.sql. Adds more passports and evidence for richer demo.

USE comm2020_dpp;

-- Passport 2: high quality, should PASS (completeness=0.9, confidence=0.8)
INSERT INTO passport (product_id, template_id, fields, completeness_score, confidence_score, status, created_by)
VALUES
(1, 1,
 JSON_OBJECT('name','EcoCell A1','brand','EcoBrand','origin','China','chemistry','Li-ion'),
 0.9000, 0.8000, 'SUBMITTED', 1);

-- Passport 3: medium quality, borderline (completeness=0.7, confidence=0.5)
INSERT INTO passport (product_id, template_id, fields, completeness_score, confidence_score, status, created_by)
VALUES
(2, 2,
 JSON_OBJECT('name','GreenPhone X','brand','GreenTech','origin','Taiwan'),
 0.7000, 0.5000, 'DRAFT', 1);

-- Evidence for passport 2 (high quality - 4 pieces)
INSERT INTO evidence (passport_id, type, issuer, evidence_date, summary, file_reference, field_key) VALUES
(2, 'CERTIFICATE', 'ISO Body', '2026-01-15', 'ISO 14001 environmental certification', 'uploads/cert_iso14001.pdf', 'chemistry'),
(2, 'TEST_REPORT', 'SGS Lab', '2026-01-20', 'Battery safety test report', 'uploads/test_safety.pdf', 'chemistry'),
(2, 'CERTIFICATE', 'TUV Rheinland', '2026-02-01', 'CE marking certificate', 'uploads/cert_ce.pdf', 'origin'),
(2, 'AUDIT', 'Bureau Veritas', '2026-02-05', 'Supply chain audit report', 'uploads/audit_supply.pdf', 'brand');

-- Evidence for passport 3 (medium - 2 pieces)
INSERT INTO evidence (passport_id, type, issuer, evidence_date, summary, file_reference, field_key) VALUES
(3, 'CERTIFICATE', 'UL Labs', '2026-01-10', 'Electronics safety certificate', 'uploads/cert_ul.pdf', 'origin'),
(3, 'TEST_REPORT', 'Intertek', '2026-01-25', 'EMC compliance test', 'uploads/test_emc.pdf', 'name');
