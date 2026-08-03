CREATE SCHEMA IF NOT EXISTS dev;

CREATE TABLE dev.campaign (
    id                BIGSERIAL PRIMARY KEY,
    name              VARCHAR(150) NOT NULL,
    business_purpose  VARCHAR(255),
    status            VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    owner             VARCHAR(100),
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE dev.template (
    id                    BIGSERIAL PRIMARY KEY,
    campaign_id           BIGINT NOT NULL REFERENCES dev.campaign(id),
    parent_template_id    BIGINT REFERENCES dev.template(id),
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

CREATE TABLE dev.template_config (
    id                     BIGSERIAL PRIMARY KEY,
    template_id            BIGINT NOT NULL REFERENCES dev.template(id),
    communication_medium   VARCHAR(20) NOT NULL,
    external_template_id   VARCHAR(100),
    use_active             BOOLEAN NOT NULL DEFAULT false,
    contact_flow_id        VARCHAR(100),
    created_at             TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE dev.comm_window (
    id            BIGSERIAL PRIMARY KEY,
    config_id     BIGINT NOT NULL REFERENCES dev.template_config(id),
    start_window  TIME NOT NULL,
    end_window    TIME NOT NULL,
    occurrence    VARCHAR(20)
);
