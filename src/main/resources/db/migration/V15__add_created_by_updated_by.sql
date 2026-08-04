ALTER TABLE public.campaign ADD COLUMN created_by BIGINT REFERENCES app_user(id);
ALTER TABLE public.campaign ADD COLUMN updated_by BIGINT REFERENCES app_user(id);

ALTER TABLE public.template ADD COLUMN created_by BIGINT REFERENCES app_user(id);
ALTER TABLE public.template ADD COLUMN updated_by BIGINT REFERENCES app_user(id);

ALTER TABLE public.template_config ADD COLUMN created_by BIGINT REFERENCES app_user(id);
ALTER TABLE public.template_config ADD COLUMN updated_by BIGINT REFERENCES app_user(id);

ALTER TABLE public.comm_window ADD COLUMN created_by BIGINT REFERENCES app_user(id);
ALTER TABLE public.comm_window ADD COLUMN updated_by BIGINT REFERENCES app_user(id);
