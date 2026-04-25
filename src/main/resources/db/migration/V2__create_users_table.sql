-- Create users table
CREATE TABLE users (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    username NVARCHAR(50) NOT NULL UNIQUE,
    password NVARCHAR(100) NOT NULL,
    role_id BIGINT NOT NULL,
    CONSTRAINT FK_users_roles FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- Create index on username for faster lookups
CREATE INDEX IDX_users_username ON users(username);

-- Create index on role_id for faster joins
CREATE INDEX IDX_users_role_id ON users(role_id);
