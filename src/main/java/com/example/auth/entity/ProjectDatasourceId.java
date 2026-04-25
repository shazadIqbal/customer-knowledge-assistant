package com.example.auth.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDatasourceId implements Serializable {

    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "datasource_id")
    private Long datasourceId;
}
