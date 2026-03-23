USE comm2020_dpp;

-- SHA-256 hex hash of "password" (backend uses PasswordUtil.hash("password"))
INSERT INTO users (username, password_hash, role) VALUES
('player1', '5e884898da2847151d0e56f8dc6292773603d0d6aabbdd62a11ef721d154151d8', 'PLAYER'),
('gamekeeper1', '5e884898da2847151d0e56f8dc6292773603d0d6aabbdd62a11ef721d154151d8', 'GAMEKEEPER');

INSERT INTO product (name, category, brand, description, passport_version) VALUES
('EcoCell A1', 'Battery', 'EcoBrand', 'Demo battery product', 1),
('GreenPhone X', 'Electronics', 'GreenTech', 'Demo electronics product', 1);

INSERT INTO template (category, required_fields, optional_fields, rule_set_id, created_by) VALUES
('Battery', JSON_ARRAY('name','brand','origin','chemistry'), JSON_ARRAY('recyclability','warrantyMonths'), 1, 2),
('Electronics', JSON_ARRAY('name','brand','origin'), JSON_ARRAY('repairabilityScore'), 1, 2);

INSERT INTO validation_rule (rule_set_id, description, severity, implementation_reference) VALUES
(1, 'Required field: name', 'ERROR', 'REQ_FIELD:name'),
(1, 'Required field: brand', 'ERROR', 'REQ_FIELD:brand'),
(1, 'Required field: origin', 'ERROR', 'REQ_FIELD:origin'),
(1, 'Battery requires chemistry', 'ERROR', 'REQ_FIELD:chemistry'),
(1, 'Evidence recommended for chemistry', 'WARN', 'EVIDENCE_RECOMMENDED:chemistry');

INSERT INTO passport (product_id, template_id, fields, completeness_score, confidence_score, status, created_by)
VALUES
(1, 1, JSON_OBJECT('name','EcoCell A1','brand','EcoBrand'), 0.5000, 0.2000, 'DRAFT', 1);

INSERT INTO evidence (passport_id, type, issuer, evidence_date, summary, file_reference, field_key)
VALUES
(1, 'CERTIFICATE', 'ISO Body', '2026-02-01', 'ISO certificate for chemistry', 'uploads/cert1.pdf', 'chemistry');

INSERT INTO challenge (title, category, constraints, scoring_rules, start_date, end_date, created_by)
VALUES
('Battery Compliance Sprint', 'Battery',
 JSON_OBJECT('minCompleteness', 0.8, 'requiredEvidenceTypes', JSON_ARRAY('CERTIFICATE')),
 JSON_OBJECT('base', 100, 'bonusEvidence', 20),
 '2026-02-01', '2026-03-01', 2);
