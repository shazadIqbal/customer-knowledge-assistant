package com.example.auth.service;

import com.example.auth.dto.CreateDatasourceRequest;
import com.example.auth.dto.DatasourceResponse;
import com.example.auth.dto.PagedResponse;
import com.example.auth.dto.UpdateDatasourceRequest;
import com.example.auth.entity.Datasource;
import com.example.auth.exception.ResourceNotFoundException;
import com.example.auth.repository.DatasourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DatasourceService {

    @Autowired
    private DatasourceRepository datasourceRepository;

    @Transactional
    public DatasourceResponse createDatasource(CreateDatasourceRequest request) {
        Datasource datasource = new Datasource();
        datasource.setName(request.getName());
        datasource.setStatus(request.getStatus() != null ? request.getStatus() : "Active");

        Datasource saved = datasourceRepository.save(datasource);
        return toResponse(saved);
    }

    public PagedResponse<DatasourceResponse> getAllDatasources(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Datasource> datasourcePage = datasourceRepository.findAll(pageable);

        Page<DatasourceResponse> responsePage = datasourcePage.map(this::toResponse);
        return PagedResponse.of(responsePage);
    }

    public DatasourceResponse getDatasourceById(Long id) {
        Datasource datasource = datasourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Datasource", "id", id));
        return toResponse(datasource);
    }

    @Transactional
    public DatasourceResponse updateDatasource(Long id, UpdateDatasourceRequest request) {
        Datasource datasource = datasourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Datasource", "id", id));

        if (request.getName() != null) {
            datasource.setName(request.getName());
        }
        if (request.getStatus() != null) {
            datasource.setStatus(request.getStatus());
        }

        Datasource updated = datasourceRepository.save(datasource);
        return toResponse(updated);
    }

    @Transactional
    public void deleteDatasource(Long id) {
        if (!datasourceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Datasource", "id", id);
        }
        datasourceRepository.deleteById(id);
    }

    private DatasourceResponse toResponse(Datasource datasource) {
        DatasourceResponse response = new DatasourceResponse();
        response.setId(datasource.getId());
        response.setName(datasource.getName());
        response.setStatus(datasource.getStatus());
        return response;
    }
}
