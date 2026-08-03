ALTER TABLE edit_request ADD COLUMN target_schema VARCHAR(20) NOT NULL DEFAULT 'public';
ALTER TABLE edit_request ADD COLUMN approved_with_override BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE audit_log ADD COLUMN target_schema VARCHAR(20) NOT NULL DEFAULT 'public';
