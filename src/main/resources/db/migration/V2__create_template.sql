CREATE TABLE template (
    id                    BIGSERIAL PRIMARY KEY,
    campaign_id           BIGINT NOT NULL REFERENCES campaign(id),
    parent_template_id    BIGINT REFERENCES template(id),
    is_parent             BOOLEAN NOT NULL DEFAULT false,
    template_name         VARCHAR(150) NOT NULL,
    template_description  VARCHAR(255),
    customer_type         VARCHAR(20),
    language              VARCHAR(10),
    priority              INT,
    event_type            VARCHAR(50),
    status                VARCHAR(1) NOT NULL DEFAULT 'Y',
    created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT now()
);
