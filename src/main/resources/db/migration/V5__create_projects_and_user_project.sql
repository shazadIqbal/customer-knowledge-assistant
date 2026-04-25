-- Projects table
CREATE TABLE projects (
    id                  BIGSERIAL    PRIMARY KEY,
    name                VARCHAR(100) NOT NULL,
    path                VARCHAR(255),
    database_table_name VARCHAR(100),
    status              VARCHAR(20)  NOT NULL DEFAULT 'Active'
);

CREATE INDEX idx_projects_status ON projects (status);

-- Junction table: user to project (many-to-many)
CREATE TABLE user_project (
    user_id    BIGINT NOT NULL,
    project_id BIGINT NOT NULL,
    CONSTRAINT pk_user_project         PRIMARY KEY (user_id, project_id),
    CONSTRAINT fk_user_project_users   FOREIGN KEY (user_id)    REFERENCES users    (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_project_projects FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE
);
