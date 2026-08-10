ALTER TABLE dev.template_config ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ NOT NULL DEFAULT now();
ALTER TABLE dev.comm_window ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ NOT NULL DEFAULT now();
ALTER TABLE dev.comm_window ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ NOT NULL DEFAULT now();

CREATE UNIQUE INDEX IF NOT EXISTS uq_dev_template_config_active_medium
ON dev.template_config (template_id, communication_medium)
WHERE use_active = true;

ALTER TABLE dev.campaign ADD COLUMN IF NOT EXISTS created_by BIGINT REFERENCES public.app_user(id);
ALTER TABLE dev.campaign ADD COLUMN IF NOT EXISTS updated_by BIGINT REFERENCES public.app_user(id);
ALTER TABLE dev.template ADD COLUMN IF NOT EXISTS created_by BIGINT REFERENCES public.app_user(id);
ALTER TABLE dev.template ADD COLUMN IF NOT EXISTS updated_by BIGINT REFERENCES public.app_user(id);
ALTER TABLE dev.template_config ADD COLUMN IF NOT EXISTS created_by BIGINT REFERENCES public.app_user(id);
ALTER TABLE dev.template_config ADD COLUMN IF NOT EXISTS updated_by BIGINT REFERENCES public.app_user(id);
ALTER TABLE dev.comm_window ADD COLUMN IF NOT EXISTS created_by BIGINT REFERENCES public.app_user(id);
ALTER TABLE dev.comm_window ADD COLUMN IF NOT EXISTS updated_by BIGINT REFERENCES public.app_user(id);
