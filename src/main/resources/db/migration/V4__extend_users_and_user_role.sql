-- Extend users table with business profile fields
ALTER TABLE users ADD COLUMN fullname  VARCHAR(100);
ALTER TABLE users ADD COLUMN job_title VARCHAR(100);
ALTER TABLE users ADD COLUMN email     VARCHAR(150);
ALTER TABLE users ADD COLUMN status    VARCHAR(20) DEFAULT 'Active';

-- Index on email for faster lookups
CREATE INDEX idx_users_email ON users (email);

-- Junction table: user to role (many-to-many as specified in schema)
CREATE TABLE user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    CONSTRAINT pk_user_role        PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_role_users  FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_role_roles  FOREIGN KEY (role_id) REFERENCES roles (id)
);
