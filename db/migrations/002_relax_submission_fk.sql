-- 002_relax_submission_fk.sql
-- Drop passport FK on submission table to allow mock passport IDs during development.
-- Re-add this constraint when passport module (中2) is fully integrated.
ALTER TABLE submission DROP FOREIGN KEY fk_submission_passport;
