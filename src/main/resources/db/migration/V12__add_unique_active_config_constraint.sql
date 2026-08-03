CREATE UNIQUE INDEX uq_template_config_active_medium
ON template_config (template_id, communication_medium)
WHERE use_active = true;
