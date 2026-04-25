package com.example.auth.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProjectResponse {

    private Long id;
    private String name;
    private String path;
    private String databaseTableName;
    private String status;
    private List<DatasourceSummary> datasources;

    @Data
    public static class DatasourceSummary {
        private Long datasourceId;
        private String datasourceName;
        private String apiKey;
        private String folderUrl;
    }
}
