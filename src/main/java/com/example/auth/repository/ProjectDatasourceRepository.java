package com.example.auth.repository;

import com.example.auth.entity.ProjectDatasource;
import com.example.auth.entity.ProjectDatasourceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectDatasourceRepository extends JpaRepository<ProjectDatasource, ProjectDatasourceId> {

    List<ProjectDatasource> findByIdProjectId(Long projectId);

    void deleteByIdProjectId(Long projectId);
}
