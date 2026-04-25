package com.example.auth.repository;

import com.example.auth.entity.UserProject;
import com.example.auth.entity.UserProjectId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserProjectRepository extends JpaRepository<UserProject, UserProjectId> {

    List<UserProject> findByIdUserId(Long userId);

    void deleteByIdUserId(Long userId);
}
