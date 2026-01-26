CREATE DATABASE IF NOT EXISTS comm2020_dpp CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE comm2020_dpp;

CREATE TABLE IF NOT EXISTS users (
  user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(64) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  role ENUM('PLAYER','GAMEKEEPER') NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS product (
  product_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(128) NOT NULL,
  category VARCHAR(64) NOT NULL,
  brand VARCHAR(64) NOT NULL,
  description TEXT,
  passport_version INT NOT NULL DEFAULT 1
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS template (
  template_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  category VARCHAR(64) NOT NULL,
  required_fields JSON NOT NULL,
  optional_fields JSON NOT NULL,
  rule_set_id INT NOT NULL,
  created_by BIGINT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_template_user FOREIGN KEY (created_by) REFERENCES users(user_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS validation_rule (
  rule_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  rule_set_id INT NOT NULL,
  description VARCHAR(255) NOT NULL,
  severity ENUM('ERROR','WARN') NOT NULL,
  implementation_reference VARCHAR(128) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS passport (
  passport_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  product_id BIGINT NOT NULL,
  template_id BIGINT NOT NULL,
  fields JSON NOT NULL,
  completeness_score DECIMAL(5,4) NOT NULL DEFAULT 0.0000,
  confidence_score DECIMAL(5,4) NOT NULL DEFAULT 0.0000,
  status ENUM('DRAFT','SUBMITTED','PUBLISHED') NOT NULL DEFAULT 'DRAFT',
  created_by BIGINT NOT NULL,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_passport_product FOREIGN KEY (product_id) REFERENCES product(product_id),
  CONSTRAINT fk_passport_template FOREIGN KEY (template_id) REFERENCES template(template_id),
  CONSTRAINT fk_passport_user FOREIGN KEY (created_by) REFERENCES users(user_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS evidence (
  evidence_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  passport_id BIGINT NOT NULL,
  type VARCHAR(32) NOT NULL,
  issuer VARCHAR(128),
  evidence_date DATE,
  summary TEXT,
  file_reference VARCHAR(255),
  field_key VARCHAR(64),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_evidence_passport FOREIGN KEY (passport_id) REFERENCES passport(passport_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS challenge (
  challenge_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(128) NOT NULL,
  category VARCHAR(64) NOT NULL,
  constraints JSON NOT NULL,
  scoring_rules JSON NOT NULL,
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  created_by BIGINT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_challenge_user FOREIGN KEY (created_by) REFERENCES users(user_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS submission (
  submission_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  challenge_id BIGINT NOT NULL,
  passport_id BIGINT NOT NULL,
  submitted_by BIGINT NOT NULL,
  submitted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  score INT NOT NULL DEFAULT 0,
  outcome ENUM('PASS','FAIL') NOT NULL,
  CONSTRAINT fk_submission_challenge FOREIGN KEY (challenge_id) REFERENCES challenge(challenge_id),
  CONSTRAINT fk_submission_passport FOREIGN KEY (passport_id) REFERENCES passport(passport_id),
  CONSTRAINT fk_submission_user FOREIGN KEY (submitted_by) REFERENCES users(user_id)
) ENGINE=InnoDB;

CREATE INDEX idx_product_category ON product(category);
CREATE INDEX idx_template_category ON template(category);
CREATE INDEX idx_challenge_category ON challenge(category);
CREATE INDEX idx_submission_challenge ON submission(challenge_id);
