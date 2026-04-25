-- Datasources table
CREATE TABLE datasources (
    id     BIGSERIAL    PRIMARY KEY,
    name   VARCHAR(100) NOT NULL,
    status VARCHAR(20)  NOT NULL DEFAULT 'Active'
);

CREATE INDEX idx_datasources_status ON datasources (status);

-- Junction table: project to datasource (many-to-many with extra columns)
CREATE TABLE project_datasource (
    project_id    BIGINT        NOT NULL,
    datasource_id BIGINT        NOT NULL,
    api_key       VARCHAR(500),
    folder_url    VARCHAR(1000),
    CONSTRAINT pk_project_datasource  PRIMARY KEY (project_id, datasource_id),
    CONSTRAINT fk_pd_projects         FOREIGN KEY (project_id)    REFERENCES projects    (id) ON DELETE CASCADE,
    CONSTRAINT fk_pd_datasources      FOREIGN KEY (datasource_id) REFERENCES datasources (id) ON DELETE CASCADE
);
