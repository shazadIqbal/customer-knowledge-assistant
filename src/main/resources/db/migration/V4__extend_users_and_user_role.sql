-- Extend users table with business profile fields
ALTER TABLE users ADD fullname NVARCHAR(100) NULL;
ALTER TABLE users ADD job_title NVARCHAR(100) NULL;
ALTER TABLE users ADD email NVARCHAR(150) NULL;
ALTER TABLE users ADD status NVARCHAR(20) DEFAULT 'Active' NULL;

-- Plain index on email for faster lookups
CREATE INDEX IDX_users_email ON users(email);

-- Junction table: user to role (many-to-many as specified in schema)
CREATE TABLE user_role (
    user_id  BIGINT NOT NULL,
    role_id  BIGINT NOT NULL,
    CONSTRAINT PK_user_role PRIMARY KEY (user_id, role_id),
    CONSTRAINT FK_user_role_users FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT FK_user_role_roles FOREIGN KEY (role_id) REFERENCES roles(id)
);
