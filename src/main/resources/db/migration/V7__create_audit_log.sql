CREATE TABLE audit_log (
    id                BIGSERIAL PRIMARY KEY,
    table_name        VARCHAR(50) NOT NULL,
    record_id         BIGINT NOT NULL,
    field_name        VARCHAR(100) NOT NULL,
    old_value         TEXT,
    new_value         TEXT,
    actor_id          BIGINT NOT NULL REFERENCES app_user(id),
    reason            TEXT,
    edit_request_id   BIGINT REFERENCES edit_request(id),
    changed_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);
