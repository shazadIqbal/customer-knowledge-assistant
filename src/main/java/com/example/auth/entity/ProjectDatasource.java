package com.example.auth.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

@Entity
@Table(name = "project_datasource")
@Data
@NoArgsConstructor
public class ProjectDatasource {

    @EmbeddedId
    private ProjectDatasourceId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("projectId")
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("datasourceId")
    @JoinColumn(name = "datasource_id")
    private Datasource datasource;

    @Column(name = "api_key", length = 500)
    private String apiKey;

    @Column(name = "folder_url", length = 1000)
    private String folderUrl;

    public ProjectDatasource(Project project, Datasource datasource, String apiKey, String folderUrl) {
        this.id = new ProjectDatasourceId(project.getId(), datasource.getId());
        this.project = project;
        this.datasource = datasource;
        this.apiKey = apiKey;
        this.folderUrl = folderUrl;
    }
}
