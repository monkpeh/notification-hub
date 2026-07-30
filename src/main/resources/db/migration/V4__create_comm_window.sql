CREATE TABLE comm_window (
    id            BIGSERIAL PRIMARY KEY,
    config_id     BIGINT NOT NULL REFERENCES template_config(id),
    start_window  TIME NOT NULL,
    end_window    TIME NOT NULL,
    occurrence    VARCHAR(20)
);
