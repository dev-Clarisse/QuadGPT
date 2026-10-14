CREATE TABLE users (
    id          BIGSERIAL PRIMARY KEY,
    email       VARCHAR(255) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    department  VARCHAR(255) NOT NULL,
    role        VARCHAR(255) NOT NULL DEFAULT 'USER'
);

CREATE INDEX idx_users_email ON users (email);