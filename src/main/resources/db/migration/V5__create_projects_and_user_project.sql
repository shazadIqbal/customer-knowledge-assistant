-- Projects table
CREATE TABLE projects (
    id                  BIGINT IDENTITY(1,1) PRIMARY KEY,
    name                NVARCHAR(100) NOT NULL,
    path                NVARCHAR(255) NULL,
    database_table_name NVARCHAR(100) NULL,
    status              NVARCHAR(20) NOT NULL DEFAULT 'Active'
);

CREATE INDEX IDX_projects_status ON projects(status);

-- Junction table: user to project (many-to-many)
CREATE TABLE user_project (
    user_id    BIGINT NOT NULL,
    project_id BIGINT NOT NULL,
    CONSTRAINT PK_user_project PRIMARY KEY (user_id, project_id),
    CONSTRAINT FK_user_project_users    FOREIGN KEY (user_id)    REFERENCES users(id)    ON DELETE CASCADE,
    CONSTRAINT FK_user_project_projects FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);
