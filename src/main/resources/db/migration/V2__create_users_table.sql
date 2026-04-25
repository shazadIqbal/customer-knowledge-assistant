-- Create users table
CREATE TABLE users (
    id       BIGSERIAL PRIMARY KEY,
    username VARCHAR(50)  NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role_id  BIGINT       NOT NULL,
    CONSTRAINT fk_users_roles FOREIGN KEY (role_id) REFERENCES roles (id)
);

-- Index on username for faster lookups
CREATE INDEX idx_users_username ON users (username);

-- Index on role_id for faster joins
CREATE INDEX idx_users_role_id ON users (role_id);
