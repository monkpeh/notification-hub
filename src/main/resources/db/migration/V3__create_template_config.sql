CREATE TABLE template_config (
    id                     BIGSERIAL PRIMARY KEY,
    template_id            BIGINT NOT NULL REFERENCES template(id),
    communication_medium   VARCHAR(20) NOT NULL,
    external_template_id   VARCHAR(100),
    use_active             BOOLEAN NOT NULL DEFAULT false,
    contact_flow_id        VARCHAR(100),
    created_at             TIMESTAMPTZ NOT NULL DEFAULT now()
);
