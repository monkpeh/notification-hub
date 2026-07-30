CREATE TABLE campaign (
    id                BIGSERIAL PRIMARY KEY,
    name              VARCHAR(150) NOT NULL,
    business_purpose  VARCHAR(255),
    status            VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    owner             VARCHAR(100),
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);
