-- Datasources table
CREATE TABLE datasources (
    id     BIGINT IDENTITY(1,1) PRIMARY KEY,
    name   NVARCHAR(100) NOT NULL,
    status NVARCHAR(20) NOT NULL DEFAULT 'Active'
);

CREATE INDEX IDX_datasources_status ON datasources(status);

-- Junction table: project to datasource (many-to-many with extra columns)
CREATE TABLE project_datasource (
    project_id    BIGINT NOT NULL,
    datasource_id BIGINT NOT NULL,
    api_key       NVARCHAR(500)  NULL,
    folder_url    NVARCHAR(1000) NULL,
    CONSTRAINT PK_project_datasource PRIMARY KEY (project_id, datasource_id),
    CONSTRAINT FK_pd_projects    FOREIGN KEY (project_id)    REFERENCES projects(id)    ON DELETE CASCADE,
    CONSTRAINT FK_pd_datasources FOREIGN KEY (datasource_id) REFERENCES datasources(id) ON DELETE CASCADE
);
