CREATE TABLE app_role (
    id    BIGSERIAL PRIMARY KEY,
    name  VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE app_user (
    id         BIGSERIAL PRIMARY KEY,
    pid        VARCHAR(20) UNIQUE NOT NULL,
    full_name  VARCHAR(150),
    email      VARCHAR(150),
    role_id    BIGINT NOT NULL REFERENCES app_role(id)
);

INSERT INTO app_role (name) VALUES ('SUPER_ADMIN'), ('ADMIN'), ('TEMPLATE_BUILDER'), ('APPROVER');
