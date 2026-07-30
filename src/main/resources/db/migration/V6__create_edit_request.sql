CREATE TABLE edit_request (
    id             BIGSERIAL PRIMARY KEY,
    table_name     VARCHAR(50) NOT NULL,
    record_id      BIGINT NOT NULL,
    field_name     VARCHAR(100) NOT NULL,
    old_value      TEXT,
    new_value      TEXT,
    requested_by   BIGINT NOT NULL REFERENCES app_user(id),
    status         VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reason         TEXT,
    requested_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    resolved_by    BIGINT REFERENCES app_user(id),
    resolved_at    TIMESTAMPTZ
);
