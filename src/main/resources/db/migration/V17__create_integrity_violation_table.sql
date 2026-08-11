CREATE TABLE public.integrity_violation (
    id                  BIGSERIAL PRIMARY KEY,
    check_type          VARCHAR(50) NOT NULL,
    table_name          VARCHAR(50) NOT NULL,
    record_id           BIGINT NOT NULL,
    severity            VARCHAR(20) NOT NULL,
    details             TEXT NOT NULL,
    target_schema       VARCHAR(20) NOT NULL,
    status              VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    first_detected_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    last_detected_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    resolved_at         TIMESTAMPTZ
);

CREATE UNIQUE INDEX uq_integrity_violation_open
ON public.integrity_violation (check_type, table_name, record_id, target_schema)
WHERE status = 'OPEN';
