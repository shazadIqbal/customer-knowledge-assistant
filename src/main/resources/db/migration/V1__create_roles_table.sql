-- Create roles table
CREATE TABLE roles (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(50) NOT NULL UNIQUE
);
