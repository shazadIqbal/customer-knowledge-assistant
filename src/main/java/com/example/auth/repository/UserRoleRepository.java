package com.example.auth.repository;

import com.example.auth.entity.UserRole;
import com.example.auth.entity.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {

    List<UserRole> findByIdUserId(Long userId);

    void deleteByIdUserId(Long userId);
}
