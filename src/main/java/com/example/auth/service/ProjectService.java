package com.example.auth.service;

import com.example.auth.dto.CreateProjectRequest;
import com.example.auth.dto.PagedResponse;
import com.example.auth.dto.ProjectResponse;
import com.example.auth.dto.UpdateProjectRequest;
import com.example.auth.entity.Datasource;
import com.example.auth.entity.Project;
import com.example.auth.entity.ProjectDatasource;
import com.example.auth.exception.ResourceNotFoundException;
import com.example.auth.repository.DatasourceRepository;
import com.example.auth.repository.ProjectDatasourceRepository;
import com.example.auth.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private DatasourceRepository datasourceRepository;

    @Autowired
    private ProjectDatasourceRepository projectDatasourceRepository;

    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request) {
        Datasource datasource = datasourceRepository.findById(request.getDatasourceId())
                .orElseThrow(() -> new ResourceNotFoundException("Datasource", "id", request.getDatasourceId()));

        Project project = new Project();
        project.setName(request.getName());
        project.setPath(request.getPath());
        project.setDatabaseTableName(request.getDatabaseTableName());
        project.setStatus(request.getStatus() != null ? request.getStatus() : "Active");

        Project savedProject = projectRepository.save(project);

        ProjectDatasource pd = new ProjectDatasource(savedProject, datasource, request.getApiKey(), request.getFolderUrl());
        projectDatasourceRepository.save(pd);

        return toResponse(savedProject);
    }

    public PagedResponse<ProjectResponse> getAllProjects(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Project> projectPage = projectRepository.findAll(pageable);

        Page<ProjectResponse> responsePage = projectPage.map(this::toResponse);
        return PagedResponse.of(responsePage);
    }

    public ProjectResponse getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
        return toResponse(project);
    }

    @Transactional
    public ProjectResponse updateProject(Long id, UpdateProjectRequest request) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));

        if (request.getName() != null) {
            project.setName(request.getName());
        }
        if (request.getPath() != null) {
            project.setPath(request.getPath());
        }
        if (request.getDatabaseTableName() != null) {
            project.setDatabaseTableName(request.getDatabaseTableName());
        }
        if (request.getStatus() != null) {
            project.setStatus(request.getStatus());
        }

        Project updatedProject = projectRepository.save(project);
        return toResponse(updatedProject);
    }

    @Transactional
    public void deleteProject(Long id) {
        if (!projectRepository.existsById(id)) {
            throw new ResourceNotFoundException("Project", "id", id);
        }
        projectDatasourceRepository.deleteByIdProjectId(id);
        projectRepository.deleteById(id);
    }

    private ProjectResponse toResponse(Project project) {
        ProjectResponse response = new ProjectResponse();
        response.setId(project.getId());
        response.setName(project.getName());
        response.setPath(project.getPath());
        response.setDatabaseTableName(project.getDatabaseTableName());
        response.setStatus(project.getStatus());

        List<ProjectResponse.DatasourceSummary> datasources = projectDatasourceRepository
                .findByIdProjectId(project.getId())
                .stream()
                .map(pd -> {
                    ProjectResponse.DatasourceSummary summary = new ProjectResponse.DatasourceSummary();
                    summary.setDatasourceId(pd.getDatasource().getId());
                    summary.setDatasourceName(pd.getDatasource().getName());
                    summary.setApiKey(pd.getApiKey());
                    summary.setFolderUrl(pd.getFolderUrl());
                    return summary;
                })
                .collect(Collectors.toList());

        response.setDatasources(datasources);
        return response;
    }
}
