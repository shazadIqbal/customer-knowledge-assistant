package com.example.auth.repository;

import com.example.auth.entity.Datasource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatasourceRepository extends JpaRepository<Datasource, Long> {

    Page<Datasource> findByStatus(String status, Pageable pageable);

    boolean existsByName(String name);
}
