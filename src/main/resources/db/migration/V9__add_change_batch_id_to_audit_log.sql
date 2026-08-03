ALTER TABLE audit_log ADD COLUMN change_batch_id UUID NOT NULL DEFAULT gen_random_uuid();
